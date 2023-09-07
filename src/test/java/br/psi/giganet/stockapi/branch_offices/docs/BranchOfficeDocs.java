package br.psi.giganet.stockapi.branch_offices.docs;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BranchOfficeDocs extends BuilderIntegrationTest {

    @Autowired
    public BranchOfficeDocs(
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

    @RoleTestRoot
    public void findAll() throws Exception {
        createAndSaveBranchOffice();

        this.mockMvc.perform(get("/branch-offices")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[]").description("Lista de filiais"))
                                .andWithPrefix("[].", getBranchOfficeResponse())
                                .andWithPrefix("[].shed.", getStockProjection())
                                .andWithPrefix("[].maintenance.", getStockProjection())
                                .andWithPrefix("[].obsolete.", getStockProjection())
                                .andWithPrefix("[].defective.", getStockProjection())
                                .andWithPrefix("[].customer.", getStockProjection())
                ));
    }

    @Test
    @WithMockUser(username = "teste_filiais_disponiveis@teste.com", authorities = {"ROLE_ROOT", "ROLE_ADMIN"})
    public void findAllAvailableByCurrentEmployee() throws Exception {
        Employee employee = createAndSaveEmployee("teste_filiais_disponiveis@teste.com");
        BranchOffice office = createAndSaveBranchOffice();
        employee.setBranchOffices(new HashSet<>(Set.of(office)));
        employeeRepository.saveAndFlush(employee);

        this.mockMvc.perform(get("/branch-offices/available")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[]").description("Lista de filiais relacionadas ao usuário logado"))
                                .andWithPrefix("[].", getBranchOfficeResponse())
                                .andWithPrefix("[].shed.", getStockProjection())
                                .andWithPrefix("[].maintenance.", getStockProjection())
                                .andWithPrefix("[].obsolete.", getStockProjection())
                                .andWithPrefix("[].defective.", getStockProjection())
                                .andWithPrefix("[].customer.", getStockProjection())
                ));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();

        this.mockMvc.perform(get("/branch-offices/{id}", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da filial buscada")
                        ),
                        responseFields(getBranchOfficeResponse())
                                .andWithPrefix("shed.", getStockProjection())
                                .andWithPrefix("maintenance.", getStockProjection())
                                .andWithPrefix("obsolete.", getStockProjection())
                                .andWithPrefix("defective.", getStockProjection())
                                .andWithPrefix("customer.", getStockProjection())
                ));
    }

    private FieldDescriptor[] getBranchOfficeResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código da filial cadastrada"),
                fieldWithPath("name").description("Nome da filial"),
                fieldWithPath("city").description("Cidade"),
                fieldWithPath("shed").description("Estoque do tipo GALPÃO associado a filial"),
                fieldWithPath("maintenance").description("Estoque do tipo MANUTENÇÃO associado a filial"),
                fieldWithPath("obsolete").description("Estoque do tipo OBSOLETOS associado a filial"),
                fieldWithPath("defective").description("Estoque do tipo DEFEITUOSOS associado a filial"),
                fieldWithPath("customer").description("Estoque do tipo CLIENTES associado a filial"),
        };
    }

    private FieldDescriptor[] getStockProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código do estoque"),
                fieldWithPath("name").description("Nome do estoque"),
                fieldWithPath("type").description("Tipo do estoque")
        };
    }

}
