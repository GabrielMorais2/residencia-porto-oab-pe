package org.portodigital.residencia.oabpe.domain.instituicao;

import jakarta.validation.ConstraintViolation;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.portodigital.residencia.oabpe.domain.commons.ImportProcessor;
import org.portodigital.residencia.oabpe.domain.identidade.model.User;
import org.portodigital.residencia.oabpe.domain.instituicao.dto.InstituicaoRequestDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class InstituicaoImportProcessor implements ImportProcessor<InstituicaoRequestDTO> {

    private final Validator validator;

    @Override
    public String[] getRequiredHeaders() {
        return new String[]{"Instituicao"};
    }

    @Override
    public InstituicaoRequestDTO parse(Map<String, String> rowData) {
        InstituicaoRequestDTO dto = new InstituicaoRequestDTO();
        dto.setNome(rowData.get("Instituicao"));
        return dto;
    }

    @Override
    public void validate(InstituicaoRequestDTO dto) {
        Set<ConstraintViolation<InstituicaoRequestDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<?> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new IllegalArgumentException("Validação falhou: " + sb);
        }
    }

    @Override
    public Object convertToEntity(InstituicaoRequestDTO dto, User user) {
        Instituicao entity = new Instituicao();
        entity.setNome(dto.getNome());
        entity.setUser(user);
        return entity;
    }
}
