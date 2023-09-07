package br.psi.giganet.stockapi.technician.controller.response;

import br.psi.giganet.stockapi.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.stockapi.technician.model.TechnicianSector;
import lombok.Data;

@Data
public class TechnicianProjection {

    private String id;
    private String name;
    private String email;
    private String userId;
    private TechnicianSector sector;
    private BranchOfficeProjection branchOffice;
}
