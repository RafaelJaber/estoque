package br.psi.giganet.stockapi.commons.notifications.docs;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.notifications.controller.request.MarkAsReadRequest;
import br.psi.giganet.stockapi.common.notifications.model.Notification;
import br.psi.giganet.stockapi.common.notifications.repository.NotificationRepository;
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
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationDocs extends BuilderIntegrationTest {

    @Autowired
    public NotificationDocs(
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

    @RoleTestRoot
    public void findById() throws Exception {
        createNotificationsPermissions();
        Notification notification = createAndSaveNotification(Collections.singletonList(currentLoggedUser));

        this.mockMvc.perform(get("/notifications/{id}", notification.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da notificação buscada")
                        ),
                        responseFields(
                                getNotificationResponse())));
    }

    @RoleTestRoot
    public void findAllByCurrentEmployee() throws Exception {
        createNotificationsPermissions();
        createAndSaveNotification(Collections.singletonList(currentLoggedUser));
        this.mockMvc.perform(get("/notifications/me")
                .param("limit", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("limit").description(
                                        createDescription("Quantidade limite das notificações a serem retornadas",
                                                "Valor default: 50"))
                        ),
                        responseFields(
                                fieldWithPath("[]").description("Lista com todas as notificações do usuário logado, lidas e não lidas"))
                                .andWithPrefix("[].",
                                        getNotificationResponse())));
    }

    @RoleTestRoot
    public void findAllUnreadByCurrentEmployee() throws Exception {
        createNotificationsPermissions();
        createAndSaveNotification(Collections.singletonList(currentLoggedUser));
        this.mockMvc.perform(get("/notifications/me/unread")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[]").description("Lista com todas as notificações não lidas do usuário logado"))
                                .andWithPrefix("[].",
                                        getNotificationResponse())));
    }


    @RoleTestRoot
    public void markAllAsViewed() throws Exception {
        createNotificationsPermissions();
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
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("notifications").description(
                                        "Lista com os IDs das notificações a serem marcadas como visualizadas")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("Lista com as notificações atualizadas"))
                                .andWithPrefix("[].",
                                        getNotificationResponse())));
    }

    @RoleTestRoot
    public void markAsViewed() throws Exception {
        createNotificationsPermissions();
        Notification notification = createAndSaveNotification(Collections.singletonList(currentLoggedUser));
        this.mockMvc.perform(post("/notifications/{id}/view", notification.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da notificação a ser definida como visualizada")
                        ),
                        responseFields(
                                getNotificationResponse())));
    }

    private MarkAsReadRequest createMarkAsReadRequest() {
        return createMarkAsReadRequest(Stream.of(
                createAndSaveNotification(),
                createAndSaveNotification(),
                createAndSaveNotification())
                .map(Notification::getId)
                .collect(Collectors.toList()));
    }

    private MarkAsReadRequest createMarkAsReadRequest(List<Long> notifications) {
        MarkAsReadRequest request = new MarkAsReadRequest();
        request.setNotifications(notifications);
        return request;
    }

    private FieldDescriptor[] getNotificationResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código da notificação"),
                fieldWithPath("title").description("Título da notificação"),
                fieldWithPath("description").description("Descrição notificação"),
                fieldWithPath("date").description("Data de criação da notificação"),
                fieldWithPath("type").description("Tipo da notificação. Normalmente é de acordo com o evento associado"),
                fieldWithPath("data").optional().type(JsonFieldType.STRING)
                        .description("Campo opcional de dado. É definido de acordo com o tipo da notificação"),
                fieldWithPath("viewed").description("Informa se o usuário visualizou ou não a notificação"),
                fieldWithPath("viewedDate").optional().type(JsonFieldType.STRING)
                        .description("Data da visualização pelo usuário atual, caso exista"),
        };
    }
}
