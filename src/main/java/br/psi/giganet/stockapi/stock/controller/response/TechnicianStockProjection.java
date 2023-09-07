package br.psi.giganet.stockapi.stock.controller.response;

import br.psi.giganet.stockapi.technician.controller.response.TechnicianProjection;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TechnicianStockProjection extends StockProjection {

    private TechnicianProjection technician;

}
