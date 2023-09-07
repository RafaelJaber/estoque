package br.psi.giganet.stockapi.technicians.tests;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.technician.model.TechnicianSector;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.technician.technician_product_category.controller.request.TechnicianSectorProductCategoryRequest;
import br.psi.giganet.stockapi.technician.technician_product_category.model.TechnicianSectorProductCategory;
import br.psi.giganet.stockapi.technician.technician_product_category.repository.TechnicianSectorProductCategoryRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Arrays;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TechnicianSectorProductCategoryTests extends BuilderIntegrationTest implements RolesIntegrationTest {

    @Autowired
    public TechnicianSectorProductCategoryTests(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            TechnicianRepository technicianRepository,
            TechnicianSectorProductCategoryRepository technicianSectorProductCategoryRepository,
            ProductCategoryRepository productCategoryRepository,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.technicianRepository = technicianRepository;
        this.technicianSectorProductCategoryRepository = technicianSectorProductCategoryRepository;
        this.productCategoryRepository = productCategoryRepository;
        createCurrentUser();

    }

    @Override
    @RoleTestAdmin
    public void readAuthorized() throws Exception {
        for (int i = 0; i < 4; i++) {
            technicianSectorProductCategoryRepository.saveAndFlush(
                    new TechnicianSectorProductCategory(
                            createAndSaveCategory(),
                            i % 2 == 0 ? TechnicianSector.INSTALLATION : TechnicianSector.REPAIR));
        }

        this.mockMvc.perform(get("/technicians/sectors/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/technicians/sectors/categories/{sector}", TechnicianSector.INSTALLATION)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestRoot
    public void writeAuthorized() throws Exception {
        TechnicianSector sector = TechnicianSector.INSTALLATION;
        this.mockMvc.perform(put("/technicians/sectors/categories/{sector}", TechnicianSector.INSTALLATION)
                .content(objectMapper.writeValueAsString(createTechnicianSectorProductCategoryRequest(sector)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    public void readUnauthorized() {
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        TechnicianSector sector = TechnicianSector.INSTALLATION;
        this.mockMvc.perform(put("/technicians/sectors/categories/{sector}", TechnicianSector.INSTALLATION)
                .content(objectMapper.writeValueAsString(createTechnicianSectorProductCategoryRequest(sector)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private TechnicianSectorProductCategoryRequest createTechnicianSectorProductCategoryRequest(TechnicianSector sector) {
        TechnicianSectorProductCategoryRequest request = new TechnicianSectorProductCategoryRequest();
        request.setSector(sector);
        request.setCategories(
                Arrays.asList(
                        createAndSaveCategory().getId(),
                        createAndSaveCategory().getId()));

        return request;
    }

}
