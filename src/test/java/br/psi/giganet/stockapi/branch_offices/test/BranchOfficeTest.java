package br.psi.giganet.stockapi.branch_offices.test;

import br.psi.giganet.stockapi.branch_offices.annotations.RoleTestBranchOfficesRead;
import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BranchOfficeTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    @Autowired
    public BranchOfficeTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.stockRepository = stockRepository;
        this.branchOfficeRepository = branchOfficeRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;

        createCurrentUser();
    }


    @Override
    @Transactional
    @RoleTestBranchOfficesRead
    public void readAuthorized() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();

        this.mockMvc.perform(get("/branch-offices"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/branch-offices/{id}", office.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @Transactional
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();

        this.mockMvc.perform(get("/branch-offices"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/branch-offices/{id}", office.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    public void writeAuthorized() throws Exception {
    }

    @Override
    public void writeUnauthorized() throws Exception {
    }

}
