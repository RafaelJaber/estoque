package br.psi.giganet.stockapi.patrimonies.tests;

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
import br.psi.giganet.stockapi.patrimonies.annotations.RoleTestPatrimoniesRead;
import br.psi.giganet.stockapi.patrimonies.annotations.RoleTestPatrimoniesWrite;
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
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PatrimoniesTests extends BuilderIntegrationTest implements RolesIntegrationTest {

    private final Patrimony patrimony;

    @Autowired
    public PatrimoniesTests(
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


    @Override
    @RoleTestPatrimoniesRead
    @Transactional
    public void readAuthorized() throws Exception {
        PatrimonyLocation location = createAndSavePatrimonyLocation();
        Product product = createAndSaveProduct();
        Patrimony patrimony = null;
        final int patrimoniesSize = 3;
        for (int i = 0; i < patrimoniesSize; i++) {
            patrimony = createAndSavePatrimony(location, product);
            createAndSavePatrimonyMove(patrimony);
            patrimonyRepository.save(patrimony);
        }


        this.mockMvc.perform(get("/patrimonies")
                .param("page", "0")
                .param("pageSize", "50")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/patrimonies")
                .param("queries", "pro")
                .param("queries", "gal")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print());

        this.mockMvc.perform(get("/patrimonies/current-locations/{location}", location.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(patrimoniesSize)));

        this.mockMvc.perform(get("/patrimonies/current-locations/{location}/products/{product}", location.getId(), product.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(patrimoniesSize)));

        this.mockMvc.perform(get("/patrimonies/products/{product}", product.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(patrimoniesSize)));

        this.mockMvc.perform(get("/patrimonies/{id}", patrimony.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moves", Matchers.nullValue()));

        this.mockMvc.perform(get("/patrimonies/{id}", patrimony.getId())
                .param("withHistory", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moves", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(get("/patrimonies/unique-codes/{code}", patrimony.getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        Technician technician = createAndSaveTechnician();
        PatrimonyLocation technicianLocation = createAndSavePatrimonyLocation(technician.getUserId());
        Product technicianProduct = createAndSaveProduct();
        for (int i = 0; i < 2; i++) {
            createAndSavePatrimony(technicianLocation, product);
        }

        this.mockMvc.perform(get(
                "/basic/patrimonies/current-locations/technicians/{userId}/products/{product}",
                technician.getUserId(),
                technicianProduct.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/basic/patrimonies/{id}", patrimony.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moves", Matchers.nullValue()));

        PatrimonyLocation customerLocation = new PatrimonyLocation();
        customerLocation.setCode(UUID.randomUUID().toString().substring(0, 10));
        customerLocation.setNote("Obs");
        customerLocation.setName("Cliente Lucas");
        customerLocation.setType(PatrimonyLocationType.CUSTOMER);
        customerLocation = patrimonyLocationRepository.saveAndFlush(customerLocation);

        product = createAndSaveProduct();
        for (int i = 0; i < 2; i++) {
            createAndSavePatrimony(customerLocation, product);
        }

        this.mockMvc.perform(get("/basic/patrimonies/current-locations/customers/{userId}", customerLocation.getCode())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(2)));
    }

    @Override
    @RoleTestPatrimoniesWrite
    @Transactional
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/patrimonies")
                .content(objectMapper.writeValueAsString(createInsertPatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(put("/patrimonies/{id}", patrimony.getId())
                .content(objectMapper.writeValueAsString(createUpdatePatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/patrimonies/batch")
                .content(objectMapper.writeValueAsString(createBatchInsertPatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(post("/patrimonies/{id}/move", patrimony.getId())
                .content(objectMapper.writeValueAsString(createMovePatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        Patrimony patrimony = createAndSavePatrimony();
        this.mockMvc.perform(delete("/patrimonies/{id}", patrimony.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Override
    @RoleTestAdmin
    @Transactional
    public void readUnauthorized() throws Exception {
        PatrimonyLocation location = createAndSavePatrimonyLocation();
        Product product = createAndSaveProduct();
        Patrimony patrimony = null;
        final int patrimoniesSize = 3;
        for (int i = 0; i < patrimoniesSize; i++) {
            patrimony = createAndSavePatrimony(location, product);
            createAndSavePatrimonyMove(patrimony);
            patrimonyRepository.save(patrimony);
        }

        this.mockMvc.perform(get("/patrimonies")
                .param("page", "0")
                .param("pageSize", "50")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/patrimonies")
                .param("queries", "pro")
                .param("queries", "gal")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/patrimonies/current-locations/{location}", location.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/patrimonies/current-locations/{location}/products/{product}", location.getId(), product.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/patrimonies/products/{product}", product.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/patrimonies/{id}", patrimony.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/patrimonies/{id}", patrimony.getId())
                .param("withHistory", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/patrimonies/unique-codes/{code}", patrimony.getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        Technician technician = createAndSaveTechnician();
        PatrimonyLocation technicianLocation = createAndSavePatrimonyLocation(technician.getUserId());
        Product technicianProduct = createAndSaveProduct();
        for (int i = 0; i < 2; i++) {
            createAndSavePatrimony(technicianLocation, product);
        }

        this.mockMvc.perform(get(
                "/basic/patrimonies/current-locations/technicians/{userId}/products/{product}",
                technician.getUserId(),
                technicianProduct.getId())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/basic/patrimonies/{id}", patrimony.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());


        PatrimonyLocation customerLocation = new PatrimonyLocation();
        customerLocation.setCode(UUID.randomUUID().toString().substring(0, 10));
        customerLocation.setNote("Obs");
        customerLocation.setName("Cliente Lucas");
        customerLocation.setType(PatrimonyLocationType.CUSTOMER);
        customerLocation = patrimonyLocationRepository.saveAndFlush(customerLocation);

        product = createAndSaveProduct();
        for (int i = 0; i < 2; i++) {
            createAndSavePatrimony(customerLocation, product);
        }
        this.mockMvc.perform(get("/basic/patrimonies/current-locations/customers/{userId}", customerLocation.getCode())
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/patrimonies")
                .content(objectMapper.writeValueAsString(createInsertPatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/patrimonies/{id}", patrimony.getId())
                .content(objectMapper.writeValueAsString(createUpdatePatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(post("/patrimonies/batch")
                .content(objectMapper.writeValueAsString(createBatchInsertPatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(post("/patrimonies/{id}/move", patrimony.getId())
                .content(objectMapper.writeValueAsString(createMovePatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        Patrimony patrimony = createAndSavePatrimony();
        this.mockMvc.perform(delete("/patrimonies/{id}", patrimony.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_PATRIMONIES_WRITE"})
    public void basicWriteAuthorized() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_PATRIMONIES_WRITE"));
        Technician technician = createAndSaveTechnicianByEmployee(employeeRepository.save(e));
        createAndSavePatrimonyLocation(technician.getUserId());

        this.mockMvc.perform(post("/basic/patrimonies/technicians")
                .content(objectMapper.writeValueAsString(createBasicInsertPatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(put("/basic/patrimonies/{id}", patrimony.getId())
                .content(objectMapper.writeValueAsString(createUpdatePatrimonyRequest()))
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
        e.getPermissions().add(createAndSavePermission("ROLE_PATRIMONIES_WRITE"));
        Technician technician = createAndSaveTechnicianByEmployee(employeeRepository.save(e));
        createAndSavePatrimonyLocation(technician.getUserId());

        this.mockMvc.perform(post("/basic/patrimonies/technicians")
                .content(objectMapper.writeValueAsString(createBasicInsertPatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/basic/patrimonies/{id}", patrimony.getId())
                .content(objectMapper.writeValueAsString(createUpdatePatrimonyRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @RoleTestPatrimoniesWrite
    public void invalidInsert() throws Exception {
        this.mockMvc.perform(post("/patrimonies")
                .content(objectMapper.writeValueAsString(createInvalidInsertPatrimonyRequest(Implementation.INVALID_PRODUCT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/patrimonies")
                .content(objectMapper.writeValueAsString(createInvalidInsertPatrimonyRequest(Implementation.INVALID_CURRENT_LOCATION)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/patrimonies")
                .content(objectMapper.writeValueAsString(createInvalidInsertPatrimonyRequest(Implementation.INVALID_CODE)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());


        this.mockMvc.perform(post("/patrimonies")
                .content(objectMapper.writeValueAsString(createInvalidBatchInsertPatrimonyRequest(Implementation.INVALID_PRODUCT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/patrimonies")
                .content(objectMapper.writeValueAsString(createInvalidBatchInsertPatrimonyRequest(Implementation.INVALID_CURRENT_LOCATION)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/patrimonies")
                .content(objectMapper.writeValueAsString(createInvalidBatchInsertPatrimonyRequest(Implementation.INVALID_CODE)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

    }

    @RoleTestPatrimoniesWrite
    public void invalidUpdate() throws Exception {
        this.mockMvc.perform(put("/patrimonies/{id}", patrimony.getId())
                .content(objectMapper.writeValueAsString(createInvalidUpdatePatrimonyRequest(Implementation.INVALID_PRODUCT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/patrimonies/{id}", patrimony.getId())
                .content(objectMapper.writeValueAsString(createInvalidUpdatePatrimonyRequest(Implementation.INVALID_CODE)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }


    @RoleTestPatrimoniesWrite
    @Transactional
    public void invalidInsertWithSameCode() throws Exception {
        InsertPatrimonyRequest request = createInsertPatrimonyRequest();
        this.mockMvc.perform(post("/patrimonies")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(post("/patrimonies")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Transactional
    @Test
    @WithMockUser(username = "teste_technician@teste.com", authorities = {"ROLE_ADMIN", "ROLE_PATRIMONIES_WRITE"})
    public void basicInvalidInsert() throws Exception {
        Employee e = createAndSaveEmployee("teste_technician@teste.com");
        e.getPermissions().remove(new Permission("ROLE_ROOT"));
        e.getPermissions().add(createAndSavePermission("ROLE_PATRIMONIES_WRITE"));
        Technician technician = createAndSaveTechnicianByEmployee(employeeRepository.save(e));
        createAndSavePatrimonyLocation(technician.getUserId());

        this.mockMvc.perform(post("/basic/patrimonies/technicians")
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertPatrimonyRequest(Implementation.INVALID_PRODUCT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/basic/patrimonies/technicians")
                .content(objectMapper.writeValueAsString(createInvalidBasicInsertPatrimonyRequest(Implementation.INVALID_CODE)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

    }

    @RoleTestPatrimoniesWrite
    public void basicInvalidUpdate() throws Exception {
        this.mockMvc.perform(put("/basic/patrimonies/{id}", patrimony.getId())
                .content(objectMapper.writeValueAsString(createInvalidUpdatePatrimonyRequest(Implementation.INVALID_PRODUCT)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/basic/patrimonies/{id}", patrimony.getId())
                .content(objectMapper.writeValueAsString(createInvalidUpdatePatrimonyRequest(Implementation.INVALID_CODE)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
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
            request.getCodes().add(UUID.randomUUID().toString());
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

    private MovePatrimonyRequest createMovePatrimonyRequest() {
        MovePatrimonyRequest request = new MovePatrimonyRequest();
        request.setPatrimony(patrimony.getId());
        request.setNewLocation(createAndSavePatrimonyLocation().getId());
        request.setNote("Observação do patrimônio");

        return request;
    }

    private InsertPatrimonyRequest createInsertPatrimonyRequest() {
        return createInsertPatrimonyRequest(patrimony);
    }

    private InsertPatrimonyRequest createInsertPatrimonyRequest(Patrimony patrimony) {
        InsertPatrimonyRequest request = new InsertPatrimonyRequest();
        request.setCode(randomMACAddress());
        request.setCodeType(PatrimonyCodeType.MAC_ADDRESS);
        request.setProduct(patrimony.getProduct().getId());
        request.setCurrentLocation(patrimony.getCurrentLocation().getId());
        request.setNote("Observação do patrimônio");

        createAndSaveValidMacAddress(request.getCode());

        return request;
    }

    private UpdatePatrimonyRequest createUpdatePatrimonyRequest() {
        return createUpdatePatrimonyRequest(patrimony);
    }

    private UpdatePatrimonyRequest createUpdatePatrimonyRequest(Patrimony patrimony) {
        UpdatePatrimonyRequest request = new UpdatePatrimonyRequest();
        request.setId(patrimony.getId());
        request.setProduct(patrimony.getProduct().getId());
        request.setNote("Observação");

        return request;
    }

    private InsertPatrimonyRequest createInvalidInsertPatrimonyRequest(Implementation implementation) {
        InsertPatrimonyRequest request = new InsertPatrimonyRequest();

        if (implementation == Implementation.INVALID_CODE) {
            request.setCode("");
            request.setProduct(patrimony.getProduct().getId());
            request.setCurrentLocation(patrimony.getCurrentLocation().getId());
            request.setNote("Observação do patrimônio");

        } else if (implementation == Implementation.INVALID_PRODUCT) {
            request.setCode(randomMACAddress());
            request.setProduct("000");
            request.setCurrentLocation(patrimony.getCurrentLocation().getId());
            request.setNote("Observação do patrimônio");


        } else if (implementation == Implementation.INVALID_CURRENT_LOCATION) {
            request.setCode(randomMACAddress());
            request.setProduct(patrimony.getProduct().getId());
            request.setCurrentLocation(-1L);
            request.setNote("Observação do patrimônio");

        }

        createAndSaveValidMacAddress(request.getCode());

        return request;
    }

    private BatchInsertPatrimonyRequest createInvalidBatchInsertPatrimonyRequest(Implementation implementation) {
        BatchInsertPatrimonyRequest request = new BatchInsertPatrimonyRequest();

        if (implementation == Implementation.INVALID_CODE) {
            request.setProduct(patrimony.getProduct().getId());
            request.setCurrentLocation(patrimony.getCurrentLocation().getId());
            request.setNote("Observação do patrimônio");

        } else if (implementation == Implementation.INVALID_PRODUCT) {
            request.setCodes(
                    Arrays.asList(
                            UUID.randomUUID().toString().substring(0, 8),
                            UUID.randomUUID().toString().substring(0, 8),
                            UUID.randomUUID().toString().substring(0, 8)));
            request.setProduct("000");
            request.setCurrentLocation(patrimony.getCurrentLocation().getId());
            request.setNote("Observação do patrimônio");

        } else if (implementation == Implementation.INVALID_CURRENT_LOCATION) {
            request.setCodes(
                    Arrays.asList(
                            UUID.randomUUID().toString().substring(0, 8),
                            UUID.randomUUID().toString().substring(0, 8),
                            UUID.randomUUID().toString().substring(0, 8)));
            request.setProduct(patrimony.getProduct().getId());
            request.setCurrentLocation(-1L);
            request.setNote("Observação do patrimônio");

        }


        return request;
    }

    private BasicInsertPatrimonyRequest createInvalidBasicInsertPatrimonyRequest(Implementation implementation) {
        BasicInsertPatrimonyRequest request = new BasicInsertPatrimonyRequest();

        if (implementation == Implementation.INVALID_CODE) {
            request.setCode("");
            request.setProduct(patrimony.getProduct().getId());
            request.setNote("Observação do patrimônio");

        } else if (implementation == Implementation.INVALID_PRODUCT) {
            request.setCode(randomMACAddress());
            request.setProduct("000");
            request.setNote("Observação do patrimônio");

        }
        createAndSaveValidMacAddress(request.getCode());

        return request;
    }

    private UpdatePatrimonyRequest createInvalidUpdatePatrimonyRequest(Implementation implementation) {
        UpdatePatrimonyRequest request = new UpdatePatrimonyRequest();

        if (implementation == Implementation.INVALID_PRODUCT) {
            request.setId(patrimony.getId());
            request.setProduct("");
            request.setNote("Observação");

        }

        return request;
    }

    private enum Implementation {
        INVALID_CODE,
        INVALID_PRODUCT,
        INVALID_CURRENT_LOCATION,
    }

}
