package br.psi.giganet.stockapi.units.controller.response;

import lombok.Data;

import java.util.List;

@Data
public class UnitResponse {

    private String id;
    private String name;
    private String description;
    private String abbreviation;
    private List<UnitConversionResponse> conversions;
}
