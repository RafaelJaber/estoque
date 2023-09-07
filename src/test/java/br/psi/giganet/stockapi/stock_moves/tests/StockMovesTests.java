package br.psi.giganet.stockapi.stock_moves.tests;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
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
import br.psi.giganet.stockapi.stock.model.CustomerStock;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.StockItem;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.stock_moves.annotations.RoleTestStockMovesRead;
import br.psi.giganet.stockapi.stock_moves.annotations.RoleTestStockMovesWriteAll;
import br.psi.giganet.stockapi.stock_moves.annotations.RoleTestStockMovesWriteRoot;
import br.psi.giganet.stockapi.stock_moves.controller.request.*;
import br.psi.giganet.stockapi.stock_moves.model.*;
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
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StockMovesTests extends BuilderIntegrationTest implements RolesIntegrationTest {

    @Autowired
    public StockMovesTests(
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
        this.patrimonyRepository = patrimonyRepository;
        this.patrimonyLocationRepository = patrimonyLocationRepository;
        this.validMacAddressesRepository = validMacAddressesRepository;
        createCurrentUser();

    }

    @SuppressWarnings("unchecked")
    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicInsertFromServiceOrderAndApproveFlux() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(technicianStock, p1);

        BasicInsertMoveFromServiceOrderRequest request = new BasicInsertMoveFromServiceOrderRequest();
        request.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        request.setOrderType("R");
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
        request.getEntryItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/basic/moves/service-orders")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moves[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$.moves[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        Assert.state(technicianStock.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(technicianStock.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");

        LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) objectMapper.readValue(insertResponse, LinkedHashMap.class);
        List<LinkedHashMap<String, Object>> moves = (List<LinkedHashMap<String, Object>>) response.get("moves");
        this.mockMvc.perform(post("/moves/{id}/approve", moves.get(0).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(technicianStock.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technicianStock.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");


        this.mockMvc.perform(post("/moves/{id}/approve", moves.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(technicianStock.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technicianStock.find(p2).get().getBlockedQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");

    }

    @SuppressWarnings("unchecked")
    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicInsertFromServiceOrderAndApproveMovesInBatchFlux() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(technicianStock, p1);

        BasicInsertMoveFromServiceOrderRequest request = new BasicInsertMoveFromServiceOrderRequest();
        request.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        request.setOrderType("R");
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
        request.getEntryItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/basic/moves/service-orders")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moves[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$.moves[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        Assert.state(technicianStock.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(technicianStock.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");

        LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) objectMapper.readValue(insertResponse, LinkedHashMap.class);
        List<LinkedHashMap<String, Object>> moves = (List<LinkedHashMap<String, Object>>) response.get("moves");
        List<Object> moveIds = moves.stream().map(m -> m.get("id")).collect(Collectors.toList());
        this.mockMvc.perform(post("/moves/batch/approve")
                .content(objectMapper.writeValueAsString(Collections.singletonMap("moves", moveIds)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REALIZED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(technicianStock.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technicianStock.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");

        Assert.state(technicianStock.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technicianStock.find(p2).get().getBlockedQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");

    }

    @SuppressWarnings("unchecked")
    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicInsertFromServiceOrderAndRejectFlux() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();
        Product p3 = createAndSaveProduct();

        createAndSaveStockItem(technicianStock, p1);

        BasicInsertMoveFromServiceOrderRequest request = new BasicInsertMoveFromServiceOrderRequest();
        request.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        request.setOrderType("R");
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
        request.getEntryItems().add(item2Request);

        BasicInsertItemMoveFromServiceOrderRequest item3Request = new BasicInsertItemMoveFromServiceOrderRequest();
        item3Request.setProduct(p3.getId());
        item3Request.setQuantity(1d);
        request.getEntryItems().add(item3Request);

        String insertResponse = this.mockMvc.perform(post("/basic/moves/service-orders")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moves[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$.moves[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$.moves[2].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        Assert.state(technicianStock.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(technicianStock.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");

        LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) objectMapper.readValue(insertResponse, LinkedHashMap.class);
        List<LinkedHashMap<String, Object>> moves = (List<LinkedHashMap<String, Object>>) response.get("moves");
        this.mockMvc.perform(post("/moves/{id}/reject", moves.get(0).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));

        Assert.state(technicianStock.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technicianStock.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");


        this.mockMvc.perform(post("/moves/{id}/approve", moves.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(technicianStock.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technicianStock.find(p2).get().getBlockedQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");


        this.mockMvc.perform(delete("/moves/{id}", moves.get(2).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.CANCELED.name())));

        Assert.state(technicianStock.find(p3).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technicianStock.find(p3).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");

    }

    @SuppressWarnings("unchecked")
    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicInsertFromServiceOrderAndRejectInBatchFlux() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();
        Product p3 = createAndSaveProduct();

        createAndSaveStockItem(technicianStock, p1);

        BasicInsertMoveFromServiceOrderRequest request = new BasicInsertMoveFromServiceOrderRequest();
        request.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        request.setOrderType("R");
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
        request.getEntryItems().add(item2Request);

        BasicInsertItemMoveFromServiceOrderRequest item3Request = new BasicInsertItemMoveFromServiceOrderRequest();
        item3Request.setProduct(p3.getId());
        item3Request.setQuantity(1d);
        request.getEntryItems().add(item3Request);

        String insertResponse = this.mockMvc.perform(post("/basic/moves/service-orders")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moves[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$.moves[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$.moves[2].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        Assert.state(technicianStock.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(technicianStock.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");

        LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) objectMapper.readValue(insertResponse, LinkedHashMap.class);
        List<LinkedHashMap<String, Object>> moves = (List<LinkedHashMap<String, Object>>) response.get("moves");
        List<Object> moveIds = moves.stream().map(m -> m.get("id")).collect(Collectors.toList());
        this.mockMvc.perform(post("/moves/batch/reject")
                .content(objectMapper.writeValueAsString(Collections.singletonMap("moves", moveIds)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REJECTED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REJECTED.name())));

        Assert.state(technicianStock.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technicianStock.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");

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

        InsertMoveRequest insertRequest = new InsertMoveRequest();
        insertRequest.setFrom(shed.getId());
        insertRequest.setTo(technician.getId());
        insertRequest.setType(MoveType.BETWEEN_STOCKS);
        insertRequest.setItems(new ArrayList<>());

        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        Assert.state(shed.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");
        Assert.state(shed.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");

        List<LinkedHashMap<String, Object>> response = (ArrayList<LinkedHashMap<String, Object>>) objectMapper.readValue(insertResponse, ArrayList.class);
        this.mockMvc.perform(post("/moves/{id}/approve", response.get(0).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(shed.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(technician.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technician.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");

        this.mockMvc.perform(post("/moves/{id}/approve", response.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(shed.find(p2).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(technician.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technician.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");

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

        InsertMoveRequest insertRequest = new InsertMoveRequest();
        insertRequest.setFrom(shed.getId());
        insertRequest.setTo(technician.getId());
        insertRequest.setType(MoveType.BETWEEN_STOCKS);
        insertRequest.setItems(new ArrayList<>());

        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        Assert.state(shed.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");
        Assert.state(shed.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");

        List<LinkedHashMap<String, Object>> response = (ArrayList<LinkedHashMap<String, Object>>) objectMapper.readValue(insertResponse, ArrayList.class);
        this.mockMvc.perform(post("/basic/moves/{id}/approve", response.get(0).get("id"))
                .header("User-Id", technician.getTechnician().getUserId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(shed.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(technician.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technician.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");

        this.mockMvc.perform(post("/moves/{id}/approve", response.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(shed.find(p2).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(technician.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technician.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");

    }

    @RoleTestRoot
    @Transactional
    public void invalidMoveBetweenDifferentBranchOfficesFlux() throws Exception {
        Stock shed = createAndSaveShedStock();
        TechnicianStock technician = createAndSaveTechnicianStock(createAndSaveBranchOffice());

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        InsertMoveRequest insertRequest = new InsertMoveRequest();
        insertRequest.setFrom(shed.getId());
        insertRequest.setTo(technician.getId());
        insertRequest.setType(MoveType.BETWEEN_STOCKS);
        insertRequest.setItems(new ArrayList<>());

        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.not(Matchers.empty())));

        Assert.state(shed.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível para movimentações do estoque é inválida");
        Assert.state(shed.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível para movimentações do estoque é inválida");

    }

    @Test
    @WithMockUser(
            username = "teste_between_offices@teste.com",
            authorities = { "ROLE_MOVES_WRITE_BETWEEN_STOCKS", "ROLE_ADMIN"})
    @Transactional
    public void unauthorizedMoveBetweenDifferentBranchOfficesFlux() throws Exception {
        Employee e = createAndSaveEmployee("teste_between_offices@teste.com");
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        e.getPermissions().removeIf(p -> p.equals(new Permission("ROLE_ROOT")));
        employeeRepository.saveAndFlush(e);

        Stock shed1 = createAndSaveShedStock();
        Stock shed2 = createAndSaveShedStock();

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed1, p1);
        createAndSaveStockItem(shed1, p2);

        InsertMoveRequest insertRequest = new InsertMoveRequest();
        insertRequest.setFrom(shed1.getId());
        insertRequest.setTo(shed2.getId());
        insertRequest.setType(MoveType.BETWEEN_STOCKS);
        insertRequest.setItems(new ArrayList<>());

        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", Matchers.not(Matchers.empty())));

        Assert.state(shed1.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed1.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível para movimentações do estoque é inválida");
        Assert.state(shed1.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed1.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível para movimentações do estoque é inválida");

    }

    @SuppressWarnings("unchecked")
    @Test
    @WithMockUser(
            username = "teste_between_offices@teste.com",
            authorities = {"ROLE_MOVES_WRITE_BETWEEN_BRANCH_OFFICE", "ROLE_MOVES_WRITE_BETWEEN_STOCKS", "ROLE_ADMIN"})
    @Transactional
    public void validMoveBetweenDifferentBranchOfficesFlux() throws Exception {
        Employee e = createAndSaveEmployee("teste_between_offices@teste.com");
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_BRANCH_OFFICE"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        e.getPermissions().removeIf(p -> p.equals(new Permission("ROLE_ROOT")));
        employeeRepository.saveAndFlush(e);

        Stock shed1 = createAndSaveShedStock();
        Stock shed2 = createAndSaveShedStock();

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed1, p1);
        createAndSaveStockItem(shed1, p2);

        InsertMoveRequest insertRequest = new InsertMoveRequest();
        insertRequest.setFrom(shed1.getId());
        insertRequest.setTo(shed2.getId());
        insertRequest.setType(MoveType.BETWEEN_STOCKS);
        insertRequest.setItems(new ArrayList<>());

        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        Assert.state(shed1.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed1.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");
        Assert.state(shed1.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed1.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");

        List<LinkedHashMap<String, Object>> response = (ArrayList<LinkedHashMap<String, Object>>) objectMapper.readValue(insertResponse, ArrayList.class);
        this.mockMvc.perform(post("/moves/{id}/approve", response.get(0).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(shed1.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed1.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(shed2.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed2.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");

        this.mockMvc.perform(post("/moves/{id}/approve", response.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(shed1.find(p2).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed1.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(shed2.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed2.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");

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

        InsertMoveRequest insertRequest = new InsertMoveRequest();
        insertRequest.setFrom(shed.getId());
        insertRequest.setTo(technician.getId());
        insertRequest.setType(MoveType.BETWEEN_STOCKS);
        insertRequest.setItems(new ArrayList<>());

        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        Assert.state(shed.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");
        Assert.state(shed.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");

        List<LinkedHashMap<String, Object>> response = (ArrayList<LinkedHashMap<String, Object>>) objectMapper.readValue(insertResponse, ArrayList.class);
        this.mockMvc.perform(post("/moves/{id}/reject", response.get(0).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));

        Assert.state(shed.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(technician.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technician.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");

        this.mockMvc.perform(post("/moves/{id}/reject", response.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));

        Assert.state(shed.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(technician.find(p2).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technician.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");

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

        InsertMoveRequest insertRequest = new InsertMoveRequest();
        insertRequest.setFrom(shed.getId());
        insertRequest.setTo(technician.getId());
        insertRequest.setType(MoveType.BETWEEN_STOCKS);
        insertRequest.setItems(new ArrayList<>());

        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        Assert.state(shed.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");
        Assert.state(shed.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");

        List<LinkedHashMap<String, Object>> response = (ArrayList<LinkedHashMap<String, Object>>) objectMapper.readValue(insertResponse, ArrayList.class);
        this.mockMvc.perform(post("/basic/moves/{id}/reject", response.get(0).get("id"))
                .header("User-Id", technician.getTechnician().getUserId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));

        Assert.state(shed.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(technician.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technician.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");

        this.mockMvc.perform(post("/moves/{id}/reject", response.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));

        Assert.state(shed.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(technician.find(p2).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technician.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");

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

        InsertMoveRequest insertRequest = new InsertMoveRequest();
        insertRequest.setFrom(shed.getId());
        insertRequest.setTo(technician.getId());
        insertRequest.setType(MoveType.BETWEEN_STOCKS);
        insertRequest.setItems(new ArrayList<>());

        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        String insertResponse = this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REQUESTED.name())))
                .andReturn().getResponse().getContentAsString();

        Assert.state(shed.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");
        Assert.state(shed.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível para movimentações do estoque é inválida");

        List<LinkedHashMap<String, Object>> response = (ArrayList<LinkedHashMap<String, Object>>) objectMapper.readValue(insertResponse, ArrayList.class);
        this.mockMvc.perform(post("/moves/{id}/approve", response.get(0).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));

        Assert.state(shed.find(p1).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed.find(p1).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(technician.find(p1).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technician.find(p1).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");

        this.mockMvc.perform(post("/moves/{id}/reject", response.get(1).get("id"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));

        Assert.state(shed.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(technician.find(p2).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technician.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");

    }

    @SuppressWarnings("unchecked")
    @RoleTestStockMovesWriteRoot
    @Transactional
    public void requestAndAutoApproveFlux() throws Exception {
        Employee moveRoot = createAndSaveEmployee("teste_write_root@teste.com");
        moveRoot.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_ROOT"));
        employeeRepository.saveAndFlush(moveRoot);

        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        InsertMoveRequest insertRequest = new InsertMoveRequest();
        insertRequest.setFrom(shed.getId());
        insertRequest.setTo(technician.getId());
        insertRequest.setType(MoveType.BETWEEN_STOCKS);
        insertRequest.setItems(new ArrayList<>());

        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        insertRequest.getItems().add(item1Request);

        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        insertRequest.getItems().add(item2Request);

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(insertRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REALIZED.name())))
                .andExpect(jsonPath("$[1].status", Matchers.equalTo(MoveStatus.REALIZED.name())))
                .andReturn().getResponse().getContentAsString();

        Assert.state(shed.find(p2).get().getQuantity().equals(0d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(shed.find(p2).get().getAvailableQuantity().equals(0d), "Quantidade disponível pós movimentação é inválida");
        Assert.state(technician.find(p2).get().getQuantity().equals(1d), "Quantidade atual do estoque pós movimentação é inválida");
        Assert.state(technician.find(p2).get().getAvailableQuantity().equals(1d), "Quantidade disponível pós movimentação é inválida");

    }

    @Override
    @RoleTestStockMovesRead
    @Transactional
    public void readAuthorized() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock shed = office.shed().orElseThrow();
        Stock technician = createAndSaveTechnicianStock(office);
        Product product = createAndSaveProduct();

        StockMove entryMove = createAndSaveDetachedStockMove(null, shed, product);
        entryMove.setStatus(MoveStatus.REALIZED);
        stockMovesRepository.save(entryMove);

        StockMove betweenMove = createAndSaveDetachedStockMove(shed, technician, product);
        betweenMove.setStatus(MoveStatus.REALIZED);
        stockMovesRepository.save(betweenMove);

        StockMove outMove = createAndSaveDetachedStockMove(technician, null, product);
        outMove.setStatus(MoveStatus.REALIZED);
        stockMovesRepository.save(outMove);

        createAndSaveDetachedStockMove(shed, technician, product);
        createAndSaveDetachedStockMove(technician, null, product);

        this.mockMvc.perform(get("/moves")
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(get("/moves/realized")
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(get("/moves/pending")
                .param("type", MoveType.BETWEEN_STOCKS.name())
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(get("/moves/from/{stock}/pending", shed.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(1)));

        this.mockMvc.perform(get("/moves/from/city/{city}/pending", CityOptions.IPATINGA_HORTO.name())
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/moves/from/pending")
                .param("page", "0")
                .param("pageSize", "5")
                .param("types", StockType.SHED.name(), StockType.MAINTENANCE.name())
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/moves/to/city/{city}/pending", CityOptions.IPATINGA_HORTO.name())
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/moves/to/{stock}/pending", technician.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(1)));

        this.mockMvc.perform(get("/moves/to/pending")
                .param("page", "0")
                .param("pageSize", "5")
                .param("types", StockType.SHED.name(), StockType.MAINTENANCE.name())
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/moves/{id}", betweenMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());


        Stock customer = office.customer().orElseThrow();
        for (int i = 0; i < 2; i++) {
            product = createAndSaveProduct();
            createAndSaveStockItem(technician, product);
            TechnicianStockMove move = createAndSaveTechnicianStockMove(technician, customer, product);
            move.setOrderType(ExternalOrderType.INSTALLATION);
            move.setOrderId(UUID.randomUUID().toString());
            move.setReason(MoveReason.SERVICE_ORDER);
            move.setBranchOffice(office);
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
                .andExpect(jsonPath("$.content", Matchers.hasSize(2)));

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
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestStockMovesWriteAll
    @Transactional
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInsertMoveRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())));

        Stock shed = createAndSaveShedStock();
        Product product = createAndSaveProduct();

        StockMove approvedMove = createAndSaveDetachedStockMove(null, shed, product);
        approvedMove.setStatus(MoveStatus.REQUESTED);
        stockMovesRepository.saveAndFlush(approvedMove);
        this.mockMvc.perform(post("/moves/{id}/approve", approvedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REALIZED.name())));


        StockMove rejectedMove = createAndSaveDetachedStockMove(null, shed, product);
        rejectedMove.setStatus(MoveStatus.REQUESTED);
        stockMovesRepository.saveAndFlush(rejectedMove);
        this.mockMvc.perform(post("/moves/{id}/reject", rejectedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.REJECTED.name())));


        Product canceledProduct = createAndSaveProduct();
        StockMove canceledMove = createAndSaveDetachedStockMove(null, shed, canceledProduct);
        canceledMove.setStatus(MoveStatus.REQUESTED);
        stockMovesRepository.saveAndFlush(canceledMove);
        this.mockMvc.perform(delete("/moves/{id}", canceledMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(MoveStatus.CANCELED.name())));


        // approve in batch
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());
        Product approvedInBatchProduct = createAndSaveProduct();

        StockMove approvedEntryMove = createAndSaveDetachedStockMove(null, shed, approvedInBatchProduct);

        StockMove approvedBetweenMove = createAndSaveDetachedStockMove(shed, technician, approvedInBatchProduct);
        approvedBetweenMove.getFrom().setBlockedQuantity(approvedBetweenMove.getQuantity());
        stockItemRepository.saveAndFlush(approvedBetweenMove.getFrom());

        StockMove approvedOutgoingMove = createAndSaveDetachedStockMove(technician, null, approvedInBatchProduct);
        approvedOutgoingMove.getFrom().setBlockedQuantity(approvedOutgoingMove.getQuantity());
        stockItemRepository.saveAndFlush(approvedOutgoingMove.getFrom());

        List<StockMove> approvedMoves = Arrays.asList(approvedBetweenMove, approvedOutgoingMove, approvedEntryMove);

        this.mockMvc.perform(post("/moves/batch/approve")
                .content(objectMapper.writeValueAsString(createEvaluateMovesInBatchRequest(approvedMoves)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // reject in batch
        Product rejectedInBatchProduct = createAndSaveProduct();

        StockMove rejectedEntryMove = createAndSaveDetachedStockMove(null, shed, rejectedInBatchProduct);

        StockMove rejectedBetweenMove = createAndSaveDetachedStockMove(shed, technician, rejectedInBatchProduct);
        rejectedBetweenMove.getFrom().setBlockedQuantity(rejectedBetweenMove.getQuantity());
        stockItemRepository.saveAndFlush(rejectedBetweenMove.getFrom());

        StockMove rejectedOutgoingMove = createAndSaveDetachedStockMove(technician, null, rejectedInBatchProduct);
        rejectedOutgoingMove.getFrom().setBlockedQuantity(rejectedOutgoingMove.getQuantity());
        stockItemRepository.saveAndFlush(rejectedOutgoingMove.getFrom());

        List<StockMove> rejectedMoves = Arrays.asList(rejectedBetweenMove, rejectedOutgoingMove, rejectedEntryMove);

        this.mockMvc.perform(post("/moves/batch/reject")
                .content(objectMapper.writeValueAsString(createEvaluateMovesInBatchRequest(rejectedMoves)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @Transactional
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock shed = office.shed().orElseThrow();
        Stock technician = createAndSaveTechnicianStock(office);
        Product product = createAndSaveProduct();

        StockMove entryMove = createAndSaveDetachedStockMove(null, shed, product);
        entryMove.setStatus(MoveStatus.REALIZED);
        stockMovesRepository.save(entryMove);

        StockMove betweenMove = createAndSaveDetachedStockMove(shed, technician, product);
        betweenMove.setStatus(MoveStatus.REALIZED);
        stockMovesRepository.save(betweenMove);

        StockMove outMove = createAndSaveDetachedStockMove(technician, null, product);
        outMove.setStatus(MoveStatus.REALIZED);
        stockMovesRepository.save(outMove);

        createAndSaveDetachedStockMove(shed, technician, product);
        createAndSaveDetachedStockMove(technician, null, product);

        this.mockMvc.perform(get("/moves")
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves/realized")
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves/pending")
                .param("type", MoveType.BETWEEN_STOCKS.name())
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves/from/{stock}/pending", shed.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves/from/city/{city}/pending", CityOptions.IPATINGA_HORTO.name())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves/to/city/{city}/pending", CityOptions.IPATINGA_HORTO.name())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        this.mockMvc.perform(get("/moves/from/pending")
                .param("page", "0")
                .param("pageSize", "5")
                .param("types", StockType.SHED.name(), StockType.MAINTENANCE.name())
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        this.mockMvc.perform(get("/moves/to/{stock}/pending", technician.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves/to/pending")
                .param("page", "0")
                .param("pageSize", "5")
                .param("types", StockType.SHED.name(), StockType.MAINTENANCE.name())
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves/{id}", betweenMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        Stock customer = office.customer().orElseThrow();
        for (int i = 0; i < 1; i++) {
            product = createAndSaveProduct();
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
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves")
                .param("advanced", "")
                .param("page", "0")
                .param("pageSize", "5")
                .param("search",
                        "type:BETWEEN_STOCKS",
                        ("createdDate>" + LocalDate.now().toString()),
                        ("lastModifiedDate>" + LocalDate.now().toString()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @Transactional
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInsertMoveRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        Stock shed = createAndSaveShedStock();
        Product product = createAndSaveProduct();

        StockMove approvedMove = createAndSaveDetachedStockMove(null, shed, product);
        approvedMove.setStatus(MoveStatus.REQUESTED);
        stockMovesRepository.saveAndFlush(approvedMove);
        this.mockMvc.perform(post("/moves/{id}/approve", approvedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        StockMove rejectedMove = createAndSaveDetachedStockMove(null, shed, product);
        rejectedMove.setStatus(MoveStatus.REQUESTED);
        stockMovesRepository.saveAndFlush(rejectedMove);
        this.mockMvc.perform(post("/moves/{id}/reject", rejectedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        Product canceledProduct = createAndSaveProduct();
        StockMove canceledMove = createAndSaveDetachedStockMove(null, shed, canceledProduct);
        canceledMove.setStatus(MoveStatus.REQUESTED);
        stockMovesRepository.saveAndFlush(canceledMove);
        this.mockMvc.perform(delete("/moves/{id}", canceledMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        // approve in batch
        Stock technician = createAndSaveTechnicianStock();
        Product approvedInBatchProduct = createAndSaveProduct();

        StockMove approvedEntryMove = createAndSaveDetachedStockMove(null, shed, approvedInBatchProduct);

        StockMove approvedBetweenMove = createAndSaveDetachedStockMove(shed, technician, approvedInBatchProduct);
        approvedBetweenMove.getFrom().setBlockedQuantity(approvedBetweenMove.getQuantity());
        stockItemRepository.saveAndFlush(approvedBetweenMove.getFrom());

        StockMove approvedOutgoingMove = createAndSaveDetachedStockMove(technician, null, approvedInBatchProduct);
        approvedOutgoingMove.getFrom().setBlockedQuantity(approvedOutgoingMove.getQuantity());
        stockItemRepository.saveAndFlush(approvedOutgoingMove.getFrom());

        List<StockMove> approvedMoves = Arrays.asList(approvedBetweenMove, approvedOutgoingMove, approvedEntryMove);

        this.mockMvc.perform(post("/moves/batch/approve")
                .content(objectMapper.writeValueAsString(createEvaluateMovesInBatchRequest(approvedMoves)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        // reject in batch
        Product rejectedInBatchProduct = createAndSaveProduct();

        StockMove rejectedEntryMove = createAndSaveDetachedStockMove(null, shed, rejectedInBatchProduct);

        StockMove rejectedBetweenMove = createAndSaveDetachedStockMove(shed, technician, rejectedInBatchProduct);
        rejectedBetweenMove.getFrom().setBlockedQuantity(rejectedBetweenMove.getQuantity());
        stockItemRepository.saveAndFlush(rejectedBetweenMove.getFrom());

        StockMove rejectedOutgoingMove = createAndSaveDetachedStockMove(technician, null, rejectedInBatchProduct);
        rejectedOutgoingMove.getFrom().setBlockedQuantity(rejectedOutgoingMove.getQuantity());
        stockItemRepository.saveAndFlush(rejectedOutgoingMove.getFrom());

        List<StockMove> rejectedMoves = Arrays.asList(rejectedBetweenMove, rejectedOutgoingMove, rejectedEntryMove);

        this.mockMvc.perform(post("/moves/batch/reject")
                .content(objectMapper.writeValueAsString(createEvaluateMovesInBatchRequest(rejectedMoves)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @RoleTestStockMovesRead
    @Transactional
    public void basicReadAuthorized() throws Exception {
        Stock shed = createAndSaveShedStock();
        TechnicianStock technician = createAndSaveTechnicianStock();

        StockMove move = null;
        for (int i = 0; i < 4; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            move = createAndSaveDetachedStockMove(shed, technician, product);
            if (i >= 2) {
                move.setStatus(MoveStatus.REALIZED);
                stockMovesRepository.saveAndFlush(move);
            }

        }

        this.mockMvc.perform(get("/basic/moves/technicians/{userId}", technician.getTechnician().getUserId())
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/basic/moves/technicians/{userId}/realized", technician.getTechnician().getUserId())
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/basic/moves/from/technicians/{userId}/pending", technician.getTechnician().getUserId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/basic/moves/to/technicians/{userId}/pending", technician.getTechnician().getUserId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/basic/moves/{id}", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());


        Stock customerStock = createAndSaveCustomerStock();
        Product product = createAndSaveProduct();
        TechnicianStock technicianStock = createAndSaveTechnicianStock();

        String orderId = UUID.randomUUID().toString().substring(0, 6);
        String customerName = "Lucas Henrique";

        TechnicianStockMove serviceOrderMove = createAndSaveTechnicianStockMove(technicianStock, customerStock, product);
        serviceOrderMove.setOrderId(orderId);
        serviceOrderMove.setOrderType(ExternalOrderType.INSTALLATION);
        serviceOrderMove.setCustomerName(customerName);
        serviceOrderMove.setReason(MoveReason.SERVICE_ORDER);
        stockMovesRepository.saveAndFlush(serviceOrderMove);

        this.mockMvc.perform(get("/basic/moves/service-orders/{orderId}/pending", orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)));


        TechnicianStockMove serviceOrderMoveWithActivation = createAndSaveTechnicianStockMove(technicianStock, customerStock, product);
        serviceOrderMoveWithActivation.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        serviceOrderMoveWithActivation.setActivationId(UUID.randomUUID().toString().substring(0, 8));
        serviceOrderMoveWithActivation.setOrderType(ExternalOrderType.INSTALLATION);
        serviceOrderMoveWithActivation.setCustomerName(customerName);
        serviceOrderMoveWithActivation.setReason(MoveReason.SERVICE_ORDER);
        stockMovesRepository.saveAndFlush(serviceOrderMove);

        this.mockMvc.perform(get("/basic/moves/service-orders/{orderId}/pending", serviceOrderMoveWithActivation.getOrderId())
                .param("activation", serviceOrderMoveWithActivation.getActivationId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)));

        this.mockMvc.perform(get("/basic/moves/service-orders/{orderId}/pending", serviceOrderMoveWithActivation.getOrderId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)));
    }

    @RoleTestAdmin
    @Transactional
    public void basicReadUnauthorized() throws Exception {
        Stock shed = createAndSaveShedStock();
        TechnicianStock technician = createAndSaveTechnicianStock();

        StockMove move = null;
        for (int i = 0; i < 4; i++) {
            Product product = createAndSaveProduct();
            createAndSaveStockItem(shed, product);
            move = createAndSaveDetachedStockMove(shed, technician, product);
            if (i >= 2) {
                move.setStatus(MoveStatus.REALIZED);
                stockMovesRepository.saveAndFlush(move);
            }

        }

        this.mockMvc.perform(get("/basic/moves/technicians/{userId}", technician.getTechnician().getUserId())
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/basic/moves/technicians/{userId}/realized", technician.getTechnician().getUserId())
                .param("description", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/basic/moves/from/technicians/{userId}/pending", technician.getTechnician().getUserId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/basic/moves/to/technicians/{userId}/pending", technician.getTechnician().getUserId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/basic/moves/{id}", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());



        Stock customerStock = createAndSaveCustomerStock();
        Product product = createAndSaveProduct();
        TechnicianStock technicianStock = createAndSaveTechnicianStock();

        String orderId = UUID.randomUUID().toString().substring(0, 6);
        String customerName = "Lucas Henrique";

        TechnicianStockMove serviceOrderMove = createAndSaveTechnicianStockMove(technicianStock, customerStock, product);
        serviceOrderMove.setOrderId(orderId);
        serviceOrderMove.setOrderType(ExternalOrderType.INSTALLATION);
        serviceOrderMove.setCustomerName(customerName);
        serviceOrderMove.setReason(MoveReason.SERVICE_ORDER);
        stockMovesRepository.saveAndFlush(serviceOrderMove);

        this.mockMvc.perform(get("/basic/moves/service-orders/{orderId}/pending", orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        TechnicianStockMove serviceOrderMoveWithActivation = createAndSaveTechnicianStockMove(technicianStock, customerStock, product);
        serviceOrderMoveWithActivation.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        serviceOrderMoveWithActivation.setActivationId(UUID.randomUUID().toString().substring(0, 8));
        serviceOrderMoveWithActivation.setOrderType(ExternalOrderType.INSTALLATION);
        serviceOrderMoveWithActivation.setCustomerName(customerName);
        serviceOrderMoveWithActivation.setReason(MoveReason.SERVICE_ORDER);
        stockMovesRepository.saveAndFlush(serviceOrderMove);

        this.mockMvc.perform(get("/basic/moves/service-orders/{orderId}/pending", serviceOrderMoveWithActivation.getOrderId())
                .param("activation", serviceOrderMoveWithActivation.getActivationId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/basic/moves/service-orders/{orderId}/pending", serviceOrderMoveWithActivation.getOrderId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicWriteAuthorized() throws Exception {
        BranchOffice branchOffice = createAndSaveBranchOffice();

        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(
                employeeRepository.save(e),
                branchOffice);

        this.mockMvc.perform(post("/basic/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createBasicInsertMoveRequest(technicianStock)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        Stock shed = branchOffice.shed().orElseThrow();

        StockMove rejectedMove = createAndSaveTechnicianStockMove(technicianStock, shed, createAndSaveProduct(), e);
        rejectedMove.getFrom().setBlockedQuantity(rejectedMove.getQuantity()); // set blocked quantity
        stockItemRepository.saveAndFlush(rejectedMove.getFrom());
        this.mockMvc.perform(post("/basic/moves/{id}/reject", rejectedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        StockMove approvedMove = createAndSaveTechnicianStockMove(technicianStock, shed, createAndSaveProduct(), e);
        approvedMove.getFrom().setBlockedQuantity(approvedMove.getQuantity()); // set blocked quantity
        stockItemRepository.saveAndFlush(approvedMove.getFrom());
        this.mockMvc.perform(post("/basic/moves/{id}/approve", approvedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());


        Stock customerStock = branchOffice.customer().orElseThrow();
        Product product = createAndSaveProduct();
        TechnicianStockMove approveServiceOrderMove = createAndSaveTechnicianStockMove(technicianStock, customerStock, product);
        approveServiceOrderMove.getFrom().setBlockedQuantity(approveServiceOrderMove.getQuantity());
        approveServiceOrderMove.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        approveServiceOrderMove.setOrderType(ExternalOrderType.INSTALLATION);
        approveServiceOrderMove.setCustomerName("Lucas Henrique");
        approveServiceOrderMove.setReason(MoveReason.SERVICE_ORDER);
        stockMovesRepository.saveAndFlush(approveServiceOrderMove);

        this.mockMvc.perform(post("/basic/moves/service-orders/{orderId}/approve", approveServiceOrderMove.getOrderId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());


        product = createAndSaveProduct();
        TechnicianStockMove rejectServiceOrderMove = createAndSaveTechnicianStockMove(technicianStock, customerStock, product);
        rejectServiceOrderMove.getFrom().setBlockedQuantity(rejectServiceOrderMove.getQuantity());
        rejectServiceOrderMove.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        rejectServiceOrderMove.setOrderType(ExternalOrderType.INSTALLATION);
        rejectServiceOrderMove.setCustomerName("Lucas Henrique");
        rejectServiceOrderMove.setReason(MoveReason.SERVICE_ORDER);
        stockMovesRepository.saveAndFlush(rejectServiceOrderMove);

        this.mockMvc.perform(post("/basic/moves/service-orders/{orderId}/reject", rejectServiceOrderMove.getOrderId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN"})
    public void basicWriteUnauthorized() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));

        this.mockMvc.perform(post("/basic/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createBasicInsertMoveRequest(technicianStock)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        Stock shed = technicianStock.getBranchOffice().shed().orElseThrow();

        StockMove rejectedMove = createAndSaveTechnicianStockMove(technicianStock, shed, createAndSaveProduct(), e);
        this.mockMvc.perform(post("/basic/moves/{id}/reject", rejectedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        StockMove approvedMove = createAndSaveTechnicianStockMove(technicianStock, shed, createAndSaveProduct(), e);
        this.mockMvc.perform(post("/basic/moves/{id}/approve", approvedMove.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Transactional
    @RoleTestRoot
    public void invalidInsertEntries() throws Exception {
        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.ENTRY_MOVE_NO_ITEMS)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.ENTRY_MOVE_VALID_FROM_BUT_INVALID_TO)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.ENTRY_MOVE_INVALID_TO)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.ENTRY_MOVE_INVALID_PRODUCT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

    }

    @Transactional
    @RoleTestRoot
    public void invalidInsertOutgoing() throws Exception {
        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.OUT_MOVE_NO_ITEMS)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.OUT_MOVE_VALID_TO_BUT_INVALID_FROM)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.OUT_MOVE_INVALID_FROM)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.OUT_MOVE_INVALID_PRODUCT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.OUT_MOVE_INVALID_QUANTITY)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

    }

    @Transactional
    @RoleTestRoot
    public void invalidInsertBetween() throws Exception {
        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_NO_ITEMS)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_VALID_FROM_BUT_INVALID_TO)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_VALID_TO_BUT_INVALID_FROM)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_INVALID_PRODUCT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_INVALID_QUANTITY)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createInvalidInsertMoveRequest(Implementation.BETWEEN_MOVE_FROM_EQUALS_TO)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicInvalidInsertEntries() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));

        this.mockMvc.perform(post("/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.ENTRY_MOVE_NO_ITEMS)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.ENTRY_MOVE_INVALID_TO)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.ENTRY_MOVE_INVALID_PRODUCT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicInvalidInsertOutgoing() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));

        this.mockMvc.perform(post("/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.OUT_MOVE_NO_ITEMS)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());


        this.mockMvc.perform(post("/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.OUT_MOVE_INVALID_PRODUCT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.OUT_MOVE_INVALID_QUANTITY)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicInvalidInsertBetween() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));

        this.mockMvc.perform(post("/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.BETWEEN_MOVE_NO_ITEMS)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());


        this.mockMvc.perform(post("/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.BETWEEN_MOVE_VALID_FROM_BUT_INVALID_TO)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.BETWEEN_MOVE_INVALID_PRODUCT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.BETWEEN_MOVE_INVALID_QUANTITY)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.BETWEEN_MOVE_FROM_EQUALS_TO)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.BETWEEN_MOVE_NO_STOCK)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        // VALID REQUEST BUT FROM IS NULL
        this.mockMvc.perform(post("/moves")
                .content(objectMapper.writeValueAsString(createBasicInsertMoveRequest(technicianStock)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        // VALID REQUEST BUT FROM IS INVALID
        this.mockMvc.perform(post("/moves")
                .header("User-Id", "ABCDQWERTY")
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertMoveRequest(technicianStock, Implementation.BETWEEN_MOVE_NO_STOCK)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicInsertFromServiceOrder() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));
        createAndSavePatrimonyLocation(technicianStock.getTechnician().getUserId());
        Stock customerStock = createAndSaveCustomerStock();

        this.mockMvc.perform(post("/basic/moves/service-orders")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createBasicInsertMoveFromServiceOrderRequest(technicianStock, customerStock)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN"})
    public void basicUnauthorizedInsertFromServiceOrder() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));
        createAndSavePatrimonyLocation(technicianStock.getTechnician().getUserId());
        Stock customerStock = createAndSaveCustomerStock();

        this.mockMvc.perform(post("/basic/moves/service-orders")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(createBasicInsertMoveFromServiceOrderRequest(technicianStock, customerStock)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void insertTechnicianMoveValidAndInvalidFlux() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));

        // set stock item properties
        Product product = createAndSaveProduct();
        StockItem item = createAndSaveStockItem(technicianStock, product);
        item.setQuantity(5d);
        stockItemRepository.saveAndFlush(item);

        // create request
        BasicInsertMoveRequest request = new BasicInsertMoveRequest();
        request.setTo(technicianStock.getBranchOffice().shed().orElseThrow().getId());
        request.setType(MoveType.BETWEEN_STOCKS);
        request.setItems(new ArrayList<>());

        InsertItemMoveRequest itemRequest = new InsertItemMoveRequest();
        itemRequest.setProduct(product.getId());
        itemRequest.setQuantity(4d);
        request.getItems().add(itemRequest);

        // send -> this one is valid
        this.mockMvc.perform(post("/basic/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].status", Matchers.equalTo(MoveStatus.REQUESTED.name())));


        // create another request
        BasicInsertMoveRequest request2 = new BasicInsertMoveRequest();
        request2.setTo(technicianStock.getBranchOffice().shed().orElseThrow().getId());
        request2.setType(MoveType.BETWEEN_STOCKS);
        request2.setItems(new ArrayList<>());

        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();
        item2Request.setProduct(product.getId());
        item2Request.setQuantity(2d);
        request2.getItems().add(item2Request);

        // send -> this one is invalid
        this.mockMvc.perform(post("/basic/moves")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(request2))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.not(Matchers.nullValue())));

    }

    /**
     * Invalid insert: product quantity is smaller than patrimonies quantity
     *
     * @throws Exception
     */
    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_ROOT", "ROLE_MOVES_WRITE_BETWEEN_STOCKS"})
    public void basicInvalidInsertFromServiceOrder() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"));
        TechnicianStock technicianStock = createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));
        createAndSavePatrimonyLocation(technicianStock.getTechnician().getUserId());
        createAndSaveCustomerStock();

        Product product = createAndSaveProduct();
        createAndSaveStockItem(technicianStock, product);

        BasicInsertMoveFromServiceOrderRequest request = new BasicInsertMoveFromServiceOrderRequest();
        request.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        request.setOrderType("R");
        request.setCustomerName("Lucas Henrique");
        request.setCustomerId(UUID.randomUUID().toString().substring(0, 6));
        request.setEntryItems(new ArrayList<>());

        BasicInsertItemMoveFromServiceOrderRequest itemRequest = new BasicInsertItemMoveFromServiceOrderRequest();
        itemRequest.setProduct(product.getId());
        itemRequest.setQuantity(1d);
        itemRequest.setPatrimonies(Set.of(
                createAndSavePatrimony().getCode(),
                createAndSavePatrimony().getCode()));
        request.getEntryItems().add(itemRequest);

        this.mockMvc.perform(post("/basic/moves/service-orders")
                .header("User-Id", technicianStock.getTechnician().getUserId())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    private EvaluateMovesInBatchRequest createEvaluateMovesInBatchRequest(List<StockMove> moves) {
        EvaluateMovesInBatchRequest request = new EvaluateMovesInBatchRequest();
        request.setMoves(moves.stream()
                .map(StockMove::getId)
                .collect(Collectors.toList()));
        return request;
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

    private InsertMoveRequest createInsertMoveRequest() {
        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

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

    private BasicInsertMoveFromServiceOrderRequest createBasicInsertMoveFromServiceOrderRequest(TechnicianStock technician, Stock customerStock) {
        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();
        Product p3 = createAndSaveProduct();

        createAndSaveStockItem(technician, p1);
        createAndSaveStockItem(technician, p2);
        createAndSaveStockItem(technician, p3);

        createAndSaveStockItem(customerStock, p1);
        createAndSaveStockItem(customerStock, p2);
        createAndSaveStockItem(customerStock, p3);

        BasicInsertMoveFromServiceOrderRequest request = new BasicInsertMoveFromServiceOrderRequest();
        request.setOrderId(UUID.randomUUID().toString().substring(0, 6));
        request.setOrderType("R");
        request.setCustomerName("Lucas Henrique");
        request.setCustomerId(UUID.randomUUID().toString().substring(0, 6));
        request.setEntryItems(new ArrayList<>());
        request.setOutgoingItems(new ArrayList<>());

        BasicInsertItemMoveFromServiceOrderRequest item1Request = new BasicInsertItemMoveFromServiceOrderRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        request.getEntryItems().add(item1Request);

        BasicInsertItemMoveFromServiceOrderRequest item2Request = new BasicInsertItemMoveFromServiceOrderRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        String item2RequestCode = createAndSavePatrimony().getCode();
        item2Request.setPatrimonies(Collections.singleton(item2RequestCode));
        createAndSaveValidMacAddress(item2RequestCode);
        request.getEntryItems().add(item2Request);

        BasicInsertItemMoveFromServiceOrderRequest item3Request = new BasicInsertItemMoveFromServiceOrderRequest();
        item3Request.setProduct(p3.getId());
        item3Request.setQuantity(1d);
        String item3RequestCode = UUID.randomUUID().toString().substring(0, 6);
        item3Request.setPatrimonies(Collections.singleton(item3RequestCode));
        createAndSaveValidMacAddress(item3RequestCode);
        request.getEntryItems().add(item3Request);

        BasicInsertItemMoveFromServiceOrderRequest item4Request = new BasicInsertItemMoveFromServiceOrderRequest();
        item4Request.setProduct(p1.getId());
        item4Request.setQuantity(1d);
        request.getOutgoingItems().add(item4Request);

        BasicInsertItemMoveFromServiceOrderRequest item5Request = new BasicInsertItemMoveFromServiceOrderRequest();
        item5Request.setProduct(p2.getId());
        item5Request.setQuantity(1d);
        String item5RequestCode = createAndSavePatrimony().getCode();
        item5Request.setPatrimonies(Collections.singleton(item5RequestCode));
        createAndSaveValidMacAddress(item5RequestCode);
        request.getOutgoingItems().add(item2Request);

        BasicInsertItemMoveFromServiceOrderRequest item6Request = new BasicInsertItemMoveFromServiceOrderRequest();
        item6Request.setProduct(p3.getId());
        item6Request.setQuantity(1d);
        String item6RequestCode = UUID.randomUUID().toString().substring(0, 6);
        item6Request.setPatrimonies(Collections.singleton(item6RequestCode));
        createAndSaveValidMacAddress(item6RequestCode);
        request.getOutgoingItems().add(item6Request);

        return request;
    }

    private InsertMoveRequest createInvalidInsertMoveRequest(Implementation impl) {
        InsertMoveRequest request = new InsertMoveRequest();
        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();

        Stock shed = createAndSaveShedStock();
        Stock technician = createAndSaveTechnicianStock(shed.getBranchOffice());

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(shed, p1);
        createAndSaveStockItem(shed, p2);

        if (impl == Implementation.ENTRY_MOVE_NO_ITEMS) {
            request.setTo(technician.getId());
            request.setType(MoveType.ENTRY_ITEM);
            request.setItems(new ArrayList<>());
            request.setNote("Observação da movimentação");

        } else if (impl == Implementation.ENTRY_MOVE_VALID_FROM_BUT_INVALID_TO) {
            request.setFrom(shed.getId());
            request.setType(MoveType.ENTRY_ITEM);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");

        } else if (impl == Implementation.ENTRY_MOVE_INVALID_TO) {
            request.setTo(-1L);
            request.setType(MoveType.ENTRY_ITEM);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");

        } else if (impl == Implementation.ENTRY_MOVE_INVALID_PRODUCT) {
            request.setTo(technician.getId());
            request.setType(MoveType.ENTRY_ITEM);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId() + "00a0a");
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");

        } else if (impl == Implementation.OUT_MOVE_NO_ITEMS) {
            request.setFrom(shed.getId());
            request.setType(MoveType.OUT_ITEM);
            request.setItems(new ArrayList<>());
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");
        } else if (impl == Implementation.OUT_MOVE_VALID_TO_BUT_INVALID_FROM) {
            request.setTo(shed.getId());
            request.setType(MoveType.OUT_ITEM);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");
        } else if (impl == Implementation.OUT_MOVE_INVALID_FROM) {
            request.setFrom(-1L);
            request.setType(MoveType.OUT_ITEM);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");
        } else if (impl == Implementation.OUT_MOVE_INVALID_PRODUCT) {
            request.setFrom(shed.getId());
            request.setType(MoveType.OUT_ITEM);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId() + "0a0a0a0a");
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");
        } else if (impl == Implementation.OUT_MOVE_INVALID_QUANTITY) {
            request.setFrom(shed.getId());
            request.setType(MoveType.OUT_ITEM);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1000d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(10d);
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");
        } else if (impl == Implementation.BETWEEN_MOVE_NO_ITEMS) {
            request.setFrom(shed.getId());
            request.setTo(technician.getId());
            request.setType(MoveType.BETWEEN_STOCKS);
            request.setItems(new ArrayList<>());

        } else if (impl == Implementation.BETWEEN_MOVE_VALID_FROM_BUT_INVALID_TO) {
            request.setFrom(shed.getId());
            request.setTo(-1L);
            request.setType(MoveType.BETWEEN_STOCKS);
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
            request.setType(MoveType.BETWEEN_STOCKS);
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
            request.setType(MoveType.BETWEEN_STOCKS);
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
            request.setType(MoveType.BETWEEN_STOCKS);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(100d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);

        } else if (impl == Implementation.BETWEEN_MOVE_FROM_EQUALS_TO) {
            request.setFrom(shed.getId());
            request.setTo(shed.getId());
            request.setType(MoveType.BETWEEN_STOCKS);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);
        } else if (impl == Implementation.BETWEEN_MOVE_NO_STOCK) {
            request.setType(MoveType.BETWEEN_STOCKS);
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

    private BasicInsertMoveRequest createInvalidBasicInsertMoveRequest(TechnicianStock technician, Implementation impl) {
        BasicInsertMoveRequest request = new BasicInsertMoveRequest();
        InsertItemMoveRequest item1Request = new InsertItemMoveRequest();
        InsertItemMoveRequest item2Request = new InsertItemMoveRequest();

        Stock shed = technician.getBranchOffice().shed().orElseThrow();

        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        createAndSaveStockItem(technician, p1);
        createAndSaveStockItem(technician, p2);

        if (impl == Implementation.ENTRY_MOVE_NO_ITEMS) {
            request.setTo(shed.getId());
            request.setType(MoveType.ENTRY_ITEM);
            request.setItems(new ArrayList<>());
            request.setNote("Observação da movimentação");

        } else if (impl == Implementation.ENTRY_MOVE_INVALID_TO) {
            request.setTo(-1L);
            request.setType(MoveType.ENTRY_ITEM);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");

        } else if (impl == Implementation.ENTRY_MOVE_INVALID_PRODUCT) {
            request.setTo(shed.getId());
            request.setType(MoveType.ENTRY_ITEM);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId() + "00a0a");
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");

        } else if (impl == Implementation.OUT_MOVE_NO_ITEMS) {
            request.setType(MoveType.OUT_ITEM);
            request.setItems(new ArrayList<>());
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");
        } else if (impl == Implementation.OUT_MOVE_INVALID_PRODUCT) {
            request.setType(MoveType.OUT_ITEM);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId() + "0a0a0a0a");
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");
        } else if (impl == Implementation.OUT_MOVE_INVALID_QUANTITY) {
            request.setType(MoveType.OUT_ITEM);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1000d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(10d);
            request.getItems().add(item2Request);

            request.setNote("Observação da movimentação");
        } else if (impl == Implementation.BETWEEN_MOVE_NO_ITEMS) {
            request.setTo(shed.getId());
            request.setType(MoveType.BETWEEN_STOCKS);
            request.setItems(new ArrayList<>());

        } else if (impl == Implementation.BETWEEN_MOVE_VALID_FROM_BUT_INVALID_TO) {
            request.setTo(-1L);
            request.setType(MoveType.BETWEEN_STOCKS);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);
        } else if (impl == Implementation.BETWEEN_MOVE_INVALID_PRODUCT) {
            request.setTo(shed.getId());
            request.setType(MoveType.BETWEEN_STOCKS);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId() + "0a0aa");
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);
        } else if (impl == Implementation.BETWEEN_MOVE_INVALID_QUANTITY) {
            request.setTo(shed.getId());
            request.setType(MoveType.BETWEEN_STOCKS);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(100d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);

        } else if (impl == Implementation.BETWEEN_MOVE_FROM_EQUALS_TO) {
            request.setTo(technician.getId());
            request.setType(MoveType.BETWEEN_STOCKS);
            request.setItems(new ArrayList<>());

            item1Request.setProduct(p1.getId());
            item1Request.setQuantity(1d);
            request.getItems().add(item1Request);

            item2Request.setProduct(p2.getId());
            item2Request.setQuantity(1d);
            request.getItems().add(item2Request);
        } else if (impl == Implementation.BETWEEN_MOVE_NO_STOCK) {
            request.setType(MoveType.BETWEEN_STOCKS);
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
        ENTRY_MOVE_NO_ITEMS,
        ENTRY_MOVE_VALID_FROM_BUT_INVALID_TO,
        ENTRY_MOVE_INVALID_TO,
        ENTRY_MOVE_INVALID_PRODUCT,

        OUT_MOVE_NO_ITEMS,
        OUT_MOVE_VALID_TO_BUT_INVALID_FROM,
        OUT_MOVE_INVALID_FROM,
        OUT_MOVE_INVALID_PRODUCT,
        OUT_MOVE_INVALID_QUANTITY,

        BETWEEN_MOVE_NO_ITEMS,
        BETWEEN_MOVE_VALID_FROM_BUT_INVALID_TO,
        BETWEEN_MOVE_VALID_TO_BUT_INVALID_FROM,
        BETWEEN_MOVE_INVALID_PRODUCT,
        BETWEEN_MOVE_INVALID_QUANTITY,
        BETWEEN_MOVE_FROM_EQUALS_TO,
        BETWEEN_MOVE_NO_STOCK

    }

}
