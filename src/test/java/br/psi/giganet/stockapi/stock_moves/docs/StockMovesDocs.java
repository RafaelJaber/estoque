package br.psi.giganet.stockapi.stock_moves.docs;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.valid_mac_addresses.repository.ValidMacAddressesRepository;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.patrimonies.repository.PatrimonyRepository;
import br.psi.giganet.stockapi.patrimonies_locations.repository.PatrimonyLocationRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.stock_moves.controller.request.*;
import br.psi.giganet.stockapi.stock_moves.model.*;
import br.psi.giganet.stockapi.stock_moves.repository.StockMovesRepository;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StockMovesDocs extends BuilderIntegrationTest {

    @Autowired
    public StockMovesDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            StockRepository stockRepository,
            StockItemRepository stockItemRepository,
            StockMovesRepository stockMovesRepository,
            TechnicianRepository technicianRepository,
            PatrimonyRepository patrimonyRepository,
            PatrimonyLocationRepository patrimonyLocationRepository,
            ValidMacAddressesRepository validMacAddressesRepository,
            BranchOfficeRepository branchOfficeRepository) {

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        this.stockRepository = stockRepository;
        this.stockItemRepository = stockItemRepository;
        this.stockMovesRepository = stockMovesRepository;
        this.technicianRepository = technicianRepository;
        this.patrimonyRepository = patrimonyRepository;
        this.patrimonyLocationRepository = patrimonyLocationRepository;
        this.validMacAddressesRepository = validMacAddressesRepository;
        this.branchOfficeRepository = branchOfficeRepository;
        createCurrentUser();

    }

    @RoleTestRoot
    @Transactional
    public void findAllByDescription() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            createAndSaveDetachedStockMove(shed, technician, product);
        }

        this.mockMvc.perform(get("/moves")
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", shed.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("description").description("Descrição da movimentação"),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    public void findAllAdvanced() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock shed = office.shed().orElseThrow();
        Stock technician = createAndSaveTechnicianStock(office);
        Stock customer = office.customer().orElseThrow();

        for (int i = 0; i < 2; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            createAndSaveDetachedStockMove(shed, technician, product);
        }

        for (int i = 0; i < 2; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(technician, product);
            TechnicianStockMove move = createAndSaveTechnicianStockMove(technician, customer, product);
            move.setCustomerName("Lucas " + i);
            stockMovesRepository.saveAndFlush(move);
        }

        this.mockMvc.perform(get("/moves")
                .param("advanced", "")
                .param("page", "0")
                .param("pageSize", "5")
                .param("search",
                        "type:BETWEEN_STOCKS",
                        ("createdDate>" + LocalDate.now().toString()),
                        ("lastModifiedDate>" + LocalDate.now().toString()))
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("advanced").description("Flag que representa que este método será executado"),
                                        parameterWithName("search").description("Lista com as pesquisas solicitadas, de acordo com a convenção"),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageAdvancedStockMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    public void findAllRealizedByDescription() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            StockMove move = createAndSaveDetachedStockMove(shed, technician, product);
            move.setStatus(MoveStatus.REALIZED);
            stockMovesRepository.saveAndFlush(move);
        }

        this.mockMvc.perform(get("/moves/realized")
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", shed.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("description").description("Descrição da movimentação"),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    public void findAllPendingByDescription() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            StockMove move = createAndSaveDetachedStockMove(shed, technician, product);
        }

        this.mockMvc.perform(get("/moves/pending")
                .param("type", MoveType.BETWEEN_STOCKS.name())
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", shed.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("description").description("Descrição da movimentação"),
                                        parameterWithName("type")
                                                .optional()
                                                .description(createDescription(
                                                        "Tipo da movimentação",
                                                        "Caso não seja informado, será retornado movimentação de todos os tipos")),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    public void findAllPendingFromServiceOrdersByDescription() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock technician = createAndSaveTechnicianStock(office);
        Stock customer = office.customer().orElseThrow();

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(technician, product);
            TechnicianStockMove move = createAndSaveTechnicianStockMove(technician, customer, product);
            move.setOrderType(ExternalOrderType.INSTALLATION);
            move.setOrderId(UUID.randomUUID().toString());
            move.setReason(MoveReason.SERVICE_ORDER);
            stockMovesRepository.saveAndFlush(move);
        }

        this.mockMvc.perform(get("/moves/pending/service-orders")
                .param("description", "")
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
                                        parameterWithName("description").description("Descrição da movimentação"),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageContent("Lista com as movimentações encontradas")
                                        .andWithPrefix("content[].", getServiceOrderMoveResponse())
                                        .andWithPrefix("content[].product.", getProductProjectionWithoutUnit())));
    }

    @RoleTestRoot
    @Transactional
    public void findAllPendingByStockFrom() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        Product product = createAndSaveProduct();

        createAndSaveDetachedStockMove(null, shed, product);
        createAndSaveDetachedStockMove(shed, technician, product);
        createAndSaveDetachedStockMove(technician, null, product);

        this.mockMvc.perform(get("/moves/from/{stock}/pending", shed.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", shed.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("stock").description("Código do estoque de origem das movimentações")
                                ),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    @Deprecated(forRemoval = true)
    public void findAllPendingByCityStockFrom() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock();
        Product product = createAndSaveProduct();

        createAndSaveDetachedStockMove(null, shed, product);
        createAndSaveDetachedStockMove(shed, technician, product);
        createAndSaveDetachedStockMove(technician, null, product);

        this.mockMvc.perform(get("/moves/from/city/{city}/pending", CityOptions.IPATINGA_HORTO.name())
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
                                        parameterWithName("city").description("Cidade desejada")
                                ),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageAdvancedStockMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    public void findAllPendingByStockTypeFrom() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        Product product = createAndSaveProduct();

        createAndSaveDetachedStockMove(null, shed, product);
        createAndSaveDetachedStockMove(shed, technician, product);
        createAndSaveDetachedStockMove(technician, null, product);

        this.mockMvc.perform(get("/moves/from/pending")
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
                                getPageAdvancedStockMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    @Deprecated(forRemoval = true)
    public void findAllPendingByCityStockTo() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        Product product = createAndSaveProduct();

        createAndSaveDetachedStockMove(null, shed, product);
        createAndSaveDetachedStockMove(shed, technician, product);
        createAndSaveDetachedStockMove(technician, null, product);

        Product product2 = createAndSaveProduct();
        createAndSaveStockItem(technician, product2);
        TechnicianStockMove retreatMove = createAndSaveTechnicianStockMove(technician, shed, product2);
        retreatMove.setCustomerName("Lucas Henrique");
        stockMovesRepository.saveAndFlush(retreatMove);

        this.mockMvc.perform(get("/moves/to/city/{city}/pending", CityOptions.IPATINGA_HORTO.name())
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
                                        parameterWithName("city").description(createDescription(
                                                "Cidade desejada",
                                                "Esta pesquisa tem como resultado estoques do tipo galpão, manutenção, obsoleto e defeituoso"))),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageAdvancedStockMoveProjectionWithCustomerName()));
    }

    @RoleTestRoot
    @Transactional
    public void findAllPendingByStockTypeTo() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        Product product = createAndSaveProduct();

        createAndSaveDetachedStockMove(null, shed, product);
        createAndSaveDetachedStockMove(shed, technician, product);
        createAndSaveDetachedStockMove(technician, null, product);

        Product product2 = createAndSaveProduct();
        createAndSaveStockItem(technician, product2);
        TechnicianStockMove retreatMove = createAndSaveTechnicianStockMove(technician, shed, product2);
        retreatMove.setCustomerName("Lucas Henrique");
        stockMovesRepository.saveAndFlush(retreatMove);

        this.mockMvc.perform(get("/moves/to/pending")
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
                                getPageAdvancedStockMoveProjectionWithCustomerName()));
    }

    @RoleTestRoot
    @Transactional
    public void findAllPendingByStockTo() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        Product product = createAndSaveProduct();

        createAndSaveDetachedStockMove(null, shed, product);
        createAndSaveDetachedStockMove(shed, technician, product);
        createAndSaveDetachedStockMove(technician, null, product);

        this.mockMvc.perform(get("/moves/to/{stock}/pending", shed.getId())
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
                                        parameterWithName("stock").description("Código do estoque de destino das movimentações")
                                ),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageMoveProjection()));
    }

    @RoleTestRoot
    @Transactional
    public void findById() throws Exception {
        Stock shed = createAndSaveShedStock();
        Product product = createAndSaveProduct();

        StockMove move = createAndSaveDetachedStockMove(null, shed, product);

        this.mockMvc.perform(get("/moves/{id}", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da movimentação procurada")
                        ),
                        getMoveResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInsertMoveRequest(office)))
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("from").optional().type(JsonFieldType.NUMBER).description("Código do estoque de origem"),
                                fieldWithPath("to").optional().type(JsonFieldType.NUMBER).description("Código do estoque de destino"),
                                fieldWithPath("type").description(createDescriptionWithNotNull("Tipo da movimentação")),
                                fieldWithPath("items").description(createDescriptionWithNotEmpty("Lista com os itens a serem movimentados")),
                                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre a movimentação"))
                                .andWithPrefix("items[].",
                                        fieldWithPath("product").description("Código do produto a ser movimentado (ID)"),
                                        fieldWithPath("quantity").description("Quantidade do item a ser movimentada")),
                        responseFields(fieldWithPath("[]").description("Lista com todas as movimentações resultantes separadas por item"))
                                .andWithPrefix("[].", getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void approveMove() throws Exception {
        Stock shed = createAndSaveShedStock();
        Product product = createAndSaveProduct();

        StockMove move = createAndSaveDetachedStockMove(null, shed, product);

        this.mockMvc.perform(post("/moves/{id}/approve", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da movimentação a ser aprovada")
                        ),
                        responseFields(getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void approveMovesInBatch() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        Product product = createAndSaveProduct();

        StockMove entryMove = createAndSaveDetachedStockMove(null, shed, product);

        StockMove betweenMove = createAndSaveDetachedStockMove(shed, technician, product);
        betweenMove.getFrom().setBlockedQuantity(betweenMove.getQuantity());
        stockItemRepository.saveAndFlush(betweenMove.getFrom());

        StockMove outgoingMove = createAndSaveDetachedStockMove(technician, null, product);
        outgoingMove.getFrom().setBlockedQuantity(outgoingMove.getQuantity());
        stockItemRepository.saveAndFlush(outgoingMove.getFrom());

        List<StockMove> moves = Arrays.asList(entryMove, betweenMove, outgoingMove);

        this.mockMvc.perform(post("/moves/batch/approve")
                .content(objectMapper.writeValueAsString(createEvaluateMovesInBatchRequest(moves)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("moves").description("Lista com os IDs das movimentações a serem aprovadas")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("Lista com as movimentações aprovadas"))
                                .andWithPrefix("[].", getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void rejectMovesInBatch() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        Product product = createAndSaveProduct();

        StockMove entryMove = createAndSaveDetachedStockMove(null, shed, product);

        StockMove betweenMove = createAndSaveDetachedStockMove(shed, technician, product);
        betweenMove.getFrom().setBlockedQuantity(betweenMove.getQuantity());
        stockItemRepository.saveAndFlush(betweenMove.getFrom());

        StockMove outgoingMove = createAndSaveDetachedStockMove(technician, null, product);
        outgoingMove.getFrom().setBlockedQuantity(outgoingMove.getQuantity());
        stockItemRepository.saveAndFlush(outgoingMove.getFrom());

        List<StockMove> moves = Arrays.asList(betweenMove, outgoingMove, entryMove);

        this.mockMvc.perform(post("/moves/batch/reject")
                .content(objectMapper.writeValueAsString(createEvaluateMovesInBatchRequest(moves)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("moves").description("Lista com os IDs das movimentações a serem rejeitadas")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("Lista com as movimentações rejeitadas"))
                                .andWithPrefix("[].", getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void rejectMove() throws Exception {
        Stock shed = createAndSaveShedStock();
        Product product = createAndSaveProduct();

        StockMove move = createAndSaveDetachedStockMove(null, shed, product);

        this.mockMvc.perform(post("/moves/{id}/reject", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da movimentação a ser rejeitada")
                        ),
                        responseFields(getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void cancelMove() throws Exception {
        Stock shed = createAndSaveShedStock();
        Product product = createAndSaveProduct();

        StockMove move = createAndSaveDetachedStockMove(null, shed, product);

        this.mockMvc.perform(delete("/moves/{id}", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da movimentação a ser rejeitada")
                        ),
                        responseFields(getProjection())));
    }


    @RoleTestRoot
    @Transactional
    public void basicFindAllByDescriptionAndTechnician() throws Exception {
        Stock shed = createAndSaveShedStock();
        TechnicianStock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            createAndSaveTechnicianStockMove(shed, technician, product);

        }

        this.mockMvc.perform(get("/basic/moves/technicians/{userId}", technician.getTechnician().getUserId())
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", shed.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("userId").description("Código User Id do técnico")),
                                requestParameters(
                                        parameterWithName("description").description("Descrição da movimentação"),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageMoveProjectionWithProduct()));
    }

    @RoleTestRoot
    @Transactional
    public void basicFindAllRealizedByDescriptionAndTechnician() throws Exception {
        Stock shed = createAndSaveShedStock();
        TechnicianStock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            StockMove move = createAndSaveTechnicianStockMove(shed, technician, product);
            move.setStatus(MoveStatus.REALIZED);
            stockMovesRepository.saveAndFlush(move);
        }

        this.mockMvc.perform(get("/basic/moves/technicians/{userId}/realized", technician.getTechnician().getUserId())
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("userId").description("Código User Id do técnico")),
                                requestParameters(
                                        parameterWithName("description").description("Descrição da movimentação"),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageMoveProjectionWithProduct()));
    }

    @RoleTestRoot
    @Transactional
    public void basicFindAllPendingByStockFrom() throws Exception {
        Stock shed = createAndSaveShedStock();
        TechnicianStock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        Product product = createAndSaveProduct();

        createAndSaveTechnicianStockMove(null, shed, product);
        createAndSaveTechnicianStockMove(shed, technician, product);
        createAndSaveTechnicianStockMove(technician, null, product);

        this.mockMvc.perform(get("/basic/moves/from/technicians/{userId}/pending", technician.getTechnician().getUserId())
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
                                        parameterWithName("userId").description("Campo UserId oriundo da API externa")
                                ),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageMoveProjectionWithProduct()));
    }

    @RoleTestRoot
    @Transactional
    public void basicFindAllPendingByStockTo() throws Exception {
        Stock shed = createAndSaveShedStock();
        TechnicianStock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        Product product = createAndSaveProduct();

        createAndSaveTechnicianStockMove(null, shed, product);
        createAndSaveTechnicianStockMove(shed, technician, product);
        createAndSaveTechnicianStockMove(technician, null, product);

        this.mockMvc.perform(get("/basic/moves/to/technicians/{userId}/pending", technician.getTechnician().getUserId())
                .param("onlyDetached", "true")
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
                                        parameterWithName("userId").description("Campo UserId oriundo da API externa")
                                ),
                                requestParameters(
                                        parameterWithName("onlyDetached")
                                                .optional()
                                                .description(createDescription("Filtro indicando se deve ou não ser filtrado movimentações cuja razão não seja 'DETACHED' ou 'REQUEST'",
                                                        "Caso o valor seja true, serão retornados somente as movimentações caso o status esteja entre estes valores",
                                                        "Caso o valor seja falso, todas as movimentações pendentes para o técnico serão retornadas",
                                                        "O valor default é true")),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPageMoveProjectionWithProduct()));
    }

    @RoleTestRoot
    @Transactional
    public void basicFindById() throws Exception {
        Stock technician = createAndSaveTechnicianStock();
        Product product = createAndSaveProduct();

        StockMove move = createAndSaveTechnicianStockMove(null, technician, product);

        this.mockMvc.perform(get("/basic/moves/{id}", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da movimentação procurada")
                        ),
                        getMoveResponse()));
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicInsert() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));

        this.mockMvc.perform(post("/basic/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createBasicInsertMoveRequest(technicianStock)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("to").type(JsonFieldType.NUMBER).optional().description(createDescription(
                                        "Código do estoque de destino",
                                        "Este campo é obrigatório em movimentações do tipo ENTRE ESTOQUES ou SAIDA DE ESTOQUE",
                                        "Caso a movimentação seja de ENTRADA DE ESTOQUE, este pode ser omitido")),
                                fieldWithPath("type").description(createDescriptionWithNotNull("Tipo da movimentação")),
                                fieldWithPath("items").description(createDescriptionWithNotEmpty("Lista com os itens a serem movimentados")),
                                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre a movimentação"))
                                .andWithPrefix("items[].",
                                        fieldWithPath("product").description("Código do produto a ser movimentado (ID)"),
                                        fieldWithPath("quantity").description("Quantidade do item a ser movimentada")),
                        responseFields(fieldWithPath("[]").description("Lista com todas as movimentações resultantes separadas por item"))
                                .andWithPrefix("[].", getProjection())));
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicInsertFromServiceOrder() throws Exception {
        Stock customerStock = createAndSaveCustomerStock();
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(
                employeeRepository.save(e),
                customerStock.getBranchOffice());
        createAndSavePatrimonyLocation(technicianStock.getTechnician().getUserId());

        this.mockMvc.perform(post("/basic/moves/service-orders")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createBasicInsertMoveFromServiceOrderRequest(technicianStock, customerStock)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("orderId").description(createDescriptionWithNotNull("Código ID da ordem de serviço")),
                                fieldWithPath("activationId").type(JsonFieldType.STRING).optional()
                                        .description("Código ID da solicitação de ativação referente a ordem de serviço, caso exista"),
                                fieldWithPath("orderType").description(createDescriptionWithNotNull("Tipo da ordem de serviço")),
                                fieldWithPath("customerName").description(createDescriptionWithNotNull(
                                        "Nome do cliente", "Será utilizado para criar a localização do patrimônio, caso não exista")),
                                fieldWithPath("customerId").description(createDescriptionWithNotNull("Código ID do cliente")),
                                fieldWithPath("entryItems")
                                        .optional()
                                        .type(JsonFieldType.ARRAY)
                                        .description(createDescription(
                                                "Lista com os itens utilizados na ordem de serviço os quais irão para o ESTOQUE DO TÉCNICO")),
                                fieldWithPath("outgoingItems")
                                        .optional()
                                        .type(JsonFieldType.ARRAY)
                                        .description(createDescription(
                                                "Lista com os itens utilizados na ordem de serviço os quais irão para o ESTOQUE DO CLIENTE")))
                                .andWithPrefix("entryItems[].",
                                        fieldWithPath("product").description("Código do produto a ser movimentado (ID)"),
                                        fieldWithPath("quantity").description("Quantidade do item a ser movimentada"),
                                        fieldWithPath("codeType")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription(
                                                        "Qual é o tipo do código do patrimônio informado",
                                                        "Caso não haja patrimônio associado, este campo não é obrigatório")),
                                        fieldWithPath("patrimonies")
                                                .optional()
                                                .type(JsonFieldType.ARRAY)
                                                .description(createDescription(
                                                        "Lista com os códigos dos respectivos patrimônios utilizados",
                                                        "Caso nenhum patrimônio seja utilizado, este campo pode ser omitido",
                                                        "NOTE QUE: refere-se ao campo \"code\" e não ao ID"
                                                )))
                                .andWithPrefix("outgoingItems[].",
                                        fieldWithPath("product").description("Código do produto a ser movimentado (ID)"),
                                        fieldWithPath("quantity").description("Quantidade do item a ser movimentada"),
                                        fieldWithPath("codeType")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription(
                                                        "Qual é o tipo do código do patrimônio informado",
                                                        "Caso não haja patrimônio associado, este campo não é obrigatório")),
                                        fieldWithPath("patrimonies")
                                                .optional()
                                                .type(JsonFieldType.ARRAY)
                                                .description(createDescription(
                                                        "Lista com os códigos dos respectivos patrimônios utilizados",
                                                        "Caso nenhum patrimônio seja utilizado, este campo pode ser omitido",
                                                        "NOTE QUE: refere-se ao campo \"code\" e não ao ID"
                                                ))),
                        responseFields(
                                fieldWithPath("moves").description("Lista com todas as movimentações de estoque resultantes separadas por item"),
                                fieldWithPath("patrimonies")
                                        .optional()
                                        .type(JsonFieldType.ARRAY)
                                        .description("Lista com todas as movimentações de patrimonios resultantes separados por patrimonio"))
                                .andWithPrefix("moves[].",
                                        getProjection())
                                .andWithPrefix("patrimonies[].",
                                        fieldWithPath("id").description("Código do patrimônio, oriundo do banco de dados"),
                                        fieldWithPath("code").description("Código único de identificação do patrimônio"),
                                        fieldWithPath("codeType").description("Tipo do código do patrimônio"),
                                        fieldWithPath("product").description("Produto"),
                                        fieldWithPath("currentLocation").description("Local atual onde o patrimônio se encontra"))
                                .andWithPrefix("patrimonies[].currentLocation.",
                                        fieldWithPath("id").description("Código ID do local"),
                                        fieldWithPath("code").description(createDescription(
                                                "Código de referencia",
                                                "Caso seja um Técnico ou Cliente, este campo corresponderá ao \"userId\"",
                                                "Caso seja Manutenção, Galpão, Obsoleto ou Defeituoso, este campo corresponderá ao id do estoque correspondente"
                                        )),
                                        fieldWithPath("name").description("Nome do local"))
                                .andWithPrefix("patrimonies[].product.",
                                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                                        fieldWithPath("name").description("Nome do produto"),
                                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                                .andWithPrefix("patrimonies[].product.unit.",
                                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                                        fieldWithPath("name").description("Nome da unidade padrão do produto"))));
    }

    @RoleTestRoot
    @Transactional
    public void basicApproveMove() throws Exception {
        Stock shed = createAndSaveShedStock();
        Product product = createAndSaveProduct();
        TechnicianStock technicianStock = createAndSaveTechnicianStock(shed.getBranchOffice());

        StockMove move = createAndSaveTechnicianStockMove(technicianStock, shed, product);

        this.mockMvc.perform(post("/basic/moves/{id}/approve", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da movimentação a ser aprovada")
                        ),
                        responseFields(getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void basicFindAllPendingByServiceOrderId() throws Exception {
        Stock customerStock = createAndSaveCustomerStock();
        Product product = createAndSaveProduct();
        TechnicianStock technicianStock = createAndSaveTechnicianStock(customerStock.getBranchOffice());

        String orderId = UUID.randomUUID().toString().substring(0, 6);
        String customerName = "Lucas Henrique";

        TechnicianStockMove move1 = createAndSaveTechnicianStockMove(technicianStock, customerStock, product);
        move1.setOrderId(orderId);
        move1.setOrderType(ExternalOrderType.INSTALLATION);
        move1.setCustomerName(customerName);
        move1.setReason(MoveReason.SERVICE_ORDER);
        stockMovesRepository.saveAndFlush(move1);

        TechnicianStockMove move2 = createAndSaveTechnicianStockMove(customerStock, technicianStock, product);
        move2.setOrderId(orderId);
        move2.setOrderType(ExternalOrderType.INSTALLATION);
        move2.setCustomerName(customerName);
        move2.setReason(MoveReason.SERVICE_ORDER);
        stockMovesRepository.saveAndFlush(move2);

        this.mockMvc.perform(get("/basic/moves/service-orders/{orderId}/pending", orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("orderId").description("Código da ordem de serviço na API externa (Smartnet)")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("Lista com todas as movimentações pendentes dado o ID da ordem de serviço"))
                                .andWithPrefix("[].", getServiceOrderMoveResponse())
                                .andWithPrefix("[].product.", getProductProjectionWithoutUnit())));
    }

    @RoleTestRoot
    @Transactional
    public void basicFindAllByServiceOrderId() throws Exception {
        Stock customerStock = createAndSaveCustomerStock();
        Product product = createAndSaveProduct();
        TechnicianStock technicianStock = createAndSaveTechnicianStock(customerStock.getBranchOffice());

        String orderId = UUID.randomUUID().toString().substring(0, 6);
        String activationId = UUID.randomUUID().toString().substring(0, 6);
        String customerName = "Lucas Henrique";

        TechnicianStockMove move1 = createAndSaveTechnicianStockMove(technicianStock, customerStock, product);
        move1.setOrderId(orderId);
        move1.setActivationId(activationId);
        move1.setOrderType(ExternalOrderType.INSTALLATION);
        move1.setCustomerName(customerName);
        move1.setReason(MoveReason.SERVICE_ORDER);
        stockMovesRepository.saveAndFlush(move1);

        TechnicianStockMove move2 = createAndSaveTechnicianStockMove(customerStock, technicianStock, product);
        move2.setOrderId(orderId);
        move2.setOrderId(activationId);
        move2.setOrderType(ExternalOrderType.INSTALLATION);
        move2.setCustomerName(customerName);
        move2.setReason(MoveReason.SERVICE_ORDER);
        stockMovesRepository.saveAndFlush(move2);

        this.mockMvc.perform(get("/basic/moves/service-orders/{orderId}", orderId)
                .param("activation", activationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("activation").optional().description("Código ID da ativação, caso exista")
                        ),
                        pathParameters(
                                parameterWithName("orderId").description("Código da ordem de serviço na API externa (Smartnet)")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("Lista com todas as movimentações pendentes dado o ID da ordem de serviço"))
                                .andWithPrefix("[].", getServiceOrderMoveResponse())
                                .andWithPrefix("[].product.", getProductProjectionWithoutUnit())));
    }

    @RoleTestRoot
    @Transactional
    public void basicApproveAllByExternalOrderId() throws Exception {
        Stock customerStock = createAndSaveCustomerStock();
        Product product = createAndSaveProduct();
        TechnicianStock technicianStock = createAndSaveTechnicianStock(customerStock.getBranchOffice());

        TechnicianStockMove move = createAndSaveTechnicianStockMove(technicianStock, customerStock, product);
        move.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        move.setOrderType(ExternalOrderType.INSTALLATION);
        move.setCustomerName("Lucas Henrique");
        move.setReason(MoveReason.SERVICE_ORDER);
        stockMovesRepository.saveAndFlush(move);

        this.mockMvc.perform(post("/basic/moves/service-orders/{orderId}/approve", move.getOrderId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("orderId").description("Código da ordem de serviço na API externa (Smartnet)")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("Lista com as movimentações aprovadas"))
                                .andWithPrefix("[].", getServiceOrderMoveResponse())
                                .andWithPrefix("[].product.", getProductProjectionWithoutUnit())));
    }

    @RoleTestRoot
    @Transactional
    public void basicRejectMove() throws Exception {
        Stock shed = createAndSaveShedStock();
        Product product = createAndSaveProduct();
        TechnicianStock technicianStock = createAndSaveTechnicianStock(shed.getBranchOffice());

        StockMove move = createAndSaveTechnicianStockMove(technicianStock, shed, product);

        this.mockMvc.perform(post("/basic/moves/{id}/reject", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da movimentação a ser rejeitada")
                        ),
                        responseFields(getProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void basicRejectAllByExternalOrderId() throws Exception {
        Stock customerStock = createAndSaveCustomerStock();
        Product product = createAndSaveProduct();
        TechnicianStock technicianStock = createAndSaveTechnicianStock(customerStock.getBranchOffice());

        TechnicianStockMove move = createAndSaveTechnicianStockMove(technicianStock, customerStock, product);
        move.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        move.setOrderType(ExternalOrderType.INSTALLATION);
        move.setCustomerName("Lucas Henrique");
        move.setReason(MoveReason.SERVICE_ORDER);
        stockMovesRepository.saveAndFlush(move);

        this.mockMvc.perform(post("/basic/moves/service-orders/{orderId}/reject", move.getOrderId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("orderId").description("Código da ordem de serviço na API externa (Smartnet)")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("Lista com as movimentações rejeitadas"))
                                .andWithPrefix("[].", getServiceOrderMoveResponse())
                                .andWithPrefix("[].product.", getProductProjectionWithoutUnit())));
    }

    private BasicInsertMoveRequest createBasicInsertMoveRequest(TechnicianStock technician) {
        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(technician, p1);
        createAndSaveStockItem(technician, p2);

        BasicInsertMoveRequest request = new BasicInsertMoveRequest();
        request.setTo(technician.getBranchOffice().shed().orElseThrow().getId());
        request.setType(MoveType.BETWEEN_STOCKS);
        request.setItems(new ArrayList<>());

        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        request.getItems().add(item1Request);

        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        request.getItems().add(item2Request);

        request.setNote("Observação da movimentação");

        return request;
    }

    private BasicInsertMoveFromServiceOrderRequest createBasicInsertMoveFromServiceOrderRequest(TechnicianStock technician, Stock customerStock) {
        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();
        Product p3 = createAndSaveProduct();

        createAndSaveStockItem(technician, p1);
        createAndSaveStockItem(technician, p2);
        createAndSaveStockItem(customerStock, p3);

        BasicInsertMoveFromServiceOrderRequest request = new BasicInsertMoveFromServiceOrderRequest();
        request.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        request.setActivationId(UUID.randomUUID().toString().substring(0, 6));
        request.setOrderType("I");
        request.setCustomerName("Lucas Henrique");
        request.setCustomerId(UUID.randomUUID().toString().substring(0, 6));
        request.setEntryItems(new ArrayList<>());
        request.setOutgoingItems(new ArrayList<>());

        BasicInsertItemMoveFromServiceOrderRequest item1Request = new BasicInsertItemMoveFromServiceOrderRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        request.getOutgoingItems().add(item1Request);

        BasicInsertItemMoveFromServiceOrderRequest item2Request = new BasicInsertItemMoveFromServiceOrderRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        String item2PatrimonyCode = createAndSavePatrimony().getCode();
        item2Request.setPatrimonies(Collections.singleton(item2PatrimonyCode));
        createAndSaveValidMacAddress(item2PatrimonyCode);
        request.getOutgoingItems().add(item2Request);

        BasicInsertItemMoveFromServiceOrderRequest item3Request = new BasicInsertItemMoveFromServiceOrderRequest();
        item3Request.setProduct(p3.getId());
        item3Request.setQuantity(1d);
        String item3PatrimonyCode = UUID.randomUUID().toString().substring(0, 6);
        item3Request.setPatrimonies(Collections.singleton(item3PatrimonyCode));
        createAndSaveValidMacAddress(item3PatrimonyCode);
        request.getEntryItems().add(item3Request);

        return request;
    }

    private EvaluateMovesInBatchRequest createEvaluateMovesInBatchRequest(List<StockMove> moves) {
        EvaluateMovesInBatchRequest request = new EvaluateMovesInBatchRequest();
        request.setMoves(moves.stream()
                .map(StockMove::getId)
                .collect(Collectors.toList()));
        return request;
    }

    private InsertMoveRequest createInsertMoveRequest(BranchOffice office) {
        Stock shed = office.shed().orElseThrow();
        Stock technician = createAndSaveTechnicianStock(office);

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        InsertMoveRequest request = new InsertMoveRequest();
        request.setFrom(shed.getId());
        request.setTo(technician.getId());
        request.setType(MoveType.BETWEEN_STOCKS);
        request.setItems(new ArrayList<>());

        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        request.getItems().add(item1Request);

        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        request.getItems().add(item2Request);

        request.setNote("Observação da movimentação");

        return request;
    }

    private ResponseFieldsSnippet getPageMoveProjection() {
        return getPageContent("Lista com as movimentações encontradas")
                .andWithPrefix("content[].", getProjection());
    }

    private FieldDescriptor[] getProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                fieldWithPath("origin").optional().type(JsonFieldType.STRING).description("Origem da movimentação"),
                fieldWithPath("status").optional().type(JsonFieldType.STRING).description("Status atual da movimentação"),
                fieldWithPath("type").optional().type(JsonFieldType.STRING).description("Tipo de movimentação"),
                fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description("Quantidade movimentada"),
                fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação")};
    }

    private ResponseFieldsSnippet getPageMoveProjectionWithProduct() {
        return getPageContent("Lista com as movimentações encontradas")
                .andWithPrefix("content[].", getProjectionWithProduct())
                .andWithPrefix("content[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"));
    }

    private FieldDescriptor[] getProjectionWithProduct() {
        return new FieldDescriptor[]{
                fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                fieldWithPath("origin").optional().type(JsonFieldType.STRING).description("Origem da movimentação"),
                fieldWithPath("status").optional().type(JsonFieldType.STRING).description("Status atual da movimentação"),
                fieldWithPath("type").optional().type(JsonFieldType.STRING).description("Tipo de movimentação"),
                fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description("Quantidade movimentada"),
                fieldWithPath("product").description("Produto movimentado"),
                fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação")};
    }

    private ResponseFieldsSnippet getPageAdvancedStockMoveProjection() {
        return getPageContent("Lista com as movimentações encontradas")
                .andWithPrefix("content[].", getAdvancedStockMoveProjection())
                .andWithPrefix("content[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"));
    }

    private ResponseFieldsSnippet getPageAdvancedStockMoveProjectionWithCustomerName() {
        return getPageContent("Lista com as movimentações encontradas")
                .andWithPrefix("content[].", getAdvancedStockMoveProjectionWithCustomerName())
                .andWithPrefix("content[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"));
    }

    private FieldDescriptor[] getAdvancedStockMoveProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                fieldWithPath("status").optional().type(JsonFieldType.STRING).description("Status atual da movimentação"),
                fieldWithPath("from").optional().type(JsonFieldType.STRING).description("Nome do estoque de origem, caso exista"),
                fieldWithPath("to").optional().type(JsonFieldType.STRING).description("Nome do estoque de destino, caso exista"),
                fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação"),
                fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description("Quantidade movimentada"),
                fieldWithPath("product").description("Produto movimentado")};
    }

    private FieldDescriptor[] getAdvancedStockMoveProjectionWithCustomerName() {
        return new FieldDescriptor[]{
                fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                fieldWithPath("status").optional().type(JsonFieldType.STRING).description("Status atual da movimentação"),
                fieldWithPath("from").optional().type(JsonFieldType.STRING).description("Nome do estoque de origem, caso exista"),
                fieldWithPath("to").optional().type(JsonFieldType.STRING).description("Nome do estoque de destino, caso exista"),
                fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação"),
                fieldWithPath("customerName").optional().type(JsonFieldType.STRING)
                        .description(createDescription(
                        "Nome do cliente associado, caso exista",
                        "Em casos de movimentações de técnicos para a manutenção oriundas de recolhimento de equipamentos," +
                                " este nome será o nome do cliente associado na OS")),
                fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description("Quantidade movimentada"),
                fieldWithPath("product").description("Produto movimentado")};
    }

    private ResponseFieldsSnippet getMoveResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da movimentação"),
                fieldWithPath("date").description("Data da movimentação"),
                fieldWithPath("lastModifiedDate").description("Data da última atualização da movimentação"),
                fieldWithPath("origin").description("Origem da movimentação"),
                fieldWithPath("status").description("Status atual da movimentação"),
                fieldWithPath("type").description("Tipo de movimentação"),
                fieldWithPath("quantity").description("Quantidade movimentada"),
                fieldWithPath("from").optional().type(JsonFieldType.OBJECT).description("Estoque de origem"),
                fieldWithPath("to").optional().type(JsonFieldType.OBJECT).description("Estoque de destino"),
                fieldWithPath("product").description("Produto movimentado"),
                fieldWithPath("requester").description("Funcionário soliciatante"),
                fieldWithPath("responsible").optional().type(JsonFieldType.OBJECT).description("Funcionário responsável, o qual aprovou/rejeitou a movimentação"),
                fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação contendo todos os dados em formato String"),
                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações da movimentação"))
                .andWithPrefix("from.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Código do estoque"),
                        fieldWithPath("type").type(JsonFieldType.STRING).description("Tipo do estoque"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("Nome do estoque"))
                .andWithPrefix("to.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Código do estoque"),
                        fieldWithPath("type").type(JsonFieldType.STRING).description("Tipo do estoque"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("Nome do estoque"))
                .andWithPrefix("product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"))
                .andWithPrefix("requester.",
                        fieldWithPath("id").description("Código do funcionário"),
                        fieldWithPath("name").description("Nome do funcionário"))
                .andWithPrefix("responsible.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Código do funcionário"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("Nome do funcionário"));
    }

    private FieldDescriptor[] getServiceOrderMoveResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                fieldWithPath("lastModifiedDate").optional().type(JsonFieldType.STRING).description("Data da última atualização da movimentação"),
                fieldWithPath("status").optional().type(JsonFieldType.STRING).description("Status atual da movimentação"),
                fieldWithPath("from").optional().type(JsonFieldType.STRING).description(
                        createDescription(
                                "Nome do estoque de origem do equipamento",
                                "Caso o estoque de origem seja do tipo cliente, o nome do cliente oriundo da ordem de serviço será atribuido",
                                "Caso contrário, será exibido o nome padrão")),
                fieldWithPath("to").optional().type(JsonFieldType.STRING).description(
                        createDescription(
                                "Nome do estoque de destino",
                                "Caso o estoque de destino seja do tipo cliente, o nome do cliente oriundo da ordem de serviço será atribuido",
                                "Caso contrário, será exibido o nome padrão")),
                fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação"),
                fieldWithPath("type").optional().type(JsonFieldType.STRING)
                        .description("Flag indicando se a movimentação é do técnico para um cliente, ou de um cliente para um técnico"),
                fieldWithPath("product").optional().type(JsonFieldType.OBJECT).description("Produto movimentado"),
                fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description("Quantidade movimentada")};
    }

    private FieldDescriptor[] getProductProjectionWithoutUnit() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                fieldWithPath("name").description("Nome do produto")};
    }

}
