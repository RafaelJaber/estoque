package br.psi.giganet.stockapi.entries.tests;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.address.service.AddressService;
import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.entries.annotations.RoleTestEntriesRead;
import br.psi.giganet.stockapi.entries.annotations.RoleTestEntriesWriteManual;
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
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EntriesTests extends BuilderIntegrationTest implements RolesIntegrationTest {

    private final Entry entryTest;

    @Autowired
    public EntriesTests(
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

        this.branchOfficeRepository = branchOfficeRepository;

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.addressService = addressService;
        this.entryRepository = entryRepository;
        this.stockRepository = stockRepository;
        createCurrentUser();

        entryTest = createAndSaveEntry();
    }

    @Override
    @RoleTestEntriesRead
    public void readAuthorized() throws Exception {
        Entry entry = createAndSaveEntry();
        BranchOffice branchOffice = entry.getBranchOffice();

        this.mockMvc.perform(get("/entries")
                .header("Office-Id", branchOffice.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/entries/{id}", entry.getId())
                .header("Office-Id", branchOffice.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/entries/{id}", entry.getId())
                .header("Office-Id", branchOffice.getId())
                .param("withMetaData", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/entries/items/{id}", entry.getItems().get(0).getId())
                .header("Office-Id", branchOffice.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestEntriesWriteManual
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/entries")
                .content(objectMapper.writeValueAsString(createInsertEntryRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        Entry entry = createAndSaveEntry();
        BranchOffice branchOffice = entry.getBranchOffice();

        this.mockMvc.perform(get("/entries")
                .header("Office-Id", branchOffice.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/entries/{id}", entry.getId())
                .header("Office-Id", branchOffice.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        this.mockMvc.perform(get("/entries/{id}", entry.getId())
                .header("Office-Id", branchOffice.getId())
                .param("withMetaData", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/entries/items/{id}", entry.getItems().get(0).getId())
                .header("Office-Id", branchOffice.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/entries")
                .content(objectMapper.writeValueAsString(createInsertEntryRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @RoleTestRoot
    public void invalidInserts() throws Exception {
        this.mockMvc.perform(post("/entries")
                .content(objectMapper.writeValueAsString(createInvalidInsertEntryRequest(InvalidRequestType.QUANTITY_BIGGER_THAN_ORDER)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/entries")
                .content(objectMapper.writeValueAsString(createInvalidInsertEntryRequest(InvalidRequestType.NO_ITEMS)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/entries")
                .content(objectMapper.writeValueAsString(createInvalidInsertEntryRequest(InvalidRequestType.FINALIZED_ORDER)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/entries")
                .content(objectMapper.writeValueAsString(createInvalidInsertEntryRequest(InvalidRequestType.NEGATIVE_QUANTITY)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/entries")
                .content(objectMapper.writeValueAsString(createInvalidInsertEntryRequest(InvalidRequestType.QUANTITY_ZERO)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @RoleTestRoot
    public void insertEntryTwice() throws Exception {
        PurchaseOrder order = createAndSavePurchaseOrder();

        this.mockMvc.perform(post("/entries")
                .content(objectMapper.writeValueAsString(createInsertEntryRequest(order)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(post("/entries")
                .content(objectMapper.writeValueAsString(createInsertEntryRequest(order)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    private InsertEntryRequest createInsertEntryRequest() {
        return createInsertEntryRequest(createAndSavePurchaseOrder());
    }

    private InsertEntryRequest createInsertEntryRequest(PurchaseOrder order) {
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

    private InsertEntryRequest createInvalidInsertEntryRequest(InvalidRequestType type) {
        final PurchaseOrder order = createAndSavePurchaseOrder();
        InsertEntryRequest request = new InsertEntryRequest();

        if (type == InvalidRequestType.QUANTITY_BIGGER_THAN_ORDER) {
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
                        entryItem.setReceivedQuantity(orderItem.getQuantity() + 1);
                        entryItem.setDocumentProductCode("CODE_PRODUCT_321");
                        return entryItem;
                    }).collect(Collectors.toList()));


        } else if (type == InvalidRequestType.NEGATIVE_QUANTITY) {
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
                        entryItem.setReceivedQuantity(-1d);
                        entryItem.setDocumentProductCode("CODE_PRODUCT_321");
                        return entryItem;
                    }).collect(Collectors.toList()));

        } else if (type == InvalidRequestType.FINALIZED_ORDER) {
            order.setStatus(ProcessStatus.FINALIZED);
            order.getItems().forEach(i -> i.setStatus(ProcessStatus.FINALIZED));
            purchaseOrderRepository.save(order);

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

        } else if (type == InvalidRequestType.NO_ITEMS) {
            request.setOrder(order.getId());
            request.setFiscalDocument("DOCUMENT_ABCD");
            request.setDocumentAccessCode("12345");
            request.setNote("Observações");
            request.setIsManual(Boolean.TRUE);
            request.setUpdateStock(Boolean.TRUE);
            request.setItems(new ArrayList<>());

        } else if (type == InvalidRequestType.QUANTITY_ZERO) {
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
                        entryItem.setReceivedQuantity(0d);
                        entryItem.setDocumentProductCode("CODE_PRODUCT_321");
                        return entryItem;
                    }).collect(Collectors.toList()));

        }
        return request;
    }

    private enum InvalidRequestType {
        QUANTITY_BIGGER_THAN_ORDER,
        NO_ITEMS,
        NEGATIVE_QUANTITY,
        QUANTITY_ZERO,
        FINALIZED_ORDER
    }

}
