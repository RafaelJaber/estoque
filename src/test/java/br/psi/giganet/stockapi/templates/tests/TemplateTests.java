package br.psi.giganet.stockapi.templates.tests;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock_moves.repository.StockMovesRepository;
import br.psi.giganet.stockapi.patrimonies.repository.PatrimonyRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.templates.annotations.RoleTestTemplatesRead;
import br.psi.giganet.stockapi.templates.annotations.RoleTestTemplatesWrite;
import br.psi.giganet.stockapi.templates.controller.request.InsertTemplateItemRequest;
import br.psi.giganet.stockapi.templates.controller.request.InsertTemplateRequest;
import br.psi.giganet.stockapi.templates.controller.request.UpdateTemplateItemRequest;
import br.psi.giganet.stockapi.templates.controller.request.UpdateTemplateRequest;
import br.psi.giganet.stockapi.templates.model.Template;
import br.psi.giganet.stockapi.templates.repository.TemplateRepository;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.RolesIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TemplateTests extends BuilderIntegrationTest implements RolesIntegrationTest {

    @Autowired
    public TemplateTests(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository,
            StockRepository stockRepository,
            StockItemRepository stockItemRepository,
            PatrimonyRepository patrimonyRepository,
            StockMovesRepository stockMovesRepository,
            TechnicianRepository technicianRepository,
            TemplateRepository templateRepository,
            BranchOfficeRepository branchOfficeRepository) {

        this.branchOfficeRepository = branchOfficeRepository;

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        this.stockRepository = stockRepository;
        this.stockItemRepository = stockItemRepository;
        this.patrimonyRepository = patrimonyRepository;
        this.stockMovesRepository = stockMovesRepository;
        this.technicianRepository = technicianRepository;
        this.templateRepository = templateRepository;
        createCurrentUser();

    }

    @Override
    @RoleTestTemplatesRead
    @Transactional
    public void readAuthorized() throws Exception {
        for (int i = 0; i < 3; i++) {
            createAndSaveTemplate();
        }

        this.mockMvc.perform(get("/moves/templates")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        Template template = createAndSaveTemplate();
        this.mockMvc.perform(get("/moves/templates/{id}", template.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

    }

    @Override
    @RoleTestTemplatesWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/moves/templates")
                .content(objectMapper.writeValueAsString(createInsertTemplateRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        Template template = createAndSaveTemplate();
        this.mockMvc.perform(put("/moves/templates/{id}", template.getId())
                .content(objectMapper.writeValueAsString(createUpdateTemplateRequest(template)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        template = createAndSaveTemplate();
        this.mockMvc.perform(delete("/moves/templates/{id}", template.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Override
    @RoleTestAdmin
    @Transactional
    public void readUnauthorized() throws Exception {
        for (int i = 0; i < 3; i++) {
            createAndSaveTemplate();
        }

        this.mockMvc.perform(get("/moves/templates")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        Template template = createAndSaveTemplate();
        this.mockMvc.perform(get("/moves/templates/{id}", template.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

    }

    @Override
    @RoleTestAdmin
    @Transactional
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/moves/templates")
                .content(objectMapper.writeValueAsString(createInsertTemplateRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        Template template = createAndSaveTemplate();
        this.mockMvc.perform(put("/moves/templates/{id}", template.getId())
                .content(objectMapper.writeValueAsString(createUpdateTemplateRequest(template)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        template = createAndSaveTemplate();
        this.mockMvc.perform(delete("/moves/templates/{id}", template.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private InsertTemplateRequest createInsertTemplateRequest() {
        Product p1 = createAndSaveProduct();
        Product p2 = createAndSaveProduct();

        InsertTemplateRequest request = new InsertTemplateRequest();
        request.setName("Template");
        request.setItems(new ArrayList<>());

        InsertTemplateItemRequest item1Request = new InsertTemplateItemRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        request.getItems().add(item1Request);

        InsertTemplateItemRequest item2Request = new InsertTemplateItemRequest();
        item2Request.setProduct(p2.getId());
        item2Request.setQuantity(1d);
        request.getItems().add(item2Request);

        return request;
    }

    private UpdateTemplateRequest createUpdateTemplateRequest(Template template) {
        Product p1 = createAndSaveProduct();

        UpdateTemplateRequest request = new UpdateTemplateRequest();
        request.setName("Template " + getRandomId());
        request.setItems(template.getItems().stream().limit(1).map(item -> {
            UpdateTemplateItemRequest templateItem = new UpdateTemplateItemRequest();
            templateItem.setId(item.getId());
            templateItem.setProduct(item.getProduct().getId());
            templateItem.setQuantity(item.getQuantity() + 1d);

            return templateItem;
        }).collect(Collectors.toList()));

        UpdateTemplateItemRequest item1Request = new UpdateTemplateItemRequest();
        item1Request.setProduct(p1.getId());
        item1Request.setQuantity(1d);
        request.getItems().add(item1Request);

        return request;
    }


}
