package br.psi.giganet.stockapi.technicians.docs;

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
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.util.Arrays;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TechnicianSectorProductCategoryDocs extends BuilderIntegrationTest {

    @Autowired
    public TechnicianSectorProductCategoryDocs(
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

    @RoleTestRoot
    @Transactional
    public void findAll() throws Exception {
        for (int i = 0; i < 4; i++) {
            technicianSectorProductCategoryRepository.saveAndFlush(
                    new TechnicianSectorProductCategory(
                            createAndSaveCategory(),
                            i % 2 == 0 ? TechnicianSector.INSTALLATION : TechnicianSector.REPAIR));
        }

        this.mockMvc.perform(get("/technicians/sectors/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("[]").description("Lista com as associações encontradas"))
                                        .andWithPrefix("[].", getTechnicianSectorProductCategoryResponse())
                                        .andWithPrefix("[].category.", getCategoryResponse())));
    }

    @RoleTestRoot
    @Transactional
    public void findBySector() throws Exception {
        for (int i = 0; i < 2; i++) {
            technicianSectorProductCategoryRepository.saveAndFlush(
                    new TechnicianSectorProductCategory(
                            createAndSaveCategory(),
                            TechnicianSector.INSTALLATION));
        }

        this.mockMvc.perform(get("/technicians/sectors/categories/{sector}", TechnicianSector.INSTALLATION)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("sector").description("Setor a ser filtrado")),
                                responseFields(fieldWithPath("[]").description("Lista com as associações encontradas"))
                                        .andWithPrefix("[].", getTechnicianSectorProductCategoryResponse())
                                        .andWithPrefix("[].category.", getCategoryResponse())));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        TechnicianSector sector = TechnicianSector.INSTALLATION;

        this.mockMvc.perform(put("/technicians/sectors/categories/{sector}", TechnicianSector.INSTALLATION)
                .content(objectMapper.writeValueAsString(createTechnicianSectorProductCategoryRequest(sector)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("sector").description("Setor associado")),
                        requestFields(
                                fieldWithPath("categories").description("Lista com os IDs das categorias associadas"),
                                fieldWithPath("sector").description("Setor associado")),
                        responseFields(fieldWithPath("[]").description("Lista com as associações atualizadas"))
                                .andWithPrefix("[].", getTechnicianSectorProductCategoryResponse())
                                .andWithPrefix("[].category.", getCategoryResponse())));
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

    private FieldDescriptor[] getTechnicianSectorProductCategoryResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("category").description("Categoria associada"),
                fieldWithPath("sector").description("Setor associado")};
    }

    private FieldDescriptor[] getCategoryResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código ID da categoria"),
                fieldWithPath("name").description("Nome da categoria"),
                fieldWithPath("description").description("Descrição da categoria")};
    }

}
