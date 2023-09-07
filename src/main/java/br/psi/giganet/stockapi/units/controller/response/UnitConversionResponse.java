package br.psi.giganet.stockapi.units.controller.response;

import lombok.Data;

@Data
public class UnitConversionResponse {

    private String id;
    private UnitProjection to;
    private Double conversion;

}
