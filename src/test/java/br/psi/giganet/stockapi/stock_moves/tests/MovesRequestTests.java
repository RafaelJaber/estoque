package br.psi.giganet.stockapi.stock_moves.tests;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.model.Employee;
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
import br.psi.giganet.stockapi.stock_moves.annotations.RoleTestStockMovesRead;
import br.psi.giganet.stockapi.stock_moves.annotations.RoleTestStockMovesWriteAll;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;
import br.psi.giganet.stockapi.stock_moves.repository.StockMovesRepository;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MovesRequestTests extends BuilderIntegrationTest implements RolesIntegrationTest {

    @Autowired
    public MovesRequestTests(
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

    @SuppressWarnings("unchecked")
    @RoleTestRoot
    @Transactional
    public void requestAndApproveFlux() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        InsertRequestedMoveRequest insertRequest = new InsertRequestedMoveRequest();
        insertRequest.setFrom(shed.getId());
        insertRequest.setTo(technician.getId());
        insertRequest.setItems(new ArrayList<>());

        InsertRequestedMoveItemRequest item1Request = new InsertRequestedMoveItemRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertRequestedMoveItemRequest item2Request = new InsertRequestedMoveItemRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        List<LinkedHashMap<String, Object>> response = (ArrayList<LinkedHashMap<String, Object>>) objectMapper.readValue(insertResponse, ArrayList.class);
        this.mockMvc.perform(post("/moves/requests/{id}/approve", response.get(0).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(shed.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós aprovação da solicitação é inválida");

        this.mockMvc.perform(post("/moves/requests/{id}/approve", response.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(shed.find(p2).get().getQuantity().equals(0d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós aprovação da solicitação é inválida");

    }

    @SuppressWarnings("unchecked")
    @RoleTestRoot
    @Transactional
    public void requestByShedAndApproveByTechnicianFlux() throws Exception {
        Stock shed = createAndSaveShedStock();
        TechnicianStock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        InsertRequestedMoveRequest insertRequest = new InsertRequestedMoveRequest();
        insertRequest.setFrom(shed.getId());
        insertRequest.setTo(technician.getId());
        insertRequest.setItems(new ArrayList<>());

        InsertRequestedMoveItemRequest item1Request = new InsertRequestedMoveItemRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertRequestedMoveItemRequest item2Request = new InsertRequestedMoveItemRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        List<LinkedHashMap<String, Object>> response = (ArrayList<LinkedHashMap<String, Object>>) objectMapper.readValue(insertResponse, ArrayList.class);
        this.mockMvc.perform(post("/basic/moves/requests/{id}/approve", response.get(0).get("id"))
                .header("User-Id", technician.getTechnician().getUserId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(shed.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós aprovação da solicitação é inválida");

        this.mockMvc.perform(post("/moves/requests/{id}/approve", response.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(shed.find(p2).get().getQuantity().equals(0d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós aprovação da solicitação é inválida");

    }

    @SuppressWarnings("unchecked")
    @RoleTestRoot
    @Transactional
    public void requestAndRejectFlux() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        InsertRequestedMoveRequest insertRequest = new InsertRequestedMoveRequest();
        insertRequest.setFrom(shed.getId());
        insertRequest.setTo(technician.getId());
        insertRequest.setItems(new ArrayList<>());

        InsertRequestedMoveItemRequest item1Request = new InsertRequestedMoveItemRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertRequestedMoveItemRequest item2Request = new InsertRequestedMoveItemRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        List<LinkedHashMap<String, Object>> response = (ArrayList<LinkedHashMap<String, Object>>) objectMapper.readValue(insertResponse, ArrayList.class);
        this.mockMvc.perform(post("/moves/requests/{id}/reject", response.get(0).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));

        Assert.state(shed.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós aprovação da solicitação é inválida");

        this.mockMvc.perform(post("/moves/requests/{id}/reject", response.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));

        Assert.state(shed.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p2).get().getQuantity().equals(0d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós aprovação da solicitação é inválida");

    }

    @SuppressWarnings("unchecked")
    @RoleTestRoot
    @Transactional
    public void requestByShedAndRejectByTechnicianFlux() throws Exception {
        Stock shed = createAndSaveShedStock();
        TechnicianStock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        InsertRequestedMoveRequest insertRequest = new InsertRequestedMoveRequest();
        insertRequest.setFrom(shed.getId());
        insertRequest.setTo(technician.getId());
        insertRequest.setItems(new ArrayList<>());

        InsertRequestedMoveItemRequest item1Request = new InsertRequestedMoveItemRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertRequestedMoveItemRequest item2Request = new InsertRequestedMoveItemRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        List<LinkedHashMap<String, Object>> response = (ArrayList<LinkedHashMap<String, Object>>) objectMapper.readValue(insertResponse, ArrayList.class);
        this.mockMvc.perform(post("/basic/moves/requests/{id}/reject", response.get(0).get("id"))
                .header("User-Id", technician.getTechnician().getUserId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));

        Assert.state(shed.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós aprovação da solicitação é inválida");

        this.mockMvc.perform(post("/moves/requests/{id}/reject", response.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));

        Assert.state(shed.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p2).get().getQuantity().equals(0d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós aprovação da solicitação é inválida");

    }

    @SuppressWarnings("unchecked")
    @RoleTestRoot
    @Transactional
    public void requestAndApproveOneAndRejectOtherFlux() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        InsertRequestedMoveRequest insertRequest = new InsertRequestedMoveRequest();
        insertRequest.setFrom(shed.getId());
        insertRequest.setTo(technician.getId());
        insertRequest.setItems(new ArrayList<>());

        InsertRequestedMoveItemRequest item1Request = new InsertRequestedMoveItemRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertRequestedMoveItemRequest item2Request = new InsertRequestedMoveItemRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        List<LinkedHashMap<String, Object>> response = (ArrayList<LinkedHashMap<String, Object>>) objectMapper.readValue(insertResponse, ArrayList.class);
        this.mockMvc.perform(post("/moves/requests/{id}/approve", response.get(0).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(shed.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós aprovação da solicitação é inválida");

        this.mockMvc.perform(post("/moves/requests/{id}/reject", response.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));

        Assert.state(shed.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p2).get().getQuantity().equals(0d), "Quantidade atual do estoque pós aprovação da solicitação é inválida");
        Assert.state(technician.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós aprovação da solicitação é inválida");

    }

    @Override
    @RoleTestStockMovesRead
    @Transactional
    public void readAuthorized() throws Exception {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        RequestedMove requestedMove = null;
        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            StockMove move = createAndSaveDetachedStockMove(shed, technician, product);
            requestedMove = createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(shed, product),
                    createAndSaveStockItem(technician, product),
                    move);

            createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(technician, product),
                    createAndSaveStockItem(shed, product),
                    move);
        }

        this.mockMvc.perform(get("/moves/requests")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/moves/requests/{id}", requestedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());


        this.mockMvc.perform(get("/moves/requests/to/city/{city}/pending", CityOptions.IPATINGA_HORTO)
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/moves/requests/from/city/{city}/pending", CityOptions.IPATINGA_HORTO)
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/moves/requests/from/pending")
                .param("page", "0")
                .param("pageSize", "5")
                .param("types", StockType.SHED.name(), StockType.MAINTENANCE.name())
                .header("Office-Id", shed.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/moves/requests/to/pending")
                .param("page", "0")
                .param("pageSize", "5")
                .param("types", StockType.SHED.name(), StockType.MAINTENANCE.name(), StockType.TECHNICIAN.name())
                .header("Office-Id", shed.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

    }

    @Override
    @RoleTestStockMovesWriteAll
    @Transactional
    public void writeAuthorized() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(createInsertRequestedMoveRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())));


        Product approvedProduct = createAndSaveProduct();
        RequestedMove approvedRequest = createAndSaveRequestedMove(
                approvedProduct,
                createAndSaveStockItem(office.shed().orElseThrow(), approvedProduct),
                createAndSaveStockItem(createAndSaveTechnicianStock(office), approvedProduct),
                null);
        this.mockMvc.perform(post("/moves/requests/{id}/approve", approvedRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));


        Product rejectedProduct = createAndSaveProduct();
        RequestedMove rejectedRequest = createAndSaveRequestedMove(
                rejectedProduct,
                createAndSaveStockItem(office.shed().orElseThrow(), rejectedProduct),
                createAndSaveStockItem(createAndSaveTechnicianStock(office), rejectedProduct),
                null);
        this.mockMvc.perform(post("/moves/requests/{id}/reject", rejectedRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));


        Product canceledProduct = createAndSaveProduct();
        RequestedMove canceledRequest = createAndSaveRequestedMove(
                canceledProduct,
                createAndSaveStockItem(office.shed().orElseThrow(), canceledProduct),
                createAndSaveStockItem(createAndSaveTechnicianStock(office), canceledProduct),
                null);
        this.mockMvc.perform(delete("/moves/requests/{id}", canceledRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.CANCELED.name())));
    }

    @Override
    @Transactional
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        Stock shed = createAndSaveShedStock();
        TechnicianStock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        RequestedMove requestedMove = null;
        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            StockMove move = createAndSaveDetachedStockMove(shed, technician, product);
            requestedMove = createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(shed, product),
                    createAndSaveStockItem(technician, product),
                    move);
            createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(technician, product),
                    createAndSaveStockItem(shed, product),
                    move);
        }

        this.mockMvc.perform(get("/moves/requests")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves/requests/{id}", requestedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        this.mockMvc.perform(get("/moves/requests/to/city/{city}/pending", CityOptions.IPATINGA_HORTO)
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves/requests/from/city/{city}/pending", CityOptions.IPATINGA_HORTO)
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves/requests/from/pending")
                .param("page", "0")
                .param("pageSize", "5")
                .param("types", StockType.SHED.name(), StockType.MAINTENANCE.name())
                .header("Office-Id", shed.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves/requests/to/pending")
                .param("page", "0")
                .param("pageSize", "5")
                .param("types", StockType.SHED.name(), StockType.MAINTENANCE.name(), StockType.TECHNICIAN.name())
                .header("Office-Id", shed.getBranchOffice().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @Transactional
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(createInsertRequestedMoveRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        Product approvedProduct = createAndSaveProduct();
        RequestedMove approvedRequest = createAndSaveRequestedMove(
                approvedProduct,
                createAndSaveStockItem(office.shed().orElseThrow(), approvedProduct),
                createAndSaveStockItem(createAndSaveTechnicianStock(office), approvedProduct),
                null);
        this.mockMvc.perform(post("/moves/requests/{id}/approve", approvedRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        Product rejectedProduct = createAndSaveProduct();
        RequestedMove rejectedRequest = createAndSaveRequestedMove(
                rejectedProduct,
                createAndSaveStockItem(office.shed().orElseThrow(), rejectedProduct),
                createAndSaveStockItem(createAndSaveTechnicianStock(office), rejectedProduct),
                null);
        this.mockMvc.perform(post("/moves/requests/{id}/reject", rejectedRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        Product canceledProduct = createAndSaveProduct();
        RequestedMove canceledRequest = createAndSaveRequestedMove(
                canceledProduct,
                createAndSaveStockItem(office.shed().orElseThrow(), canceledProduct),
                createAndSaveStockItem(createAndSaveTechnicianStock(office), canceledProduct),
                null);
        this.mockMvc.perform(delete("/moves/requests/{id}", canceledRequest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Transactional
    @RoleTestRoot
    public void invalidInsertBetween() throws Exception {
        this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_NO_ITEMS)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_VALID_FROM_BUT_INVALID_TO)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_VALID_TO_BUT_INVALID_FROM)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_INVALID_PRODUCT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_INVALID_QUANTITY)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_FROM_EQUALS_TO)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves/requests")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_DIFFERENT_BRANCH_OFFICES)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicWriteAuthorized() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().removeIf(p -> p.equals(new Permission("ROLE_ROOT")));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        employeeRepository.saveAndFlush(e);

        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(e);

        this.mockMvc.perform(post("/basic/moves/requests")
                .content(objectMapper.writeValueAsString(createBasicInsertRequestedMoveRequest(technicianStock)))
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN"})
    public void basicWriteUnauthorized() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().removeIf(p -> p.equals(new Permission("ROLE_ROOT")));
        employeeRepository.saveAndFlush(e);

        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(e);

        this.mockMvc.perform(post("/basic/moves/requests")
                .content(objectMapper.writeValueAsString(createBasicInsertRequestedMoveRequest(technicianStock)))
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicFindAllByTechnicianTo() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));

        BranchOffice office = createAndSaveBranchOffice();
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(
                employeeRepository.save(e),
                office);
        Stock shed = office.shed().orElseThrow();

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(shed, product),
                    createAndSaveStockItem(technicianStock, product),
                    null);
        }

        this.mockMvc.perform(get("/basic/moves/requests/to/technicians/{userId}", technicianStock.getTechnician().getUserId())
                .param("status", "REQUESTED")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/basic/moves/requests/to/technicians/{userId}", technicianStock.getTechnician().getUserId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicFindAllByTechnicianFrom() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        BranchOffice office = createAndSaveBranchOffice();
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(
                employeeRepository.save(e),
                office);
        Stock shed = office.shed().orElseThrow();

        for (int i = 0; i < 3; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(technicianStock, product);
            createAndSaveRequestedMove(
                    product,
                    createAndSaveStockItem(technicianStock, product),
                    createAndSaveStockItem(shed, product),
                    null);
        }

        this.mockMvc.perform(get("/basic/moves/requests/from/technicians/{userId}", technicianStock.getTechnician().getUserId())
                .param("status", "REQUESTED")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/basic/moves/requests/from/technicians/{userId}", technicianStock.getTechnician().getUserId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    private InsertRequestedMoveRequest createInsertRequestedMoveRequest() {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

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


    private InsertRequestedMoveRequest createInvalidInsertMoveRequest(Implementation impl) {
        InsertRequestedMoveRequest request = new InsertRequestedMoveRequest();
        InsertRequestedMoveItemRequest item1Request = new InsertRequestedMoveItemRequest();
        InsertRequestedMoveItemRequest item2Request = new InsertRequestedMoveItemRequest();

        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        if (impl == Implementation.BETWEEN_MOVE_NO_ITEMS) {
            request.setFrom(shed.getId());
            request.setTo(technician.getId());
            request.setItems(new ArrayList<>());

        } else if (impl == Implementation.BETWEEN_MOVE_VALID_FROM_BUT_INVALID_TO) {
            request.setFrom(shed.getId());
            request.setTo(-1L);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);
        } else if (impl == Implementation.BETWEEN_MOVE_VALID_TO_BUT_INVALID_FROM) {
            request.setFrom(-1L);
            request.setTo(technician.getId());
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);
        } else if (impl == Implementation.BETWEEN_MOVE_INVALID_PRODUCT) {
            request.setFrom(shed.getId());
            request.setTo(technician.getId());
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId() + "0a0aa");
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);
        } else if (impl == Implementation.BETWEEN_MOVE_INVALID_QUANTITY) {
            request.setFrom(shed.getId());
            request.setTo(technician.getId());
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(-100d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(0d);
            request.getItems().add(item2Request);

        } else if (impl == Implementation.BETWEEN_MOVE_FROM_EQUALS_TO) {
            request.setFrom(shed.getId());
            request.setTo(shed.getId());
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);
        } else if (impl == Implementation.BETWEEN_MOVE_NO_STOCK) {
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);
        } else if (impl == Implementation.BETWEEN_MOVE_DIFFERENT_BRANCH_OFFICES) {
            request.setFrom(shed.getId());
            request.setTo(createAndSaveShedStock().getId());
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);
        }


        return request;
    }

    private enum Implementation {

        BETWEEN_MOVE_NO_ITEMS,
        BETWEEN_MOVE_VALID_FROM_BUT_INVALID_TO,
        BETWEEN_MOVE_VALID_TO_BUT_INVALID_FROM,
        BETWEEN_MOVE_INVALID_PRODUCT,
        BETWEEN_MOVE_INVALID_QUANTITY,
        BETWEEN_MOVE_FROM_EQUALS_TO,
        BETWEEN_MOVE_NO_STOCK,
        BETWEEN_MOVE_DIFFERENT_BRANCH_OFFICES

    }

}
