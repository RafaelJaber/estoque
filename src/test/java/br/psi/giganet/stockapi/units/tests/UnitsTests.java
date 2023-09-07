package br.psi.giganet.stockapi.units.tests;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.units.annotations.RoleTestUnitsRead;
import br.psi.giganet.stockapi.units.model.Unit;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UnitsTests extends BuilderIntegrationTest implements RolesIntegrationTest {

    private final Unit unitTest;

    @Autowired
    public UnitsTests(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            UnitRepository unitRepository,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        createCurrentUser();

        unitTest = createAndSaveUnit();
    }

    @Override
    @RoleTestUnitsRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/units"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/units/{id}", unitTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/units"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/units/{id}", unitTest.getId()))
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
