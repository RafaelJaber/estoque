package br.psi.giganet.stockapi.purchase_orders.tests;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.address.service.AddressService;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrder;
import br.psi.giganet.stockapi.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.stockapi.purchase_orders.annotations.RoleTestPurchaseOrdersRead;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PurchaseOrderTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private final PurchaseOrder orderTest;

    @Autowired
    public PurchaseOrderTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            AddressService addressService,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.addressService = addressService;
        createCurrentUser();

        orderTest = createAndSavePurchaseOrder();
    }

    @Override
    @RoleTestPurchaseOrdersRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-orders/{id}", orderTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-orders/{id}/pending", orderTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-orders/items")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    public void writeAuthorized() throws Exception {
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-orders/{id}", orderTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-orders/{id}/pending", orderTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-orders/items")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
    }

}
