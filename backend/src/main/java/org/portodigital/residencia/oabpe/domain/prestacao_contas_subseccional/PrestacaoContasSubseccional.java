package org.portodigital.residencia.oabpe.domain.prestacao_contas_subseccional;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.portodigital.residencia.oabpe.domain.identidade.model.User;
import org.portodigital.residencia.oabpe.domain.prestacao_contas_subseccional.subseccional.Subseccional;
import org.portodigital.residencia.oabpe.domain.prestacao_contas_subseccional.tipo_desconto.TipoDesconto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PrestacaoContasSubseccional")
public class PrestacaoContasSubseccional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "Id_Subseccional", referencedColumnName = "id")
    private Subseccional subseccional;

    @Column(name = "MesReferencia", nullable = false, length = 10)
    private String mesReferencia;

    @Column(name = "Ano", nullable = false, length = 4)
    private String ano;

    @Column(name = "DtPrevEntr", nullable = false)
    private LocalDate dtPrevEntr;

    @Column(name = "DtEntrega")
    private LocalDate dtEntrega;

    @Column(name = "DtPagto")
    private LocalDate dtPagto;

    @Column(name = "ValorDuodecimo", precision = 15, scale = 2)
    private BigDecimal valorDuodecimo;

    @Column(name = "ValorDesconto", precision = 15, scale = 2)
    private BigDecimal valorDesconto;

    @Column(name = "ValorPago", precision = 15, scale = 2)
    private BigDecimal valorPago;

    @Column(name = "ProtocoloSGD", length = 17)
    private String protocoloSGD;

    @Column(name = "Observacao")
    private String observacao;

    @Column(name = "Status", nullable = false)
    private Boolean status = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "Id_usuario", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "Id_TipoDesconto", referencedColumnName = "id")
    private TipoDesconto tipoDesconto;

    @Column(name = "DAT_CRIACAO_REGISTRO")
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private LocalDateTime dataCriacaoRegistro;

    @Column(name = "DAT_ALTERACAO_REGISTRO")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private LocalDateTime dataAlteracaoRegistro;

    public BigDecimal getValorPago() {
        BigDecimal duodecimo = valorDuodecimo != null ? valorDuodecimo : BigDecimal.ZERO;
        BigDecimal desconto = valorDesconto != null ? valorDesconto : BigDecimal.ZERO;
        return duodecimo.subtract(desconto);
    }

}