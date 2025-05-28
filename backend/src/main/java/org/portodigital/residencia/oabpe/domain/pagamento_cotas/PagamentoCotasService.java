package org.portodigital.residencia.oabpe.domain.pagamento_cotas;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.portodigital.residencia.oabpe.domain.balancete_cfoab.BalanceteCFOAB;
import org.portodigital.residencia.oabpe.domain.identidade.model.User;
import org.portodigital.residencia.oabpe.domain.instituicao.InstituicaoRepository;
import org.portodigital.residencia.oabpe.domain.pagamento_cotas.dto.PagamentoCotasFilteredRequest;
import org.portodigital.residencia.oabpe.domain.pagamento_cotas.dto.PagamentoCotasRequestDTO;
import org.portodigital.residencia.oabpe.domain.pagamento_cotas.dto.PagamentoCotasResponseDTO;
import org.portodigital.residencia.oabpe.domain.prestacao_contas_subseccional.tipo_desconto.TipoDescontoRepository;
import org.portodigital.residencia.oabpe.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.portodigital.residencia.oabpe.domain.commons.AbstractFileImportService;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoCotasService extends AbstractFileImportService<PagamentoCotasRequestDTO>{

    private final PagamentoCotasRepository pagamentoCotasRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final TipoDescontoRepository tipoDescontoRepository;
    private final PagamentoCotasImportProcessor processor;
    private final ModelMapper mapper;

    @Transactional
    public void importarArquivo(MultipartFile file, User user) throws IOException{
        List<Object> entidades = importFile(file, user, processor);
        pagamentoCotasRepository.saveAll(entidades.stream().map(e -> (PagamentoCotas) e).toList());
    }

    public Page<PagamentoCotasResponseDTO> getAllFiltered(PagamentoCotasFilteredRequest filter, Pageable pageable) {
        return pagamentoCotasRepository.findAllActiveByFilter(filter, pageable)
                .map(pagamentoCotas -> mapper.map(pagamentoCotas, PagamentoCotasResponseDTO.class));
    }

    public PagamentoCotasResponseDTO getById(Long id) {
        return pagamentoCotasRepository.findById(id)
                .map(pagamentoCotas -> mapper.map(pagamentoCotas, PagamentoCotasResponseDTO.class))
                .orElseThrow(() -> new EntityNotFoundException("Pagamento de Cota não encontrado."));
    }

    public PagamentoCotasResponseDTO create(PagamentoCotasRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new SecurityException("Acesso não autorizado");
        }

        var instituicao = instituicaoRepository.findByNomeAtivo(request.getInstituicaoNome())
                .orElseThrow(() -> new EntityNotFoundException("Instituição não encontrada com nome: " + request.getInstituicaoNome()));
        var tipoDesconto = tipoDescontoRepository.findByNomeAtivo(request.getTipoDesconto())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de desconto não encontrado com nome: " + request.getTipoDesconto()));

        User user = (User) authentication.getPrincipal();
        PagamentoCotas pagamentoCotas = mapper.map(request, PagamentoCotas.class);
        pagamentoCotas.setStatus(true);
        pagamentoCotas.setUser(user);
        pagamentoCotas.setInstituicao(instituicao);
        pagamentoCotas.setTipoDesconto(tipoDesconto);

        PagamentoCotas savedPagamentoCotas = pagamentoCotasRepository.save(pagamentoCotas);
        return mapper.map(savedPagamentoCotas, PagamentoCotasResponseDTO.class);
    }

    public void delete(Long id) {
        var existingPagamento = pagamentoCotasRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento de Cotas não encontrado."));
        existingPagamento.setStatus(false);

        // rastreia o usuário que fez a inativação
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new SecurityException("Acesso não autorizado");
        }
        User user = (User) authentication.getPrincipal();
        existingPagamento.setUser(user);

        pagamentoCotasRepository.save(existingPagamento);
    }

    public PagamentoCotasResponseDTO update(Long id, PagamentoCotasRequestDTO request) {
        PagamentoCotas existing = pagamentoCotasRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento de Cotas não encontrado com id: " + id));


        if (request.getInstituicaoNome() != null) {
            var instituicao = instituicaoRepository.findByNomeAtivo(request.getInstituicaoNome())
                    .orElseThrow(() -> new EntityNotFoundException("Instituição não encontrada com ID: " + request.getInstituicaoNome()));
            existing.setInstituicao(instituicao);
        }
        if (request.getTipoDesconto() != null) {
            var tipoDesconto = tipoDescontoRepository.findByNomeAtivo(request.getTipoDesconto())
                    .orElseThrow(() -> new EntityNotFoundException("Tipo de desconto não encontrado com ID: " + request.getTipoDesconto()));
            existing.setTipoDesconto(tipoDesconto);
        }

        mapper.map(request, existing);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new SecurityException("Acesso não autorizado");
        }
        User user = (User) authentication.getPrincipal();
        existing.setUser(user);

        PagamentoCotas updated = pagamentoCotasRepository.save(existing);
        return mapper.map(updated, PagamentoCotasResponseDTO.class);
    }
}