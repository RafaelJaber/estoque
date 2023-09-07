package br.psi.giganet.stockapi.commons.valid_mac_addresses;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.valid_mac_addresses.repository.ValidMacAddressesRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ValidMacAddressDocs extends BuilderIntegrationTest {

    @Autowired
    public ValidMacAddressDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ValidMacAddressesRepository validMacAddressesRepository,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.validMacAddressesRepository = validMacAddressesRepository;
        createCurrentUser();
    }

    @RoleTestRoot
    public void findAllAvailable() throws Exception {
        for (int i = 0; i < 3; i++) {
            createAndSaveValidMacAddress();
        }

        this.mockMvc.perform(get("/patrimonies/mac-addresses/available")
                .param("address", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("address").description("Endereço MAC pesquisado"),
                                getPagePathParameter(),
                                getPageSizePathParameter()),
                        getPageContent("Lista com todos endereços MAC os quais não foram cadastrados ainda")));

    }

    @RoleTestRoot
    public void basicFindAllAvailable() throws Exception {
        for (int i = 0; i < 3; i++) {
            createAndSaveValidMacAddress();
        }

        this.mockMvc.perform(get("/basic/patrimonies/mac-addresses/available")
                .param("address", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("address").description("Endereço MAC pesquisado"),
                                getPagePathParameter(),
                                getPageSizePathParameter()),
                        getPageContent("Lista com todos endereços MAC os quais não foram cadastrados ainda")));

    }

    @RoleTestRoot
    public void basicValidateAddress() throws Exception {
        String address = createAndSaveValidMacAddress().getAddress();

        this.mockMvc.perform(get("/basic/patrimonies/mac-addresses/validate")
                .param("address", address)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("address").description("Endereço MAC pesquisado")),
                        responseFields(
                                fieldWithPath("valid").description("Informa se o endereço está disponível ou não"))));

    }

}
