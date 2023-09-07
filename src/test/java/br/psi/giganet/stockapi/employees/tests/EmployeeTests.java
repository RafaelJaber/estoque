package br.psi.giganet.stockapi.employees.tests;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.controller.request.UpdatePermissionsRequest;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmployeeTests extends BuilderIntegrationTest implements RolesIntegrationTest {

    @Autowired
    public EmployeeTests(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        createCurrentUser();
    }

    @Override
    @RoleTestRoot
    @Transactional
    public void readAuthorized() throws Exception {
        for (int i = 0; i < 2; i++) {
            createAndSaveEmployee();
        }

        this.mockMvc.perform(get("/employees")
                .param("name", "e")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(get("/employees/permissions/{permission}", "ROLE_ADMIN")
                .param("name", "e")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        Employee employee = createAndSaveEmployee();
        this.mockMvc.perform(get("/employees/{id}", employee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestRoot
    public void writeAuthorized() throws Exception {
        Employee employee = createAndSaveEmployee();

        this.mockMvc.perform(put("/employees/{id}/permissions", employee.getId())
                .content(objectMapper.writeValueAsString(createUpdatePermissionsRequest(employee)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    public void readUnauthorized() throws Exception {
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        Employee employee = createAndSaveEmployee();

        this.mockMvc.perform(put("/employees/{id}/permissions", employee.getId())
                .content(objectMapper.writeValueAsString(createUpdatePermissionsRequest(employee)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }


    private UpdatePermissionsRequest createUpdatePermissionsRequest(Employee employee) {
        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setId(employee.getId());
        request.setName(employee.getName());
        request.setPermissions(
                employee.getPermissions().stream()
                        .limit(1)
                        .map(Permission::getName)
                        .collect(Collectors.toSet()));

        request.getPermissions().add(createAndSavePermission("ROLE_PERMISSION_TEST").getName());
        request.setBranchOffices(
                branchOfficeRepository.findAll()
                        .stream()
                        .map(BranchOffice::getId)
                        .collect(Collectors.toSet()));


        return request;
    }

}
