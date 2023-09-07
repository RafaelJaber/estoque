package br.psi.giganet.stockapi.employees.controller.response;

import br.psi.giganet.stockapi.branch_offices.controller.response.BranchOfficeProjection;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
public class EmployeeResponse {

    private Long id;
    private ZonedDateTime createdDate;
    private ZonedDateTime lastModifiedDate;
    private String email;
    private String name;
    private Set<String> permissions;
    private Set<BranchOfficeProjection> branchOffices;

}
