package br.psi.giganet.stockapi.commons;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.address.model.Address;
import br.psi.giganet.stockapi.common.address.service.AddressService;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AddressDocs extends BuilderIntegrationTest {

    @MockBean
    private AddressService addressService;

    @Autowired
    public AddressDocs(
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;
    }

    @RoleTestAdmin
    public void findAddressByPostalCode() throws Exception {
        final String cep = "35160296";
        this.setMockAddress(cep);
        this.mockMvc.perform(get("/address")
                .param("cep", cep)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("cep").description(
                                        createDescriptionWithNotNull("Código postal buscado",
                                                "Deve ser informado apenas números"))),
                        responseFields(
                                fieldWithPath("complement").description("Complemento"),
                                fieldWithPath("postalCode").description("CEP"),
                                fieldWithPath("street").description("Rua"),
                                fieldWithPath("number").optional().type(JsonFieldType.STRING).description("Número"),
                                fieldWithPath("district").description("Bairro"),
                                fieldWithPath("city").description("Cidade"),
                                fieldWithPath("state").description("Estado"))));

    }

    public void setMockAddress(String cep) {
        Address address = new Address();
        address.setPostalCode(cep);
        address.setComplement("");
        address.setCity("Ipatinga");
        address.setDistrict("Horto");
        address.setState("MG");
        address.setStreet("Rua Cedro");
        address.setNumber("393");

        when(addressService.findAddressByPostalCode(cep))
                .thenReturn(Optional.of(address));
    }

}
