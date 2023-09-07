package br.psi.giganet.stockapi.technicians.docs;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.technician.controller.request.UpdateTechnicianRequest;
import br.psi.giganet.stockapi.technician.factory.TechnicianFactory;
import br.psi.giganet.stockapi.technician.model.Technician;
import br.psi.giganet.stockapi.technician.model.TechnicianSector;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.technician.service.RemoteTechnicianService;
import br.psi.giganet.stockapi.technician.service.dto.PlainDTO;
import br.psi.giganet.stockapi.technician.service.dto.TechnicianScheduleDTO;
import br.psi.giganet.stockapi.utils.BuilderIntegrationTest;
import br.psi.giganet.stockapi.utils.annotations.RoleTestRoot;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TechnicianDocs extends BuilderIntegrationTest {

    @MockBean
    private RemoteTechnicianService remoteTechnicianService;

    private final TechnicianFactory technicianFactory;


    @Autowired
    public TechnicianDocs(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            TechnicianRepository technicianRepository,
            StockRepository stockRepository,
            TechnicianFactory technicianFactory,
            BranchOfficeRepository branchOfficeRepository) {

        this.branchOfficeRepository = branchOfficeRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.technicianRepository = technicianRepository;
        this.stockRepository = stockRepository;
        this.technicianFactory = technicianFactory;
        createCurrentUser();

    }

    @RoleTestRoot
    @Transactional
    public void findAllRemote() throws Exception {
        this.setupData(createAndSaveTechnician());

        this.mockMvc.perform(post("/technicians/remote")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())));
    }

    @RoleTestRoot
    @Transactional
    public void findAll() throws Exception {
        BranchOffice office = createAndSaveBranchOffice();
        for (int i = 0; i < 3; i++) {
            createAndSaveTechnician(office);
        }

        this.mockMvc.perform(get("/technicians")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("[]").description("Lista com os técnicos encontrados"))
                                        .andWithPrefix("[].", getProjection())
                                        .andWithPrefix("[].branchOffice.", getBranchOfficeProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void getTechnicianSchedule() throws Exception {
        TechnicianStock technicianStock = createAndSaveTechnicianStock();
        this.setupData(technicianStock.getTechnician());

        this.mockMvc.perform(get("/technicians/stocks/{id}", technicianStock.getId())
                .param("initialDate", LocalDate.of(2020, 6, 1).toString())
                .param("finalDate", LocalDate.of(2020, 7, 1).toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("id").description(createDescription(
                                        "Código ID do estoque do técnico",
                                        "NOTE QUE: este ID é diferente do ID e do USER ID do técnico"
                                ))),
                                requestParameters(
                                        parameterWithName("initialDate").description("Data inicial para consulta da agenda"),
                                        parameterWithName("finalDate").description("Data final para consulta da agenda")
                                ),
                                responseFields(fieldWithPath("[]")
                                        .description("Lista os agendamentos encontrados a partir de uma consulta a API externa (Smartnet)"))
                                        .andWithPrefix("[].", getTechnicianScheduleDTOProjection())
                                        .andWithPrefix("[].plain.", getPlainDTOProjection())));
    }


    @RoleTestRoot
    @Transactional
    public void findById() throws Exception {
        Technician technician = createAndSaveTechnician();

        this.mockMvc.perform(get("/technicians/{id}", technician.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do técnico procurado")
                        ),
                        responseFields(getResponse())
                                .andWithPrefix("branchOffice.", getBranchOfficeProjection())));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        Employee e = createAndSaveEmployee();
        e.setPermissions(new HashSet<>(Arrays.asList(
                createAndSavePermission("ROLE_ADMIN"),
                createAndSavePermission("ROLE_UNITS_READ"),
                createAndSavePermission("ROLE_PRODUCTS_READ"),
                createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"),
                createAndSavePermission("ROLE_MOVES_WRITE_ENTRY_ITEMS"),
                createAndSavePermission("ROLE_MOVES_READ"),
                createAndSavePermission("ROLE_PATRIMONIES_LOCATIONS_READ"),
                createAndSavePermission("ROLE_STOCKS_READ"),
                createAndSavePermission("ROLE_PATRIMONIES_WRITE"),
                createAndSavePermission("ROLE_PATRIMONIES_READ"))));
        employeeRepository.saveAndFlush(e);

        Technician technician = createAndSaveTechnicianByEmployee(e);

        this.mockMvc.perform(put("/technicians/{id}", technician.getId())
                .content(objectMapper.writeValueAsString(createUpdateTechnicianRequest(technician)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id").description("Código do técnico")),
                        requestFields(
                                fieldWithPath("id").description("Código do técnico"),
                                fieldWithPath("name").description("Nome do técnico"),
                                fieldWithPath("sector").description("Setor associado"),
                                fieldWithPath("isActive").description(createDescription(
                                        "Refere a situação atual do técnico",
                                        "Esta mesma situação será utilizada para atualizar a situação do estoque do técnico"
                                )),
                                fieldWithPath("branchOffice").description("Código da filial associada")),
                        responseFields(getProjection())
                                .andWithPrefix("branchOffice.", getBranchOfficeProjection())));
    }

    private UpdateTechnicianRequest createUpdateTechnicianRequest(Technician technician) {
        UpdateTechnicianRequest request = new UpdateTechnicianRequest();
        request.setId(technician.getId());
        request.setName(technician.getName());
        request.setSector(TechnicianSector.REPAIR);
        request.setIsActive(Boolean.TRUE);
        request.setBranchOffice(technician.getBranchOffice().getId());

        return request;
    }

    private FieldDescriptor[] getProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código do técnico"),
                fieldWithPath("name").description("Nome"),
                fieldWithPath("email").description("Email"),
                fieldWithPath("userId").description("UserId do técnico na base de dados externa (Smartnet)"),
                fieldWithPath("sector")
                        .optional()
                        .type(JsonFieldType.STRING)
                        .description("Setor associado, caso tenha sido cadastrado"),
                fieldWithPath("branchOffice")
                        .optional()
                        .type(JsonFieldType.OBJECT)
                        .description("Filial associada ao técnico, caso exista")};
    }

    private FieldDescriptor[] getResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código do técnico"),
                fieldWithPath("name").description("Nome"),
                fieldWithPath("email").description("Email"),
                fieldWithPath("userId").description("UserId do técnico na base de dados externa (Smartnet)"),
                fieldWithPath("isActive").description("Situação do técnico"),
                fieldWithPath("sector")
                        .optional()
                        .type(JsonFieldType.STRING)
                        .description("Setor associado, caso tenha sido cadastrado"),
                fieldWithPath("branchOffice")
                        .optional()
                        .type(JsonFieldType.OBJECT)
                        .description("Filial associada ao técnico, caso exista")};
    }

    private FieldDescriptor[] getBranchOfficeProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código ID da filial"),
                fieldWithPath("name").description("Nome da Filial"),
                fieldWithPath("city").description("Cidade associada")};
    }

    private FieldDescriptor[] getTechnicianScheduleDTOProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("date").description("Data do agendamento"),
                fieldWithPath("subtype").description("Subtipo da OS"),
                fieldWithPath("address").description(createDescription(
                        "Endereço do agendamento",
                        "NOTE QUE: trata-se de uma String plana, com os campos concatenados")),
                fieldWithPath("box").optional().type(JsonFieldType.STRING)
                        .description("Tecnologia utilizada na caixa (GPON/GEPON), caso seja possível identificá-la"),
                fieldWithPath("plain").description("Plano atual do cliente referente a insalação")
        };
    }

    private FieldDescriptor[] getPlainDTOProjection() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código ID do plano na API externa (Smartnet)"),
                fieldWithPath("name").description("Nome do plano")
        };
    }

    private void setupData(Technician technician) {
        Mockito.when(this.remoteTechnicianService.getRemoteTechnicians())
                .thenReturn(Arrays.asList(
                        technicianFactory.create(
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                "Jose Das Couves",
                                "tecnico" + getRandomId() + "@giganet.psi.br",
                                UUID.randomUUID().toString(),
                                Boolean.TRUE),
                        technicianFactory.create(
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                "Lucas Henrique",
                                "tecnico" + getRandomId() + "@giganet.psi.br",
                                UUID.randomUUID().toString(),
                                Boolean.TRUE),
                        technicianFactory.create(
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                "Carlos",
                                "tecnico" + getRandomId() + "@giganet.psi.br",
                                UUID.randomUUID().toString(),
                                Boolean.TRUE)
                ));

        Mockito.when(this.remoteTechnicianService.getTechnicianSchedule(
                technician,
                LocalDate.of(2020, 6, 1),
                LocalDate.of(2020, 7, 1)))
                .thenReturn(
                        Arrays.asList(
                                new TechnicianScheduleDTO(
                                        LocalDate.now(),
                                        "Instação predial",
                                        "Av Castelo Branco, 314, Horto, Ipatinga, MG, 35162-123",
                                        "GEPON",
                                        new PlainDTO(UUID.randomUUID().toString(), "Giganet 50 + 50")),
                                new TechnicianScheduleDTO(
                                        LocalDate.now(),
                                        "Instação predial",
                                        "Av Castelo Branco, 314, Horto, Ipatinga, MG, 35162-123",
                                        "GPON",
                                        new PlainDTO(UUID.randomUUID().toString(), "Giganet 50 + 50")),
                                new TechnicianScheduleDTO(
                                        LocalDate.now(),
                                        "Instação predial",
                                        "Av Castelo Branco, 314, Horto, Ipatinga, MG, 35162-123",
                                        "GPON",
                                        new PlainDTO(UUID.randomUUID().toString(), "Giganet 50 + 50"))));

        createAndSavePermission("ROLE_ADMIN");
        createAndSavePermission("ROLE_UNITS_READ");
        createAndSavePermission("ROLE_PRODUCTS_READ");
        createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS");
        createAndSavePermission("ROLE_MOVES_WRITE_ENTRY_ITEMS");
        createAndSavePermission("ROLE_MOVES_READ");
        createAndSavePermission("ROLE_PATRIMONIES_LOCATIONS_READ");
        createAndSavePermission("ROLE_STOCKS_READ");
        createAndSavePermission("ROLE_PATRIMONIES_WRITE");
        createAndSavePermission("ROLE_PATRIMONIES_READ");
    }

}
