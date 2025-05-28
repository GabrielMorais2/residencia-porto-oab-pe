package org.portodigital.residencia.oabpe.domain.prestacao_contas_subseccional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.portodigital.residencia.oabpe.domain.balancete_cfoab.dto.BalanceteCFOABResponseDTO;
import org.portodigital.residencia.oabpe.domain.identidade.model.User;
import org.portodigital.residencia.oabpe.domain.prestacao_contas_subseccional.dto.PrestacaoContasSubseccionalFiltroRequest;
import org.portodigital.residencia.oabpe.domain.prestacao_contas_subseccional.dto.PrestacaoContasSubseccionalRequestDTO;
import org.portodigital.residencia.oabpe.domain.prestacao_contas_subseccional.dto.PrestacaoContasSubseccionalResponseDTO;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/v1/prestacao-contas")
@RequiredArgsConstructor
@Tag(name = "Prestação de Contas", description = "Gerencia registros de prestação de contas das subseccionais")
public class PrestacaoContasSubseccionalController {

    private final PrestacaoContasSubseccionalService prestacaoContasSubseccionalService;

    @Operation(summary = "Listar prestações de contas com filtros")
    @GetMapping
    @PreAuthorize("hasPermission('modulo_prestacao_contas_subseccional', 'LEITURA')")
    public ResponseEntity<Page<PrestacaoContasSubseccionalResponseDTO>> getAllComFiltro(
            @Valid @ParameterObject PrestacaoContasSubseccionalFiltroRequest filtro,
            Pageable pageable) {
        return ResponseEntity.ok(prestacaoContasSubseccionalService.getAllComFiltro(filtro, pageable));
    }
    @Operation(summary = "Buscar prestação de contas por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prestação encontrada"),
            @ApiResponse(responseCode = "404", description = "Prestação não encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('modulo_prestacao_contas_subseccional', 'LEITURA')")
    public ResponseEntity<PrestacaoContasSubseccionalResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(prestacaoContasSubseccionalService.getById(id));
    }

    @Operation(summary = "Criar uma nova prestação de contas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Prestação criada com sucesso")
    })
    @PostMapping
    @PreAuthorize("hasPermission('modulo_prestacao_contas_subseccional', 'ESCRITA')")
    public ResponseEntity<PrestacaoContasSubseccionalResponseDTO> create(@RequestBody PrestacaoContasSubseccionalRequestDTO request) {
        PrestacaoContasSubseccionalResponseDTO response = prestacaoContasSubseccionalService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualizar uma prestação de contas existente")
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('modulo_prestacao_contas_subseccional', 'ESCRITA')")
    public ResponseEntity<PrestacaoContasSubseccionalResponseDTO> update(@PathVariable Long id,
                                                                         @RequestBody PrestacaoContasSubseccionalRequestDTO request) {
        return ResponseEntity.ok(prestacaoContasSubseccionalService.update(id, request));
    }

    @Operation(summary = "Excluir uma prestação de contas (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Prestação desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Prestação não encontrada")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('modulo_prestacao_contas_subseccional', 'ESCRITA')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        prestacaoContasSubseccionalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Faz o upload de prestacão de contas",
            description = "Permite que um usuário envie um arquivo",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do arquivo a ser enviado",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Prestacão de contas criadas com sucesso",
                    content = @Content(schema = @Schema(implementation = BalanceteCFOABResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadFile(
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) Authentication authentication
    ) throws IOException {
        User user = (User) authentication.getPrincipal();
        prestacaoContasSubseccionalService.importarArquivo(file, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


}