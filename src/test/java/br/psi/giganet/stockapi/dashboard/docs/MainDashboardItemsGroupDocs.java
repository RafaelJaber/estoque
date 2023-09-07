package br.psi.giganet.stockapi.dashboard.docs;

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
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MainDashboardItemsGroupDocs extends BuilderIntegrationTest {

    @Autowired
    public MainDashboardItemsGroupDocs(
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

    @RoleTestAdmin
    public void findAll() throws Exception {
        createAndSaveGroup();
        for (int i = 0; i < 3; i++) {
            createAndSaveGroup("Custom " + i, GroupCategory.CUSTOM, createAndSaveEmployee());
        }

        this.mockMvc.perform(get("/dashboard/main-item-groups")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        getListMainDashboardItemGroupResponse()));

    }

    @RoleTestAdmin
    public void findById() throws Exception {
        MainDashboardItemGroup group = createAndSaveGroup();

        this.mockMvc.perform(get("/dashboard/main-item-groups/{id}", group.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código ID do grupo associado")),
                        getMainDashboardItemGroupResponse()));

    }

    @RoleTestRoot
    public void insert() throws Exception {
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

        this.mockMvc.perform(post("/dashboard/main-item-groups")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("id").optional().description("Código ID do grupo. Será desconsiderado na inserção"),
                                fieldWithPath("label").description("Nome do grupo, utilizado apenas para identificação"),
                                fieldWithPath("category").description("Categoria do grupo"),
                                fieldWithPath("branchOffice").description("Filial associada"),
                                fieldWithPath("employees").description("Lista com os IDs dos funcionários associados"),
                                fieldWithPath("items").description("Lista com os itens associados"))
                                .andWithPrefix("items[].",
                                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código ID do relacionamento"),
                                        fieldWithPath("index").description("Índice representando a ordem de prioridade do item"),
                                        fieldWithPath("product").description("Código ID do produto associado")),
                        getMainDashboardItemGroupProjection()));

    }

    @RoleTestRoot
    public void update() throws Exception {
        MainDashboardItemGroup group = createAndSaveGroup();

        MainDashboardItemGroupRequest request = new MainDashboardItemGroupRequest();
        request.setId(group.getId());
        request.setLabel("Label do Grupo "+ getRandomId());
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

        this.mockMvc.perform(put("/dashboard/main-item-groups/{id}", request.getId())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código ID do grupo")
                        ),
                        requestFields(
                                fieldWithPath("id").description("Código ID do grupo"),
                                fieldWithPath("label").description("Nome do grupo, utilizado apenas para identificação"),
                                fieldWithPath("category").description("Categoria do grupo"),
                                fieldWithPath("branchOffice").description("Filial associada"),
                                fieldWithPath("employees").description("Lista com os IDs dos funcionários associados"),
                                fieldWithPath("items").description("Lista com os itens associados"))
                                .andWithPrefix("items[].",
                                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código ID do relacionamento"),
                                        fieldWithPath("index").description("Índice representando a ordem de prioridade do item"),
                                        fieldWithPath("product").description("Código ID do produto associado")),
                        getMainDashboardItemGroupProjection()));

    }

    private ResponseFieldsSnippet getListMainDashboardItemGroupResponse() {
        return responseFields(
                fieldWithPath("[]").description("Lista com os grupos de itens principais para o Dashboard"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código ID do grupo"),
                        fieldWithPath("label").description("Label do grupo utilizado como descrição"),
                        fieldWithPath("category").description("Categoria do grupo. Pode ser: 'DEFAULT' ou 'CUSTOM'"),
                        fieldWithPath("branchOffice").description("Filial associada ao grupo"),
                        fieldWithPath("employees").description("Lista com os usuários associados a este grupo"),
                        fieldWithPath("items").description("Lista de itens associados"))
                .andWithPrefix("[].branchOffice.",
                        fieldWithPath("id").description("Código da filial associada"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("city").description("Cidade"))
                .andWithPrefix("[].employees[].",
                        fieldWithPath("id").description("Código ID do funcionário"),
                        fieldWithPath("name").description("Nome do funcionário"))
                .andWithPrefix("[].items[].",
                        fieldWithPath("id").description("Código ID do relacionamento"),
                        fieldWithPath("product").description("Produto associado"))
                .andWithPrefix("[].items[].product.",
                        fieldWithPath("id").description("Código ID do produto"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("code").description("Código do produto (Código único estabelecido pelos responsáveis)"));
    }

    private ResponseFieldsSnippet getMainDashboardItemGroupResponse() {
        return responseFields(
                fieldWithPath("id").description("Código ID do grupo"),
                fieldWithPath("label").description("Label do grupo utilizado como descrição"),
                fieldWithPath("category").description("Categoria do grupo. Pode ser: 'DEFAULT' ou 'CUSTOM'"),
                fieldWithPath("employees").description("Lista com os usuários associados a este grupo"),
                fieldWithPath("branchOffice").description("Filial associada ao grupo"),
                fieldWithPath("items").description("Lista de itens associados"))
                .andWithPrefix("branchOffice.",
                        fieldWithPath("id").description("Código da filial associada"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("city").description("Cidade"))
                .andWithPrefix("employees[].",
                        fieldWithPath("id").description("Código ID do funcionário"),
                        fieldWithPath("name").description("Nome do funcionário"))
                .andWithPrefix("items[].",
                        fieldWithPath("id").description("Código ID do relacionamento"),
                        fieldWithPath("product").description("Produto associado"))
                .andWithPrefix("items[].product.",
                        fieldWithPath("id").description("Código ID do produto"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("code").description("Código do produto (Código único estabelecido pelos responsáveis)"));
    }

    private ResponseFieldsSnippet getMainDashboardItemGroupProjection() {
        return responseFields(
                fieldWithPath("id").description("Código ID do grupo"),
                fieldWithPath("label").description("Label do grupo utilizado como descrição"),
                fieldWithPath("category").description("Categoria do grupo. Pode ser: 'DEFAULT' ou 'CUSTOM'"));
    }

}
