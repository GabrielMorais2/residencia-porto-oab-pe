package org.portodigital.residencia.oabpe.domain.prestacao_contas_subseccional.subseccional;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.portodigital.residencia.oabpe.domain.commons.AbstractFileImportService;
import org.portodigital.residencia.oabpe.domain.identidade.model.User;
import org.portodigital.residencia.oabpe.domain.prestacao_contas_subseccional.subseccional.dto.SubseccionalRequest;
import org.portodigital.residencia.oabpe.domain.prestacao_contas_subseccional.subseccional.dto.SubseccionalResponse;
import org.portodigital.residencia.oabpe.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubseccionalService extends AbstractFileImportService<SubseccionalRequest> {

    private final SubseccionalRepository subseccionalRepository;
    private final SubseccionalImportProcessor processor;
    private final ModelMapper modelMapper;

    public Page<SubseccionalResponse> getAll(String nome, Pageable pageable) {
        Page<Subseccional> page = subseccionalRepository
                .findBySubSeccionalContainingIgnoreCaseAndStatusTrue(nome != null ? nome : "", pageable);

        return page.map(subseccional -> modelMapper.map(subseccional, SubseccionalResponse.class));
    }

    public SubseccionalResponse getById(Long id) {
        Subseccional subseccional = subseccionalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subseccional não encontrada com id: " + id));
        return modelMapper.map(subseccional, SubseccionalResponse.class);
    }

    public SubseccionalResponse create(SubseccionalRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Subseccional subseccional = new Subseccional();
        subseccional.setSubSeccional(request.getSubSeccional());
        subseccional.setUsuario(user);
        Subseccional saved = subseccionalRepository.save(subseccional);
        return modelMapper.map(saved, SubseccionalResponse.class);
    }

    public SubseccionalResponse update(Long id, SubseccionalRequest request) {
        Subseccional existing = subseccionalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subseccional não encontrada com id: " + id));

        existing.setSubSeccional(request.getSubSeccional());

        Subseccional updated = subseccionalRepository.save(existing);

        return modelMapper.map(updated, SubseccionalResponse.class);
    }

    public void softDelete(Long id) {
        Subseccional existing = subseccionalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subseccional não encontrada com id: " + id));
        existing.setStatus(false);
        subseccionalRepository.save(existing);
    }

    @Transactional
    public void importarArquivo(MultipartFile file, User user) throws IOException {
        List<Object> entidades = importFile(file, user, processor);
        subseccionalRepository.saveAll(entidades.stream().map(e -> (Subseccional) e).toList());
    }
}