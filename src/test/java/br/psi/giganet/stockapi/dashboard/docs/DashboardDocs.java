package br.psi.giganet.stockapi.dashboard.docs;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.dashboard.repository.DashboardRepositoryImpl;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock.model.enums.QuantityLevel;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.stock_moves.model.ExternalOrderType;
import br.psi.giganet.stockapi.technician.model.Technician;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DashboardDocs extends BuilderIntegrationTest {

    @MockBean
    private DashboardRepositoryImpl dashboardRepository;

    @Autowired
    public DashboardDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            TechnicianRepository technicianRepository,
            BranchOfficeRepository branchOfficeRepository,
            StockRepository stockRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.stockRepository = stockRepository;

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.technicianRepository = technicianRepository;
        createCurrentUser();
    }

    @RoleTestAdmin
    @Transactional
    public void getData() throws Exception {
        BranchOffice branchOffice = createAndSaveBranchOffice();
        Technician technician = createAndSaveTechnician(branchOffice);
        LocalDate initialDate = LocalDate.now().minusDays(10);
        LocalDate finalDate = LocalDate.now();
        this.setDashboardData(initialDate, finalDate, technician);

        this.mockMvc.perform(get("/dashboard")
                .param("initialDate", initialDate.toString())
                .param("finalDate", finalDate.toString())
                .param("city", CityOptions.IPATINGA_HORTO.name())
                .header("Office-Id", branchOffice.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("initialDate").description(
                                        createDescriptionWithNotNull("Data inicial a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("finalDate").description(
                                        createDescriptionWithNotNull("Data final a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("city").description(
                                        createDescriptionWithNotNull("Cidade utilizada como filtro para as informações relacionadas a " +
                                                "galpão, manutenção, obsoletos e defeituosos"))),
                        responseFields(
                                fieldWithPath("initialDate").description("Data inicial dos dados"),
                                fieldWithPath("finalDate").description("Data final dos dados"),
                                fieldWithPath("techniciansStockItemsWithMovesCount")
                                        .description("Lista com todos os itens presentes nos estoques dos técnicos com as respectivas movimentações"),
                                fieldWithPath("mainItems").description("Lista com os itens principais do dashboard"),
                                fieldWithPath("mainItemsInShedAndTechnicianAndMaintenance")
                                        .description("Relação de itens e quantidades dos itens principais nos estoques do galpão, técnicos e manutenção"),
                                fieldWithPath("entryItems").description("Relação de todos os itens os quais entraram no estoque a partir de recebimentos de ordens de compras"),
                                fieldWithPath("obsoleteItems").description("Lista de todos os itens presentes no estoque de Obsoletos"),
                                fieldWithPath("generalItems").description("Lista total contendo todos os itens presentes no inventário, excluindo o estoque 'Cliente'"),
                                fieldWithPath("totalsByTechnicianStocks")
                                        .description("Valor total dos equipamentos que estão com o técnico no momento, considerando o último preço de compra"),
                                fieldWithPath("usedItemsInServiceOrders")
                                        .description("Relação dos itens e quantidades utilizados em ordens de serviço, agrupadas por tipo de OS"))
                                .andWithPrefix("totalsByTechnicianStocks[].",
                                        fieldWithPath("technician").description("Nome do Técnico"),
                                        fieldWithPath("total").description("Valor total aproximado dos equipamentos"))
                                .andWithPrefix("mainItems[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("quantity").description("Quantidade atual no estoque GALPÃO"),
                                        fieldWithPath("currentLevel").description("Nível atual do estoque do respectivo item no GALPÃO"),
                                        fieldWithPath("unit").description("Abreviação da unidade padrão do item"))
                                .andWithPrefix("mainItemsInShedAndTechnicianAndMaintenance[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("shed").description("Quantidade atual no estoque GALPÃO"),
                                        fieldWithPath("maintenance").description("Quantidade atual no estoque MANUTENÇÃO"),
                                        fieldWithPath("technician").description("Quantidade atual somando todos os estoques dos TÉCNICOS"),
                                        fieldWithPath("total").description("Soma das quantidades no estoque GALPÂO, MANUTENÇÃO e TÉCNICOS"))
                                .andWithPrefix("entryItems[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("quantity").description("Quantidade associada"))
                                .andWithPrefix("obsoleteItems[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("quantity").description("Quantidade atual no estoque OBSOLETOS"))
                                .andWithPrefix("generalItems[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("quantity").description("Quantidade associada"))
                                .andWithPrefix("techniciansStockItemsWithMovesCount[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("quantity").description("Quantidade atual independentemente do período selecionado"),
                                        fieldWithPath("replacement")
                                                .description("Quantidade de itens os quais vieram de movimentações do " +
                                                        "Galpão, Manutenção, Obsoletos, Defeituosos ou entradas avulsas de estoque"),
                                        fieldWithPath("retreat").description("Quantidade de itens recolhida através de OS"),
                                        fieldWithPath("devolution").description("Quantidade de itens devolvidas para o Galpão, Manutenção, Obsoletos, Defeituosos, " +
                                                "enviadas para outros técnicos ou saída avulsa de estoque"),
                                        fieldWithPath("installation").description("Quantidade instalada em clientes através de OS"))
                                .andWithPrefix("usedItemsInServiceOrders.",
                                        fieldWithPath("installation").description("Lista com os itens utilizados em OS de instalação"),
                                        fieldWithPath("repair").description("Lista com os itens utilizados em OS de reparo"),
                                        fieldWithPath("cancellation").description("Lista com os itens utilizados em OS de cancelamento"),
                                        fieldWithPath("addressChange").description("Lista com os itens utilizados em OS de mudança de endereço"),
                                        fieldWithPath("secondPoint").description("Lista com os itens utilizados em OS de segundo ponto"))
                                .andWithPrefix("usedItemsInServiceOrders.installation[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("quantity").description("Quantidade utilizada"))
                                .andWithPrefix("usedItemsInServiceOrders.repair[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("quantity").description("Quantidade utilizada"))
                                .andWithPrefix("usedItemsInServiceOrders.cancellation[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("quantity").description("Quantidade utilizada"))
                                .andWithPrefix("usedItemsInServiceOrders.addressChange[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("quantity").description("Quantidade utilizada"))
                                .andWithPrefix("usedItemsInServiceOrders.secondPoint[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("quantity").description("Quantidade utilizada"))));

    }

    @RoleTestAdmin
    @Transactional
    public void getGeneralTechniciansData() throws Exception {
        BranchOffice branchOffice = createAndSaveBranchOffice();
        Technician technician = createAndSaveTechnician(branchOffice);
        LocalDate initialDate = LocalDate.now().minusDays(10);
        LocalDate finalDate = LocalDate.now();
        this.setDashboardData(initialDate, finalDate, technician);

        this.mockMvc.perform(get("/dashboard/technicians")
                .param("initialDate", initialDate.toString())
                .param("finalDate", finalDate.toString())
                .header("Office-Id", branchOffice.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("initialDate").description(
                                        createDescriptionWithNotNull("Data inicial a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("finalDate").description(
                                        createDescriptionWithNotNull("Data final a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD"))),
                        responseFields(
                                fieldWithPath("initialDate").description("Data inicial dos dados"),
                                fieldWithPath("finalDate").description("Data final dos dados"),
                                fieldWithPath("patrimonies").description("Lista com os códigos dos patrimônios presentes em estoques de técnicos"),
                                fieldWithPath("stockItemsWithMovesCount").description(""))
                                .andWithPrefix("patrimonies[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("code").description("Código do patrimônio"))
                                .andWithPrefix("stockItemsWithMovesCount[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("quantity").description("Quantidade atual independentemente do período selecionado"),
                                        fieldWithPath("replacement")
                                                .description("Quantidade de itens os quais vieram de movimentações do " +
                                                        "Galpão, Manutenção, Obsoletos, Defeituosos ou entradas avulsas de estoque"),
                                        fieldWithPath("retreat").description("Quantidade de itens recolhida através de OS"),
                                        fieldWithPath("devolution").description("Quantidade de itens devolvidas para o Galpão, Manutenção, Obsoletos, Defeituosos, " +
                                                "enviadas para outros técnicos ou saída avulsa de estoque"),
                                        fieldWithPath("installation").description("Quantidade instalada em clientes através de OS"))));

    }

    @RoleTestAdmin
    @Transactional
    public void getTechniciansData() throws Exception {
        BranchOffice branchOffice = createAndSaveBranchOffice();
        Technician technician = createAndSaveTechnician(branchOffice);
        LocalDate initialDate = LocalDate.now().minusDays(10);
        LocalDate finalDate = LocalDate.now();
        this.setDashboardData(initialDate, finalDate, technician);

        this.mockMvc.perform(get("/dashboard/technicians")
                .param("initialDate", initialDate.toString())
                .param("finalDate", finalDate.toString())
                .param("technician", technician.getId())
                .header("Office-Id", branchOffice.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("initialDate").description(
                                        createDescriptionWithNotNull("Data inicial a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("finalDate").description(
                                        createDescriptionWithNotNull("Data final a ser buscado os dados",
                                                "Deve ser informado no formato: YYYY-MM-DD")),
                                parameterWithName("technician").description("Código ID do técnico pesquisado")),
                        responseFields(
                                fieldWithPath("initialDate").description("Data inicial dos dados"),
                                fieldWithPath("finalDate").description("Data final dos dados"),
                                fieldWithPath("patrimonies").description("Lista com os códigos dos patrimônios presentes em estoques de técnicos"),
                                fieldWithPath("stockItemsWithMovesCount").description(""))
                                .andWithPrefix("patrimonies[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("code").description("Código do patrimônio"))
                                .andWithPrefix("stockItemsWithMovesCount[].",
                                        fieldWithPath("item").description("Nome do item"),
                                        fieldWithPath("quantity").description("Quantidade atual independentemente do período selecionado"),
                                        fieldWithPath("replacement")
                                                .description("Quantidade de itens os quais vieram de movimentações do " +
                                                        "Galpão, Manutenção, Obsoletos, Defeituosos ou entradas avulsas de estoque"),
                                        fieldWithPath("retreat").description("Quantidade de itens recolhida através de OS"),
                                        fieldWithPath("devolution").description("Quantidade de itens devolvidas para o Galpão, Manutenção, Obsoletos, Defeituosos, " +
                                                "enviadas para outros técnicos ou saída avulsa de estoque"),
                                        fieldWithPath("installation").description("Quantidade instalada em clientes através de OS"))));

    }

    private void setDashboardData(LocalDate initialDate, LocalDate finalDate, Technician technician) {
        BranchOffice office = technician.getBranchOffice();
        when(dashboardRepository.findAllUsedItemsByOrderType(ExternalOrderType.INSTALLATION, office, initialDate, finalDate))
                .thenReturn(getItemQuantityList("Roteador AC750", "Roteador 1200", "Modem ONU"));
        when(dashboardRepository.findAllUsedItemsByOrderType(ExternalOrderType.REPAIR, office, initialDate, finalDate))
                .thenReturn(getItemQuantityList("Roteador AC750", "Roteador 1200", "Modem ONU"));
        when(dashboardRepository.findAllUsedItemsByOrderType(ExternalOrderType.ADDRESS_CHANGE, office, initialDate, finalDate))
                .thenReturn(getItemQuantityList("Roteador AC750", "Roteador 1200", "Modem ONU"));
        when(dashboardRepository.findAllUsedItemsByOrderType(ExternalOrderType.SECOND_POINT, office, initialDate, finalDate))
                .thenReturn(getItemQuantityList("Roteador AC750", "Roteador 1200", "Modem ONU"));
        when(dashboardRepository.findAllUsedItemsByOrderType(ExternalOrderType.CANCELLATION, office, initialDate, finalDate))
                .thenReturn(getItemQuantityList("Roteador AC750"));

        when(dashboardRepository.findAllMainItems(currentLoggedUser, office))
                .thenReturn(getMainItemsList("Roteador AC750", "Switch 8 portas", "Modem ONU GPON"));

        when(dashboardRepository.findAllMainItemsWithQuantityInShedAndTechnicianAndMaintenance(currentLoggedUser, office))
                .thenReturn(getMainItemsInStocksList("Roteador AC750", "Switch 8 portas", "Modem ONU GPON"));

        when(dashboardRepository.findAllEntryItemsWithQuantityByDate(initialDate, finalDate, office))
                .thenReturn(getItemQuantityList("Roteador AC750", "Switch 8 portas", "Modem ONU GPON"));

        when(dashboardRepository.findAllObsoleteItems(office))
                .thenReturn(getItemQuantityList("Roteador AC750", "Switch 8 portas", "Modem ONU GPON"));

        when(dashboardRepository.findAllStockItemsInShedTechnicianMaintenanceObsoleteDefective(office))
                .thenReturn(getItemQuantityList("Roteador AC750", "Switch 8 portas", "Modem ONU GPON"));

        when(dashboardRepository.totalsByTechnicianStocks(office))
                .thenReturn(getTechnicianTotalsList("Lucas Henrique", "Carlos", "Marcos"));

        when(dashboardRepository.getAllTechniciansStockItemsWithMoveCounts(initialDate, finalDate, office))
                .thenReturn(getTechniciansItemQuantityList("Roteador AC750", "Switch 8 portas", "Modem ONU GPON"));

        when(dashboardRepository.findAllPatrimoniesWithTechnicians())
                .thenReturn(getPatrimoniesItemsList("Roteador AC750", "Switch 8 portas", "Modem ONU GPON"));

        when(dashboardRepository.getAllTechniciansStockItemsWithMoveCounts(technician, initialDate, finalDate, office))
                .thenReturn(getTechniciansItemQuantityList("Roteador AC750", "Switch 8 portas", "Modem ONU GPON"));

        when(dashboardRepository.findAllPatrimoniesByTechnician(technician))
                .thenReturn(getPatrimoniesItemsList("Roteador AC750", "Switch 8 portas", "Modem ONU GPON"));

    }

    private List<Map<String, Object>> getItemQuantityList(String... products) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < products.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("item", products[i]);
            map.put("quantity", (i + 1) * 10);

            list.add(map);
        }

        return list;
    }

    private List<Map<String, Object>> getTechnicianTotalsList(String... technicians) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < technicians.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("technician", technicians[i]);
            map.put("total", (i + 1) * 100);

            list.add(map);
        }

        return list;
    }

    private List<Map<String, Object>> getMainItemsList(String... products) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < products.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("item", products[i]);
            map.put("quantity", (i + 1) * 10);
            map.put("currentLevel", i % 2 == 0 ? QuantityLevel.LOW : QuantityLevel.NORMAL);
            map.put("unit", "UNID");

            list.add(map);
        }

        return list;
    }

    private List<Map<String, Object>> getMainItemsInStocksList(String... products) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < products.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("item", products[i]);
            map.put("shed", (i + 1) * 2);
            map.put("maintenance", (i + 2) * 3);
            map.put("technician", (i + 3) * 4);
            map.put("total", (i + 4) * 5);

            list.add(map);
        }

        return list;
    }

    private List<Map<String, Object>> getTechniciansItemQuantityList(String... products) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < products.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("item", products[i]);
            map.put("quantity", (i + 1) * 10);
            map.put("replacement", (i + 1) * 12);
            map.put("retreat", (i + 1) * 15);
            map.put("devolution", (i + 1) * 12);
            map.put("installation", (i + 1) * 5);

            list.add(map);
        }

        return list;
    }

    private List<Map<String, Object>> getPatrimoniesItemsList(String... products) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < products.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("item", products[i]);
            map.put("code", UUID.randomUUID().toString().substring(0, 8));

            list.add(map);
        }

        return list;
    }

}
