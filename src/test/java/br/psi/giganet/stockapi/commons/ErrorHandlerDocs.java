package br.psi.giganet.stockapi.commons;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.exception.exception.UnauthorizedException;
import br.psi.giganet.stockapi.products.controller.ProductController;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ErrorHandlerDocs extends BuilderIntegrationTest {

    @MockBean
    private ProductController controller;

    @Autowired
    public ErrorHandlerDocs(
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;
    }

    @Test
    public void notFound() throws Exception {
        this.mockMvc.perform(get("/employees/9999"))
                .andExpect(status().isNotFound())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("error").description("Descrição do erro encontrado"))));
    }

    @Test
    public void badRequest() throws Exception {
        this.mockMvc.perform(post("/patrimonies"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())));
    }

    @RoleTestAdmin
    public void forbidden() throws Exception {
        this.mockMvc.perform(get("/units"))
                .andExpect(status().isForbidden())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())));
    }

    @Test()
    public void internalServerError() throws Exception {
        Mockito.when(controller.findById("1")).thenThrow(new RuntimeException("Tested error"));
        this.mockMvc.perform(get("/products/1"))
                .andExpect(status().isInternalServerError())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("error").description("Descrição do erro encontrado"))));
    }

    @Test
    public void unauthenticated() throws Exception {
        Mockito.when(controller.findById("2")).thenThrow(UnauthorizedException.class);
        this.mockMvc.perform(get("/products/2"))
                .andExpect(status().isUnauthorized())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("error").description("Descrição do erro encontrado"))));
    }

}
