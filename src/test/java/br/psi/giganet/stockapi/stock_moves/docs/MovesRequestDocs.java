package br.psi.giganet.stockapi.stock_moves.docs;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.moves_request.controller.request.BasicInsertRequestedMoveRequest;
import br.psi.giganet.stockapi.moves_request.controller.request.InsertRequestedMoveItemRequest;
import br.psi.giganet.stockapi.moves_request.controller.request.InsertRequestedMoveRequest;
import br.psi.giganet.stockapi.moves_request.model.RequestedMove;
import br.psi.giganet.stockapi.moves_request.repository.MovesRequestRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;
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
import java.util.ArrayList;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MovesRequestDocs extends BuilderIntegrationTest {

    @Autowired
    public MovesRequestDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            StockRepository stockRepository,
            StockItemRepository stockItemRepository,
            StockMovesRepository stockMovesRepository,
            TechnicianRepository technicianRepository,
            MovesRequestRepository movesRequestRepository,
            BranchOfficeRepository branchOfficeRepository) {

        this.branchOfficeRepository = branchOfficeRepository;

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        this.technicianRepository = technicianRepository;
        this.stockRepository = stockRepository;
        this.stockItemRepository = stockItemRepository;
        this.stockMovesRepository = stockMovesRepository;
        this.movesRequestRepository = movesRequestRepository;

        createCurrentUser();

    }

    @RoleTestRoot
    @Transactional
    public void findAll() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock shed = office.shed().orElseThrow();
        Stock technician = createAndSaveTechnicianStock(office);

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            StockMove move = createAndSaveDetachedStockMove(shed, technician, product);
            createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(shed, product),
                    createAndSaveStockItem(technician, product),
                    move);
        }

        this.mockMvc.perform(get("/moves/requests")
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
                                getPageRequestedMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    @Deprecated(forRemoval = true)
    public void findAllPendingByCityStockFrom() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock();

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            StockMove move = createAndSaveDetachedStockMove(shed, technician, product);
            createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(shed, product),
                    createAndSaveStockItem(technician, product),
                    move);
        }

        this.mockMvc.perform(get("/moves/requests/from/city/{city}/pending", CityOptions.IPATINGA_HORTO)
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("city").description("Cidade dos estoques de origem a serem filtrados")),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageRequestedMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    public void findAllPendingByStockTypeFrom() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock shed = office.shed().orElseThrow();
        Stock technician = createAndSaveTechnicianStock(office);

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            StockMove move = createAndSaveDetachedStockMove(shed, technician, product);
            createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(shed, product),
                    createAndSaveStockItem(technician, product),
                    move);
        }

        this.mockMvc.perform(get("/moves/requests/from/pending")
                .param("page", "0")
                .param("pageSize", "5")
                .param("types", StockType.SHED.name(), StockType.MAINTENANCE.name())
                .header("Office-Id", shed.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("types").description("Lista com os tipos de estoques desejados"),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageRequestedMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    @Deprecated(forRemoval = true)
    public void findAllPendingByCityStockTo() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock();

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            StockMove move = createAndSaveDetachedStockMove(shed, technician, product);
            createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(technician, product),
                    createAndSaveStockItem(shed, product),
                    move);
        }

        this.mockMvc.perform(get("/moves/requests/to/city/{city}/pending", CityOptions.IPATINGA_HORTO)
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("city").description("Cidade dos estoques de destino a serem filtrados")),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageRequestedMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    public void findAllPendingByStockTypeTo() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock shed = office.shed().orElseThrow();
        Stock technician = createAndSaveTechnicianStock(office);

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            StockMove move = createAndSaveDetachedStockMove(shed, technician, product);
            createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(technician, product),
                    createAndSaveStockItem(shed, product),
                    move);
        }

        this.mockMvc.perform(get("/moves/requests/to/pending")
                .param("page", "0")
                .param("pageSize", "5")
                .param("types", StockType.SHED.name(), StockType.MAINTENANCE.name(), StockType.TECHNICIAN.name())
                .header("Office-Id", shed.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("types").description("Lista com os tipos de estoques desejados"),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageRequestedMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    public void findById() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        Product product = createAndSaveProduct();

        StockMove move = createAndSaveDetachedStockMove(shed, technician, product);
        RequestedMove requestedMove = createAndSaveRequestedMove(
                product,
                createAndSaveStockItem(shed, product),
                createAndSaveStockItem(technician, product),
                move);
        requestedMove.setStatus(MoveStatus.REALIZED);
        movesRequestRepository.save(requestedMove);

        this.mockMvc.perform(get("/moves/requests/{id}", requestedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da solicitação procurada")
                        ),
                        responseFields(getRequestedMoveResponse())
                                .andWithPrefix("product.", getProductProjectionWithoutUnit())
                                .andWithPrefix("from.", getStockProjection())
                                .andWithPrefix("to.", getStockProjection())
                                .andWithPrefix("requester.", getEmployeeProjection())
                                .andWithPrefix("move.", getStockMoveProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(createInsertRequestedMoveRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("from").optional().type(JsonFieldType.NUMBER).description("Código do estoque de origem"),
                                fieldWithPath("to").optional().type(JsonFieldType.NUMBER).description("Código do estoque de destino"),
                                fieldWithPath("items").description(createDescriptionWithNotEmpty("Lista com os itens a serem movimentados")),
                                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre a movimentação"))
                                .andWithPrefix("items[].",
                                        fieldWithPath("product").description("Código do produto a ser movimentado (ID)"),
                                        fieldWithPath("quantity").description("Quantidade do item a ser movimentada")),
                        responseFields(fieldWithPath("[]").description("Lista com todas as solicitações resultantes separadas por item"))
                                .andWithPrefix("[].", getRequestedMoveProjection())
                                .andWithPrefix("[].product.", getProductProjectionWithoutUnit())
                                .andWithPrefix("[].from.", getStockProjection())
                                .andWithPrefix("[].to.", getStockProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void approveRequest() throws Exception {
        Product product = createAndSaveProduct();
        BranchOffice office = createAndSaveBranchOffice();
        RequestedMove requestedMove = createAndSaveRequestedMove(
                product,
                createAndSaveStockItem(office.shed().orElseThrow(), product),
                createAndSaveStockItem(createAndSaveTechnicianStock(office), product),
                null);

        this.mockMvc.perform(post("/moves/requests/{id}/approve", requestedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da solicitação a ser aprovada")
                        ),
                        responseFields(getRequestedMoveProjection())
                                .andWithPrefix("product.", getProductProjectionWithoutUnit())
                                .andWithPrefix("from.", getStockProjection())
                                .andWithPrefix("to.", getStockProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void rejectRequest() throws Exception {
        Product product = createAndSaveProduct();
        BranchOffice office = createAndSaveBranchOffice();
        RequestedMove requestedMove = createAndSaveRequestedMove(
                product,
                createAndSaveStockItem(office.shed().orElseThrow(), product),
                createAndSaveStockItem(createAndSaveTechnicianStock(office), product),
                null);

        this.mockMvc.perform(post("/moves/requests/{id}/reject", requestedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da solicitação a ser rejeitada")
                        ),
                        responseFields(getRequestedMoveProjection())
                                .andWithPrefix("product.", getProductProjectionWithoutUnit())
                                .andWithPrefix("from.", getStockProjection())
                                .andWithPrefix("to.", getStockProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void cancelRequest() throws Exception {
        Product product = createAndSaveProduct();
        BranchOffice office = createAndSaveBranchOffice();
        RequestedMove requestedMove = createAndSaveRequestedMove(
                product,
                createAndSaveStockItem(office.shed().orElseThrow(), product),
                createAndSaveStockItem(createAndSaveTechnicianStock(office), product),
                null);

        this.mockMvc.perform(delete("/moves/requests/{id}", requestedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da solicitação a ser rejeitada")
                        ),
                        responseFields(getRequestedMoveProjection())
                                .andWithPrefix("product.", getProductProjectionWithoutUnit())
                                .andWithPrefix("from.", getStockProjection())
                                .andWithPrefix("to.", getStockProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void basicFindAllByTechnicianTo() throws Exception {
        Stock shed = createAndSaveShedStock();
        TechnicianStock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(shed, product),
                    createAndSaveStockItem(technician, product),
                    null);
        }

        this.mockMvc.perform(get("/basic/moves/requests/to/technicians/{userId}", technician.getTechnician().getUserId())
                .param("status", "REQUESTED")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("userId").description("UserId do técnico buscado")),
                                requestParameters(
                                        parameterWithName("status").optional().description(createDescription(
                                                "Status a ser utilizado como filtro",
                                                "Este parâmetro é opcional. Caso não seja relevante, basta não informá-lo na consulta"
                                        )),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageRequestedMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    public void basicFindAllByTechnicianFrom() throws Exception {
        Stock shed = createAndSaveShedStock();
        TechnicianStock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(technician, product);
            createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(technician, product),
                    createAndSaveStockItem(shed, product),
                    null);
        }

        this.mockMvc.perform(get("/basic/moves/requests/from/technicians/{userId}", technician.getTechnician().getUserId())
                .param("status", "REQUESTED")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("userId").description("UserId do técnico buscado")),
                                requestParameters(
                                        parameterWithName("status").optional().description(createDescription(
                                                "Status a ser utilizado como filtro",
                                                "Este parâmetro é opcional. Caso não seja relevante, basta não informá-lo na consulta"
                                        )),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageRequestedMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    public void basicInsert() throws Exception {
        TechnicianStock technicianStock = createAndSaveTechnicianStock();

        this.mockMvc.perform(post("/basic/moves/requests")
                .content(objectMapper.writeValueAsString(createBasicInsertRequestedMoveRequest(technicianStock)))
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("from").optional().type(JsonFieldType.NUMBER).description("Código do estoque de origem"),
                                fieldWithPath("to").optional().type(JsonFieldType.NUMBER).description("Código do estoque de destino"),
                                fieldWithPath("items").description(createDescriptionWithNotEmpty("Lista com os itens a serem movimentados")),
                                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre a movimentação"))
                                .andWithPrefix("items[].",
                                        fieldWithPath("product").description("Código do produto a ser movimentado (ID)"),
                                        fieldWithPath("quantity").description("Quantidade do item a ser movimentada")),
                        responseFields(fieldWithPath("[]").description("Lista com todas as solicitações resultantes separadas por item"))
                                .andWithPrefix("[].", getRequestedMoveProjection())
                                .andWithPrefix("[].product.", getProductProjectionWithoutUnit())
                                .andWithPrefix("[].from.", getStockProjection())
                                .andWithPrefix("[].to.", getStockProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void basicApproveRequest() throws Exception {
        Product product = createAndSaveProduct();
        BranchOffice office = createAndSaveBranchOffice();
        TechnicianStock technicianStock = createAndSaveTechnicianStock(office);
        RequestedMove requestedMove = createAndSaveRequestedMove(
                product,
                createAndSaveStockItem(office.shed().orElseThrow(), product),
                createAndSaveStockItem(technicianStock, product),
                null);

        this.mockMvc.perform(post("/basic/moves/requests/{id}/approve", requestedMove.getId())
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da solicitação a ser aprovada")
                        ),
                        responseFields(getRequestedMoveProjection())
                                .andWithPrefix("product.", getProductProjectionWithoutUnit())
                                .andWithPrefix("from.", getStockProjection())
                                .andWithPrefix("to.", getStockProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void basicRejectRequest() throws Exception {
        Product product = createAndSaveProduct();
        BranchOffice office = createAndSaveBranchOffice();
        TechnicianStock technicianStock = createAndSaveTechnicianStock(office);
        RequestedMove requestedMove = createAndSaveRequestedMove(
                product,
                createAndSaveStockItem(office.shed().orElseThrow(), product),
                createAndSaveStockItem(technicianStock, product),
                null);

        this.mockMvc.perform(post("/basic/moves/requests/{id}/reject", requestedMove.getId())
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da solicitação a ser rejeitada")
                        ),
                        responseFields(getRequestedMoveProjection())
                                .andWithPrefix("product.", getProductProjectionWithoutUnit())
                                .andWithPrefix("from.", getStockProjection())
                                .andWithPrefix("to.", getStockProjection())));
    }

    private InsertRequestedMoveRequest createInsertRequestedMoveRequest() {
        return createInsertRequestedMoveRequest(createAndSaveTechnicianStock());
    }

    private InsertRequestedMoveRequest createInsertRequestedMoveRequest(Stock technician) {
        Stock shed = technician.getBranchOffice().shed().orElseThrow();

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        InsertRequestedMoveRequest request = new InsertRequestedMoveRequest();
        request.setFrom(shed.getId());
        request.setTo(technician.getId());
        request.setItems(new ArrayList<>());

        InsertRequestedMoveItemRequest item1Request = new InsertRequestedMoveItemRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        request.getItems().add(item1Request);

        InsertRequestedMoveItemRequest item2Request = new InsertRequestedMoveItemRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        request.getItems().add(item2Request);

        request.setNote("Observação da movimentação");

        return request;
    }

    private BasicInsertRequestedMoveRequest createBasicInsertRequestedMoveRequest(Stock technician) {
        Stock shed = technician.getBranchOffice().shed().orElseThrow();

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        BasicInsertRequestedMoveRequest request = new BasicInsertRequestedMoveRequest();
        request.setFrom(shed.getId());
        request.setItems(new ArrayList<>());

        InsertRequestedMoveItemRequest item1Request = new InsertRequestedMoveItemRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        request.getItems().add(item1Request);

        InsertRequestedMoveItemRequest item2Request = new InsertRequestedMoveItemRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        request.getItems().add(item2Request);

        request.setNote("Observação da movimentação");

        return request;
    }

    private ResponseFieldsSnippet getPageRequestedMoveProjection() {
        return getPageContent("Lista com as solicitações encontradas")
                .andWithPrefix("content[].", getRequestedMoveProjection())
                .andWithPrefix("content[].product.", getProductProjectionWithoutUnit())
                .andWithPrefix("content[].from.", getStockProjection())
                .andWithPrefix("content[].to.", getStockProjection());
    }

    private FieldDescriptor[] getRequestedMoveProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código da solicitação"),
                fieldWithPath("date").description("Data da solicitação"),
                fieldWithPath("status").description("Status atual da solicitação"),
                fieldWithPath("quantity").description("Quantidade solicitada"),
                fieldWithPath("product").description("Produto solicitado"),
                fieldWithPath("from").description("Estoque de origem"),
                fieldWithPath("to").description("Estoque de destino"),
                fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da solicitação"),
        };
    }

    private FieldDescriptor[] getRequestedMoveResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código da solicitação"),
                fieldWithPath("date").description("Data da solicitação"),
                fieldWithPath("status").description("Status atual da solicitação"),
                fieldWithPath("quantity").description("Quantidade solicitada"),
                fieldWithPath("product").description("Produto solicitado"),
                fieldWithPath("from").description("Estoque de origem"),
                fieldWithPath("to").description("Estoque de destino"),
                fieldWithPath("requester").description("Solicitante"),
                fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da solicitação"),
                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observação da solicitação"),
                fieldWithPath("move").optional().type(JsonFieldType.OBJECT).description("Movimentação gerada a partir da presente solicitação"),
        };
    }

    private FieldDescriptor[] getEmployeeProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código do funcionário"),
                fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome do funcionário"),
        };
    }

    private FieldDescriptor[] getStockMoveProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                fieldWithPath("origin").optional().type(JsonFieldType.STRING).description("Origem da movimentação"),
                fieldWithPath("status").optional().type(JsonFieldType.STRING).description("Status atual da movimentação"),
                fieldWithPath("type").optional().type(JsonFieldType.STRING).description("Tipo de movimentação"),
                fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description("Quantidade movimentada"),
                fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação")};
    }

    private FieldDescriptor[] getStockProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código do estoque"),
                fieldWithPath("type").optional().type(JsonFieldType.STRING).description("Tipo do estoque"),
                fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome do estoque"),
        };
    }

    private FieldDescriptor[] getProductProjectionWithoutUnit() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código ID do produto"),
                fieldWithPath("code").description("Código do produto"),
                fieldWithPath("name").description("Nome do produto"),
        };
    }

}
