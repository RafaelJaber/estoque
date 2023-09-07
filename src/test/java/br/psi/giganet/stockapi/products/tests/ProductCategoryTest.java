package br.psi.giganet.stockapi.products.tests;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.products.annotations.RoleTestProductsRead;
import br.psi.giganet.stockapi.products.categories.model.Category;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductCategoryTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private final Category categoryTest;

    @Autowired
    public ProductCategoryTest(
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

    @Override
    @RoleTestProductsRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/products/categories"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/products/categories/{id}", categoryTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/products/categories"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/products/categories/{id}", categoryTest.getId()))
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
