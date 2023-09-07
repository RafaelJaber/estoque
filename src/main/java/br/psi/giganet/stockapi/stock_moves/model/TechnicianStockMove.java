package br.psi.giganet.stockapi.stock_moves.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "technician_stock_moves")
public class TechnicianStockMove extends StockMove {

    private String orderId;
    private String activationId;
    @Enumerated(EnumType.STRING)
    private ExternalOrderType orderType;
    private String customerName;

}
