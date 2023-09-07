package br.psi.giganet.stockapi.templates.docs;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock_moves.repository.StockMovesRepository;
import br.psi.giganet.stockapi.patrimonies.repository.PatrimonyRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.templates.controller.request.InsertTemplateItemRequest;
import br.psi.giganet.stockapi.templates.controller.request.InsertTemplateRequest;
import br.psi.giganet.stockapi.templates.controller.request.UpdateTemplateItemRequest;
import br.psi.giganet.stockapi.templates.controller.request.UpdateTemplateRequest;
import br.psi.giganet.stockapi.templates.model.Template;
import br.psi.giganet.stockapi.templates.repository.TemplateRepository;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TemplateDocs extends BuilderIntegrationTest {

    @Autowired
    public TemplateDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            StockRepository stockRepository,
            StockItemRepository stockItemRepository,
            PatrimonyRepository patrimonyRepository,
            StockMovesRepository stockMovesRepository,
            TechnicianRepository technicianRepository,
            TemplateRepository templateRepository,
            BranchOfficeRepository branchOfficeRepository) {

        this.branchOfficeRepository = branchOfficeRepository;

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        this.stockRepository = stockRepository;
        this.stockItemRepository = stockItemRepository;
        this.patrimonyRepository = patrimonyRepository;
        this.stockMovesRepository = stockMovesRepository;
        this.technicianRepository = technicianRepository;
        this.templateRepository = templateRepository;
        createCurrentUser();

    }

    @RoleTestRoot
    @Transactional
    public void findAll() throws Exception {
        for (int i = 0; i < 3; i++) {
            createAndSaveTemplate();
        }

        this.mockMvc.perform(get("/moves/templates")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("[]").description("Lista com os templates encontrados"))
                                        .andWithPrefix("[].", getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void findById() throws Exception {
        Template template = createAndSaveTemplate();

        this.mockMvc.perform(get("/moves/templates/{id}", template.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do template procurado")
                        ),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/moves/templates")
                .content(objectMapper.writeValueAsString(createInsertTemplateRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome do template"))
                                .andWithPrefix("items[].",
                                        fieldWithPath("product").description("Código do produto a ser movimentado (ID)"),
                                        fieldWithPath("quantity").description("Quantidade do item a ser movimentada")),
                        responseFields(getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        Template template = createAndSaveTemplate();

        this.mockMvc.perform(put("/moves/templates/{id}", template.getId())
                .content(objectMapper.writeValueAsString(createUpdateTemplateRequest(template)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id").description("Código do template")),
                        requestFields(
                                fieldWithPath("id").description("Código do template"),
                                fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome do template"))
                                .andWithPrefix("items[].",
                                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código do item do template"),
                                        fieldWithPath("product").description("Código do produto a ser movimentado (ID)"),
                                        fieldWithPath("quantity").description("Quantidade do item a ser movimentada")),
                        responseFields(getProjection())));
    }


    @RoleTestRoot
    @Transactional
    public void deleteById() throws Exception {
        Template template = createAndSaveTemplate();

        this.mockMvc.perform(delete("/moves/templates/{id}", template.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do template procurado"))));
    }

    private InsertTemplateRequest createInsertTemplateRequest() {
        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        InsertTemplateRequest request = new InsertTemplateRequest();
        request.setName("Template");
        request.setItems(new ArrayList<>());

        InsertTemplateItemRequest item1Request = new InsertTemplateItemRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        request.getItems().add(item1Request);

        InsertTemplateItemRequest item2Request = new InsertTemplateItemRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        request.getItems().add(item2Request);

        return request;
    }

    private UpdateTemplateRequest createUpdateTemplateRequest(Template template) {
        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        UpdateTemplateRequest request = new UpdateTemplateRequest();
        request.setId(template.getId());
        request.setName("Template " + getRandomId());
        request.setItems(template.getItems().stream().map(item -> {
            UpdateTemplateItemRequest templateItem = new UpdateTemplateItemRequest();
            templateItem.setId(item.getId());
            templateItem.setProduct(item.getProduct().getId());
            templateItem.setQuantity(item.getQuantity());

            return templateItem;
        }).collect(Collectors.toList()));

        UpdateTemplateItemRequest item1Request = new UpdateTemplateItemRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        request.getItems().add(item1Request);

        return request;
    }

    private FieldDescriptor[] getProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código da template"),
                fieldWithPath("name").description("Nome do template")};
    }

    private ResponseFieldsSnippet getResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da movimentação"),
                fieldWithPath("name").description("Nome do template"),
                fieldWithPath("items").description("Itens do template"))
                .andWithPrefix("items[].",
                        fieldWithPath("id").description("Código do item do template"),
                        fieldWithPath("product").description("Item escolhido"),
                        fieldWithPath("quantity").description("Quantidade"))
                .andWithPrefix("items[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("items[].product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"));
    }

}
