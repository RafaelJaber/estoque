package br.psi.giganet.stockapi.employees.docs;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.controller.request.UpdatePermissionsRequest;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmployeeDocs extends BuilderIntegrationTest {

    @Autowired
    public EmployeeDocs(
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

    @RoleTestRoot
    @Transactional
    public void findByName() throws Exception {
        for (int i = 0; i < 3; i++) {
            createAndSaveEmployee();
        }

        this.mockMvc.perform(get("/employees")
                .param("name", "e")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("name").description(createDescription("Nome do funcionário",
                                                "Caso não sensitivo"))),
                                responseFields(fieldWithPath("[]").description("Lista com os funcionários encontrados"))
                                        .andWithPrefix("[].",
                                                fieldWithPath("id").description("Código do funcionário"),
                                                fieldWithPath("name").description("Nome"),
                                                fieldWithPath("email").description("Email"))));
    }

    @RoleTestRoot
    @Transactional
    public void findByNameAndPermissions() throws Exception {
        for (int i = 0; i < 3; i++) {
            createAndSaveEmployee();
        }

        this.mockMvc.perform(get("/employees/permissions/{permission}", "ROLE_ADMIN")
                .param("name", "e")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("permission").description("Permissão procurada")),
                                requestParameters(
                                        parameterWithName("name").description(createDescription("Nome do funcionário",
                                                "Caso não sensitivo"))),
                                responseFields(fieldWithPath("[]").description("Lista com os funcionários encontrados"))
                                        .andWithPrefix("[].", getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void findById() throws Exception {
        Employee employee = createAndSaveEmployee();
        BranchOffice office = createAndSaveBranchOffice();
        employee.setBranchOffices(new HashSet<>(Set.of(office)));
        employeeRepository.saveAndFlush(employee);

        this.mockMvc.perform(get("/employees/{id}", employee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do funcionário procurado")
                        ),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        Employee employee = createAndSaveEmployee();

        this.mockMvc.perform(put("/employees/{id}/permissions", employee.getId())
                .content(objectMapper.writeValueAsString(createUpdatePermissionsRequest(employee)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id").description("Código do funcionário")),
                        requestFields(
                                fieldWithPath("id").description("Código do funcionário"),
                                fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome do funcionário"),
                                fieldWithPath("permissions").type(JsonFieldType.ARRAY).description("Lista com as permissões do usuário"),
                                fieldWithPath("branchOffices").type(JsonFieldType.ARRAY).description("Lista com as filiais as quais o usuário possui acesso")),
                        responseFields(getProjection())));
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

    private FieldDescriptor[] getProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código do funcionário"),
                fieldWithPath("name").description("Nome")};
    }

    private ResponseFieldsSnippet getResponse() {
        return responseFields(
                fieldWithPath("id").description("Código do funcionário"),
                fieldWithPath("name").description("Nome"),
                fieldWithPath("email").description("Email"),
                fieldWithPath("createdDate").description(createDescription("Data de criação do registro",
                        "Pode ser através do primeiro login ou através da importação do funcionário")),
                fieldWithPath("lastModifiedDate").description("Data da última modificação"),
                fieldWithPath("permissions").description("Permissões associadas ao funcionário"),
                fieldWithPath("branchOffices").description("Lista com as filiais as quais o usuário possui acesso"))
                .andWithPrefix("branchOffices[].",
                        fieldWithPath("id").description("Código ID da filial"),
                        fieldWithPath("name").description("Nome da Filial"),
                        fieldWithPath("city").description("Cidade da filial"));
    }

}
