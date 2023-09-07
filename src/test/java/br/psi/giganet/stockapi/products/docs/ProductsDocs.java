package br.psi.giganet.stockapi.products.docs;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.technician.model.Technician;
import br.psi.giganet.stockapi.technician.model.TechnicianSector;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.technician.technician_product_category.model.TechnicianSectorProductCategory;
import br.psi.giganet.stockapi.technician.technician_product_category.repository.TechnicianSectorProductCategoryRepository;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductsDocs extends BuilderIntegrationTest {

    private final Product productTest;

    @Autowired
    public ProductsDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            TechnicianRepository technicianRepository,
            TechnicianSectorProductCategoryRepository technicianSectorProductCategoryRepository,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        this.technicianRepository = technicianRepository;
        this.technicianSectorProductCategoryRepository = technicianSectorProductCategoryRepository;

        createCurrentUser();

        productTest = createAndSaveProduct();
    }

    @RoleTestRoot
    public void findByNameContaining() throws Exception {
        this.mockMvc.perform(get("/products")
                .param("name", productTest.getName().substring(0, 3))
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("name")
                                                .optional()
                                                .description(createDescription(
                                                        "Nome do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("page")
                                                .optional()
                                                .description(createDescription(
                                                        "Número da página solicitada",
                                                        "Valor baseado em 0 (ex: pagina inicial: 0)",
                                                        "Valor default: \"0\"")),
                                        parameterWithName("pageSize")
                                                .optional()
                                                .description(createDescription(
                                                        "Tamanho da página solicitada",
                                                        "Valor default: \"100\""))
                                ),
                                getPageContent("Lista com todos os produtos encontrados referentes aos filtros")
                                        .andWithPrefix("content[].",
                                                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("code").description("Código interno do produto"),
                                                fieldWithPath("manufacturer").description("Nome do fabricante"),
                                                fieldWithPath("unit").description("Unidade padrão"))
                                        .andWithPrefix("content[].unit.",
                                                fieldWithPath("id").description("Código da unidade"),
                                                fieldWithPath("name").description("Nome da unidade"),
                                                fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/products/{id}", productTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do produto buscado, gerado pelo banco de dados")
                        ),
                        getProductResponse()));
    }

    @RoleTestRoot
    public void findByCode() throws Exception {
        this.mockMvc.perform(get("/products/code/{code}", productTest.getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("code").description("Código do produto buscado, como por exemplo o número serial do produto")
                        ),
                        getProductResponse()));
    }

    @RoleTestRoot
    public void basicFindByNameContaining() throws Exception {
        this.mockMvc.perform(get("/basic/products")
                .param("name", productTest.getName().substring(0, 3))
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("name")
                                                .optional()
                                                .description(createDescription(
                                                        "Nome do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("page")
                                                .optional()
                                                .description(createDescription(
                                                        "Número da página solicitada",
                                                        "Valor baseado em 0 (ex: pagina inicial: 0)",
                                                        "Valor default: \"0\"")),
                                        parameterWithName("pageSize")
                                                .optional()
                                                .description(createDescription(
                                                        "Tamanho da página solicitada",
                                                        "Valor default: \"100\""))
                                ),
                                getPageContent("Lista com todos os produtos encontrados referentes aos filtros")
                                        .andWithPrefix("content[].",
                                                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("code").description("Código interno do produto"),
                                                fieldWithPath("manufacturer").description("Nome do fabricante"),
                                                fieldWithPath("unit").description("Unidade padrão"))
                                        .andWithPrefix("content[].unit.",
                                                fieldWithPath("id").description("Código da unidade"),
                                                fieldWithPath("name").description("Nome da unidade"),
                                                fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))));
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_PRODUCTS_READ"})
    public void basicFindByNameAndTechniciansCategory() throws Exception {
        Employee employee = createAndSaveEmployee("teste_technician@teste.com");
        employee.getPermissions().removeIf(p -> p.equals(new Permission("ROLE_ROOT")));
        employee.getPermissions().add(createAndSavePermission("ROLE_PRODUCTS_READ"));

        Product product = createAndSaveProduct();
        TechnicianSector sector = TechnicianSector.INSTALLATION;
        Technician technician = createAndSaveTechnicianByEmployeeAndSector(
                employeeRepository.saveAndFlush(employee),
                sector);

        TechnicianSectorProductCategory sectorCategory = new TechnicianSectorProductCategory();
        sectorCategory.setCategory(product.getCategory());
        sectorCategory.setSector(sector);
        technicianSectorProductCategoryRepository.saveAndFlush(sectorCategory);

        this.mockMvc.perform(get("/basic/products")
                .header("User-Id", technician.getUserId())
                .param("name", product.getName().substring(0, 3))
                .param("page", "0")
                .param("pageSize", "5")
                .param("filterCategory", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())))
                .andExpect(jsonPath("$.content[0].name", Matchers.is(product.getName())))
                .andDo(MockMvcResultHandlers.print())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("name")
                                                .optional()
                                                .description(createDescription(
                                                        "Nome do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("page")
                                                .optional()
                                                .description(createDescription(
                                                        "Número da página solicitada",
                                                        "Valor baseado em 0 (ex: pagina inicial: 0)",
                                                        "Valor default: \"0\"")),
                                        parameterWithName("pageSize")
                                                .optional()
                                                .description(createDescription(
                                                        "Tamanho da página solicitada",
                                                        "Valor default: \"100\"")),
                                        parameterWithName("filterCategory").description("Flag que indica que este método deve ser executado")
                                ),
                                getPageContent("Lista com todos os produtos encontrados referentes aos filtros")
                                        .andWithPrefix("content[].",
                                                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("code").description("Código interno do produto"))));
    }

    @RoleTestRoot
    public void basicFindById() throws Exception {
        this.mockMvc.perform(get("/basic/products/{id}", productTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do produto buscado, gerado pelo banco de dados")
                        ),
                        getProductResponse()));
    }

    @RoleTestRoot
    public void basicFindByCode() throws Exception {
        this.mockMvc.perform(get("/basic/products/code/{code}", productTest.getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("code").description("Código do produto buscado, como por exemplo o número serial do produto")
                        ),
                        getProductResponse()));
    }

    private ResponseFieldsSnippet getProductResponse() {
        return responseFields(
                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                fieldWithPath("name").description("Nome do produto"),
                fieldWithPath("code").description("Código interno do produto"),
                fieldWithPath("category").description("Categoria do produto"),
                fieldWithPath("manufacturer").description("Nome do fabricante"),
                fieldWithPath("description").type(JsonFieldType.STRING)
                        .optional()
                        .description("Uma breve descrição do produto"),
                fieldWithPath("unit").description("Unidade padrão"))
                .andWithPrefix("unit.",
                        fieldWithPath("id").description("Código da unidade"),
                        fieldWithPath("name").description("Nome da unidade"),
                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"),
                        fieldWithPath("description").description("Descrição da unidade"),
                        fieldWithPath("conversions[]").type(JsonFieldType.ARRAY)
                                .optional()
                                .description("Lista com todas as conversões cadastradas para esta unidade"))
                .andWithPrefix("unit.conversions[].",
                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                .optional()
                                .description("Código do registro"),
                        fieldWithPath("to").type(JsonFieldType.OBJECT)
                                .optional()
                                .description("Unidade de destino"),
                        fieldWithPath("conversion").type(JsonFieldType.NUMBER)
                                .optional()
                                .description("Fator de conversão da presente unidade para a unidade destino. Mais informações na sessão Unidades"))
                .andWithPrefix("unit.conversions[].to.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                .optional()
                                .description("Código da unidade"),
                        fieldWithPath("name").type(JsonFieldType.STRING)
                                .optional()
                                .description("Nome da unidade"),
                        fieldWithPath("abbreviation").type(JsonFieldType.STRING)
                                .optional()
                                .description("Abreviação utilizada para a unidade"))
                .andWithPrefix("category.",
                        fieldWithPath("id").description("Código da categoria"),
                        fieldWithPath("name").description("Nome da categoria"),
                        fieldWithPath("description").description("Descrição da categoria"));
    }

}
