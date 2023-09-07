package br.psi.giganet.stockapi.stock_moves.docs;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.schedules.controller.request.InsertScheduledItemMoveRequest;
import br.psi.giganet.stockapi.schedules.controller.request.InsertScheduledMoveRequest;
import br.psi.giganet.stockapi.schedules.controller.request.UpdateScheduledItemMoveRequest;
import br.psi.giganet.stockapi.schedules.controller.request.UpdateScheduledMoveRequest;
import br.psi.giganet.stockapi.schedules.model.ScheduledExecution;
import br.psi.giganet.stockapi.schedules.model.ScheduledMove;
import br.psi.giganet.stockapi.schedules.repository.ScheduledMoveRepository;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.stock_moves.repository.StockMovesRepository;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SchedulingMovesDocs extends BuilderIntegrationTest {

    @Autowired
    public SchedulingMovesDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            StockRepository stockRepository,
            StockItemRepository stockItemRepository,
            StockMovesRepository stockMovesRepository,
            TechnicianRepository technicianRepository,
            ScheduledMoveRepository scheduledMoveRepository,
            BranchOfficeRepository branchOfficeRepository) {

        this.branchOfficeRepository = branchOfficeRepository;

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        this.stockRepository = stockRepository;
        this.stockItemRepository = stockItemRepository;
        this.stockMovesRepository = stockMovesRepository;
        this.technicianRepository = technicianRepository;
        this.scheduledMoveRepository = scheduledMoveRepository;
        createCurrentUser();

    }

    @RoleTestRoot
    @Transactional
    public void findAllBetween() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock shed = office.shed().orElseThrow();
        Stock technician = createAndSaveTechnicianStock(office);
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            products.add(createAndSaveProduct());
        }
        for (int i = 0; i < 3; i++) {
            createAndSaveScheduledMove(shed, technician, products);
        }

        this.mockMvc.perform(get("/moves/schedules")
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageContent("Lista com todas as movimentações agendadas")
                                        .andWithPrefix("content[].", getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void findById() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            products.add(createAndSaveProduct());
        }

        ScheduledMove move = createAndSaveScheduledMove(shed, technician, products);

        this.mockMvc.perform(get("/moves/schedules/{id}", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("Código do agendamento")),
                                getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void findByIdWithCurrentQuantity() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            products.add(createAndSaveProduct());
        }

        ScheduledMove move = createAndSaveScheduledMove(shed, technician, products);

        this.mockMvc.perform(get("/moves/schedules/{id}", move.getId())
                .param("withCurrentQuantity", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("Código do agendamento")),
                                requestParameters(
                                        parameterWithName("withCurrentQuantity")
                                                .description(createDescription(
                                                        "Parâmetro que indica o tipo do retorno esperado",
                                                        "O valor real deste parâmetro é ignorado, entretanto, deve estar presente para que este método seja executado")
                                                )),
                                getResponseWithCurrentQuantity()));
    }

    @RoleTestRoot
    @Transactional
    public void execute() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            products.add(createAndSaveProduct());
        }

        ScheduledMove move = createAndSaveScheduledMove(shed, technician, products);

        this.mockMvc.perform(post("/moves/schedules/{id}/execute", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("Código do agendamento")),
                                responseFields(getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void cancel() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            products.add(createAndSaveProduct());
        }

        ScheduledMove move = createAndSaveScheduledMove(shed, technician, products);

        this.mockMvc.perform(delete("/moves/schedules/{id}", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("Código do agendamento")),
                                responseFields(getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/moves/schedules")
                .content(objectMapper.writeValueAsString(createInsertScheduledMoveRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("date").description("Data e hora de executação do agendamento"),
                                        fieldWithPath("type").description("Tipo de movimentação"),
                                        fieldWithPath("execution").description("Forma de execução do agendamento: Manual ou Automática"),
                                        fieldWithPath("note").description("Observações da movimentação"),
                                        fieldWithPath("items").description("Lista com os itens a serem movimentados nessa operação, quando executada"),
                                        fieldWithPath("from").optional().type(JsonFieldType.NUMBER).description("Estoque de origem"),
                                        fieldWithPath("to").optional().type(JsonFieldType.NUMBER).description("Estoque de destino"))
                                        .andWithPrefix("items[].",
                                                fieldWithPath("product").description("Código do produto a ser movimentado (ID)"),
                                                fieldWithPath("quantity").description("Quantidade do item a ser movimentada")),
                                responseFields(getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            products.add(createAndSaveProduct());
        }
        ScheduledMove move = createAndSaveScheduledMove(shed, technician, products);

        this.mockMvc.perform(put("/moves/schedules/{id}", move.getId())
                .content(objectMapper.writeValueAsString(createUpdateScheduledMoveRequest(move)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("id").description("Código do agendamento"),
                                        fieldWithPath("date").description("Data e hora de executação do agendamento"),
                                        fieldWithPath("type").description("Tipo de movimentação"),
                                        fieldWithPath("execution").description("Forma de execução do agendamento: Manual ou Automática"),
                                        fieldWithPath("note").description("Observações da movimentação"),
                                        fieldWithPath("items").description("Lista com os itens a serem movimentados nessa operação, quando executada"),
                                        fieldWithPath("from").optional().type(JsonFieldType.NUMBER).description("Estoque de origem"),
                                        fieldWithPath("to").optional().type(JsonFieldType.NUMBER).description("Estoque de destino"))
                                        .andWithPrefix("items[].",
                                                fieldWithPath("id").description("Código do item do agendamento"),
                                                fieldWithPath("product").description("Código do produto a ser movimentado (ID)"),
                                                fieldWithPath("quantity").description("Quantidade do item a ser movimentada")),
                                responseFields(getProjection())));
    }


    private InsertScheduledMoveRequest createInsertScheduledMoveRequest() {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        InsertScheduledMoveRequest request = new InsertScheduledMoveRequest();
        request.setFrom(shed.getId());
        request.setTo(technician.getId());
        request.setType(MoveType.BETWEEN_STOCKS);
        request.setDate(ZonedDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        request.setExecution(ScheduledExecution.MANUAL);
        request.setItems(new ArrayList<>());

        InsertScheduledItemMoveRequest item1Request = new InsertScheduledItemMoveRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        request.getItems().add(item1Request);

        InsertScheduledItemMoveRequest item2Request = new InsertScheduledItemMoveRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        request.getItems().add(item2Request);

        request.setNote("Observação da movimentação");

        return request;
    }

    private UpdateScheduledMoveRequest createUpdateScheduledMoveRequest(ScheduledMove move) {
        UpdateScheduledMoveRequest request = new UpdateScheduledMoveRequest();
        request.setId(move.getId());
        request.setFrom(move.getFrom().getId());
        request.setTo(move.getTo().getId());
        request.setType(MoveType.BETWEEN_STOCKS);
        request.setDate(ZonedDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        request.setExecution(ScheduledExecution.MANUAL);
        request.setItems(
                move.getItems().stream()
                        .limit(2)
                        .map(item -> {
                            UpdateScheduledItemMoveRequest itemRequest = new UpdateScheduledItemMoveRequest();
                            itemRequest.setProduct(item.getProduct().getId());
                            itemRequest.setQuantity(1d);

                            return itemRequest;
                        })
                        .collect(Collectors.toList()));

        UpdateScheduledItemMoveRequest newItem = new UpdateScheduledItemMoveRequest();
        Product product = createAndSaveProduct();
        newItem.setProduct(product.getId());
        newItem.setQuantity(1d);
        request.getItems().add(newItem);

        request.setNote("Observação da movimentação");

        return request;
    }

    private FieldDescriptor[] getProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código do agendamento"),
                fieldWithPath("date").description("Data e hora de executação do agendamento"),
                fieldWithPath("origin").description("Origem da movimentação"),
                fieldWithPath("status").description("Status atual do agendamento"),
                fieldWithPath("type").description("Tipo de movimentação"),
                fieldWithPath("execution").description("Forma de execução do agendamento: Manual ou Automática"),
                fieldWithPath("description").description("Descrição da movimentação")};
    }

    private ResponseFieldsSnippet getResponse() {
        return responseFields(
                fieldWithPath("id").description("Código do agendamento"),
                fieldWithPath("date").description("Data e hora de executação do agendamento"),
                fieldWithPath("origin").description("Origem da movimentação"),
                fieldWithPath("status").description("Status atual do agendamento"),
                fieldWithPath("type").description("Tipo de movimentação"),
                fieldWithPath("execution").description("Forma de execução do agendamento: Manual ou Automática"),
                fieldWithPath("description").description("Descrição da movimentação"),
                fieldWithPath("items").description("Lista com os itens a serem movimentados nessa operação, quando executada"),
                fieldWithPath("from").optional().type(JsonFieldType.OBJECT).description("Estoque de origem"),
                fieldWithPath("to").optional().type(JsonFieldType.OBJECT).description("Estoque de destino"))
                .andWithPrefix("from.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Código do estoque"),
                        fieldWithPath("type").type(JsonFieldType.STRING).description("Tipo do estoque"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("Nome do estoque"))
                .andWithPrefix("to.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Código do estoque"),
                        fieldWithPath("type").type(JsonFieldType.STRING).description("Tipo do estoque"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("Nome do estoque"))
                .andWithPrefix("items[].",
                        fieldWithPath("id").description("Código item no agendamento"),
                        fieldWithPath("quantity").description("Quantidade a ser movimentada"),
                        fieldWithPath("product").description("Produto movimentado"),
                        fieldWithPath("move").optional().type(JsonFieldType.OBJECT).description("Movimentação associada, caso exista"))
                .andWithPrefix("items[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("items[].product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"))
                .andWithPrefix("items[].move.",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                        fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                        fieldWithPath("origin").optional().type(JsonFieldType.STRING).description("Origem da movimentação"),
                        fieldWithPath("status").optional().type(JsonFieldType.STRING).description("Status atual da movimentação"),
                        fieldWithPath("type").optional().type(JsonFieldType.STRING).description("Tipo de movimentação"),
                        fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description("Quantidade movimentada"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação"));
    }

    private ResponseFieldsSnippet getResponseWithCurrentQuantity() {
        return responseFields(
                fieldWithPath("id").description("Código do agendamento"),
                fieldWithPath("date").description("Data e hora de executação do agendamento"),
                fieldWithPath("origin").description("Origem da movimentação"),
                fieldWithPath("status").description("Status atual do agendamento"),
                fieldWithPath("type").description("Tipo de movimentação"),
                fieldWithPath("execution").description("Forma de execução do agendamento: Manual ou Automática"),
                fieldWithPath("description").description("Descrição da movimentação"),
                fieldWithPath("items").description("Lista com os itens a serem movimentados nessa operação, quando executada"),
                fieldWithPath("from").optional().type(JsonFieldType.OBJECT).description("Estoque de origem"),
                fieldWithPath("to").optional().type(JsonFieldType.OBJECT).description("Estoque de destino"))
                .andWithPrefix("from.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Código do estoque"),
                        fieldWithPath("type").type(JsonFieldType.STRING).description("Tipo do estoque"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("Nome do estoque"))
                .andWithPrefix("to.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Código do estoque"),
                        fieldWithPath("type").type(JsonFieldType.STRING).description("Tipo do estoque"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("Nome do estoque"))
                .andWithPrefix("items[].",
                        fieldWithPath("id").description("Código item no agendamento"),
                        fieldWithPath("availableQuantity").description("Quantidade disponível na origem no momento da pesquisa"),
                        fieldWithPath("quantity").description("Quantidade a ser movimentada"),
                        fieldWithPath("product").description("Produto movimentado"),
                        fieldWithPath("move").optional().type(JsonFieldType.OBJECT).description("Movimentação associada, caso exista"))
                .andWithPrefix("items[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("items[].product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"))
                .andWithPrefix("items[].move.",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                        fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                        fieldWithPath("origin").optional().type(JsonFieldType.STRING).description("Origem da movimentação"),
                        fieldWithPath("status").optional().type(JsonFieldType.STRING).description("Status atual da movimentação"),
                        fieldWithPath("type").optional().type(JsonFieldType.STRING).description("Tipo de movimentação"),
                        fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description("Quantidade movimentada"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação"));
    }
}
