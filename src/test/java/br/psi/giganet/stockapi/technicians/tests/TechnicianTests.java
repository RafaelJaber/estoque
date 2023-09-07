package br.psi.giganet.stockapi.technicians.tests;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.technician.controller.request.UpdateTechnicianRequest;
import br.psi.giganet.stockapi.technician.factory.TechnicianFactory;
import br.psi.giganet.stockapi.technician.model.Technician;
import br.psi.giganet.stockapi.technician.model.TechnicianSector;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.technician.service.RemoteTechnicianService;
import br.psi.giganet.stockapi.technician.service.dto.PlainDTO;
import br.psi.giganet.stockapi.technician.service.dto.TechnicianScheduleDTO;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TechnicianTests extends BuilderIntegrationTest implements RolesIntegrationTest {
    @MockBean
    private RemoteTechnicianService remoteTechnicianService;

    private final TechnicianFactory technicianFactory;


    @Autowired
    public TechnicianTests(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            TechnicianRepository technicianRepository,
            StockRepository stockRepository,
            TechnicianFactory technicianFactory,
            BranchOfficeRepository branchOfficeRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.technicianRepository = technicianRepository;
        this.technicianFactory = technicianFactory;
        createCurrentUser();
    }

    @RoleTestRoot
    @Override
    @Transactional
    public void readAuthorized() throws Exception {
        Technician technician = null;
        BranchOffice branchOffice = createAndSaveBranchOffice();
        for (int i = 0; i < 3; i++) {
            technician = createAndSaveTechnician(branchOffice);
        }

        TechnicianStock technicianStock = createAndSaveTechnicianStock(technician);
        this.setupData(technician);

        this.mockMvc.perform(get("/technicians/stocks/{id}", technicianStock.getId())
                .param("initialDate", LocalDate.of(2020, 6, 1).toString())
                .param("finalDate", LocalDate.of(2020, 7, 1).toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/technicians")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/technicians/{id}", technician.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @RoleTestAdmin
    @Override
    @Transactional
    public void readUnauthorized() throws Exception {
        Technician technician = null;
        BranchOffice branchOffice = createAndSaveBranchOffice();
        for (int i = 0; i < 3; i++) {
            technician = createAndSaveTechnician(branchOffice);
        }

        TechnicianStock technicianStock = createAndSaveTechnicianStock(technician);
        this.setupData(technician);

        this.mockMvc.perform(get("/technicians/stocks/{id}", technicianStock.getId())
                .param("initialDate", LocalDate.of(2020, 6, 1).toString())
                .param("finalDate", LocalDate.of(2020, 7, 1).toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/technicians")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/technicians/{id}", technician.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }


    @RoleTestRoot
    @Transactional
    @Override
    public void writeAuthorized() throws Exception {
        Employee e = createAndSaveEmployee();
        e.setPermissions(new HashSet<>(Arrays.asList(
                createAndSavePermission("ROLE_ADMIN"),
                createAndSavePermission("ROLE_UNITS_READ"),
                createAndSavePermission("ROLE_PRODUCTS_READ"),
                createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"),
                createAndSavePermission("ROLE_MOVES_WRITE_ENTRY_ITEMS"),
                createAndSavePermission("ROLE_MOVES_READ"),
                createAndSavePermission("ROLE_PATRIMONIES_LOCATIONS_READ"),
                createAndSavePermission("ROLE_STOCKS_READ"),
                createAndSavePermission("ROLE_PATRIMONIES_WRITE"),
                createAndSavePermission("ROLE_PATRIMONIES_READ"))));
        employeeRepository.saveAndFlush(e);

        Technician technician = createAndSaveTechnicianByEmployee(e);

        this.setupData(technician);

        this.mockMvc.perform(post("/technicians/remote")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        this.mockMvc.perform(put("/technicians/{id}", technician.getId())
                .content(objectMapper.writeValueAsString(createValidUpdateTechnicianRequest(technician)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @RoleTestAdmin
    @Transactional
    @Override
    public void writeUnauthorized() throws Exception {
        Employee e = createAndSaveEmployee();
        e.setPermissions(new HashSet<>(Arrays.asList(
                createAndSavePermission("ROLE_ADMIN"),
                createAndSavePermission("ROLE_UNITS_READ"),
                createAndSavePermission("ROLE_PRODUCTS_READ"),
                createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"),
                createAndSavePermission("ROLE_MOVES_WRITE_ENTRY_ITEMS"),
                createAndSavePermission("ROLE_MOVES_READ"),
                createAndSavePermission("ROLE_PATRIMONIES_LOCATIONS_READ"),
                createAndSavePermission("ROLE_STOCKS_READ"),
                createAndSavePermission("ROLE_PATRIMONIES_WRITE"),
                createAndSavePermission("ROLE_PATRIMONIES_READ"))));
        employeeRepository.saveAndFlush(e);

        Technician technician = createAndSaveTechnicianByEmployee(e);

        this.setupData(technician);

        this.mockMvc.perform(post("/technicians/remote")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/technicians/{id}", technician.getId())
                .content(objectMapper.writeValueAsString(createValidUpdateTechnicianRequest(technician)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private UpdateTechnicianRequest createValidUpdateTechnicianRequest(Technician technician) {
        UpdateTechnicianRequest request = new UpdateTechnicianRequest();
        request.setId(technician.getId());
        request.setName(technician.getName());
        request.setSector(TechnicianSector.REPAIR);
        request.setIsActive(Boolean.TRUE);
        request.setBranchOffice(technician.getBranchOffice().getId());

        return request;
    }

    private void setupData(Technician technician) {
        Mockito.when(this.remoteTechnicianService.getRemoteTechnicians())
                .thenReturn(Arrays.asList(
                        technicianFactory.create(
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                "Jose Das Couves",
                                "tecnico" + getRandomId() + "@giganet.psi.br",
                                UUID.randomUUID().toString(),
                                Boolean.TRUE),
                        technicianFactory.create(
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                "Lucas Henrique",
                                "tecnico" + getRandomId() + "@giganet.psi.br",
                                UUID.randomUUID().toString(),
                                Boolean.TRUE),
                        technicianFactory.create(
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                "Carlos",
                                "tecnico" + getRandomId() + "@giganet.psi.br",
                                UUID.randomUUID().toString(),
                                Boolean.TRUE)
                ));

        Mockito.when(this.remoteTechnicianService.getTechnicianSchedule(
                technician,
                LocalDate.of(2020, 6, 1),
                LocalDate.of(2020, 7, 1)))
                .thenReturn(
                        Arrays.asList(
                                new TechnicianScheduleDTO(
                                        LocalDate.now(),
                                        "Instação predial",
                                        "Av Castelo Branco, 314, Horto, Ipatinga, MG, 35162-123",
                                        "GEPON",
                                        new PlainDTO(UUID.randomUUID().toString(), "Giganet 50 + 50")),
                                new TechnicianScheduleDTO(
                                        LocalDate.now(),
                                        "Instação predial",
                                        "Av Castelo Branco, 314, Horto, Ipatinga, MG, 35162-123",
                                        "GPON",
                                        new PlainDTO(UUID.randomUUID().toString(), "Giganet 50 + 50")),
                                new TechnicianScheduleDTO(
                                        LocalDate.now(),
                                        "Instação predial",
                                        "Av Castelo Branco, 314, Horto, Ipatinga, MG, 35162-123",
                                        "GPON",
                                        new PlainDTO(UUID.randomUUID().toString(), "Giganet 50 + 50"))));

        createAndSavePermission("ROLE_ADMIN");
        createAndSavePermission("ROLE_UNITS_READ");
        createAndSavePermission("ROLE_PRODUCTS_READ");
        createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS");
        createAndSavePermission("ROLE_MOVES_WRITE_ENTRY_ITEMS");
        createAndSavePermission("ROLE_MOVES_READ");
        createAndSavePermission("ROLE_PATRIMONIES_LOCATIONS_READ");
        createAndSavePermission("ROLE_STOCKS_READ");
        createAndSavePermission("ROLE_PATRIMONIES_WRITE");
        createAndSavePermission("ROLE_PATRIMONIES_READ");
    }

}
