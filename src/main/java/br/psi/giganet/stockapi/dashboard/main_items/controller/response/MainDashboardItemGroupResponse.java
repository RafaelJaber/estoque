package br.psi.giganet.stockapi.dashboard.main_items.controller.response;

import br.psi.giganet.stockapi.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.stockapi.dashboard.main_items.model.GroupCategory;
import br.psi.giganet.stockapi.employees.controller.response.EmployeeProjection;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class MainDashboardItemGroupResponse {

    private Long id;
    private String label;
    private GroupCategory category;
    private BranchOfficeProjection branchOffice;
    private Set<EmployeeProjection> employees;
    private List<MainDashboardItemResponse> items;

}
