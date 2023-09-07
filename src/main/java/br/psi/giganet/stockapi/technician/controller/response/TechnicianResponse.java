package br.psi.giganet.stockapi.technician.controller.response;

import lombok.Data;

@Data
public class TechnicianResponse extends TechnicianProjection {

    private Boolean isActive;

}
