package br.psi.giganet.stockapi.utils;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.address.model.Address;
import br.psi.giganet.stockapi.common.address.service.AddressService;
import br.psi.giganet.stockapi.common.notifications.model.Notification;
import br.psi.giganet.stockapi.common.notifications.model.NotificationEmployee;
import br.psi.giganet.stockapi.common.notifications.model.NotificationRole;
import br.psi.giganet.stockapi.common.notifications.model.NotificationType;
import br.psi.giganet.stockapi.common.notifications.repository.NotificationRepository;
import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.common.valid_mac_addresses.model.ValidMacAddress;
import br.psi.giganet.stockapi.common.valid_mac_addresses.repository.ValidMacAddressesRepository;
import br.psi.giganet.stockapi.config.contenxt.BranchOfficeContext;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.dashboard.main_items.model.GroupCategory;
import br.psi.giganet.stockapi.dashboard.main_items.model.MainDashboardItem;
import br.psi.giganet.stockapi.dashboard.main_items.model.MainDashboardItemGroup;
import br.psi.giganet.stockapi.dashboard.main_items.repository.MainDashboardItemGroupRepository;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.entries.model.Entry;
import br.psi.giganet.stockapi.entries.model.EntryItem;
import br.psi.giganet.stockapi.entries.model.enums.EntryStatus;
import br.psi.giganet.stockapi.entries.repository.EntryRepository;
import br.psi.giganet.stockapi.moves_request.model.RequestedMove;
import br.psi.giganet.stockapi.moves_request.repository.MovesRequestRepository;
import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.patrimonies.model.PatrimonyCodeType;
import br.psi.giganet.stockapi.patrimonies.model.PatrimonyMove;
import br.psi.giganet.stockapi.patrimonies.repository.PatrimonyMoveRepository;
import br.psi.giganet.stockapi.patrimonies.repository.PatrimonyRepository;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocationType;
import br.psi.giganet.stockapi.patrimonies_locations.repository.PatrimonyLocationRepository;
import br.psi.giganet.stockapi.products.categories.model.Category;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrder;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrderFreight;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrderItem;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrderSupplier;
import br.psi.giganet.stockapi.purchase_order.model.enums.FreightType;
import br.psi.giganet.stockapi.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.stockapi.schedules.model.ScheduledExecution;
import br.psi.giganet.stockapi.schedules.model.ScheduledMove;
import br.psi.giganet.stockapi.schedules.model.ScheduledMoveItem;
import br.psi.giganet.stockapi.schedules.model.ScheduledStatus;
import br.psi.giganet.stockapi.schedules.repository.ScheduledMoveRepository;
import br.psi.giganet.stockapi.stock.model.*;
import br.psi.giganet.stockapi.stock.model.enums.QuantityLevel;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock.repository.StockItemQuantityLevelRepository;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.stock_moves.model.*;
import br.psi.giganet.stockapi.stock_moves.repository.StockMovesRepository;
import br.psi.giganet.stockapi.technician.model.Technician;
import br.psi.giganet.stockapi.technician.model.TechnicianSector;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.technician.technician_product_category.repository.TechnicianSectorProductCategoryRepository;
import br.psi.giganet.stockapi.templates.model.Template;
import br.psi.giganet.stockapi.templates.model.TemplateItem;
import br.psi.giganet.stockapi.templates.repository.TemplateRepository;
import br.psi.giganet.stockapi.units.model.Unit;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;


@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@ActiveProfiles("test")
public class BuilderIntegrationTest extends DocsDescriptions {

    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected Employee currentLoggedUser;

    protected UnitRepository unitRepository;
    protected ProductCategoryRepository productCategoryRepository;
    protected ProductRepository productRepository;
    protected EmployeeRepository employeeRepository;
    protected PermissionRepository permissionRepository;
    protected PurchaseOrderRepository purchaseOrderRepository;
    protected EntryRepository entryRepository;
    protected StockRepository stockRepository;
    protected StockItemRepository stockItemRepository;
    protected StockMovesRepository stockMovesRepository;
    protected TechnicianRepository technicianRepository;
    protected PatrimonyRepository patrimonyRepository;
    protected ScheduledMoveRepository scheduledMoveRepository;
    protected TemplateRepository templateRepository;
    protected PatrimonyLocationRepository patrimonyLocationRepository;
    protected PatrimonyMoveRepository patrimonyMoveRepository;
    protected ValidMacAddressesRepository validMacAddressesRepository;
    protected MovesRequestRepository movesRequestRepository;
    protected StockItemQuantityLevelRepository stockItemQuantityLevelRepository;
    protected TechnicianSectorProductCategoryRepository technicianSectorProductCategoryRepository;
    protected NotificationRepository notificationRepository;
    protected MainDashboardItemGroupRepository mainDashboardItemGroupRepository;

    protected AddressService addressService;

    protected BranchOfficeRepository branchOfficeRepository;

    /**
     * For convenience, it will added a new request header 'Office-Id'.
     * This is necessary in 'BranchOfficeFilter'. Therefore, to simplify tests that don't need it,
     * a random branch office will be added. Otherwise, if header is present, the existent wont be override.
     */
    @BeforeEach
    public void mockMvcConfig(WebApplicationContext webApplicationContext,
                              RestDocumentationContextProvider restDocumentation) {

        var tempBranchOffice = branchOfficeRepository.findAll(PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseGet(this::createAndSaveBranchOffice);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .addFilter(((req, res, filterChain) -> {
                    req.setCharacterEncoding("UTF-8");
                    filterChain.doFilter(req, res);
                }))
                .addFilter((req, res, filterChain) -> { // execute the same logic that 'BranchOfficeFilter', only simplifying the restrictions
                    HttpServletRequest request = (HttpServletRequest) req;
                    final String officeId = "Office-Id";
                    BranchOffice office;
                    if (request.getHeader(officeId) != null) {
                        office = new BranchOffice();
                        office.setId(Long.parseLong(request.getHeader(officeId)));
                    } else {
                        office = tempBranchOffice;
                    }
                    BranchOfficeContext.setCurrentBranchOffice(office);
                    filterChain.doFilter(req, res);
                })
                .build();

    }

    protected void createNotificationsPermissions() {
        createAndSavePermission("ROLE_NOTIFICATIONS");
        createAndSavePermission("ROLE_NOTIFICATIONS_STOCK_ITEM_LOW_LEVEL");
        createAndSavePermission("ROLE_NOTIFICATIONS_STOCK_ITEM_VERY_LOW_LEVEL");
    }

    protected Notification createAndSaveNotification() {
        return createAndSaveNotification(employeeRepository.findAll());
    }

    protected Notification createAndSaveNotification(List<Employee> employees) {
        return createAndSaveNotification(employees, NotificationType.STOCK_ITEM_VERY_LOW_LEVEL);
    }

    protected Notification createAndSaveNotification(List<Employee> employees, NotificationType type) {
        Notification notification = new Notification();
        notification.setTitle("Teste");
        notification.setDescription("Notificação de teste");
        notification.setType(type);
        notification.setData(UUID.randomUUID().toString());
        notification.setRoles(Collections.singletonList(new NotificationRole(
                createAndSavePermission("ROLE_NOTIFICATIONS"),
                notification
        )));
        notification.setEmployees(employees.stream()
                .map(employee -> new NotificationEmployee(employee, notification, Boolean.FALSE))
                .collect(Collectors.toList()));

        return notificationRepository.saveAndFlush(notification);
    }

    protected Unit createAndSaveUnit() {
        int value = getRandomId();

        Unit unit = new Unit();
        unit.setId(UUID.randomUUID().toString().substring(0, 10));
        unit.setName("Unit  " + value);
        unit.setAbbreviation("abbrev  " + value);
        unit.setDescription("Unidade de teste " + value);
        return unitRepository.saveAndFlush(unit);
    }

    protected Category createAndSaveCategory() {
        int value = getRandomId();

        Category category = new Category();
        category.setId(UUID.randomUUID().toString().substring(0, 10));
        category.setName("Categoria  " + value);
        category.setDescription("Categoria de teste " + value);
        return productCategoryRepository.saveAndFlush(category);
    }

    protected Template createAndSaveTemplate() {
        Template template = new Template();
        template.setName("Template " + getRandomId());
        template.setItems(new ArrayList<>());

        for (int i = 0; i < 2; i++) {
            TemplateItem item = new TemplateItem();
            item.setQuantity(10d);
            item.setProduct(createAndSaveProduct());
            item.setTemplate(template);
            template.getItems().add(item);
        }

        return templateRepository.save(template);
    }

    @Transactional
    protected BranchOffice createAndSaveBranchOffice() {
        BranchOffice office = new BranchOffice();
        office.setName("Filial " + getRandomId());
        office.setCity(CityOptions.IPATINGA_HORTO);
        office.setStocks(new HashSet<>());

        BranchOffice saved = branchOfficeRepository.saveAndFlush(office);

        createAndSaveShedStock(saved);
        createAndSaveMaintenanceStock(saved);
        createAndSaveObsoleteStock(saved);
        createAndSaveDefectiveStock(saved);
        createAndSaveCustomerStock(saved);

        return saved;
    }

    protected BranchOffice createAndSaveEmptyBranchOffice(CityOptions city) {
        BranchOffice office = new BranchOffice();
        office.setName("Filial " + getRandomId());
        office.setCity(city);
        office.setStocks(new HashSet<>());

        return branchOfficeRepository.saveAndFlush(office);
    }

    protected ScheduledMove createAndSaveScheduledMove(Stock from, Stock to, List<Product> products) {
        ScheduledMove move = new ScheduledMove();
        move.setDate(ZonedDateTime.now().plusHours(2));
        move.setExecution(ScheduledExecution.MANUAL);
        move.setFrom(from);
        move.setTo(to);
        move.setStatus(ScheduledStatus.SCHEDULED);
        move.setOrigin(MoveOrigin.SCHEDULE);
        move.setResponsible(currentLoggedUser);
        move.setBranchOffice(from != null ? from.getBranchOffice() : to.getBranchOffice());

        MoveType type = from != null && to != null ?
                MoveType.BETWEEN_STOCKS : from != null ?
                MoveType.OUT_ITEM : MoveType.ENTRY_ITEM;
        move.setType(type);

        move.setItems(products.stream().map(p -> {
            ScheduledMoveItem item = new ScheduledMoveItem();
            item.setScheduled(move);
            item.setFrom(from == null ? null : from.find(p)
                    .orElseGet(() -> {
                        StockItem stockItem = createAndSaveStockItem(from, p);
                        stockItem.setQuantity(20d);
                        return stockItem;
                    }));

            item.setTo(to != null ? to.find(p).orElse(createAndSaveStockItem(to, p)) : null);
            item.setOrigin(MoveOrigin.SCHEDULE);
            item.setReason(MoveReason.DETACHED);
            item.setType(type);
            item.setProduct(p);
            item.setQuantity(2d);

            return item;
        }).collect(Collectors.toList()));

        return scheduledMoveRepository.saveAndFlush(move);
    }

    protected DetachedStockMove createAndSaveDetachedStockMove(Stock from, Stock to, Product product) {
        StockItem fromItem = from != null ? createAndSaveStockItem(from, product) : null;
        StockItem toItem = to != null ? createAndSaveStockItem(to, product) : null;
        MoveType type = from != null ? to != null ? MoveType.BETWEEN_STOCKS : MoveType.OUT_ITEM : MoveType.ENTRY_ITEM;
        return createAndSaveDetachedStockMove(fromItem, toItem, type);
    }

    protected DetachedStockMove createAndSaveDetachedStockMove(Stock from, Stock to, Product product, Employee employee) {
        StockItem fromItem = from != null ? createAndSaveStockItem(from, product) : null;
        StockItem toItem = to != null ? createAndSaveStockItem(to, product) : null;
        MoveType type = from != null ? to != null ? MoveType.BETWEEN_STOCKS : MoveType.OUT_ITEM : MoveType.ENTRY_ITEM;
        return createAndSaveDetachedStockMove(fromItem, toItem, type, employee);
    }

    protected DetachedStockMove createAndSaveDetachedStockMove(StockItem from, StockItem to, MoveType type) {
        DetachedStockMove move = new DetachedStockMove();
        move.setFrom(from);
        move.setTo(to);
        move.setProduct(from != null ? from.getProduct() : to.getProduct());
        move.setQuantity(1d);
        move.setType(type);
        move.setStatus(MoveStatus.REQUESTED);
        move.setOrigin(MoveOrigin.LOGGED_USER);
        move.setReason(MoveReason.DETACHED);
        move.setDescription("");

        boolean isBetweenShed = from != null && to != null && from.getStock().isShed() && to.getStock().isShed();
        if (!isBetweenShed) {
            move.setBranchOffice(from != null ?
                    from.getStock().getBranchOffice() :
                    to.getStock().getBranchOffice());
        }

        Employee employee = createAndSaveEmployee();
        employee.getPermissions().addAll(Arrays.asList(
                createAndSavePermission("ROLE_MOVES_WRITE_ENTRY_ITEMS"),
                createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"),
                createAndSavePermission("ROLE_MOVES_WRITE_OUT_ITEM")));
        move.setRequester(employee);
        return stockMovesRepository.saveAndFlush(move);
    }

    protected DetachedStockMove createAndSaveDetachedStockMove(
            StockItem from,
            StockItem to,
            MoveType type,
            Employee employee) {
        DetachedStockMove move = new DetachedStockMove();
        move.setFrom(from);
        move.setTo(to);
        move.setProduct(from != null ? from.getProduct() : to.getProduct());
        move.setQuantity(1d);
        move.setType(type);
        move.setStatus(MoveStatus.REQUESTED);
        move.setOrigin(MoveOrigin.LOGGED_USER);
        move.setReason(MoveReason.DETACHED);
        move.setDescription("");
        move.setRequester(employee);

        boolean isBetweenShed = from != null && to != null && from.getStock().isShed() && to.getStock().isShed();
        if (!isBetweenShed) {
            move.setBranchOffice(from != null ?
                    from.getStock().getBranchOffice() :
                    to.getStock().getBranchOffice());
        }

        return stockMovesRepository.saveAndFlush(move);
    }

    protected TechnicianStockMove createAndSaveTechnicianStockMove(Stock from, Stock to, Product product, Employee employee) {
        StockItem fromItem = from != null ? createAndSaveStockItem(from, product) : null;
        StockItem toItem = to != null ? createAndSaveStockItem(to, product) : null;
        MoveType type = from != null ? to != null ? MoveType.BETWEEN_STOCKS : MoveType.OUT_ITEM : MoveType.ENTRY_ITEM;
        return createAndSaveTechnicianStockMove(fromItem, toItem, type, employee);
    }

    protected TechnicianStockMove createAndSaveTechnicianStockMove(
            StockItem from,
            StockItem to,
            MoveType type,
            Employee employee) {
        TechnicianStockMove move = new TechnicianStockMove();
        move.setFrom(from);
        move.setTo(to);
        move.setProduct(from != null ? from.getProduct() : to.getProduct());
        move.setQuantity(1d);
        move.setType(type);
        move.setStatus(MoveStatus.REQUESTED);
        move.setOrigin(MoveOrigin.LOGGED_USER);
        move.setReason(MoveReason.DETACHED);
        move.setDescription("");
        move.setRequester(employee);

        move.setBranchOffice(from != null ?
                from.getStock().getBranchOffice() :
                to.getStock().getBranchOffice());

        return stockMovesRepository.saveAndFlush(move);
    }

    protected TechnicianStockMove createAndSaveTechnicianStockMove(Stock from, Stock to, Product product) {
        StockItem fromItem = from != null ? createAndSaveStockItem(from, product) : null;
        StockItem toItem = to != null ? createAndSaveStockItem(to, product) : null;
        MoveType type = from != null ? to != null ? MoveType.BETWEEN_STOCKS : MoveType.OUT_ITEM : MoveType.ENTRY_ITEM;
        return createAndSaveTechnicianStockMove(fromItem, toItem, type);
    }

    protected TechnicianStockMove createAndSaveTechnicianStockMove(StockItem from, StockItem to, MoveType type) {
        TechnicianStockMove move = new TechnicianStockMove();
        move.setFrom(from);
        move.setTo(to);
        move.setProduct(from != null ? from.getProduct() : to.getProduct());
        move.setQuantity(1d);
        move.setType(type);
        move.setStatus(MoveStatus.REQUESTED);
        move.setOrigin(MoveOrigin.LOGGED_USER);
        move.setReason(MoveReason.DETACHED);
        move.setDescription("");

        move.setBranchOffice(from != null ?
                from.getStock().getBranchOffice() :
                to.getStock().getBranchOffice());

        Employee employee = createAndSaveEmployee();
        employee.getPermissions().addAll(Arrays.asList(
                createAndSavePermission("ROLE_MOVES_WRITE_ENTRY_ITEMS"),
                createAndSavePermission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"),
                createAndSavePermission("ROLE_MOVES_WRITE_OUT_ITEM")));
        move.setRequester(employee);
        return stockMovesRepository.saveAndFlush(move);
    }

    protected Product createAndSaveProduct() {
        int value = getRandomId();

        Product product = new Product();
        product.setId(UUID.randomUUID().toString().substring(0, 10));
        product.setName("Product " + value);
        product.setCode(UUID.randomUUID().toString().substring(0, 10));
        product.setCategory(createAndSaveCategory());
        product.setUnit(createAndSaveUnit());
        product.setManufacturer("Fabricante " + value);
        product.setDescription("Produto de teste " + value);
        return productRepository.saveAndFlush(product);
    }

    protected Entry createAndSaveEntry() {
        Entry entry = new Entry();
        entry.setStatus(EntryStatus.REALIZED);
        entry.setIsManual(Boolean.TRUE);
        entry.setDocumentAccessCode("CODE_1234");
        entry.setFiscalDocumentNumber("QAZWSX231");
        entry.setResponsible(createAndSaveEmployee());
        entry.setPurchaseOrder(createAndSavePurchaseOrder());
        entry.setStock(createAndSaveShedStock());
        entry.setBranchOffice(entry.getStock().getBranchOffice());

        entry.setItems(entry.getPurchaseOrder().getItems().stream()
                .map(orderItem -> {
                    final var entryItem = new EntryItem();
                    entryItem.setEntry(entry);
                    entryItem.setStatus(EntryStatus.RECEIVED);
                    entryItem.setQuantity(orderItem.getQuantity());
                    entryItem.setEntryMove(null);
                    entryItem.setPurchaseOrderItem(orderItem);
                    entryItem.setDocumentProductCode(getRandomId() + "OAKCI");
                    entryItem.setIcms(orderItem.getIcms());
                    entryItem.setIpi(orderItem.getIcms());
                    entryItem.setPrice(orderItem.getPrice());
                    entryItem.setTotal(orderItem.getTotal());
                    entryItem.setProduct(orderItem.getProduct());
                    entryItem.setUnit(orderItem.getUnit());
                    entryItem.setSupplier(entry.getPurchaseOrder().getSupplier());

                    return entryItem;
                })
                .collect(Collectors.toList()));

        return entryRepository.saveAndFlush(entry);
    }

    protected RequestedMove createAndSaveRequestedMove(Stock from, Stock to) {
        Product product = createAndSaveProduct();
        return createAndSaveRequestedMove(
                product,
                createAndSaveStockItem(from, product),
                createAndSaveStockItem(to, product),
                null);
    }

    protected RequestedMove createAndSaveRequestedMove(Product product, StockItem from, StockItem to, StockMove move) {
        RequestedMove request = new RequestedMove();
        request.setStatus(MoveStatus.REQUESTED);
        request.setMove(move);
        request.setQuantity(1d);
        request.setProduct(product);
        request.setOrigin(MoveOrigin.LOGGED_USER);
        request.setTo(to);
        request.setFrom(from);
        request.setRequester(currentLoggedUser);
        request.setDescription("Descrição da solicitação");
        request.setNote("Observação das solicitações");
        request.setBranchOffice(from != null ? from.getStock().getBranchOffice() : to.getStock().getBranchOffice());

        return movesRequestRepository.save(request);
    }

    protected StockItem createAndSaveStockItem(Stock stock) {
        return createAndSaveStockItem(stock, createAndSaveProduct());
    }

    @Transactional
    protected StockItem createAndSaveStockItem(Stock stock, Product product) {
        Stock foundStock = stockRepository.getOne(stock.getId());

        if (foundStock.find(product).isEmpty()) {
            StockItem item = stockItemRepository.saveAndFlush(new StockItem(foundStock, product, null, null,
                    1d, 0d, 1d, 1000d, BigDecimal.TEN, QuantityLevel.UNDEFINED, new ArrayList<>()));
            if (foundStock.getItems() == null) {
                foundStock.setItems(new ArrayList<>());
            }
            foundStock.getItems().add(item);
            return item;
        }
        return foundStock.find(product).get();
    }

    protected ShedStock createAndSaveShedStock() {
        return createAndSaveBranchOffice().shed().orElse(null);
    }

    protected ShedStock createAndSaveShedStock(BranchOffice office) {
        ShedStock shedStock = new ShedStock();
        shedStock.setName("Galpão stock " + getRandomId());
        shedStock.setType(StockType.SHED);
        shedStock.setIsVisible(Boolean.TRUE);
        shedStock.setCity(office.getCity());
        shedStock.setBranchOffice(office);
        office.getStocks().add(shedStock);
        return stockRepository.saveAndFlush(shedStock);

    }

    protected MaintenanceStock createAndSaveMaintenanceStock(BranchOffice office) {
        MaintenanceStock maintenanceStock = new MaintenanceStock();
        maintenanceStock.setName("Manutenção stock " + getRandomId());
        maintenanceStock.setType(StockType.MAINTENANCE);
        maintenanceStock.setIsVisible(Boolean.TRUE);
        maintenanceStock.setCity(office.getCity());
        maintenanceStock.setBranchOffice(office);
        office.getStocks().add(maintenanceStock);
        return stockRepository.saveAndFlush(maintenanceStock);

    }

    protected ObsoleteStock createAndSaveObsoleteStock(BranchOffice office) {
        ObsoleteStock obsoleteStock = new ObsoleteStock();
        obsoleteStock.setName("Obsoletos stock " + getRandomId());
        obsoleteStock.setType(StockType.OBSOLETE);
        obsoleteStock.setIsVisible(Boolean.TRUE);
        obsoleteStock.setCity(office.getCity());
        obsoleteStock.setBranchOffice(office);
        office.getStocks().add(obsoleteStock);
        return stockRepository.saveAndFlush(obsoleteStock);

    }

    protected DefectiveStock createAndSaveDefectiveStock(BranchOffice office) {
        DefectiveStock defectiveStock = new DefectiveStock();
        defectiveStock.setName("Defeituosos stock " + getRandomId());
        defectiveStock.setType(StockType.DEFECTIVE);
        defectiveStock.setIsVisible(Boolean.TRUE);
        defectiveStock.setCity(office.getCity());
        defectiveStock.setBranchOffice(office);
        office.getStocks().add(defectiveStock);
        return stockRepository.saveAndFlush(defectiveStock);

    }

    protected CustomerStock createAndSaveCustomerStock(BranchOffice office) {
        CustomerStock customerStock = new CustomerStock();
        customerStock.setName("Clientes stock " + getRandomId());
        customerStock.setType(StockType.CUSTOMER);
        customerStock.setIsVisible(Boolean.TRUE);
        customerStock.setBranchOffice(office);
        office.getStocks().add(customerStock);
        return stockRepository.saveAndFlush(customerStock);

    }

    @Transactional
    protected CustomerStock createAndSaveCustomerStock() {
        return createAndSaveBranchOffice().customer().orElse(null);
    }

    @Transactional
    protected Stock createAndSaveMaintenanceStock() {
        return createAndSaveBranchOffice().maintenance().orElse(null);
    }

    protected TechnicianStock createAndSaveTechnicianStock() {
        return createAndSaveTechnicianStock(createAndSaveTechnician());
    }

    protected TechnicianStock createAndSaveTechnicianStock(BranchOffice branchOffice) {
        return createAndSaveTechnicianStock(createAndSaveTechnician(branchOffice));
    }

    protected TechnicianStock createAndSaveTechnicianStock(Technician technician) {
        TechnicianStock stock = new TechnicianStock();
        stock.setName("Tecnico Teste stock " + getRandomId());
        stock.setType(StockType.TECHNICIAN);
        stock.setIsVisible(technician.getIsActive());
        stock.setTechnician(technician);
        stock.setBranchOffice(technician.getBranchOffice());
        technician.getBranchOffice().getStocks().add(stock);

        return stockRepository.saveAndFlush(stock);

    }

    protected TechnicianStock createAndSaveTechnicianStockByEmployee(Employee employee) {
        return createAndSaveTechnicianStockByEmployee(employee, createAndSaveBranchOffice());
    }

    protected TechnicianStock createAndSaveTechnicianStockByEmployee(Employee employee, BranchOffice office) {
        TechnicianStock stock = new TechnicianStock();
        stock.setName("Tecnico Teste stock " + getRandomId());
        stock.setType(StockType.TECHNICIAN);
        stock.setIsVisible(Boolean.TRUE);
        stock.setTechnician(createAndSaveTechnicianByEmployee(employee, office));
        stock.setBranchOffice(office);

        TechnicianStock savedStock = stockRepository.findByUserId(stock.getTechnician().getUserId())
                .orElseGet(() -> stockRepository.saveAndFlush(stock));
        stock.getTechnician().setStock(savedStock);
        technicianRepository.saveAndFlush(savedStock.getTechnician());

        office.getStocks().add(savedStock);

        return savedStock;
    }

    protected Technician createAndSaveTechnician() {
        Technician technician = new Technician();
        technician.setName("Técnico de Teste");
        technician.setEmail("tecnico" + getRandomId() + "@giganet.psi.br");
        technician.setTechnicianId(UUID.randomUUID().toString().substring(0, 10));
        technician.setUserId(UUID.randomUUID().toString().substring(0, 10));
        technician.setId(UUID.randomUUID().toString().substring(0, 10));
        technician.setIsActive(Boolean.TRUE);
        technician.setSector(TechnicianSector.INSTALLATION);
        technician.setBranchOffice(createAndSaveBranchOffice());

        return technicianRepository.save(technician);
    }

    protected Technician createAndSaveTechnician(BranchOffice branchOffice) {
        Technician technician = new Technician();
        technician.setName("Técnico de Teste");
        technician.setEmail("tecnico" + getRandomId() + "@giganet.psi.br");
        technician.setTechnicianId(UUID.randomUUID().toString().substring(0, 10));
        technician.setUserId(UUID.randomUUID().toString().substring(0, 10));
        technician.setId(UUID.randomUUID().toString().substring(0, 10));
        technician.setIsActive(Boolean.TRUE);
        technician.setSector(TechnicianSector.INSTALLATION);
        technician.setBranchOffice(branchOffice);

        return technicianRepository.save(technician);
    }

    protected Technician createAndSaveTechnicianByEmployee(Employee employee) {
        return createAndSaveTechnicianByEmployee(employee, createAndSaveBranchOffice());
    }

    protected Technician createAndSaveTechnicianByEmployee(Employee employee, BranchOffice office) {
        Optional<Technician> op = technicianRepository.findByEmail(employee.getEmail());
        if (op.isPresent()) {
            return op.get();
        }
        Technician technician = new Technician();
        technician.setName(employee.getName());
        technician.setEmail(employee.getEmail());
        technician.setTechnicianId(UUID.randomUUID().toString().substring(0, 10));
        technician.setUserId(UUID.randomUUID().toString().substring(0, 10));
        technician.setId(employee.getId().toString());
        technician.setIsActive(Boolean.TRUE);
        technician.setSector(TechnicianSector.INSTALLATION);
        technician.setBranchOffice(office);
        Technician saved = technicianRepository.saveAndFlush(technician);
        saved.setStock(createAndSaveTechnicianStock(saved));

        return technicianRepository.saveAndFlush(saved);
    }

    protected Technician createAndSaveTechnicianByEmployeeAndSector(Employee employee, TechnicianSector sector) {
        Technician technician = new Technician();
        technician.setName(employee.getName());
        technician.setEmail(employee.getEmail());
        technician.setTechnicianId(UUID.randomUUID().toString().substring(0, 10));
        technician.setUserId(UUID.randomUUID().toString().substring(0, 10));
        technician.setId(employee.getId().toString());
        technician.setIsActive(Boolean.TRUE);
        technician.setSector(sector);

        return technicianRepository.findByEmail(employee.getEmail())
                .orElseGet(() -> technicianRepository.saveAndFlush(technician));
    }

    protected Patrimony createAndSavePatrimony() {
        return createAndSavePatrimony(createAndSavePatrimonyLocation(), createAndSaveProduct());
    }

    protected Patrimony createAndSavePatrimony(PatrimonyLocation patrimonyLocation) {
        return createAndSavePatrimony(patrimonyLocation, createAndSaveProduct());
    }

    protected Patrimony createAndSavePatrimony(Product product) {
        return createAndSavePatrimony(createAndSavePatrimonyLocation(), product);
    }

    protected Patrimony createAndSavePatrimony(PatrimonyLocation location, Product product) {
        Patrimony p = new Patrimony();
        p.setCurrentLocation(location);
        p.setCode(randomMACAddress());
        p.setNote("Obs");
        p.setProduct(product);
        p.setCodeType(PatrimonyCodeType.MAC_ADDRESS);
        p.setIsVisible(Boolean.TRUE);

        return patrimonyRepository.saveAndFlush(p);
    }

    protected ValidMacAddress createAndSaveValidMacAddress() {
        return createAndSaveValidMacAddress(randomMACAddress());
    }

    protected String randomMACAddress() {
        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);

        macAddr[0] = (byte) (macAddr[0] & (byte) 254);  //zeroing last 2 bytes to make it unicast and locally adminstrated

        StringBuilder sb = new StringBuilder(18);
        for (byte b : macAddr) {

            if (sb.length() > 0)
                sb.append(":");

            sb.append(String.format("%02x", b));
        }


        return sb.toString().toUpperCase();
    }

    protected ValidMacAddress createAndSaveValidMacAddress(String macAddress) {
        return validMacAddressesRepository.save(
                new ValidMacAddress(
                        macAddress.replaceAll("[:.-]", ""),
                        Boolean.FALSE));
    }

    protected PatrimonyLocation createAndSavePatrimonyLocation() {
        PatrimonyLocation location = new PatrimonyLocation();
        location.setCode(UUID.randomUUID().toString().substring(0, 10));
        location.setNote("Obs");
        location.setName("Galpão");
        location.setType(PatrimonyLocationType.SHED);

        return patrimonyLocationRepository.saveAndFlush(location);
    }

    protected PatrimonyLocation createAndSavePatrimonyLocation(String code) {
        PatrimonyLocation location = new PatrimonyLocation();
        location.setCode(code);
        location.setNote("Obs");
        location.setName("Galpão");
        location.setType(PatrimonyLocationType.SHED);

        return patrimonyLocationRepository.saveAndFlush(location);
    }

    protected PatrimonyMove createAndSavePatrimonyMove(Patrimony patrimony) {
        return createAndSavePatrimonyMove(patrimony, createAndSaveEmployee());
    }

    protected PatrimonyMove createAndSavePatrimonyMove(Patrimony patrimony, Employee employee) {
        PatrimonyMove move = new PatrimonyMove();
        move.setFrom(patrimony.getCurrentLocation());
        move.setTo(createAndSavePatrimonyLocation());
        move.setResponsible(employee);
        move.setPatrimony(patrimony);
        move.setNote("Observação");

        move = patrimonyMoveRepository.saveAndFlush(move);

        if (patrimony.getMoves() == null) {
            patrimony.setMoves(new ArrayList<>());
        }
        patrimony.getMoves().add(move);

        return move;
    }

    protected PurchaseOrder createAndSavePurchaseOrder() {
        int value = getRandomId();
        final var order = new PurchaseOrder();
        order.setId(UUID.randomUUID().toString().substring(0, 10));
        order.setStatus(ProcessStatus.PENDING);
        order.setNote("Observacao de teste " + value);
        order.setResponsible(createAndSaveEmployee().getName());

        order.setCostCenter("Centro de Custo");
        order.setExternalCreatedDate(ZonedDateTime.now());
        order.setDateOfNeed(LocalDate.now().plusMonths(1));

        order.setFreight(new PurchaseOrderFreight());
        order.getFreight().setId(UUID.randomUUID().toString().substring(0, 10));
        order.getFreight().setPrice(BigDecimal.TEN);
        order.getFreight().setType(FreightType.FOB);
        order.getFreight().setOrder(order);
        order.getFreight().setDeliveryAddress(addressService.getPurchaseOrderDeliveryAddress());
        order.getFreight().setDeliveryDate(ZonedDateTime.now().plusDays(20));

        order.setSupplier(new PurchaseOrderSupplier());
        order.getSupplier().setId(UUID.randomUUID().toString().substring(0, 10));
        order.getSupplier().setName("Fornecedor " + value);
        order.getSupplier().setEmail("email@email.com");
        order.getSupplier().setCellphone("12341234");
        order.getSupplier().setTelephone("12341234");
        order.getSupplier().setCnpj("55191816000131");
        order.getSupplier().setDescription("Descrição de teste " + value);
        order.getSupplier().setAddress(new Address());
        order.getSupplier().getAddress().setStreet("Rua teste " + value);
        order.getSupplier().getAddress().setNumber("120");
        order.getSupplier().getAddress().setDistrict("Horto");
        order.getSupplier().getAddress().setCity("Ipatinga");
        order.getSupplier().getAddress().setState("MG");

        order.setItems(new ArrayList<>());

        for (int i = 0; i < 3; i++) {
            final var item = new PurchaseOrderItem();
            item.setId(UUID.randomUUID().toString().substring(0, 10));
            item.setOrder(order);
            item.setStatus(ProcessStatus.PENDING);
            item.setProduct(createAndSaveProduct());
            item.setQuantity(2d * (i + 1));
            item.setIpi(4f);
            item.setIcms(18f);
            item.setUnit(createAndSaveUnit());
            item.setPrice(BigDecimal.valueOf(1.5 * getRandomId() + 1));
            item.setTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            order.getItems().add(item);
        }
        order.setTotal(
                order.getItems().stream()
                        .map(PurchaseOrderItem::getTotal)
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO)
        );

        return purchaseOrderRepository.save(order);
    }

    public MainDashboardItemGroup createAndSaveGroup() {
        return createAndSaveGroup("Grupo Default " + getRandomId(), GroupCategory.DEFAULT, currentLoggedUser);
    }

    public MainDashboardItemGroup createAndSaveGroup(String label, GroupCategory category, Employee employee) {
        MainDashboardItemGroup group = new MainDashboardItemGroup();
        group.setLabel(label);
        group.setCategory(category);
        group.setBranchOffice(createAndSaveBranchOffice());
        group.setEmployees(Collections.singleton(employee));
        group.setItems(new ArrayList<>());

        for (int i = 0; i < 2; i++) {
            MainDashboardItem item = new MainDashboardItem();
            item.setGroup(group);
            item.setProduct(createAndSaveProduct());
            item.setIndex(i);
            group.getItems().add(item);
        }

        return mainDashboardItemGroupRepository.saveAndFlush(group);
    }

    protected Employee createAndSaveEmployee() {
        int value = getRandomId();

        Employee employee = new Employee();
        employee.setName("Employee " + value);
        employee.setEmail("employee" + value + "@email.com");
        employee.setPassword(new BCryptPasswordEncoder().encode("123456"));
        employee.setPermissions(new HashSet<>(Collections.singletonList(createAndSavePermission())));

        return employeeRepository.findByEmail(employee.getEmail())
                .orElseGet(() -> employeeRepository.saveAndFlush(employee));
    }

    protected Employee createAndSaveEmployee(String email) {
        int value = getRandomId();

        Employee employee = new Employee();
        employee.setName("Employee " + value);
        employee.setEmail(email);
        employee.setPassword(new BCryptPasswordEncoder().encode("123456"));
        employee.setPermissions(new HashSet<>(Arrays.asList(
                createAndSavePermission("ROLE_ADMIN"),
                createAndSavePermission("ROLE_ROOT")
        )));

        return employeeRepository.findByEmail(employee.getEmail())
                .orElseGet(() -> employeeRepository.saveAndFlush(employee));
    }

    protected Permission createAndSavePermission() {
        final String permission = "ROLE_ADMIN";
        return permissionRepository.findById(permission)
                .orElseGet(() -> permissionRepository.saveAndFlush(new Permission(permission)));
    }

    protected Permission createAndSavePermission(String permission) {
        return permissionRepository.findById(permission)
                .orElseGet(() -> permissionRepository.saveAndFlush(new Permission(permission)));
    }

    protected void createCurrentUser() {
        try {
            if (currentLoggedUser == null) {
                currentLoggedUser = createAndSaveEmployee("teste@teste.com");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected int getRandomId() {
        final Random r = new Random();
        return r.nextInt(10000);
    }

}
