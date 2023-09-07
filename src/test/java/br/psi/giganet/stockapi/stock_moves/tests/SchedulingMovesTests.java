package br.psi.giganet.stockapi.stock_moves.tests;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock_moves.annotations.RoleTestStockMovesRead;
import br.psi.giganet.stockapi.stock_moves.annotations.RoleTestStockMovesWriteAll;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.stock_moves.repository.StockMovesRepository;
import br.psi.giganet.stockapi.schedules.controller.request.InsertScheduledItemMoveRequest;
import br.psi.giganet.stockapi.schedules.controller.request.InsertScheduledMoveRequest;
import br.psi.giganet.stockapi.schedules.controller.request.UpdateScheduledItemMoveRequest;
import br.psi.giganet.stockapi.schedules.controller.request.UpdateScheduledMoveRequest;
import br.psi.giganet.stockapi.schedules.model.ScheduledExecution;
import br.psi.giganet.stockapi.schedules.model.ScheduledMove;
import br.psi.giganet.stockapi.schedules.model.ScheduledStatus;
import br.psi.giganet.stockapi.schedules.repository.ScheduledMoveRepository;
import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.patrimonies.repository.PatrimonyRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SchedulingMovesTests extends BuilderIntegrationTest implements RolesIntegrationTest {

    @Autowired
    public SchedulingMovesTests(
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

    @Override
    @RoleTestStockMovesRead
    @Transactional
    public void readAuthorized() throws Exception {
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
                .andExpect(jsonPath("$.content", Matchers.hasSize(3)));

        ScheduledMove move = createAndSaveScheduledMove(shed, technician, products);

        this.mockMvc.perform(get("/moves/schedules/{id}", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/moves/schedules/{id}", move.getId())
                .param("withCurrentQuantity", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestStockMovesWriteAll
    @Transactional
    public void writeAuthorized() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock shed = office.shed().orElseThrow();
        Stock technician = createAndSaveTechnicianStock(office);
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            products.add(createAndSaveProduct());
        }

        ScheduledMove move = createAndSaveScheduledMove(shed, technician, products);
        this.mockMvc.perform(post("/moves/schedules/{id}/execute", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(ScheduledStatus.SUCCESS.name())));

        move = createAndSaveScheduledMove(shed, technician, products);
        this.mockMvc.perform(delete("/moves/schedules/{id}", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(ScheduledStatus.CANCELED.name())));

        this.mockMvc.perform(post("/moves/schedules")
                .content(objectMapper.writeValueAsString(createInsertScheduledMoveRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", Matchers.equalTo(ScheduledStatus.SCHEDULED.name())));

        move = createAndSaveScheduledMove(shed, technician, products);
        this.mockMvc.perform(put("/moves/schedules/{id}", move.getId())
                .content(objectMapper.writeValueAsString(createUpdateScheduledMoveRequest(move)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    @Transactional
    public void readUnauthorized() throws Exception {
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
                .andExpect(status().isForbidden());

        ScheduledMove move = createAndSaveScheduledMove(shed, technician, products);

        this.mockMvc.perform(get("/moves/schedules/{id}", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/moves/schedules/{id}", move.getId())
                .param("withCurrentQuantity", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    @Transactional
    public void writeUnauthorized() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock shed = office.shed().orElseThrow();
        Stock technician = createAndSaveTechnicianStock(office);
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            products.add(createAndSaveProduct());
        }

        ScheduledMove move = createAndSaveScheduledMove(shed, technician, products);
        this.mockMvc.perform(post("/moves/schedules/{id}/execute", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        move = createAndSaveScheduledMove(shed, technician, products);
        this.mockMvc.perform(delete("/moves/schedules/{id}", move.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(post("/moves/schedules")
                .content(objectMapper.writeValueAsString(createInsertScheduledMoveRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        move = createAndSaveScheduledMove(shed, technician, products);
        this.mockMvc.perform(put("/moves/schedules/{id}", move.getId())
                .content(objectMapper.writeValueAsString(createUpdateScheduledMoveRequest(move)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @RoleTestRoot
    @Transactional
    public void cancelMultipleTimesRequest() throws Exception {
        String projection = this.mockMvc.perform(post("/moves/schedules")
                .content(objectMapper.writeValueAsString(createInsertScheduledMoveRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        this.mockMvc.perform(delete("/moves/schedules/{id}", objectMapper.readTree(projection).get("id").asText())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(ScheduledStatus.CANCELED.name())));

        this.mockMvc.perform(delete("/moves/schedules/{id}", objectMapper.readTree(projection).get("id").asText())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

    }

    @RoleTestRoot
    @Transactional
    public void executeAndCancelRequest() throws Exception {
        String projection = this.mockMvc.perform(post("/moves/schedules")
                .content(objectMapper.writeValueAsString(createInsertScheduledMoveRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        this.mockMvc.perform(post("/moves/schedules/{id}/execute", objectMapper.readTree(projection).get("id").asText())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.equalTo(ScheduledStatus.SUCCESS.name())));

        this.mockMvc.perform(delete("/moves/schedules/{id}", objectMapper.readTree(projection).get("id").asText())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

    }

    @RoleTestRoot
    @Transactional
    public void cancelAndExecuteRequest() throws Exception {
        String projection = this.mockMvc.perform(post("/moves/schedules")
                .content(objectMapper.writeValueAsString(createInsertScheduledMoveRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        this.mockMvc.perform(delete("/moves/schedules/{id}", objectMapper.readTree(projection).get("id").asText())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.status", Matchers.equalTo(ScheduledStatus.CANCELED.name())));

        this.mockMvc.perform(post("/moves/schedules/{id}/execute", objectMapper.readTree(projection).get("id").asText())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

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

}
