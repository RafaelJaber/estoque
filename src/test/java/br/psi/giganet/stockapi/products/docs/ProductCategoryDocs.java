package br.psi.giganet.stockapi.products.docs;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.products.categories.model.Category;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductCategoryDocs extends BuilderIntegrationTest {

    private final Category categoryTest;

    @Autowired
    public ProductCategoryDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductCategoryRepository productCategoryRepository,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        createCurrentUser();

        categoryTest = createAndSaveCategory();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/products/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[]").description("Lista de categorias encontradas"))
                                .andWithPrefix("[].",
                                        fieldWithPath("id").description("Código da categoria"),
                                        fieldWithPath("name").description("Nome"),
                                        fieldWithPath("description").description("Descrição da categoria"))
                ));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/products/categories/{id}", categoryTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da categoria buscada")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código da categoria"),
                                fieldWithPath("name").description("Nome"),
                                fieldWithPath("description").description("Descrição da categoria")
                        )
                ));
    }

}
