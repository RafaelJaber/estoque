package br.psi.giganet.stockapi.units.docs;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.units.annotations.RoleTestUnitsRead;
import br.psi.giganet.stockapi.units.model.Unit;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UnitsDocs extends BuilderIntegrationTest {

    private final Unit unitTest;

    @Autowired
    public UnitsDocs(
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

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/units")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[]").description("Lista de unidades encontradas"))
                                .andWithPrefix("[].",
                                        fieldWithPath("id").description("Código da unidade"),
                                        fieldWithPath("name").description("Nome"),
                                        fieldWithPath("abbreviation").description("Abreviação da unidade"))
                ));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/units/{id}", unitTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da unidade buscada")
                        ),
                        getUnitResponse()));
    }

    private ResponseFieldsSnippet getUnitResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da unidade"),
                fieldWithPath("name").description("Nome"),
                fieldWithPath("abbreviation").description("Abreviação da unidade"),
                fieldWithPath("description").description("Descrição da unidade"),
                fieldWithPath("conversions")
                        .optional()
                        .type(JsonFieldType.ARRAY)
                        .description("Lista com todas as conversões cadastradas para esta unidade"))
                .andWithPrefix("conversions[].",
                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                .optional()
                                .description("Código do registro"),
                        fieldWithPath("to").type(JsonFieldType.OBJECT)
                                .optional()
                                .description("Unidade de destino"),
                        fieldWithPath("conversion").type(JsonFieldType.NUMBER)
                                .optional()
                                .description("Fator de conversão da presente unidade para a unidade destino."))
                .andWithPrefix("conversions[].to.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                .optional()
                                .description("Código da unidade"),
                        fieldWithPath("name").type(JsonFieldType.STRING)
                                .optional()
                                .description("Nome da unidade"),
                        fieldWithPath("abbreviation").type(JsonFieldType.STRING)
                                .optional()
                                .description("Abreviação utilizada para a unidade"));
    }

}
