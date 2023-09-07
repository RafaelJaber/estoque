package br.psi.giganet.stockapi.patrimonies_locations.docs;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.patrimonies_locations.controller.request.InsertPatrimonyLocationRequest;
import br.psi.giganet.stockapi.patrimonies_locations.controller.request.UpdatePatrimonyLocationRequest;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocationType;
import br.psi.giganet.stockapi.patrimonies_locations.repository.PatrimonyLocationRepository;
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
import java.util.UUID;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PatrimonyLocationDocs extends BuilderIntegrationTest {

    @Autowired
    public PatrimonyLocationDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            PatrimonyLocationRepository patrimonyLocationRepository,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;

        this.patrimonyLocationRepository = patrimonyLocationRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        createCurrentUser();

    }

    @RoleTestRoot
    @Transactional
    public void findAll() throws Exception {
        for (int i = 0; i < 3; i++) {
            createAndSavePatrimonyLocation();
        }

        this.mockMvc.perform(get("/patrimonies/locations")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("name").optional().description("Nome de localização utilizado como filtro"),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()
                                ),
                                getPageContent("Lista contendo todas as localizações encontradas")
                                        .andWithPrefix("content[].", getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void findById() throws Exception {
        PatrimonyLocation location = createAndSavePatrimonyLocation();

        this.mockMvc.perform(get("/patrimonies/locations/{id}", location.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da localização procurada")
                        ),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/patrimonies/locations")
                .content(objectMapper.writeValueAsString(createInsertPatrimonyLocationRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome da Localização"),
                                fieldWithPath("type").description(createDescription(
                                        "Tipo da localização",
                                        "Tem como objetivo facilitar a busca e identificação dos tipos associados, bem como os valores presentes na coluna 'code'")),
                                fieldWithPath("code").optional().type(JsonFieldType.STRING).description("Código da Localização"),
                                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações da Localização")
                        ),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        PatrimonyLocation location = createAndSavePatrimonyLocation();

        this.mockMvc.perform(put("/patrimonies/locations/{id}", location.getId())
                .content(objectMapper.writeValueAsString(createUpdatePatrimonyLocationRequest(location)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id").description("Código da localização")),
                        requestFields(
                                fieldWithPath("id").description("Código da localização"),
                                fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome da Localização"),
                                fieldWithPath("type").description(createDescription(
                                        "Tipo da localização",
                                        "Tem como objetivo facilitar a busca e identificação dos tipos associados, bem como os valores presentes na coluna 'code'")),
                                fieldWithPath("code").optional().type(JsonFieldType.STRING).description("Código da Localização"),
                                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações da Localização")),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void basicFindAll() throws Exception {
        for (int i = 0; i < 3; i++) {
            createAndSavePatrimonyLocation();
        }

        this.mockMvc.perform(get("/basic/patrimonies/locations")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("name").optional().description("Nome de localização utilizado como filtro"),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()
                                ),
                                getPageContent("Lista contendo todas as localizações encontradas")
                                        .andWithPrefix("content[].", getProjection())));
    }


    private InsertPatrimonyLocationRequest createInsertPatrimonyLocationRequest() {
        InsertPatrimonyLocationRequest request = new InsertPatrimonyLocationRequest();
        request.setCode(UUID.randomUUID().toString().substring(0, 8));
        request.setName("Localização");
        request.setType(PatrimonyLocationType.CUSTOMER);
        request.setNote("Observação");

        return request;
    }

    private UpdatePatrimonyLocationRequest createUpdatePatrimonyLocationRequest(PatrimonyLocation location) {
        UpdatePatrimonyLocationRequest request = new UpdatePatrimonyLocationRequest();
        request.setId(location.getId());
        request.setCode(location.getCode());
        request.setType(PatrimonyLocationType.CUSTOMER);
        request.setName("Localização Atualizada");
        request.setNote("Observação Atualizada");

        return request;
    }

    private FieldDescriptor[] getProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código da localização, gerado pelo banco de dados"),
                fieldWithPath("name").description("Nome do localização"),
                fieldWithPath("code").description(createDescription(
                        "Código de identificação da localização",
                        "Deve ser único",
                        "Este código pode corresponder a ids externos, de acordo com a localização associada"))};
    }

    private ResponseFieldsSnippet getResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da localização, gerado pelo banco de dados"),
                fieldWithPath("name").description("Nome do localização"),
                fieldWithPath("type")
                        .optional().type(JsonFieldType.STRING)
                        .description(createDescription(
                        "Tipo da localização",
                        "Tem como objetivo facilitar a busca e identificação dos tipos associados, bem como os valores presentes na coluna 'code'")),
                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações da localização"),
                fieldWithPath("code").description(createDescription(
                        "Código de identificação da localização",
                        "Deve ser único",
                        "Este código pode corresponder a ids externos, de acordo com a localização associada")));
    }

}
