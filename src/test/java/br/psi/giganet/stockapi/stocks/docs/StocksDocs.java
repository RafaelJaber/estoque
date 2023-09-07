package br.psi.giganet.stockapi.stocks.docs;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
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
import br.psi.giganet.stockapi.stock.model.*;
import br.psi.giganet.stockapi.stock.model.enums.QuantityLevel;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock.repository.StockItemQuantityLevelRepository;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StocksDocs extends BuilderIntegrationTest {

    private final Stock stockTest;

    @Autowired
    public StocksDocs(
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

    @RoleTestRoot
    @Transactional
    public void findAllStockItemsByStock() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock stockTest = createAndSaveTechnicianStock(office);
        for (int i = 0; i < 3; i++) {
            StockItem item = createAndSaveStockItem(stockTest);
            item.setQuantity(10d);
            stockItemRepository.save(item);
        }

        this.mockMvc.perform(get("/stocks/{stock}/items", stockTest.getId())
                .param("name", "")
                .param("code", "")
                .param("filterEmpty", "true")
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(3)))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("stock").description(createDescriptionWithNotNull("Código do estoque solicitado"))
                                ),
                                requestParameters(
                                        parameterWithName("name")
                                                .optional()
                                                .description(createDescription(
                                                        "Nome do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("code")
                                                .optional()
                                                .description(createDescription(
                                                        "Código do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("filterEmpty")
                                                .optional()
                                                .description(createDescription(
                                                        "Informa se deve ser listado apenas os itens cuja quantidade é superior a 0 ou todos os itens",
                                                        "Caso seja true, será retornado apenas os itens com quantidade em estoque superior a 0",
                                                        "Valor default: false")),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()
                                ),
                                getPageContent("Lista com todos os itens presentes no estoque em questão")
                                        .andWithPrefix("content[].",
                                                fieldWithPath("id").description("Código do estoque do item"),
                                                fieldWithPath("product").description("Produto"),
                                                fieldWithPath("quantity").description("Quantidade em estoque"),
                                                fieldWithPath("blockedQuantity").description("Quantidade bloqueada para movimentações"),
                                                fieldWithPath("availableQuantity").description("Quantidade disponível para movimentações"),
                                                fieldWithPath("minQuantity").description("Quantidade mínima do item"),
                                                fieldWithPath("maxQuantity").description("Quantidade máxima do item"),
                                                fieldWithPath("lastPricePerUnit").description("Último preço unitário do item"))
                                        .andWithPrefix("content[].product.",
                                                fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                                                fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("unit").description("Unidade padrão para o produto"),
                                                fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                                        .andWithPrefix("content[].product.unit.",
                                                fieldWithPath("id").description("Código da unidade padrão do produto"),
                                                fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                                                fieldWithPath("name").description("Nome da unidade padrão do produto"))));
    }

    @RoleTestRoot
    @Transactional
    public void findAllAvailableStockItemsByStock() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock stockTest = createAndSaveTechnicianStock(office);
        for (int i = 0; i < 3; i++) {
            StockItem item = createAndSaveStockItem(stockTest);
            item.setQuantity(10d);
            stockItemRepository.save(item);
        }

        this.mockMvc.perform(get("/stocks/{stock}/items/available", stockTest.getId())
                .param("name", "")
                .param("code", "")
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(3)))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("stock").description(createDescriptionWithNotNull("Código do estoque solicitado"))
                                ),
                                requestParameters(
                                        parameterWithName("name")
                                                .optional()
                                                .description(createDescription(
                                                        "Nome do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("code")
                                                .optional()
                                                .description(createDescription(
                                                        "Código do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()
                                ),
                                getPageContent("Lista com todos os itens presentes no estoque em questão")
                                        .andWithPrefix("content[].",
                                                fieldWithPath("id").description("Código do estoque do item"),
                                                fieldWithPath("product").description("Produto"),
                                                fieldWithPath("quantity").description("Quantidade em estoque"),
                                                fieldWithPath("blockedQuantity").description("Quantidade bloqueada para movimentações"),
                                                fieldWithPath("availableQuantity").description("Quantidade disponível para movimentações"),
                                                fieldWithPath("minQuantity").description("Quantidade mínima do item"),
                                                fieldWithPath("maxQuantity").description("Quantidade máxima do item"),
                                                fieldWithPath("lastPricePerUnit").description("Último preço unitário do item"))
                                        .andWithPrefix("content[].product.",
                                                fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                                                fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("unit").description("Unidade padrão para o produto"),
                                                fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                                        .andWithPrefix("content[].product.unit.",
                                                fieldWithPath("id").description("Código da unidade padrão do produto"),
                                                fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                                                fieldWithPath("name").description("Nome da unidade padrão do produto"))));
    }

    @RoleTestRoot
    @Transactional
    public void findAllStockItemsByStockWithCurrentLevel() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock stockTest = createAndSaveTechnicianStock(office);
        for (int i = 0; i < 3; i++) {
            StockItem item = createAndSaveStockItem(stockTest);
            item.setQuantity(10d);
            stockItemRepository.save(item);
        }

        this.mockMvc.perform(get("/stocks/{stock}/items", stockTest.getId())
                .param("name", "")
                .param("code", "")
                .param("filterEmpty", "true")
                .param("page", "0")
                .param("pageSize", "5")
                .param("withCurrentLevel", "")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(3)))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("stock").description(createDescriptionWithNotNull("Código do estoque solicitado"))
                                ),
                                requestParameters(
                                        parameterWithName("name")
                                                .optional()
                                                .description(createDescription(
                                                        "Nome do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("code")
                                                .optional()
                                                .description(createDescription(
                                                        "Código do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("filterEmpty")
                                                .optional()
                                                .description(createDescription(
                                                        "Informa se deve ser listado apenas os itens cuja quantidade é superior a 0 ou todos os itens",
                                                        "Caso seja true, será retornado apenas os itens com quantidade em estoque superior a 0",
                                                        "Valor default: false")),
                                        parameterWithName("withCurrentLevel")
                                                .description(createDescription(
                                                        "Flag a qual identifica o tipo do retorno",
                                                        "Este método irá retornar também o nível atual do estoque requerido",
                                                        "O valor da variável é desconsiderado, entretanto, é necessário que ela seja informada")),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()
                                ),
                                getPageContent("Lista com todos os itens presentes no estoque em questão")
                                        .andWithPrefix("content[].",
                                                fieldWithPath("id").description("Código do estoque do item"),
                                                fieldWithPath("product").description("Produto"),
                                                fieldWithPath("quantity").description("Quantidade em estoque"),
                                                fieldWithPath("blockedQuantity").description("Quantidade bloqueada para movimentações"),
                                                fieldWithPath("availableQuantity").description("Quantidade disponível para movimentações"),
                                                fieldWithPath("minQuantity").description("Quantidade mínima do item"),
                                                fieldWithPath("maxQuantity").description("Quantidade máxima do item"),
                                                fieldWithPath("currentLevel").description("Nível atual da quantidade em estoque do respectivo item"),
                                                fieldWithPath("lastPricePerUnit").description("Último preço unitário do item"))
                                        .andWithPrefix("content[].product.",
                                                fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                                                fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("unit").description("Unidade padrão para o produto"),
                                                fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                                        .andWithPrefix("content[].product.unit.",
                                                fieldWithPath("id").description("Código da unidade padrão do produto"),
                                                fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                                                fieldWithPath("name").description("Nome da unidade padrão do produto"))));
    }

    @RoleTestRoot
    @Transactional
    public void findAll() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        for (int i = 0; i < 3; i++) {
            createAndSaveTechnicianStock(office);
        }

        this.mockMvc.perform(get("/stocks")
                .param("type", StockType.TECHNICIAN.name())
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("type")
                                                .optional()
                                                .description(createDescription(
                                                        "Tipo do estoque a ser filtrado",
                                                        "Caso não seja informado (valor = null), todos os estoques serão retornados",
                                                        "Valor default: null")),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()
                                ),
                                getPageContent("Lista com todos os estoques encontrados dado o tipo solicitado")
                                        .andWithPrefix("content[].",
                                                fieldWithPath("id").description("Código do estoque"),
                                                fieldWithPath("type").description("Tipo do estoque"),
                                                fieldWithPath("name").description("Nome do estoque"))));
    }

    @RoleTestRoot
    @Transactional
    public void findAllTechnicianStocks() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        for (int i = 0; i < 3; i++) {
            createAndSaveTechnicianStock(office);
        }

        this.mockMvc.perform(get("/stocks/technicians")
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()
                                ),
                                getPageContent("Lista com todos os estoques de técnicos encontrados")
                                        .andWithPrefix("content[].",
                                                fieldWithPath("id").description("Código do estoque"),
                                                fieldWithPath("type").description("Tipo do estoque"),
                                                fieldWithPath("name").description("Nome do estoque"),
                                                fieldWithPath("technician").description("Tecnico associado"))
                                        .andWithPrefix("content[].technician.",
                                                fieldWithPath("id").description("Código ID do técnico"),
                                                fieldWithPath("name").description("Nome do técnico"),
                                                fieldWithPath("sector").optional().type(JsonFieldType.STRING).description("Setor do técnico"),
                                                fieldWithPath("email").description("Email do técnico"),
                                                fieldWithPath("userId").description("User ID do técnico na API externa"),
                                                fieldWithPath("branchOffice")
                                                        .optional()
                                                        .type(JsonFieldType.OBJECT)
                                                        .description("Filial associada ao técnico, caso exista"))
                                        .andWithPrefix("content[].technician.branchOffice.",
                                                fieldWithPath("id").description("Código ID da filial"),
                                                fieldWithPath("name").description("Nome da Filial"),
                                                fieldWithPath("city").description("Cidade associada"))));
    }

    @RoleTestRoot
    @Transactional
    public void findByProductGroupByProduct() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock stock = createAndSaveTechnicianStock(office);
        for (int i = 0; i < 2; i++) {
            StockItem item1 = createAndSaveStockItem(createAndSaveShedStock(office));
            item1.setQuantity(4d);
            stockItemRepository.save(item1);

            StockItem item2 = createAndSaveStockItem(stock, item1.getProduct());
            item2.setQuantity(2d);
            stockItemRepository.save(item2);
        }

        this.mockMvc.perform(get("/stocks/general")
                .param("name", "")
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("name")
                                                .optional()
                                                .description(createDescription(
                                                        "Nome do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()
                                ),
                                getPageContent("Lista com todos os estoques encontrados dado o tipo solicitado")
                                        .andWithPrefix("content[].",
                                                fieldWithPath("product").description("Produto"),
                                                fieldWithPath("quantity").description("Quantidade total"),
                                                fieldWithPath("price").description("Preço total estimado, considerando o último preço unitário para cada item"))
                                        .andWithPrefix("content[].product.",
                                                fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                                                fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("unit").description("Unidade padrão para o produto"),
                                                fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                                        .andWithPrefix("content[].product.unit.",
                                                fieldWithPath("id").description("Código da unidade padrão do produto"),
                                                fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                                                fieldWithPath("name").description("Nome da unidade padrão do produto"))));
    }

    @RoleTestRoot
    public void findByStockId() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Stock stock = office.shed().orElseThrow();
        this.mockMvc.perform(get("/stocks/{id}", stock.getId())
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do estoque buscado, gerado pelo banco de dados")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código do estoque"),
                                fieldWithPath("type").description("Tipo do estoque"),
                                fieldWithPath("name").description("Nome do estoque"))));
    }


    @RoleTestRoot
    @Transactional
    public void findByStockAndCode() throws Exception {
        StockItem item = createAndSaveStockItem(stockTest);

        this.mockMvc.perform(get("/stocks/{stock}/items/codes/{code}", stockTest.getId(), item.getProduct().getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("stock").description("Código do estoque responsável pelo item"),
                                parameterWithName("code").description("Código do produto buscado")
                        ),
                        getStockItemResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void findByStockItemId() throws Exception {
        StockItem item = createAndSaveStockItem(stockTest);

        this.mockMvc.perform(get("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("stock").description("Código do estoque responsável pelo item"),
                                parameterWithName("id").description("Código do estoque do item")
                        ),
                        getStockItemResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void findByStockItemIdWithLevels() throws Exception {
        StockItem item = createAndSaveStockItem(stockTest);
        item.getLevels().add(new StockItemQuantityLevel(item, QuantityLevel.NORMAL, 0f, 100f));
        stockItemQuantityLevelRepository.saveAll(item.getLevels());
        item.setCurrentLevel(QuantityLevel.NORMAL);
        stockItemRepository.saveAndFlush(item);

        this.mockMvc.perform(get("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .param("withLevels", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("stock").description("Código do estoque responsável pelo item"),
                                parameterWithName("id").description("Código do estoque do item")
                        ),
                        requestParameters(parameterWithName("withLevels")
                                .description("Flag que indica que o retorno deve conter as informações sobre os níveis para o item")),
                        getStockItemResponseWithLevels()));
    }

    @RoleTestRoot
    @Transactional
    public void updateStockItemParameters() throws Exception {
        createNotificationsPermissions();
        StockItem item = createAndSaveStockItem(stockTest);

        this.mockMvc.perform(put("/stocks/{stock}/items/{id}", stockTest.getId(), item.getId())
                .content(objectMapper.writeValueAsString(createUpdateStockItemParametersRequest(item)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("stock").description("Código do estoque responsável pelo item"),
                                parameterWithName("id").description("Código do estoque do item")),
                        requestFields(
                                fieldWithPath("id").description(createDescriptionWithNotNull("Código do estoque do item")),
                                fieldWithPath("stock").description(createDescriptionWithNotNull("Código do estoque responsável pelo item")),
                                fieldWithPath("minQuantity").description(createDescriptionWithNotNull("Quantidade mínima para o item no estoque")),
                                fieldWithPath("maxQuantity").description(createDescriptionWithNotNull("Quantidade máxima para o item no estoque")),
                                fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description(createDescriptionWithNotNull(
                                        "Quantidade atual para o item no estoque",
                                        "Caso não seja informado, nada será alterado",
                                        "Caso este campo seja informado, o sistema calculará a diferença entre a quantidade atuale a quantidade informada e," +
                                                "desta forma, criará movimentações de forma que a quantidade atual seja esta quantidade informada",
                                        "O fluxo da movimentação continua o mesmo")),
                                fieldWithPath("pricePerUnit").description(createDescriptionWithNotNull("Último preço por unidade")),
                                fieldWithPath("levels").optional().type(JsonFieldType.ARRAY).description("Níveis do estoque"))
                                .andWithPrefix("levels[].",
                                        fieldWithPath("id").description("ID do registro"),
                                        fieldWithPath("level").description("Nível relacionado"),
                                        fieldWithPath("from").optional()
                                                .type(JsonFieldType.NUMBER)
                                                .description(createDescription(
                                                        "Porcentagem inicial do intervalo",
                                                        "Deve ser informado o valor entre 0 e 100, considerando porcentagem",
                                                        "Caso não seja informado, é considerado que o intervalo é abaixo ou igual ao valor do campo 'to'")),
                                        fieldWithPath("to").optional()
                                                .type(JsonFieldType.NUMBER)
                                                .description(createDescription(
                                                        "Porcentagem final do intervalo",
                                                        "Deve ser informado o valor entre 0 e 100, considerando porcentagem",
                                                        "Caso não seja informado, é considerado que o intervalo é acima ou igual ao valor do campo 'from'"))),
                        getStockItemProjection("")));
    }

    @RoleTestRoot
    @Transactional
    public void getStockSituationReport() throws Exception {
        Stock stock = createAndSaveShedStock();
        for (int i = 0; i < 2; i++) {
            createAndSaveStockItem(stock);
        }

        this.mockMvc.perform(get("/stocks/reports/current-situation/{id}", stock.getId())
                .param("format", ReportFormat.PDF.name())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("Código ID do estoque desejado")),
                                requestParameters(
                                        parameterWithName("format").description("O formato do relatório. Aceita: PDF ou CSV"))));
    }

    @RoleTestRoot
    @Transactional
    public void findAllStockItemsFromTechnicianStockByUserId() throws Exception {
        TechnicianStock stockTest = createAndSaveTechnicianStock();
        for (int i = 0; i < 3; i++) {
            StockItem item = createAndSaveStockItem(stockTest);
            item.setQuantity(10d);
            stockItemRepository.save(item);
        }

        this.mockMvc.perform(get("/basic/stocks/technicians/{userId}/items", stockTest.getTechnician().getUserId())
                .param("name", "")
                .param("code", "")
                .param("filterEmpty", "true")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(3)))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("userId").description(createDescriptionWithNotNull("ID do usuário desejado", "É necessário que este seja um técnico"))
                                ),
                                requestParameters(
                                        parameterWithName("name")
                                                .optional()
                                                .description(createDescription(
                                                        "Nome do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("code")
                                                .optional()
                                                .description(createDescription(
                                                        "Código do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("filterEmpty")
                                                .optional()
                                                .description(createDescription(
                                                        "Informa se deve ser listado apenas os itens cuja quantidade é superior a 0 ou todos os itens",
                                                        "Caso seja true, será retornado apenas os itens com quantidade em estoque superior a 0",
                                                        "Valor default: false")),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()
                                ),
                                getPageContent("Lista com todos os itens presentes no estoque em questão")
                                        .andWithPrefix("content[].",
                                                fieldWithPath("id").description("Código do estoque do item"),
                                                fieldWithPath("product").description("Produto"),
                                                fieldWithPath("quantity").description("Quantidade em estoque"),
                                                fieldWithPath("blockedQuantity").description("Quantidade bloqueada para movimentações"),
                                                fieldWithPath("availableQuantity").description("Quantidade disponível para movimentações"),
                                                fieldWithPath("minQuantity").description("Quantidade mínima do item"),
                                                fieldWithPath("maxQuantity").description("Quantidade máxima do item"),
                                                fieldWithPath("lastPricePerUnit").description("Último preço unitário do item"))
                                        .andWithPrefix("content[].product.",
                                                fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                                                fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("unit").description("Unidade padrão para o produto"),
                                                fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                                        .andWithPrefix("content[].product.unit.",
                                                fieldWithPath("id").description("Código da unidade padrão do produto"),
                                                fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                                                fieldWithPath("name").description("Nome da unidade padrão do produto"))));
    }

    @RoleTestRoot
    @Transactional
    public void basicFindAll() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        for (int i = 0; i < 3; i++) {
            createAndSaveTechnicianStock(office);
        }

        this.mockMvc.perform(get("/basic/stocks")
                .param("type", StockType.TECHNICIAN.name())
                .param("page", "0")
                .param("pageSize", "5")
                .header("Office-Id", office.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("type")
                                                .optional()
                                                .description(createDescription(
                                                        "Tipo do estoque a ser filtrado",
                                                        "Caso não seja informado (valor = null), todos os estoques serão retornados",
                                                        "Valor default: null")),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()
                                ),
                                getPageContent("Lista com todos os estoques encontrados dado o tipo solicitado")
                                        .andWithPrefix("content[].",
                                                fieldWithPath("id").description("Código do estoque"),
                                                fieldWithPath("type").description("Tipo do estoque"),
                                                fieldWithPath("name").description("Nome do estoque"))));
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_STOCKS_READ"})
    public void findAllAvailableToMoveByTechnician() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_STOCKS_READ"));
        createAndSaveTechnicianStockByEmployee(employeeRepository.save(e), office);

        for (int i = 0; i < 3; i++) {
            createAndSaveTechnicianStock(office);
        }

        this.mockMvc.perform(get("/basic/stocks/technicians/available")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()
                                ),
                                getPageContent("Lista com todos os estoques de técnicos encontrados")
                                        .andWithPrefix("content[].",
                                                fieldWithPath("id").description("Código do estoque"),
                                                fieldWithPath("type").description("Tipo do estoque"),
                                                fieldWithPath("name").description("Nome do estoque"))));
    }

    private UpdateStockItemParametersRequest createUpdateStockItemParametersRequest(StockItem item) {
        UpdateStockItemParametersRequest request = new UpdateStockItemParametersRequest();
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
            levelRequest.setTo((float) (20 * (i + 1)));
            levelRequest.setLevel(levels[i]);
            request.getLevels().add(levelRequest);
        }

        return request;
    }

    private ResponseFieldsSnippet getStockItemProjection(String prefix) {
        return responseFields(
                fieldWithPath("id").description("Código do estoque do item"),
                fieldWithPath("product").description("Produto"),
                fieldWithPath("quantity").description("Quantidade em estoque"),
                fieldWithPath("blockedQuantity").description("Quantidade bloqueada para movimentações"),
                fieldWithPath("availableQuantity").description("Quantidade disponível para movimentações"),
                fieldWithPath("minQuantity").description("Quantidade mínima do item"),
                fieldWithPath("maxQuantity").description("Quantidade máxima do item"),
                fieldWithPath("lastPricePerUnit").description("Último preço unitário do item"))
                .andWithPrefix(prefix + "product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix(prefix + "product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"));
    }

    private ResponseFieldsSnippet getStockItemResponse() {
        return responseFields(
                fieldWithPath("id").description("Código do estoque do item"),
                fieldWithPath("product").description("Produto"),
                fieldWithPath("quantity").description("Quantidade em estoque"),
                fieldWithPath("blockedQuantity").description("Quantidade bloqueada para movimentações"),
                fieldWithPath("availableQuantity").description("Quantidade disponível para movimentações"),
                fieldWithPath("minQuantity").description("Quantidade mínima do item"),
                fieldWithPath("maxQuantity").description("Quantidade máxima do item"),
                fieldWithPath("lastPricePerUnit").description("Último preço unitário do item"),

                fieldWithPath("stock").description("Código do estoque responsável pelo item"),
                fieldWithPath("lastEntryMoves").optional().type(JsonFieldType.ARRAY).description(createDescription(
                        "Lista com as últimas movimentações de entrada associadas ao item",
                        "Possui limite máximo de 50 registros ordenados de forma decrescente")),
                fieldWithPath("lastOutgoingMoves").optional().type(JsonFieldType.ARRAY).description(createDescription(
                        "Lista com as últimas movimentações de saída associadas ao item",
                        "Possui limite máximo de 50 registros ordenados de forma decrescente")))
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
                .andWithPrefix("lastEntryMoves[].",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                        fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                        fieldWithPath("origin").optional().type(JsonFieldType.STRING).description("Origem da movimentação"),
                        fieldWithPath("status").optional().type(JsonFieldType.STRING).description("Status atual da movimentação"),
                        fieldWithPath("type").optional().type(JsonFieldType.STRING).description("Tipo de movimentação"),
                        fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description("Quantidade movimentada"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação"))
                .andWithPrefix("lastOutgoingMoves[].",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                        fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                        fieldWithPath("origin").optional().type(JsonFieldType.STRING).description("Origem da movimentação"),
                        fieldWithPath("status").optional().type(JsonFieldType.STRING).description("Status atual da movimentação"),
                        fieldWithPath("type").optional().type(JsonFieldType.STRING).description("Tipo de movimentação"),
                        fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description("Quantidade movimentada"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação"));
    }

    private ResponseFieldsSnippet getStockItemResponseWithLevels() {
        return responseFields(
                fieldWithPath("id").description("Código do estoque do item"),
                fieldWithPath("product").description("Produto"),
                fieldWithPath("quantity").description("Quantidade em estoque"),
                fieldWithPath("blockedQuantity").description("Quantidade bloqueada para movimentações"),
                fieldWithPath("availableQuantity").description("Quantidade disponível para movimentações"),
                fieldWithPath("minQuantity").description("Quantidade mínima do item"),
                fieldWithPath("maxQuantity").description("Quantidade máxima do item"),
                fieldWithPath("lastPricePerUnit").description("Último preço unitário do item"),
                fieldWithPath("levels").description("Lista com os níveis cadastrados para o item"),

                fieldWithPath("stock").description("Código do estoque responsável pelo item"),
                fieldWithPath("lastEntryMoves").optional().type(JsonFieldType.ARRAY).description(createDescription(
                        "Lista com as últimas movimentações de entrada associadas ao item",
                        "Possui limite máximo de 50 registros ordenados de forma decrescente")),
                fieldWithPath("lastOutgoingMoves").optional().type(JsonFieldType.ARRAY).description(createDescription(
                        "Lista com as últimas movimentações de saída associadas ao item",
                        "Possui limite máximo de 50 registros ordenados de forma decrescente")))
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
                .andWithPrefix("lastEntryMoves[].",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                        fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                        fieldWithPath("origin").optional().type(JsonFieldType.STRING).description("Origem da movimentação"),
                        fieldWithPath("status").optional().type(JsonFieldType.STRING).description("Status atual da movimentação"),
                        fieldWithPath("type").optional().type(JsonFieldType.STRING).description("Tipo de movimentação"),
                        fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description("Quantidade movimentada"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação"))
                .andWithPrefix("lastOutgoingMoves[].",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                        fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                        fieldWithPath("origin").optional().type(JsonFieldType.STRING).description("Origem da movimentação"),
                        fieldWithPath("status").optional().type(JsonFieldType.STRING).description("Status atual da movimentação"),
                        fieldWithPath("type").optional().type(JsonFieldType.STRING).description("Tipo de movimentação"),
                        fieldWithPath("quantity").optional().type(JsonFieldType.NUMBER).description("Quantidade movimentada"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição da movimentação"))
                .andWithPrefix("levels[].",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("ID do registro"),
                        fieldWithPath("level").optional().type(JsonFieldType.STRING).description("Nível relacionado"),
                        fieldWithPath("from").optional()
                                .type(JsonFieldType.NUMBER)
                                .description(createDescription(
                                        "Porcentagem inicial do intervalo",
                                        "Deve ser informado o valor entre 0 e 100, considerando porcentagem",
                                        "Caso não seja informado, é considerado que o intervalo é abaixo ou igual ao valor do campo 'to'")),
                        fieldWithPath("to").optional()
                                .type(JsonFieldType.NUMBER)
                                .description(createDescription(
                                        "Porcentagem final do intervalo",
                                        "Deve ser informado o valor entre 0 e 100, considerando porcentagem",
                                        "Caso não seja informado, é considerado que o intervalo é acima ou igual ao valor do campo 'from'")));
    }

}
