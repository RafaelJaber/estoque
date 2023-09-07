package br.psi.giganet.stockapi.utils;

import br.psi.giganet.stockapi.utils.messages.Messages;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.ParameterDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

public class DocsDescriptions {

    protected String createDescription(String... values) {
        return String.join(". ", values);
    }

    protected String createDescriptionWithNotEmpty(String... values) {
        List<String> v = new ArrayList<>(Arrays.asList(values));
        v.add(Messages.NOT_EMPTY.getMessage());
        return String.join(". ", v);
    }

    protected String createDescriptionWithNotNull(String... values) {
        List<String> v = new ArrayList<>(Arrays.asList(values));
        v.add(Messages.NOT_NULL.getMessage());
        return String.join(". ", v);
    }

    protected String createDescriptionWithPositiveAndNotNull(String... values) {
        List<String> v = new ArrayList<>(Arrays.asList(values));
        v.add(Messages.POSITIVE_NUMBER.getMessage());
        v.add(Messages.NOT_NULL.getMessage());
        return String.join(". ", v);
    }

    protected ResponseFieldsSnippet getPageContent(String contentDescription) {
        return responseFields(
                fieldWithPath("content").description(contentDescription),
                fieldWithPath("pageable").description("Informações sobre a paginação executada"),
                fieldWithPath("totalPages").description("Número total de páginas"),
                fieldWithPath("totalElements").description("Número total de elementos"),
                fieldWithPath("last").description("Retorna 'true' se a pagina atual é a última"),
                fieldWithPath("number").description("Número da página retornada"),
                fieldWithPath("size").description("Tamanho da página atual"),
                fieldWithPath("sort").description("Informações sobre a ordenação da página"),
                fieldWithPath("numberOfElements").description("Número de elementos da página"),
                fieldWithPath("first").description("Retorna 'true' se a pagina atual é a primeira"),
                fieldWithPath("empty").description("Retorna 'true' se a pagina atual é vazia"))
                .andWithPrefix("pageable.",
                        fieldWithPath("sort").description("Retorna informações sobre a ordenação"),
                        fieldWithPath("offset").description("Retorna o offset da página atual"),
                        fieldWithPath("pageNumber").description("Retorna o número da página atual"),
                        fieldWithPath("pageSize").description("Retorna o tamanho da página solicitada"),
                        fieldWithPath("paged").description("Retorna 'true' se a paginação está sendo executada"),
                        fieldWithPath("unpaged").description("Retorna 'true' se a paginação não está sendo executada"))
                .andWithPrefix("pageable.sort.",
                        fieldWithPath("sorted").description("Retorna 'true' se a pagina atual está ordenada"),
                        fieldWithPath("unsorted").description("Retorna 'true' se a pagina atual está desordenada"),
                        fieldWithPath("empty").description("Retorna 'true' se a pagina atual é vazia"))
                .andWithPrefix("sort.",
                        fieldWithPath("sorted").description("Retorna 'true' se a pagina atual está ordenada"),
                        fieldWithPath("unsorted").description("Retorna 'true' se a pagina atual está desordenada"),
                        fieldWithPath("empty").description("Retorna 'true' se a pagina atual é vazia"));
    }

    protected ParameterDescriptor getPagePathParameter() {
        return parameterWithName("page")
                .optional()
                .description(createDescription(
                        "Número da página solicitada",
                        "Valor baseado em 0 (ex: pagina inicial: 0)",
                        "Valor default: \"0\""));
    }

    protected ParameterDescriptor getPageSizePathParameter() {
        return parameterWithName("pageSize")
                .optional()
                .description(createDescription(
                        "Tamanho da página solicitada",
                        "Valor default: \"100\""));
    }

}
