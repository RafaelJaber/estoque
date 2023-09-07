package br.psi.giganet.stockapi.templates.adapter;

import br.psi.giganet.stockapi.patrimonies.adapter.PatrimonyAdapter;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import br.psi.giganet.stockapi.templates.controller.request.InsertTemplateRequest;
import br.psi.giganet.stockapi.templates.controller.request.UpdateTemplateRequest;
import br.psi.giganet.stockapi.templates.controller.response.TemplateItemResponse;
import br.psi.giganet.stockapi.templates.controller.response.TemplateMountResponse;
import br.psi.giganet.stockapi.templates.controller.response.TemplateProjection;
import br.psi.giganet.stockapi.templates.controller.response.TemplateResponse;
import br.psi.giganet.stockapi.templates.model.Template;
import br.psi.giganet.stockapi.templates.model.TemplateItem;
import br.psi.giganet.stockapi.templates.service.dto.TemplateMountDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TemplateAdapter {

    @Autowired
    private ProductAdapter productAdapter;
    @Autowired
    private PatrimonyAdapter patrimonyAdapter;

    public Template transform(InsertTemplateRequest request) {
        Template template = new Template();
        template.setName(request.getName());
        template.setItems(
                request.getItems().stream()
                        .map(item -> {
                            TemplateItem templateItem = new TemplateItem();
                            templateItem.setTemplate(template);
                            templateItem.setProduct(productAdapter.create(item.getProduct()));
                            templateItem.setQuantity(item.getQuantity());

                            return templateItem;
                        }).collect(Collectors.toList()));

        return template;
    }

    public Template transform(UpdateTemplateRequest request) {
        Template template = new Template();
        template.setId(request.getId());
        template.setName(request.getName());
        template.setItems(
                request.getItems().stream()
                        .map(item -> {
                            TemplateItem templateItem = new TemplateItem();
                            templateItem.setId(item.getId());
                            templateItem.setTemplate(template);
                            templateItem.setProduct(productAdapter.create(item.getProduct()));
                            templateItem.setQuantity(item.getQuantity());

                            return templateItem;
                        }).collect(Collectors.toList()));

        return template;
    }

    public TemplateProjection transform(Template template) {
        TemplateProjection projection = new TemplateProjection();
        projection.setId(template.getId());
        projection.setName(template.getName());
        return projection;
    }

    public TemplateProjection transformToResponse(Template template) {
        TemplateResponse response = new TemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setItems(
                template.getItems().stream()
                        .map(item -> {
                            TemplateItemResponse itemResponse = new TemplateItemResponse();
                            itemResponse.setId(item.getId());
                            itemResponse.setProduct(productAdapter.transform(item.getProduct()));
                            itemResponse.setQuantity(item.getQuantity());

                            return itemResponse;
                        }).collect(Collectors.toList()));
        return response;
    }

    public List<TemplateMountResponse> transform(List<TemplateMountDTO> templates) {
        return templates.stream()
                .map(dto -> {
                    TemplateMountResponse response = new TemplateMountResponse();
                    response.setProduct(productAdapter.transform(dto.getProduct()));
                    response.setQuantity(dto.getQuantity());
                    response.setAvailableQuantityOnDestiny(dto.getAvailableQuantityOnDestiny());

                    return response;
                }).collect(Collectors.toList());
    }

}
