package br.psi.giganet.stockapi.patrimonies_locations.tests;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.patrimonies_locations.annotations.RoleTestPatrimoniesLocationsRead;
import br.psi.giganet.stockapi.patrimonies_locations.annotations.RoleTestPatrimoniesLocationsWrite;
import br.psi.giganet.stockapi.patrimonies_locations.controller.request.InsertPatrimonyLocationRequest;
import br.psi.giganet.stockapi.patrimonies_locations.controller.request.UpdatePatrimonyLocationRequest;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocationType;
import br.psi.giganet.stockapi.patrimonies_locations.repository.PatrimonyLocationRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.util.UUID;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PatrimonyLocationTests extends BuilderIntegrationTest implements RolesIntegrationTest {


    @Autowired
    public PatrimonyLocationTests(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            PatrimonyLocationRepository patrimonyLocationRepository,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;

        this.patrimonyLocationRepository = patrimonyLocationRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        createCurrentUser();

    }

    @Override
    @RoleTestPatrimoniesLocationsRead
    @Transactional
    public void readAuthorized() throws Exception {
        PatrimonyLocation location = null;
        for (int i = 0; i < 3; i++) {
            location = createAndSavePatrimonyLocation();
        }

        this.mockMvc.perform(get("/patrimonies/locations")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/patrimonies/locations/{id}", location.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/basic/patrimonies/locations")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

    }

    @Override
    @RoleTestPatrimoniesLocationsWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/patrimonies/locations")
                .content(objectMapper.writeValueAsString(createInsertPatrimonyLocationRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        PatrimonyLocation location = createAndSavePatrimonyLocation();
        this.mockMvc.perform(put("/patrimonies/locations/{id}", location.getId())
                .content(objectMapper.writeValueAsString(createUpdatePatrimonyLocationRequest(location)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    @Transactional
    public void readUnauthorized() throws Exception {
        PatrimonyLocation location = null;
        for (int i = 0; i < 3; i++) {
            location = createAndSavePatrimonyLocation();
        }

        this.mockMvc.perform(get("/patrimonies/locations")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/patrimonies/locations/{id}", location.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/basic/patrimonies/locations")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    @Transactional
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/patrimonies/locations")
                .content(objectMapper.writeValueAsString(createInsertPatrimonyLocationRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        PatrimonyLocation location = createAndSavePatrimonyLocation();
        this.mockMvc.perform(put("/patrimonies/locations/{id}", location.getId())
                .content(objectMapper.writeValueAsString(createUpdatePatrimonyLocationRequest(location)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }


    private InsertPatrimonyLocationRequest createInsertPatrimonyLocationRequest() {
        InsertPatrimonyLocationRequest request = new InsertPatrimonyLocationRequest();
        request.setCode(UUID.randomUUID().toString().substring(0, 8));
        request.setName("Localização");
        request.setNote("Observação");
        request.setType(PatrimonyLocationType.CUSTOMER);

        return request;
    }

    private UpdatePatrimonyLocationRequest createUpdatePatrimonyLocationRequest(PatrimonyLocation location) {
        UpdatePatrimonyLocationRequest request = new UpdatePatrimonyLocationRequest();
        request.setId(location.getId());
        request.setCode(location.getCode());
        request.setType(PatrimonyLocationType.CUSTOMER);
        request.setName("Localização Atualizada");
        request.setNote("Observação Atualizada");

        return request;
    }


}
