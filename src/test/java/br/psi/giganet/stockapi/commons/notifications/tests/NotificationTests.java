package br.psi.giganet.stockapi.commons.notifications.tests;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.notifications.controller.request.MarkAsReadRequest;
import br.psi.giganet.stockapi.common.notifications.controller.security.RoleNotificationsRead;
import br.psi.giganet.stockapi.common.notifications.model.Notification;
import br.psi.giganet.stockapi.common.notifications.repository.NotificationRepository;
import br.psi.giganet.stockapi.commons.notifications.annotations.RoleTestNotificationsRead;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.stock.repository.StockItemQuantityLevelRepository;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationTests extends BuilderIntegrationTest implements RolesIntegrationTest {

    @Autowired
    public NotificationTests(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            StockRepository stockRepository,
            StockItemRepository stockItemRepository,
            StockItemQuantityLevelRepository stockItemQuantityLevelRepository,
            NotificationRepository notificationRepository,
            BranchOfficeRepository branchOfficeRepository) {

        this.branchOfficeRepository = branchOfficeRepository;

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        this.stockRepository = stockRepository;
        this.stockItemRepository = stockItemRepository;
        this.stockItemQuantityLevelRepository = stockItemQuantityLevelRepository;
        this.notificationRepository = notificationRepository;
        createCurrentUser();

    }


    @Override
    @RoleTestNotificationsRead
    public void readAuthorized() throws Exception {
        List<Employee> list = Collections.singletonList(currentLoggedUser);
        Notification notification = createAndSaveNotification(list);
        createAndSaveNotification(list);
        createAndSaveNotification(list);

        this.mockMvc.perform(get("/notifications/{id}", notification.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/notifications/me")
                .param("limit", "3")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/notifications/me/unread")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleNotificationsRead
    public void writeAuthorized() throws Exception {
        List<Employee> list = Collections.singletonList(currentLoggedUser);
        MarkAsReadRequest request = createMarkAsReadRequest(
                Stream.of(createAndSaveNotification(list), createAndSaveNotification(list))
                        .map(Notification::getId)
                        .collect(Collectors.toList()));

        this.mockMvc.perform(post("/notifications/view")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].viewed", Matchers.is(Boolean.TRUE)))
                .andExpect(jsonPath("$[1].viewed", Matchers.is(Boolean.TRUE)));

        Notification notification = createAndSaveNotification(list);
        this.mockMvc.perform(post("/notifications/{id}/view", notification.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewed", Matchers.is(Boolean.TRUE)));
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        List<Employee> list = Collections.singletonList(currentLoggedUser);
        Notification notification = createAndSaveNotification(list);
        createAndSaveNotification(list);
        createAndSaveNotification(list);

        this.mockMvc.perform(get("/notifications/{id}", notification.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/notifications/me")
                .param("limit", "3")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/notifications/me/unread")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        List<Employee> list = Collections.singletonList(currentLoggedUser);
        MarkAsReadRequest request = createMarkAsReadRequest(
                Stream.of(createAndSaveNotification(list), createAndSaveNotification(list))
                        .map(Notification::getId)
                        .collect(Collectors.toList()));

        this.mockMvc.perform(post("/notifications/view")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        Notification notification = createAndSaveNotification(list);
        this.mockMvc.perform(post("/notifications/{id}/view", notification.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private MarkAsReadRequest createMarkAsReadRequest(List<Long> notifications) {
        MarkAsReadRequest request = new MarkAsReadRequest();
        request.setNotifications(notifications);
        return request;
    }

}
