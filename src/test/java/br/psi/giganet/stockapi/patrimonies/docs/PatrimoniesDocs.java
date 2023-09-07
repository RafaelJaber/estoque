package br.psi.giganet.stockapi.patrimonies.docs;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.address.service.AddressService;
import br.psi.giganet.stockapi.common.valid_mac_addresses.repository.ValidMacAddressesRepository;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.entries.model.Entry;
import br.psi.giganet.stockapi.entries.model.EntryItem;
import br.psi.giganet.stockapi.entries.repository.EntryRepository;
import br.psi.giganet.stockapi.patrimonies.controller.request.*;
import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.patrimonies.model.PatrimonyCodeType;
import br.psi.giganet.stockapi.patrimonies.repository.PatrimonyMoveRepository;
import br.psi.giganet.stockapi.patrimonies.repository.PatrimonyRepository;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocationType;
import br.psi.giganet.stockapi.patrimonies_locations.repository.PatrimonyLocationRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.technician.model.Technician;
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
import java.util.ArrayList;
import java.util.UUID;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PatrimoniesDocs extends BuilderIntegrationTest {

    private final Patrimony patrimony;

    @Autowired
    public PatrimoniesDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            PatrimonyRepository patrimonyRepository,
            PatrimonyLocationRepository patrimonyLocationRepository,
            PatrimonyMoveRepository patrimonyMoveRepository,
            TechnicianRepository technicianRepository,
            EntryRepository entryRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            AddressService addressService,
            StockRepository stockRepository,
            ValidMacAddressesRepository validMacAddressesRepository,
            BranchOfficeRepository branchOfficeRepository) {

        this.branchOfficeRepository = branchOfficeRepository;

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        this.patrimonyLocationRepository = patrimonyLocationRepository;
        this.patrimonyRepository = patrimonyRepository;
        this.technicianRepository = technicianRepository;
        this.patrimonyMoveRepository = patrimonyMoveRepository;
        this.entryRepository = entryRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.addressService = addressService;
        this.stockRepository = stockRepository;
        this.validMacAddressesRepository = validMacAddressesRepository;
        createCurrentUser();

        patrimony = createAndSavePatrimony();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        for (int i = 0; i < 2; i++) {
            createAndSavePatrimony();
        }

        this.mockMvc.perform(get("/patrimonies")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPagePatrimonyProjection()));
    }

    @RoleTestRoot
    public void findAllByProductNameOrCodeOrLocation() throws Exception {
        for (int i = 0; i < 3; i++) {
            createAndSavePatrimony();
        }

        this.mockMvc.perform(get("/patrimonies")
                .param("queries", "pro")
                .param("queries", "gal")
                .param("page", "0")
                .param("pageSize", "3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("queries").description(createDescription(
                                                "Lista contendo os todas as strings a serem utilizadas como filtros",
                                                "Podem ser informados nome do produto, código do patrimônio ou nome da localização",
                                                "Filtros seguem a lógica OU"
                                        )),
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPagePatrimonyWithoutUnitProjection()));
    }

    @RoleTestRoot
    public void findAllByLocation() throws Exception {
        PatrimonyLocation location = createAndSavePatrimonyLocation();
        createAndSavePatrimony(location);

        this.mockMvc.perform(get("/patrimonies/current-locations/{location}", location.getId())
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
                                        parameterWithName("location").description("Código da localização atual dos patrimônios a serem filtrados")
                                ),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPagePatrimonyProjection()));
    }

    @RoleTestRoot
    public void findAllByCurrentLocationAndProduct() throws Exception {
        PatrimonyLocation location = createAndSavePatrimonyLocation();
        Product product = createAndSaveProduct();
        for (int i = 0; i < 2; i++) {
            createAndSavePatrimony(location, product);
        }

        this.mockMvc.perform(get("/patrimonies/current-locations/{location}/products/{product}", location.getId(), product.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(2)))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("location").description("Código da localização atual dos patrimônios a serem filtrados"),
                                        parameterWithName("product").description("Código do produto (ID)")
                                ),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPagePatrimonyProjection()));
    }

    @RoleTestRoot
    public void findAllByProduct() throws Exception {
        Product product = createAndSaveProduct();
        for (int i = 0; i < 2; i++) {
            createAndSavePatrimony(product);
        }

        this.mockMvc.perform(get("/patrimonies/products/{product}", product.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(2)))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("product").description("Código do produto (ID)")
                                ),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPagePatrimonyProjection()));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/patrimonies/{id}", patrimony.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do patrimônio buscado (ID)")
                        ),
                        getPatrimonyResponse()));
    }

    @RoleTestRoot
    public void findByIdWithHistory() throws Exception {
        this.mockMvc.perform(get("/patrimonies/{id}", patrimony.getId())
                .param("withHistory", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do patrimônio buscado (ID)")
                        ),
                        requestParameters(
                                parameterWithName("withHistory").description(
                                        "Flag que indica que o retorno deve conter também todo o histórico de movimentação do patrimônio")),
                        getPatrimonyResponse()));
    }

    @RoleTestRoot
    public void findByUniqueCode() throws Exception {
        this.mockMvc.perform(get("/patrimonies/unique-codes/{code}", patrimony.getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("code").description("Código único do patrimônio")
                        ),
                        getPatrimonyResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/patrimonies")
                .content(objectMapper.writeValueAsString(createInsertPatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("code").description("Código único de identificação do patrimônio"),
                                fieldWithPath("codeType").description("Tipo do código do patrimônio"),
                                fieldWithPath("product").description("Produto"),
                                fieldWithPath("currentLocation").description("Estoque atual onde o patrimônio se encontra"),
                                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre o patrimônio")),
                        getPatrimonyResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void insertBatch() throws Exception {
        this.mockMvc.perform(post("/patrimonies/batch")
                .content(objectMapper.writeValueAsString(createBatchInsertPatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("codes").description("Lista com os códigos de identificação do patrimônio"),
                                fieldWithPath("codeType").description("Tipo do código do patrimônio"),
                                fieldWithPath("product").description("Produto"),
                                fieldWithPath("currentLocation").description("Estoque atual onde o patrimônio se encontra"),
                                fieldWithPath("entryItem")
                                        .optional()
                                        .type(JsonFieldType.NUMBER)
                                        .description("Código do item do lançamento associado aos patrimônios, caso exista"),
                                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre o patrimônio")),
                        getListPatrimonyResponse()));
    }

    @RoleTestRoot
    public void update() throws Exception {
        this.mockMvc.perform(put("/patrimonies/{id}", patrimony.getId())
                .content(objectMapper.writeValueAsString(createUpdatePatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do patrimônio (ID)")),
                        requestFields(
                                fieldWithPath("id").description("Código do patrimônio, oriundo do banco de dados"),
                                fieldWithPath("product").description("Código do produto (ID)"),
                                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre o patrimônio")),
                        getPatrimonyResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void movePatrimony() throws Exception {
        this.mockMvc.perform(post("/patrimonies/{id}/move", patrimony.getId())
                .content(objectMapper.writeValueAsString(createMovePatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do patrimônio (ID)")),
                        requestFields(
                                fieldWithPath("patrimony").description("Código do patrimônio, oriundo do banco de dados"),
                                fieldWithPath("newLocation").description("Código da nova localização do patrimônio"),
                                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre o patrimônio")),
                        getPatrimonyResponse()));
    }

    @RoleTestRoot
    public void deleteById() throws Exception {
        Patrimony patrimony = createAndSavePatrimony();
        this.mockMvc.perform(delete("/patrimonies/{id}", patrimony.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do patrimônio (ID)"))));
    }

    @RoleTestRoot
    public void findAllFromTechnicianByProduct() throws Exception {
        Technician technician = createAndSaveTechnician();
        PatrimonyLocation location = createAndSavePatrimonyLocation(technician.getUserId());
        Product product = createAndSaveProduct();
        for (int i = 0; i < 2; i++) {
            createAndSavePatrimony(location, product);
        }

        this.mockMvc.perform(get("/basic/patrimonies/current-locations/technicians/{userId}/products/{product}", technician.getUserId(), product.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(2)))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("userId").description("User ID (Externo) do técnico buscado"),
                                        parameterWithName("product").description("Código do produto (ID)")
                                ),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPagePatrimonyProjection()));
    }

    @RoleTestRoot
    public void findAllFromCustomerByCode() throws Exception {
        PatrimonyLocation location = new PatrimonyLocation();
        location.setCode(UUID.randomUUID().toString().substring(0, 10));
        location.setNote("Obs");
        location.setName("Cliente Lucas");
        location.setType(PatrimonyLocationType.CUSTOMER);
        location = patrimonyLocationRepository.saveAndFlush(location);

        Product product = createAndSaveProduct();
        for (int i = 0; i < 2; i++) {
            createAndSavePatrimony(location, product);
        }

        this.mockMvc.perform(get("/basic/patrimonies/current-locations/customers/{userId}", location.getCode())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(2)))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("userId").description("User ID (Externo) do cliente buscado")
                                ),
                                requestParameters(
                                        getPagePathParameter(),
                                        getPageSizePathParameter()),
                                getPagePatrimonyProjection()));
    }

    @RoleTestRoot
    public void basicFindById() throws Exception {
        this.mockMvc.perform(get("/basic/patrimonies/{id}", patrimony.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do patrimônio buscado (ID)")
                        ),
                        getPatrimonyResponse()));
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_PATRIMONIES_WRITE"})
    public void insertByTechnician() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_PATRIMONIES_WRITE"));
        Technician technician = createAndSaveTechnicianByEmployee(employeeRepository.save(e));
        createAndSavePatrimonyLocation(technician.getUserId());

        this.mockMvc.perform(post("/basic/patrimonies/technicians")
                .content(objectMapper.writeValueAsString(createBasicInsertPatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("code").description("Código único de identificação do patrimônio"),
                                fieldWithPath("product").description("Produto"),
                                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre o patrimônio")),
                        getPatrimonyResponse()));
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_PATRIMONIES_WRITE"})
    public void basicUpdate() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_PATRIMONIES_WRITE"));
        Technician technician = createAndSaveTechnicianByEmployee(employeeRepository.save(e));
        createAndSavePatrimonyLocation(technician.getUserId());

        this.mockMvc.perform(put("/basic/patrimonies/{id}", patrimony.getId())
                .content(objectMapper.writeValueAsString(createUpdatePatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do patrimônio (ID)")),
                        requestFields(
                                fieldWithPath("id").description("Código do patrimônio, oriundo do banco de dados"),
                                fieldWithPath("product").description("Código do produto (ID)"),
                                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre o patrimônio")),
                        getPatrimonyResponse()));
    }

    private InsertPatrimonyRequest createInsertPatrimonyRequest() {
        InsertPatrimonyRequest request = new InsertPatrimonyRequest();
        request.setCode(randomMACAddress());
        request.setCodeType(PatrimonyCodeType.MAC_ADDRESS);
        request.setProduct(patrimony.getProduct().getId());
        request.setCurrentLocation(createAndSavePatrimonyLocation().getId());
        request.setNote("Observação do patrimônio");

        return request;
    }

    private BatchInsertPatrimonyRequest createBatchInsertPatrimonyRequest() {
        Entry entry = createAndSaveEntry();
        EntryItem item = entry.getItems().get(0);

        BatchInsertPatrimonyRequest request = new BatchInsertPatrimonyRequest();
        request.setProduct(patrimony.getProduct().getId());
        request.setCodeType(PatrimonyCodeType.MAC_ADDRESS);
        request.setCurrentLocation(createAndSavePatrimonyLocation().getId());
        request.setEntryItem(item.getId());
        request.setNote("Observação do patrimônio");
        request.setCodes(new ArrayList<>());
        for (int i = 0; i < item.getQuantity(); i++) {
            String code = randomMACAddress();
            createAndSaveValidMacAddress(code);
            request.getCodes().add(code);
        }

        return request;
    }

    private BasicInsertPatrimonyRequest createBasicInsertPatrimonyRequest() {
        BasicInsertPatrimonyRequest request = new BasicInsertPatrimonyRequest();
        request.setCode(randomMACAddress());
        request.setProduct(patrimony.getProduct().getId());
        request.setNote("Observação do patrimônio");

        createAndSaveValidMacAddress(request.getCode());

        return request;
    }

    private UpdatePatrimonyRequest createUpdatePatrimonyRequest() {
        UpdatePatrimonyRequest request = new UpdatePatrimonyRequest();
        request.setId(patrimony.getId());
        request.setProduct(patrimony.getProduct().getId());
        request.setNote("Observação");

        return request;
    }

    private MovePatrimonyRequest createMovePatrimonyRequest() {
        MovePatrimonyRequest request = new MovePatrimonyRequest();
        request.setPatrimony(patrimony.getId());
        request.setNewLocation(createAndSavePatrimonyLocation().getId());
        request.setNote("Observação do patrimônio");

        return request;
    }

    private ResponseFieldsSnippet getPagePatrimonyProjection() {
        return getPageContent("Lista com todos os patrimônios encontrados")
                .andWithPrefix("content[].",
                        fieldWithPath("id").description("Código do patrimônio, oriundo do banco de dados"),
                        fieldWithPath("code").description("Código único de identificação do patrimônio"),
                        fieldWithPath("codeType").description("Tipo do código do patrimônio"),
                        fieldWithPath("product").description("Produto"),
                        fieldWithPath("currentLocation").description("Local atual onde o patrimônio se encontra"))
                .andWithPrefix("content[].currentLocation.",
                        fieldWithPath("id").description("Código ID do local"),
                        fieldWithPath("code").description(createDescription(
                                "Código de referencia",
                                "Caso seja um Técnico ou Cliente, este campo corresponderá ao \"userId\"",
                                "Caso seja Manutenção, Galpão, Obsoleto ou Defeituoso, este campo corresponderá ao id do estoque correspondente"
                        )),
                        fieldWithPath("name").description("Nome do local"))
                .andWithPrefix("content[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("content[].product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"));
    }

    private ResponseFieldsSnippet getPagePatrimonyWithoutUnitProjection() {
        return getPageContent("Lista com todos os estoques encontrados")
                .andWithPrefix("content[].",
                        fieldWithPath("id").description("Código do patrimônio, oriundo do banco de dados"),
                        fieldWithPath("code").description("Código único de identificação do patrimônio"),
                        fieldWithPath("codeType").description("Tipo do código do patrimônio"),
                        fieldWithPath("product").description("Produto"),
                        fieldWithPath("currentLocation").description("Local atual onde o patrimônio se encontra"))
                .andWithPrefix("content[].currentLocation.",
                        fieldWithPath("id").description("Código ID do local"),
                        fieldWithPath("code").description(createDescription(
                                "Código de referencia",
                                "Caso seja um Técnico ou Cliente, este campo corresponderá ao \"userId\"",
                                "Caso seja Manutenção, Galpão, Obsoleto ou Defeituoso, este campo corresponderá ao id do estoque correspondente"
                        )),
                        fieldWithPath("name").description("Nome do local"))
                .andWithPrefix("content[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"));
    }

    private ResponseFieldsSnippet getPatrimonyResponse() {
        return responseFields(
                fieldWithPath("id").description("Código do patrimônio, oriundo do banco de dados"),
                fieldWithPath("code").description("Código único de identificação do patrimônio"),
                fieldWithPath("codeType").description("Tipo do código do patrimônio"),
                fieldWithPath("product").description("Produto"),
                fieldWithPath("entryItem").optional().type(JsonFieldType.OBJECT)
                        .description("Lançamento oriundo de uma ordem de compra associado a este patrimonio"),
                fieldWithPath("currentLocation").description("Local atual onde o patrimônio se encontra"),
                fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre o patrimônio"),
                fieldWithPath("moves").optional().type(JsonFieldType.ARRAY).description("Lista com todas as movimentações associadas ao patrimônio"))
                .andWithPrefix("currentLocation.",
                        fieldWithPath("id").description("Código ID do local"),
                        fieldWithPath("code").description(createDescription(
                                "Código de referencia",
                                "Caso seja um Técnico ou Cliente, este campo corresponderá ao \"userId\"",
                                "Caso seja Manutenção, Galpão, Obsoleto ou Defeituoso, este campo corresponderá ao id do estoque correspondente"
                        )),
                        fieldWithPath("name").description("Nome do local"))
                .andWithPrefix("entryItem.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Código do item no lançamento"),
                        fieldWithPath("entry").type(JsonFieldType.NUMBER).description("Código do registro de lançamento"),
                        fieldWithPath("fiscalDocumentNumber").type(JsonFieldType.STRING).description("Código da nota fiscal"))
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
                .andWithPrefix("moves[].",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                        fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                        fieldWithPath("from").optional().type(JsonFieldType.OBJECT).description("Localização de origem do patrimônio"),
                        fieldWithPath("to").optional().type(JsonFieldType.OBJECT).description("Localização de destino do patrimônio"),
                        fieldWithPath("responsible").optional().type(JsonFieldType.OBJECT).description("Funcionário responsável pela movimentação"),
                        fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observação da movimentação"))
                .andWithPrefix("moves[].from.",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código ID do local"),
                        fieldWithPath("code").optional().type(JsonFieldType.STRING).description(createDescription(
                                "Código de referencia",
                                "Caso seja um Técnico ou Cliente, este campo corresponderá ao \"userId\"",
                                "Caso seja Manutenção, Galpão, Obsoleto ou Defeituoso, este campo corresponderá ao id do estoque correspondente"
                        )),
                        fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome do local"))
                .andWithPrefix("moves[].to.",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código ID do local"),
                        fieldWithPath("code").optional().type(JsonFieldType.STRING).description(createDescription(
                                "Código de referencia",
                                "Caso seja um Técnico ou Cliente, este campo corresponderá ao \"userId\"",
                                "Caso seja Manutenção, Galpão, Obsoleto ou Defeituoso, este campo corresponderá ao id do estoque correspondente"
                        )),
                        fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome do local"))
                .andWithPrefix("moves[].responsible.",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código ID do funcionário"),
                        fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome do funcionário"));
    }

    private ResponseFieldsSnippet getListPatrimonyResponse() {
        return responseFields(
                fieldWithPath("[]").description("Lista com todos os patrimônios cadastrados nesta operação"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código do patrimônio, oriundo do banco de dados"),
                        fieldWithPath("code").description("Código único de identificação do patrimônio"),
                        fieldWithPath("codeType").description("Tipo do código do patrimônio"),
                        fieldWithPath("product").description("Produto"),
                        fieldWithPath("entryItem").optional().type(JsonFieldType.OBJECT)
                                .description("Lançamento oriundo de uma ordem de compra associado a este patrimonio"),
                        fieldWithPath("currentLocation").description("Local atual onde o patrimônio se encontra"),
                        fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre o patrimônio"),
                        fieldWithPath("moves").optional().type(JsonFieldType.ARRAY).description("Lista com todas as movimentações associadas ao patrimônio"))
                .andWithPrefix("[].currentLocation.",
                        fieldWithPath("id").description("Código ID do local"),
                        fieldWithPath("code").description(createDescription(
                                "Código de referencia",
                                "Caso seja um Técnico ou Cliente, este campo corresponderá ao \"userId\"",
                                "Caso seja Manutenção, Galpão, Obsoleto ou Defeituoso, este campo corresponderá ao id do estoque correspondente"
                        )),
                        fieldWithPath("name").description("Nome do local"))
                .andWithPrefix("[].entryItem.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Código do item no lançamento"),
                        fieldWithPath("entry").type(JsonFieldType.NUMBER).description("Código do registro de lançamento"),
                        fieldWithPath("fiscalDocumentNumber").type(JsonFieldType.STRING).description("Código da nota fiscal"))
                .andWithPrefix("[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("[].product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"))
                .andWithPrefix("moves[].",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código da movimentação"),
                        fieldWithPath("date").optional().type(JsonFieldType.STRING).description("Data da movimentação"),
                        fieldWithPath("from").optional().type(JsonFieldType.OBJECT).description("Localização de origem do patrimônio"),
                        fieldWithPath("to").optional().type(JsonFieldType.OBJECT).description("Localização de destino do patrimônio"),
                        fieldWithPath("responsible").optional().type(JsonFieldType.OBJECT).description("Funcionário responsável pela movimentação"),
                        fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observação da movimentação"))
                .andWithPrefix("[].moves.from.",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código ID do local"),
                        fieldWithPath("code").optional().type(JsonFieldType.STRING).description(createDescription(
                                "Código de referencia",
                                "Caso seja um Técnico ou Cliente, este campo corresponderá ao \"userId\"",
                                "Caso seja Manutenção, Galpão, Obsoleto ou Defeituoso, este campo corresponderá ao id do estoque correspondente"
                        )),
                        fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome do local"))
                .andWithPrefix("[].moves.to.",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código ID do local"),
                        fieldWithPath("code").optional().type(JsonFieldType.STRING).description(createDescription(
                                "Código de referencia",
                                "Caso seja um Técnico ou Cliente, este campo corresponderá ao \"userId\"",
                                "Caso seja Manutenção, Galpão, Obsoleto ou Defeituoso, este campo corresponderá ao id do estoque correspondente"
                        )),
                        fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome do local"))
                .andWithPrefix("[].moves.responsible.",
                        fieldWithPath("id").optional().type(JsonFieldType.NUMBER).description("Código ID do funcionário"),
                        fieldWithPath("name").optional().type(JsonFieldType.STRING).description("Nome do funcionário"));
    }

}
