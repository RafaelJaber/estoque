package br.psi.giganet.stockapi.purchase_order.model;

import br.psi.giganet.stockapi.common.address.model.Address;
import br.psi.giganet.stockapi.config.security.model.AbstractExternalModel;
import br.psi.giganet.stockapi.purchase_order.model.enums.FreightType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchase_order_freights")
public class PurchaseOrderFreight extends AbstractExternalModel {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_order_freights_order"),
            name = "purchaseOrder",
            nullable = false,
            referencedColumnName = "id"
    )
    private PurchaseOrder order;

    @Column(name = "freight_type")
    @NotNull
    @Enumerated(EnumType.STRING)
    private FreightType type;

    @Column(name = "freight_price")
    @NotNull
    @Min(0)
    private BigDecimal price;

    @Embedded
    private Address deliveryAddress;

    private ZonedDateTime deliveryDate;

}