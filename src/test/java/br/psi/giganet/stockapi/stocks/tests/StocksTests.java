package br.psi.giganet.stockapi.stocks.tests;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.reports.model.ReportFormat;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.stock.controller.request.UpdateStockItemParametersRequest;
import br.psi.giganet.stockapi.stock.controller.request.UpdateStockItemQuantityLevelRequest;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.StockItem;
import br.psi.giganet.stockapi.stock.model.StockItemQuantityLevel;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.model.enums.QuantityLevel;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock.repository.StockItemQuantityLevelRepository;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.stocks.annotations.RoleTestStocksRead;
import br.psi.giganet.stockapi.stocks.annotations.RoleTestStocksWrite;
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
import java.math.BigDecimal;
import java.util.ArrayList;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StocksTests extends BuilderIntegrationTest implements RolesIntegrationTest {

    private final Stock stockTest;

    @Autowired
    public StocksTests(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            StockRepository stockRepository,
            StockItemRepository stockItemRepository,
            StockItemQuantityLevelRepository stockItemQuantityLevelRepository,
            TechnicianRepository technicianRepository,
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
        this.technicianRepository = technicianRepository;
        createCurrentUser();

        stockTest = createAndSaveShedStock();
    }

    @Override
    @RoleTestStocksRead
    @Transactional
    public void readAuthorized() throws Exception {
        Stock stock = createAndSaveTechnicianStock();
        StockItem item1, item2 = null;
        for (int i = 0; i < 3; i++) {
            item1 = createAndSaveStockItem(createAndSaveShedStock());
            item1.setQuantity(4d);
            stockItemRepository.save(item1);

            item2 = createAndSaveStockItem(stock, item1.getProduct());
            item2.setQuantity(2d);
            stockItemRepository.save(item2);

            createAndSaveTechnician();
        }

        this.mockMvc.perform(get("/stocks/{stock}/items", stock.getId())
                .param("name", "")
                .param("code", "")
                .param("filterEmpty", "true")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/stocks/{stock}/items/available", stock.getId())
                .param("name", "")
                .param("code", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/stocks")
                .param("type", StockType.TECHNICIAN.name())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/stocks/technicians")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print());

        this.mockMvc.perform(get("/stocks/general")
                .param("name", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/stocks/{id}", stock.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/stocks/{stock}/items/codes/{code}", stock.getId(), item2.getProduct().getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/stocks/{stock}/items/{id}", stock.getId(), item2.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        StockItem item3 = createAndSaveStockItem(stockTest);
        item3.getLevels().add(new StockItemQuantityLevel(item3, QuantityLevel.NORMAL, 0f, 100f));
        item3.setCurrentLevel(QuantityLevel.NORMAL);
        stockItemRepository.saveAndFlush(item3);
        stockItemQuantityLevelRepository.saveAll(item3.getLevels());

        this.mockMvc.perform(get("/stocks/{stock}/items/{id}", stockTest.getId(), item3.getId())
                .param("withLevels", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/stocks/{stock}/items", stockTest.getId())
                .param("name", "")
                .param("code", "")
                .param("filterEmpty", "true")
                .param("page", "0")
                .param("pageSize", "5")
                .param("withCurrentLevel", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/stocks/reports/current-situation/{id}", stock.getId())
                .param("format", ReportFormat.PDF.name())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_STOCKS_READ"})
    public void basicReadAuthorized() throws Exception {
        TechnicianStock technicianStock = createAndSaveTechnicianStock();
        for (int i = 0; i < 3; i++) {
            StockItem item = createAndSaveStockItem(technicianStock);
            item.setQuantity(10d);
            stockItemRepository.save(item);
        }
        this.mockMvc.perform(get("/basic/stocks/technicians/{userId}/items", technicianStock.getTechnician().getUserId())
                .param("name", "")
                .param("code", "")
                .param("filterEmpty", "true")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/basic/stocks")
                .param("type", StockType.TECHNICIAN.name())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print());


        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_STOCKS_READ"));
        createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));

        for (int i = 0; i < 3; i++) {
            createAndSaveTechnicianStock();
        }

        this.mockMvc.perform(get("/basic/stocks/technicians/available")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Override
    @RoleTestStocksWrite
    @Transactional
    public void writeAuthorized() throws Exception {
        StockItem item = createAndSaveStockItem(stockTest);

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(createUpdateStockItemParametersRequest(item)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    @Transactional
    public void readUnauthorized() throws Exception {
        Stock stock = createAndSaveTechnicianStock();
        StockItem item1, item2 = null;
        for (int i = 0; i < 3; i++) {
            item1 = createAndSaveStockItem(createAndSaveShedStock());
            item1.setQuantity(4d);
            stockItemRepository.save(item1);

            item2 = createAndSaveStockItem(stock, item1.getProduct());
            item2.setQuantity(2d);
            stockItemRepository.save(item2);

            createAndSaveTechnician();
        }

        this.mockMvc.perform(get("/stocks/{stock}/items", stock.getId())
                .param("name", "")
                .param("code", "")
                .param("filterEmpty", "true")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/stocks/{stock}/items/available", stock.getId())
                .param("name", "")
                .param("code", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/stocks")
                .param("type", StockType.TECHNICIAN.name())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/stocks/technicians")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/stocks/general")
                .param("name", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/stocks/{id}", stock.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/stocks/{stock}/items/codes/{code}", stock.getId(), item2.getProduct().getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/stocks/{stock}/items/{id}", stock.getId(), item2.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        StockItem item3 = createAndSaveStockItem(stockTest);
        item3.getLevels().add(new StockItemQuantityLevel(item3, QuantityLevel.NORMAL, 0f, 100f));
        item3.setCurrentLevel(QuantityLevel.NORMAL);
        stockItemRepository.saveAndFlush(item3);
        stockItemQuantityLevelRepository.saveAll(item3.getLevels());

        this.mockMvc.perform(get("/stocks/{stock}/items/{id}", stockTest.getId(), item3.getId())
                .param("withLevels", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/stocks/{stock}/items", stockTest.getId())
                .param("name", "")
                .param("code", "")
                .param("filterEmpty", "true")
                .param("page", "0")
                .param("pageSize", "5")
                .param("withCurrentLevel", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        this.mockMvc.perform(get("/stocks/reports/current-situation/{id}", stock.getId())
                .param("format", ReportFormat.PDF.name())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }


    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN"})
    public void basicReadUnauthorized() throws Exception {
        TechnicianStock technicianStock = createAndSaveTechnicianStock();
        for (int i = 0; i < 3; i++) {
            StockItem item = createAndSaveStockItem(technicianStock);
            item.setQuantity(10d);
            stockItemRepository.save(item);
        }
        this.mockMvc.perform(get("/basic/stocks/technicians/{userId}/items", technicianStock.getTechnician().getUserId())
                .param("name", "")
                .param("code", "")
                .param("filterEmpty", "true")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/basic/stocks")
                .param("type", StockType.TECHNICIAN.name())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        createAndSaveTechnicianStockByEmployee(employeeRepository.save(e));

        for (int i = 0; i < 3; i++) {
            createAndSaveTechnicianStock();
        }

        this.mockMvc.perform(get("/basic/stocks/technicians/available")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    @Transactional
    public void writeUnauthorized() throws Exception {
        StockItem item = createAndSaveStockItem(stockTest);
        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(createUpdateStockItemParametersRequest(item)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @RoleTestRoot
    @Transactional
    public void invalidUpdates() throws Exception {
        StockItem item = createAndSaveStockItem(stockTest);

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(createInvalidUpdateStockItemParametersRequest(item, InvalidUpdateStockParameters.NEGATIVE_MIN_MAX_QUANTITY)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(createInvalidUpdateStockItemParametersRequest(item, InvalidUpdateStockParameters.MIN_QUANTITY_BIGGER_THAN_MAX_QUANTITY)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(createInvalidUpdateStockItemParametersRequest(item, InvalidUpdateStockParameters.NEGATIVE_PRICE_PER_UNIT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(createInvalidUpdateStockItemParametersRequest(item, InvalidUpdateStockParameters.INVALID_LEVELS_REPEATED_LEVEL)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(createInvalidUpdateStockItemParametersRequest(item, InvalidUpdateStockParameters.INVALID_LEVELS_INVALID_TO)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(createInvalidUpdateStockItemParametersRequest(item, InvalidUpdateStockParameters.INVALID_LEVELS_INVALID_FROM)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(createInvalidUpdateStockItemParametersRequest(item, InvalidUpdateStockParameters.INVALID_LEVELS_FROM_BIGGER_THAN_TO)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(createInvalidUpdateStockItemParametersRequest(item, InvalidUpdateStockParameters.NEGATIVE_QUANTITY)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(username = "test_update@test.com", authorities = {"ROLE_ROOT", "ROLE_ADMIN", "ROLE_MOVES_WRITE_ROOT"})
    @Transactional
    public void validUpdateStockItemQuantityByMoveRootUser() throws Exception {
        Employee e = createAndSaveEmployee("test_update@test.com");
        e.getPermissions().add(createAndSavePermission("ROLE_MOVES_WRITE_ROOT"));
        employeeRepository.save(e);

        StockItem item = createAndSaveStockItem(stockTest);

        UpdateStockItemParametersRequest increaseQuantityRequest = new UpdateStockItemParametersRequest();
        increaseQuantityRequest.setId(item.getId());
        increaseQuantityRequest.setMaxQuantity(100000d);
        increaseQuantityRequest.setMinQuantity(0d);
        increaseQuantityRequest.setPricePerUnit(BigDecimal.ONE);
        increaseQuantityRequest.setStock(item.getStock().getId());

        Double increasedQuantity = item.getQuantity() + 100d;
        increaseQuantityRequest.setQuantity(increasedQuantity);

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(increaseQuantityRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        Assert.state(item.getQuantity().equals(increasedQuantity), "Quantidade atual do estoque pós atualização é inválida");


        UpdateStockItemParametersRequest reduceQuantityRequest = new UpdateStockItemParametersRequest();
        reduceQuantityRequest.setId(item.getId());
        reduceQuantityRequest.setMaxQuantity(100000d);
        reduceQuantityRequest.setMinQuantity(0d);
        reduceQuantityRequest.setPricePerUnit(BigDecimal.ONE);
        reduceQuantityRequest.setStock(item.getStock().getId());

        Double reducedQuantity = item.getQuantity() - 100d;
        reduceQuantityRequest.setQuantity(reducedQuantity);

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(reduceQuantityRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        Assert.state(item.getQuantity().equals(reducedQuantity), "Quantidade atual do estoque pós atualização é inválida");
    }

    @Test
    @WithMockUser(username = "test_update@test.com", authorities = {"ROLE_ROOT", "ROLE_ADMIN"})
    @Transactional
    public void validUpdateStockItemQuantityByRootUser() throws Exception {
        createAndSaveEmployee("test_update@test.com");

        StockItem item = createAndSaveStockItem(stockTest);

        UpdateStockItemParametersRequest increaseQuantityRequest = new UpdateStockItemParametersRequest();
        increaseQuantityRequest.setId(item.getId());
        increaseQuantityRequest.setMaxQuantity(100000d);
        increaseQuantityRequest.setMinQuantity(0d);
        increaseQuantityRequest.setPricePerUnit(BigDecimal.ONE);
        increaseQuantityRequest.setStock(item.getStock().getId());

        Double increasedQuantity = item.getQuantity() + 100d;
        increaseQuantityRequest.setQuantity(increasedQuantity);

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(increaseQuantityRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        Assert.state(item.getQuantity().equals(increasedQuantity), "Quantidade atual do estoque pós atualização é inválida");
        Assert.state(item.getAvailableQuantity().equals(increasedQuantity), "Quantidade disponível do estoque pós atualização é inválida");


        UpdateStockItemParametersRequest reduceQuantityRequest = new UpdateStockItemParametersRequest();
        reduceQuantityRequest.setId(item.getId());
        reduceQuantityRequest.setMaxQuantity(100000d);
        reduceQuantityRequest.setMinQuantity(0d);
        reduceQuantityRequest.setPricePerUnit(BigDecimal.ONE);
        reduceQuantityRequest.setStock(item.getStock().getId());

        Double reducedQuantity = item.getQuantity() - 100d;
        reduceQuantityRequest.setQuantity(reducedQuantity);

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(reduceQuantityRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        Assert.state(item.getQuantity().equals(reducedQuantity + 100d), "Quantidade atual do estoque pós atualização é inválida");
        Assert.state(item.getAvailableQuantity().equals(reducedQuantity), "Quantidade disponível do estoque pós atualização é inválida");
    }

    private UpdateStockItemParametersRequest createInvalidUpdateStockItemParametersRequest(StockItem item, InvalidUpdateStockParameters implementation) {
        UpdateStockItemParametersRequest request = new UpdateStockItemParametersRequest();

        if (implementation == InvalidUpdateStockParameters.NEGATIVE_MIN_MAX_QUANTITY) {
            request.setId(item.getId());
            request.setMaxQuantity(0d);
            request.setMinQuantity(-10d);
            request.setPricePerUnit(BigDecimal.ONE);
            request.setStock(item.getStock().getId());

        } else if (implementation == InvalidUpdateStockParameters.MIN_QUANTITY_BIGGER_THAN_MAX_QUANTITY) {
            request.setId(item.getId());
            request.setMaxQuantity(1d);
            request.setMinQuantity(10d);
            request.setPricePerUnit(BigDecimal.ONE);
            request.setStock(item.getStock().getId());

        } else if (implementation == InvalidUpdateStockParameters.NEGATIVE_PRICE_PER_UNIT) {
            request.setId(item.getId());
            request.setMaxQuantity(100000d);
            request.setMinQuantity(0d);
            request.setPricePerUnit(BigDecimal.valueOf(-10));
            request.setStock(item.getStock().getId());
        } else if (implementation == InvalidUpdateStockParameters.INVALID_LEVELS_FROM_BIGGER_THAN_TO) {
            request.setId(item.getId());
            request.setMaxQuantity(100000d);
            request.setMinQuantity(0d);
            request.setPricePerUnit(BigDecimal.ONE);
            request.setStock(item.getStock().getId());
            request.setLevels(new ArrayList<>());

            QuantityLevel[] levels = new QuantityLevel[]{QuantityLevel.VERY_LOW, QuantityLevel.LOW};
            for (int i = 0; i < levels.length; i++) {
                UpdateStockItemQuantityLevelRequest levelRequest = new UpdateStockItemQuantityLevelRequest();
                levelRequest.setFrom((float) (20 * (i + 1)));
                levelRequest.setTo((float) (20 * i));
                levelRequest.setLevel(levels[i]);
                request.getLevels().add(levelRequest);
            }
        } else if (implementation == InvalidUpdateStockParameters.INVALID_LEVELS_INVALID_FROM) {
            request.setId(item.getId());
            request.setMaxQuantity(100000d);
            request.setMinQuantity(0d);
            request.setPricePerUnit(BigDecimal.ONE);
            request.setStock(item.getStock().getId());
            request.setLevels(new ArrayList<>());

            QuantityLevel[] levels = new QuantityLevel[]{QuantityLevel.VERY_LOW, QuantityLevel.LOW};
            for (int i = 0; i < levels.length; i++) {
                UpdateStockItemQuantityLevelRequest levelRequest = new UpdateStockItemQuantityLevelRequest();
                levelRequest.setFrom((float) (100 * (i + 1)));
                levelRequest.setTo((float) (100 * i));
                levelRequest.setLevel(levels[i]);
                request.getLevels().add(levelRequest);
            }
        } else if (implementation == InvalidUpdateStockParameters.INVALID_LEVELS_INVALID_TO) {
            request.setId(item.getId());
            request.setMaxQuantity(100000d);
            request.setMinQuantity(0d);
            request.setPricePerUnit(BigDecimal.ONE);
            request.setStock(item.getStock().getId());
            request.setLevels(new ArrayList<>());

            QuantityLevel[] levels = new QuantityLevel[]{QuantityLevel.VERY_LOW, QuantityLevel.LOW};
            for (int i = 0; i < levels.length; i++) {
                UpdateStockItemQuantityLevelRequest levelRequest = new UpdateStockItemQuantityLevelRequest();
                levelRequest.setFrom((float) (20 * i));
                levelRequest.setTo((float) (-20 * (i + 1)));
                levelRequest.setLevel(levels[i]);
                request.getLevels().add(levelRequest);
            }
        } else if (implementation == InvalidUpdateStockParameters.INVALID_LEVELS_REPEATED_LEVEL) {
            request.setId(item.getId());
            request.setMaxQuantity(100000d);
            request.setMinQuantity(0d);
            request.setPricePerUnit(BigDecimal.ONE);
            request.setStock(item.getStock().getId());
            request.setLevels(new ArrayList<>());

            QuantityLevel[] levels = new QuantityLevel[]{QuantityLevel.VERY_LOW, QuantityLevel.VERY_LOW};
            for (int i = 0; i < levels.length; i++) {
                UpdateStockItemQuantityLevelRequest levelRequest = new UpdateStockItemQuantityLevelRequest();
                levelRequest.setFrom((float) (20 * i));
                levelRequest.setTo((float) (20 * (i + 1)));
                levelRequest.setLevel(levels[i]);
                request.getLevels().add(levelRequest);
            }
        } else if (implementation == InvalidUpdateStockParameters.NEGATIVE_QUANTITY) {
            request.setId(item.getId());
            request.setQuantity(-1d);
            request.setMaxQuantity(100000d);
            request.setMinQuantity(0d);
            request.setPricePerUnit(BigDecimal.ONE);
            request.setStock(item.getStock().getId());
            request.setLevels(new ArrayList<>());

            QuantityLevel[] levels = new QuantityLevel[]{QuantityLevel.VERY_LOW};
            for (int i = 0; i < levels.length; i++) {
                UpdateStockItemQuantityLevelRequest levelRequest = new UpdateStockItemQuantityLevelRequest();
                levelRequest.setFrom((float) (20 * i));
                levelRequest.setTo((float) (20 * (i + 1)));
                levelRequest.setLevel(levels[i]);
                request.getLevels().add(levelRequest);
            }
        }

        return request;
    }

    private UpdateStockItemParametersRequest createUpdateStockItemParametersRequest(StockItem item) {
        UpdateStockItemParametersRequest request = new UpdateStockItemParametersRequest();
        request.setId(item.getId());
        request.setMaxQuantity(100000d);
        request.setMinQuantity(0d);
        request.setPricePerUnit(BigDecimal.ONE);
        request.setStock(item.getStock().getId());

        return request;
    }

    private enum InvalidUpdateStockParameters {
        NEGATIVE_QUANTITY,
        NEGATIVE_MIN_MAX_QUANTITY,
        NEGATIVE_PRICE_PER_UNIT,
        MIN_QUANTITY_BIGGER_THAN_MAX_QUANTITY,

        INVALID_LEVELS_FROM_BIGGER_THAN_TO,
        INVALID_LEVELS_INVALID_FROM,
        INVALID_LEVELS_INVALID_TO,
        INVALID_LEVELS_REPEATED_LEVEL,
    }
}
