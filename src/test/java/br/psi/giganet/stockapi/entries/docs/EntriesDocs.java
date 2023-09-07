package br.psi.giganet.stockapi.entries.docs;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.address.service.AddressService;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.entries.controller.request.InsertEntryItemRequest;
import br.psi.giganet.stockapi.entries.controller.request.InsertEntryRequest;
import br.psi.giganet.stockapi.entries.model.Entry;
import br.psi.giganet.stockapi.entries.repository.EntryRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrder;
import br.psi.giganet.stockapi.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EntriesDocs extends BuilderIntegrationTest {

    private final Entry entryTest;

    @Autowired
    public EntriesDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            AddressService addressService,
            EntryRepository entryRepository,
            StockRepository stockRepository,
            BranchOfficeRepository branchOfficeRepository) {

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.addressService = addressService;
        this.entryRepository = entryRepository;
        this.stockRepository = stockRepository;
        this.branchOfficeRepository = branchOfficeRepository;
        createCurrentUser();

        entryTest = createAndSaveEntry();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        Entry entry = createAndSaveEntry();
        BranchOffice branchOffice = entry.getBranchOffice();

        this.mockMvc.perform(get("/entries")
                .header("Office-Id", branchOffice.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                getEntryProjection()));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/entries/{id}", entryTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(
                                                createDescriptionWithNotNull("Código do lançamento procurado"))),
                                getEntryResponse()));
    }

    @RoleTestRoot
    public void findByIdWithMetaData() throws Exception {
        this.mockMvc.perform(get("/entries/{id}", entryTest.getId())
                .param("withMetaData", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(
                                                createDescriptionWithNotNull("Código do lançamento procurado"))),
                                requestParameters(
                                        parameterWithName("withMetaData").description(
                                                createDescriptionWithNotNull(
                                                        "Parâmetro que indica o tipo do retorno",
                                                        "O valor do parâmetro é irrelevante, mas deve estar presente para que este método seja ativado"))),
                                getListEntryItemWithMetaDataProjection()));
    }

    @RoleTestRoot
    public void findByItemIdWithMetaData() throws Exception {
        this.mockMvc.perform(get("/entries/items/{id}", entryTest.getItems().get(0).getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(
                                                createDescriptionWithNotNull("Código do item do lançamento procurado"))),
                                getEntryItemWithMetaDataProjection()));
    }

    @RoleTestRoot
    public void insert() throws Exception {
        BranchOffice branchOffice = createAndSaveBranchOffice();
        this.mockMvc.perform(post("/entries")
                .content(objectMapper.writeValueAsString(createInsertEntryRequest()))
                .header("Office-Id", branchOffice.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("order").description(createDescriptionWithNotNull("Código da ordem de compra associada")),
                                        fieldWithPath("fiscalDocument").optional().type(JsonFieldType.STRING).description("Código da nota fiscal associada"),
                                        fieldWithPath("documentAccessCode").optional().type(JsonFieldType.STRING).description("Código de acesso, no caso de uma nota fiscal eletronica"),
                                        fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações sobre o lançamento"),
                                        fieldWithPath("items").description(createDescriptionWithNotEmpty("Lista com os itens do lançamento")),
                                        fieldWithPath("updateStock").description(createDescriptionWithNotNull("Informa se este lançamento deve atualizar o estoque ou não")),
                                        fieldWithPath("isManual").description(
                                                createDescriptionWithNotNull("Informa se o lançamento é realizado de forma manual ou se é a partir de upload de uma nota fiscal",
                                                        "Se TRUE, é considerado manual")))
                                        .andWithPrefix("items[].",
                                                fieldWithPath("orderItem").description(createDescriptionWithNotNull("Código do item na ordem de compra associada")),
                                                fieldWithPath("documentProductCode").description(createDescriptionWithNotNull("Código do item na nota fiscal recebida")),
                                                fieldWithPath("receivedQuantity").description(createDescriptionWithPositiveAndNotNull("Quantidade do item recebida"))),
                                getEntryResponse()));
    }

    private ResponseFieldsSnippet getEntryProjection() {
        return responseFields(fieldWithPath("[]")
                .description("Lista de todos os lançamentos em ordem decrescente pela data de cadastro"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código do lançamento"),
                        fieldWithPath("purchaseOrder").description("Ordem de compra associada"),
                        fieldWithPath("responsible").description("Responsável pelo lançamento"),
                        fieldWithPath("fiscalDocumentNumber").description("Nota fiscal associada"),
                        fieldWithPath("note").optional().type(JsonFieldType.STRING).description("Observações"),
                        fieldWithPath("date").description("Data do lançamento"),
                        fieldWithPath("status").description("Situação do lançamento"))
                .andWithPrefix("[].purchaseOrder.",
                        fieldWithPath("id").description("Código da ordem de compra"),
                        fieldWithPath("total").description("Valor total da ordem"),
                        fieldWithPath("date").description("Data da ordem. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("status").description("Status atual da ordem de compra"),
                        fieldWithPath("supplier").description("Fornecedor selecionado para a cotação"),
                        fieldWithPath("description").description("Descrição da ordem de compra"),
                        fieldWithPath("responsible").description("Responsável pela ordem de compra"))
                .andWithPrefix("[].purchaseOrder.supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"))
                .andWithPrefix("[].responsible.",
                        fieldWithPath("id").description("Código do funcionário"),
                        fieldWithPath("name").description("Nome do funcionário"));
    }

    private ResponseFieldsSnippet getEntryResponse() {
        return responseFields(
                fieldWithPath("id").description("Código do lançamento"),
                fieldWithPath("purchaseOrder").description("Ordem de compra associada"),
                fieldWithPath("responsible").description("Responsável pelo lançamento"),
                fieldWithPath("fiscalDocumentNumber").description("Nota fiscal associada"),
                fieldWithPath("note").description("Observações"),
                fieldWithPath("date").description("Data do lançamento"),
                fieldWithPath("items").description("Lista com os itens do lançamento"),
                fieldWithPath("isManual").description("Se verdadeiro, o lançamento foi realizado manualmente, sem upload de nfe." +
                        " Caso contrário, o lançamento foi feito com anexo de nota fiscal"),
                fieldWithPath("status").description("Situação do lançamento"))
                .andWithPrefix("purchaseOrder.",
                        fieldWithPath("id").description("Código da ordem de compra"),
                        fieldWithPath("total").description("Valor total da ordem"),
                        fieldWithPath("date").description("Data da ordem. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("status").description("Status atual da ordem de compra"),
                        fieldWithPath("supplier").description("Fornecedor selecionado para a cotação"),
                        fieldWithPath("description").description("Descrição da ordem de compra"),
                        fieldWithPath("responsible").description("Responsável pela ordem de compra"))
                .andWithPrefix("purchaseOrder.supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"))
                .andWithPrefix("responsible.",
                        fieldWithPath("id").description("Código do funcionário"),
                        fieldWithPath("name").description("Nome do funcionário"))
                .andWithPrefix("items[].",
                        fieldWithPath("id").description("Código do relacionamento entre a ordem de compra e os produtos"),
                        fieldWithPath("product").description("Produto"),
                        fieldWithPath("quantity").description("Quantidade para o produto"),
                        fieldWithPath("unit").description("Unidade selecionada para o produto"),
                        fieldWithPath("ipi").description("Valor do IPI, em porcentagem"),
                        fieldWithPath("icms").description("Valor do ICMS, em porcentagem"),
                        fieldWithPath("price").description("Preço unitário para o produto"),
                        fieldWithPath("total").description("Preço total do respectivo item"),
                        fieldWithPath("documentProductCode").description("Código do item assoaciado na nota fiscal"),
                        fieldWithPath("supplier").description("Fornacedor do respectivo indivíduo"),
                        fieldWithPath("status").description("Situação do item"))
                .andWithPrefix("items[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"))
                .andWithPrefix("items[].unit.",
                        fieldWithPath("id").description("Código da unidade selecionada para o produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade selecionada para o produto"),
                        fieldWithPath("name").description("Nome da unidade selecionada para o produto"))
                .andWithPrefix("items[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("items[].product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"));
    }

    private ResponseFieldsSnippet getEntryItemWithMetaDataProjection() {
        return responseFields(
                fieldWithPath("entryItem").description("Item de um lançamento"),
                fieldWithPath("registeredPatrimonies").description("Quantidade de patrimônios já lançados para o respectivo item de lançamento"))
                .andWithPrefix("entryItem.",
                        fieldWithPath("id").description("Código do relacionamento entre a ordem de compra e os produtos"),
                        fieldWithPath("product").description("Produto"),
                        fieldWithPath("quantity").description("Quantidade para o produto"))
                .andWithPrefix("entryItem.product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("entryItem.product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"));
    }

    private ResponseFieldsSnippet getListEntryItemWithMetaDataProjection() {
        return responseFields(
                fieldWithPath("[]").description("Lista de itens do lançamento com metadados relacionados a lançamentos já executados"))
                .andWithPrefix("[].",
                        fieldWithPath("entryItem").description("Item de um lançamento"),
                        fieldWithPath("registeredPatrimonies").description("Quantidade de patrimônios já lançados para o respectivo item de lançamento"))
                .andWithPrefix("[].entryItem.",
                        fieldWithPath("id").description("Código do relacionamento entre a ordem de compra e os produtos"),
                        fieldWithPath("product").description("Produto"),
                        fieldWithPath("quantity").description("Quantidade para o produto"))
                .andWithPrefix("[].entryItem.product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("[].entryItem.product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"));
    }

    private InsertEntryRequest createInsertEntryRequest() {
        final PurchaseOrder order = createAndSavePurchaseOrder();

        InsertEntryRequest request = new InsertEntryRequest();
        request.setOrder(order.getId());
        request.setFiscalDocument("DOCUMENT_ABCD");
        request.setDocumentAccessCode("12345");
        request.setNote("Observações");
        request.setIsManual(Boolean.TRUE);
        request.setUpdateStock(Boolean.TRUE);
        request.setItems(order.getItems().stream()
                .limit(2)
                .map(orderItem -> {
                    InsertEntryItemRequest entryItem = new InsertEntryItemRequest();
                    entryItem.setOrderItem(orderItem.getId());
                    entryItem.setReceivedQuantity(orderItem.getQuantity());
                    entryItem.setDocumentProductCode("CODE_PRODUCT_321");
                    return entryItem;
                }).collect(Collectors.toList()));

        return request;
    }

}
