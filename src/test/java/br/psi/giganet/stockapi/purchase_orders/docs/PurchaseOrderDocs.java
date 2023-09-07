package br.psi.giganet.stockapi.purchase_orders.docs;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.address.service.AddressService;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PurchaseOrderDocs extends BuilderIntegrationTest {

    private final PurchaseOrder orderTest;

    @Autowired
    public PurchaseOrderDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            AddressService addressService,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.addressService = addressService;
        createCurrentUser();

        orderTest = createAndSavePurchaseOrder();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                getPurchaseOrderProjection()));
    }

    @RoleTestRoot
    public void findAllItems() throws Exception {
        this.mockMvc.perform(get("/purchase-orders/items")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("page")
                                                .optional()
                                                .description(createDescription(
                                                        "Número da página solicitada",
                                                        "Valor baseado em 0 (ex: pagina inicial: 0)",
                                                        "Valor default: \"0\"")),
                                        parameterWithName("pageSize")
                                                .optional()
                                                .description(createDescription(
                                                        "Tamanho da página solicitada",
                                                        "Valor default: \"100\""))),
                                getPageOrderItemProjection()));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/purchase-orders/{id}", orderTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(createDescriptionWithNotNull("Código da ordem de compra procurada"))),
                                getPurchaseOrderResponse()));
    }

    @RoleTestRoot
    public void findByIdFilteringPendingItems() throws Exception {
        this.mockMvc.perform(get("/purchase-orders/{id}/pending", orderTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(createDescriptionWithNotNull("Código da ordem de compra procurada"))),
                                getPurchaseOrderResponse()));
    }

    private ResponseFieldsSnippet getPageOrderItemProjection() {
        return getPageContent("Lista com todos os itens encontrados")
                .andWithPrefix("content[].",
                        fieldWithPath("status").description("Situação do item na ordem de compra"),
                        fieldWithPath("createdDate").description("Data da criação da ordem do respectivo produto. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("product").description("Produto"),
                        fieldWithPath("supplier").description("Fornecedor selecionado na compra"),
                        fieldWithPath("purchaseOrder").description("Código da ordem de compra associada"),
                        fieldWithPath("quantity").description("Quantidade comprada na ordem de compra"),
                        fieldWithPath("price").description("Preço unitário para o produto"))
                .andWithPrefix("content[].product.",
                        fieldWithPath("id").optional().type(JsonFieldType.NULL).description(""),
                        fieldWithPath("code").description("Código interno de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"))
                .andWithPrefix("content[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"));
    }

    private ResponseFieldsSnippet getPurchaseOrderProjection() {
        return responseFields(fieldWithPath("[]")
                .description("Lista de todas as ordens de compra em ordem decrescente pela data de cadastro"))
                .andWithPrefix("[].",
                        fieldWithPath("id").description("Código da ordem de compra"),
                        fieldWithPath("total").description("Valor total da ordem"),
                        fieldWithPath("date").description("Data da ordem. Exemplo: " + ZonedDateTime.now().toString()),
                        fieldWithPath("status").description("Status atual da ordem de compra"),
                        fieldWithPath("supplier").description("Fornecedor selecionado para a cotação"),
                        fieldWithPath("description").type(JsonFieldType.STRING).optional().description("Descrição da ordem de compra"),
                        fieldWithPath("responsible").description("Responsável pela ordem de compra"))
                .andWithPrefix("[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"));
    }

    private ResponseFieldsSnippet getPurchaseOrderResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da ordem de compra"),
                fieldWithPath("total").description("Valor total da ordem"),
                fieldWithPath("date").description("Data da ordem. Exemplo: " + ZonedDateTime.now().toString()),
                fieldWithPath("status").description("Status atual da ordem de compra"),
                fieldWithPath("note")
                        .type(JsonFieldType.STRING)
                        .optional()
                        .description("Observações da ordem de compra, caso existam"),
                fieldWithPath("description")
                        .type(JsonFieldType.STRING)
                        .optional()
                        .description("Descrição da ordem de compra, caso exista"),
                fieldWithPath("responsible").description("Responsável pela ordem de compra"),
                fieldWithPath("costCenter").description("Centro de custo da ordem de compra"),
                fieldWithPath("dateOfNeed").description("Data de necessidade para os itens"),
                fieldWithPath("freight").description("Frete referente a ordem de compra"),
                fieldWithPath("supplier").description("Fornecedor contemplado durante a cotação para os respectivos itens"),
                fieldWithPath("items").description("Itens da ordem de compra"))
                .andWithPrefix("freight.",
                        fieldWithPath("id").description("Código do registro do frete"),
                        fieldWithPath("type").description("Tipo do frete. FOB ou CIF"),
                        fieldWithPath("deliveryDate").description("Data estipulada para a entrega"),
                        fieldWithPath("deliveryAddress").description("Endereço de entrega"),
                        fieldWithPath("price").description("Preço total do frete"))
                .andWithPrefix("freight.deliveryAddress.",
                        fieldWithPath("complement").description("Complemento"),
                        fieldWithPath("postalCode").description("CEP"),
                        fieldWithPath("street").description("Rua"),
                        fieldWithPath("number").description("Número"),
                        fieldWithPath("district").description("Bairro"),
                        fieldWithPath("city").description("Cidade"),
                        fieldWithPath("state").description("Estado"))
                .andWithPrefix("supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"))
                .andWithPrefix("items[].",
                        fieldWithPath("id").description("Código do relacionamento entre a ordem de compra e os produtos"),
                        fieldWithPath("product").description("Produto"),
                        fieldWithPath("quantity").description("Quantidade para o produto"),
                        fieldWithPath("unit").description("Unidade selecionada para o produto"),
                        fieldWithPath("ipi").description("Valor do IPI, em porcentagem"),
                        fieldWithPath("icms").description("Valor do ICMS, em porcentagem"),
                        fieldWithPath("price").description("Preço unitário para o produto"),
                        fieldWithPath("discount").optional().type(JsonFieldType.NUMBER).description("Desconto do respectivo item, caso exista"),
                        fieldWithPath("total").description("Preço total do respectivo item"),
                        fieldWithPath("status").description("Situação do item"))
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

}
