package br.psi.giganet.stockapi.dashboard.tests;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.dashboard.main_items.controller.request.MainDashboardItemGroupRequest;
import br.psi.giganet.stockapi.dashboard.main_items.controller.request.MainDashboardItemRequest;
import br.psi.giganet.stockapi.dashboard.main_items.model.GroupCategory;
import br.psi.giganet.stockapi.dashboard.main_items.model.MainDashboardItemGroup;
import br.psi.giganet.stockapi.dashboard.main_items.repository.MainDashboardItemGroupRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MainDashboardItemsGroupTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    @Autowired
    public MainDashboardItemsGroupTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            MainDashboardItemGroupRepository mainDashboardItemGroupRepository,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.unitRepository = unitRepository;
        this.mainDashboardItemGroupRepository = mainDashboardItemGroupRepository;

        createCurrentUser();
    }

    @Override
    @RoleTestRoot
    public void readAuthorized() throws Exception {
        MainDashboardItemGroup group = createAndSaveGroup();
        for (int i = 0; i < 3; i++) {
            createAndSaveGroup("Custom " + getRandomId(), GroupCategory.CUSTOM, createAndSaveEmployee());
        }

        this.mockMvc.perform(get("/dashboard/main-item-groups")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/dashboard/main-item-groups/{id}", group.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestRoot
    public void writeAuthorized() throws Exception {
        MainDashboardItemGroup group = createAndSaveGroup();

        this.mockMvc.perform(post("/dashboard/main-item-groups")
                .content(objectMapper.writeValueAsString(createMainDashboardItemGroupRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(put("/dashboard/main-item-groups/{id}", group.getId())
                .content(objectMapper.writeValueAsString(createMainDashboardItemGroupRequest(group)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    public void readUnauthorized() {
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        MainDashboardItemGroup group = createAndSaveGroup();

        this.mockMvc.perform(post("/dashboard/main-item-groups")
                .content(objectMapper.writeValueAsString(createMainDashboardItemGroupRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/dashboard/main-item-groups/{id}", group.getId())
                .content(objectMapper.writeValueAsString(createMainDashboardItemGroupRequest(group)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }


    private MainDashboardItemGroupRequest createMainDashboardItemGroupRequest() {
        MainDashboardItemGroupRequest request = new MainDashboardItemGroupRequest();
        request.setLabel("Label Custom " + getRandomId());
        request.setCategory(GroupCategory.CUSTOM);
        request.setBranchOffice(createAndSaveBranchOffice().getId());
        request.setEmployees(Stream.of(currentLoggedUser).map(AbstractModel::getId).collect(Collectors.toSet()));
        request.setItems(new ArrayList<>());
        for (int i = 0; i < 3; i++) {
            MainDashboardItemRequest item = new MainDashboardItemRequest();
            item.setIndex(i);
            item.setProduct(createAndSaveProduct().getId());
            request.getItems().add(item);
        }

        return request;
    }

    private MainDashboardItemGroupRequest createMainDashboardItemGroupRequest(MainDashboardItemGroup group) {
        MainDashboardItemGroupRequest request = new MainDashboardItemGroupRequest();
        request.setId(group.getId());
        request.setLabel("Label do Grupo " + getRandomId());
        request.setCategory(GroupCategory.DEFAULT);
        request.setBranchOffice(group.getBranchOffice().getId());
        request.setEmployees(Collections.emptySet());
        request.setItems(new ArrayList<>());
        for (int i = 0; i < 3; i++) {
            MainDashboardItemRequest item = new MainDashboardItemRequest();
            item.setIndex(i);
            item.setProduct(createAndSaveProduct().getId());
            request.getItems().add(item);
        }

        return request;
    }
}
