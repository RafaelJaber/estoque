package br.psi.giganet.stockapi.templates.controller.response;

import lombok.Data;

import java.util.List;

@Data
public class TemplateResponse extends TemplateProjection {

    private List<TemplateItemResponse> items;

}
