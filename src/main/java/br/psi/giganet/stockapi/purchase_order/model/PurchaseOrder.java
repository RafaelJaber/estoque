package br.psi.giganet.stockapi.purchase_order.model;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.config.security.model.AbstractExternalModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder extends AbstractExternalModel {

    private String note;
    @NotNull
    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    private String responsible;

    private String costCenter;

    private String description;

    private ZonedDateTime externalCreatedDate;

    private LocalDate dateOfNeed;

    @NotNull
    @Min(0)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_orders_supplier"),
            name = "supplier",
            nullable = false,
            referencedColumnName = "id")
    private PurchaseOrderSupplier supplier;

    @NotEmpty
    @OneToMany(
            mappedBy = "order",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    private List<PurchaseOrderItem> items;

    @OneToOne(
            mappedBy = "order",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    private PurchaseOrderFreight freight;
}
