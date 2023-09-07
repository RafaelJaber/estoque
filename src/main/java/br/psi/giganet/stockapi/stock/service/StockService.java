package br.psi.giganet.stockapi.stock.service;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.common.notifications.service.NotificationService;
import br.psi.giganet.stockapi.common.reports.model.ReportFormat;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.entries.model.Entry;
import br.psi.giganet.stockapi.entries.model.EntryItem;
import br.psi.giganet.stockapi.entries.service.EntryService;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.service.ProductService;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock.model.*;
import br.psi.giganet.stockapi.stock.model.enums.QuantityLevel;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock.repository.GeneralStockItem;
import br.psi.giganet.stockapi.stock.repository.StockItemQuantityLevelRepository;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.stock_moves.factory.MoveFactory;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.stock_moves.service.StockMovesService;
import br.psi.giganet.stockapi.technician.model.Technician;
import br.psi.giganet.stockapi.technician.service.TechnicianService;
import org.dom4j.Branch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockService {

    @Autowired
    private StockRepository stocksRepository;
    @Autowired
    private StockItemRepository stockItemRepository;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private StockMovesService stockMovesService;
    @Autowired
    private MoveFactory moveFactory;
    @Autowired
    private StockFactory stocksFactory;
    @Autowired
    private ProductService productService;
    @Autowired
    private TechnicianService technicianService;
    @Autowired
    private EntryService entryService;
    @Autowired
    private StockItemQuantityLevelRepository quantityLevelRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private StockReportService stockReportService;
    @Autowired
    private BranchOfficeService branchOfficeService;

    @Transactional
    public Page<Stock> findAll(Integer page, Integer pageSize) {
        return this.stocksRepository.findAll(
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name")));
    }

    @Transactional
    public Page<Stock> findAllByCurrentBranchOffice(Integer page, Integer pageSize) {
        return this.stocksRepository.findByBranchOfficeAndIsVisible(
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Page<Stock> findAllAvailableToMove(Integer page, Integer pageSize) {
        return this.stocksRepository.findAllAvailableToMoveByBranchOffice(
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name")));
    }

    @Transactional
    public Page<Stock> findAllAvailableToMoveByTechnician(Integer page, Integer pageSize) {
        Employee employee = employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));

        TechnicianStock technicianStock = technicianService.findByEmail(employee.getEmail())
                .map(Technician::getStock)
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado"));

        return this.stocksRepository.findAllAvailableToMoveByTechnician(
                technicianStock,
                technicianStock.getBranchOffice(),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name")));
    }

    @Transactional
    public Page<Stock> findByStockType(StockType type, Integer page, Integer pageSize) {
        return this.stocksRepository.findByStockType(type,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name")));
    }

    @Transactional
    public Page<Stock> findByStockTypeAndCurrentBranchOffice(StockType type, Integer page, Integer pageSize) {
        return this.stocksRepository.findByStockTypeAndBranchOfficeAndIsVisible(
                type,
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name")));
    }

    @Transactional
    public Page<GeneralStockItem> findByProductGroupByProductAndCurrentBranchOffice(String name, Integer page, Integer pageSize) {
        return this.stockItemRepository.findByProductAndBranchOfficeGroupByProduct(
                name,
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "product.name")));
    }

    public Page<StockItem> findByStockAndNameContainingAndCodeFilteringEmpties(Stock stock, String name, String code, Integer page, Integer pageSize) {
        return this.stockItemRepository.findByStockAndNameContainingAndCodeFilteringEmpties(stock, name, code,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "product.name")));
    }

    public Page<StockItem> findByStockAndNameContainingAndCodeFilteringAvailable(Stock stock, String name, String code, Integer page, Integer pageSize) {
        return this.stockItemRepository.findByStockAndNameContainingAndCodeFilteringAvailable(stock, name, code,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "product.name")));
    }

    public Page<StockItem> findByStockAndNameContainingAndCode(Stock stock, String name, String code, Integer page, Integer pageSize) {
        return this.stockItemRepository.findByStockAndNameContainingAndCode(stock, name, code,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "product.name")));
    }

    public Page<StockItem> findByStockAndNameContainingAndCodeFilteringEmpties(TechnicianStock stock, String name, String code, Integer page, Integer pageSize) {
        return this.stockItemRepository.findByStockAndNameContainingAndCodeFilteringEmpties(
                stocksRepository.findByUserId(stock.getTechnician().getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                name,
                code,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "product.name")));
    }

    public Page<StockItem> findByStockAndNameContainingAndCode(TechnicianStock stock, String name, String code, Integer page, Integer pageSize) {
        return this.stockItemRepository.findByStockAndNameContainingAndCode(
                stocksRepository.findByUserId(stock.getTechnician().getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                name,
                code,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "product.name")));
    }

    public Optional<StockItem> findByStockAndCode(Stock stock, String code) {
        return this.stockItemRepository.findByStockAndCode(stock, code);
    }

    public Optional<StockItem> findByStockAndProductId(Stock stock, String id) {
        return this.stockItemRepository.findByStockAndProductId(stock, id);
    }

    public Optional<Stock> findById(Long id) {
        return this.stocksRepository.findById(id);
    }

    public Optional<TechnicianStock> findByUserId(String userId) {
        return this.stocksRepository.findByUserId(userId);
    }

    public Optional<Stock> findByUser(String userId) {
        return this.stocksRepository.findByUser(userId);
    }

    public Optional<StockItem> findByStockItemId(Long stockItem, Stock stock) {
        this.findById(stock.getId()).orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado"));
        return this.stockItemRepository.findById(stockItem);
    }

    public File getStockSituationReport(Stock stock, ReportFormat format) throws Exception {
        return stockReportService.getStockSituationReport(
                stocksRepository.findById(stock.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                format);
    }

    public Optional<Stock> updateStockBranchOffice(Long id, BranchOffice branchOffice) {
        return this.stocksRepository.findById(id)
                .map(stock -> {
                    if (stockMovesService.existsAnyPendingMoveByStock(stock)) {
                        throw new IllegalArgumentException("Não é possível alterar a filial do estoque pois ainda há movimentações pendentes");
                    }

                    stock.setBranchOffice(branchOffice);
                    return stocksRepository.save(stock);
                });
    }

    @Transactional
    public Optional<StockItem> updateParameters(Long stockItem, Stock stock, StockItem item) {
        return this.findByStockItemId(stockItem, stock)
                .map(saved -> {
                    if (item.getBlockedQuantity() != null) {
                        saved.setBlockedQuantity(item.getBlockedQuantity());
                    }

                    if (item.getMinQuantity() > item.getMaxQuantity()) {
                        throw new IllegalArgumentException("Quantidade mínima não pode ser maior do que a quantidade máxima");
                    } else if (item.getQuantity() != null && item.getQuantity() < 0) {
                        throw new IllegalArgumentException("Quantidade não pode ser menor do que 0");
                    }

                    if (item.getQuantity() != null && item.getQuantity() > saved.getQuantity()) {
                        stockMovesService.insertDetachedMove(Collections.singletonList(moveFactory.createDetachedStockMove(
                                null,
                                saved,
                                item.getQuantity() - saved.getQuantity(),
                                MoveType.ENTRY_ITEM)));
                    } else if (item.getQuantity() != null && item.getQuantity() < saved.getQuantity()) {
                        stockMovesService.insertDetachedMove(Collections.singletonList(moveFactory.createDetachedStockMove(
                                saved,
                                null,
                                saved.getQuantity() - item.getQuantity(),
                                MoveType.OUT_ITEM)));
                    }

                    saved.setMinQuantity(item.getMinQuantity());
                    saved.setMaxQuantity(item.getMaxQuantity());

                    if (saved.getLevels() != null) {
                        quantityLevelRepository.deleteInBatch(
                                saved.getLevels().stream()
                                        .filter(level -> !item.getLevels().contains(level))
                                        .collect(Collectors.toList()));
                        saved.getLevels().removeIf(level -> !item.getLevels().contains(level));

                        item.getLevels().stream()
                                .filter(level -> saved.getLevels().contains(level))
                                .forEach(level -> {
                                    final int index = saved.getLevels().indexOf(level);
                                    saved.getLevels().get(index).setFrom(level.getFrom());
                                    saved.getLevels().get(index).setTo(level.getTo());
                                });
                    }
                    saved.getLevels().addAll(item.getLevels()
                            .stream()
                            .filter(level -> !saved.getLevels().contains(level))
                            .peek(level -> level.setStockItem(saved))
                            .collect(Collectors.toList()));

                    final Set<QuantityLevel> tempLevels = new HashSet<>();
                    saved.getLevels().forEach(level -> {
                        if (level.getFrom() != null && level.getTo() != null &&
                                level.getFrom().compareTo(level.getTo()) >= 0) {
                            throw new IllegalArgumentException("Intervalo definido de " +
                                    level.getFrom() + " a " + level.getTo() + " é inválido");
                        }
                        if (tempLevels.contains(level.getLevel())) {
                            throw new IllegalArgumentException("Nível " + level.getLevel().name() +
                                    " foi informado mais de 1 vez");
                        }
                        tempLevels.add(level.getLevel());
                    });
                    quantityLevelRepository.saveAll(saved.getLevels());
                    saved.setCurrentLevel(getCurrentLevel(saved));
                    handleNotifications(saved);

                    StockItem updated = this.stockItemRepository.saveAndFlush(saved);
                    stockItemRepository.updatePricePerUnitByProduct(updated.getProduct(), item.getLastPricePerUnit());

                    return updated;
                });
    }

    public void addItemsToStockByEntry(Entry entry) {
        Stock stock = entry.getStock();
        Stock saved = this.stocksRepository.findById(stock.getId())
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado"));

        entry.getItems().forEach(entryItem -> {
            final StockItem stockItem = stock.find(entryItem.getProduct())
                    .orElseGet(() -> saveStockItem(saved, entryItem.getProduct(), entryItem.getQuantity(), entryItem.getPrice()));
            stockItem.setLastPricePerUnit(entryItem.getPrice());

            entryItem.setEntryMove(stockMovesService.addItemsToStock(entryItem, stockItem)
                    .orElseThrow(() -> new IllegalArgumentException("Não foi possível realizar este lançamento")));
            stockItem.setCurrentLevel(getCurrentLevel(stockItem));
            handleNotifications(stockItem);
            stockItemRepository.save(stockItem);
        });
    }

    public Optional<StockItem> updateStockItem(StockItem stockItem) {
        return this.findByStockItemId(stockItem.getId(), stockItem.getStock())
                .map(saved -> {
                    saved.setQuantity(stockItem.getQuantity());
                    saved.setCurrentLevel(getCurrentLevel(saved));
                    handleNotifications(saved);
                    return stockItemRepository.save(saved);
                });
    }

    public Optional<Stock> updateStockVisibility(Long id, Boolean isVisible) {
        return stocksRepository.findById(id)
                .map(stock -> {
                    if (!stock.isTechnician()) {
                        throw new IllegalArgumentException("É permitido alterar a visibilidade apenas de estoques do tipo TECNICO");
                    }
                    stock.setIsVisible(isVisible);
                    return stocksRepository.save(stock);
                });
    }

    public StockItem saveStockItem(Stock stock, Product product) {
        Optional<EntryItem> entryItem = entryService.findLastEntryItemByProduct(product);
        BigDecimal lastPricePerUnit = BigDecimal.ZERO;
        if (entryItem.isPresent()) {
            lastPricePerUnit = entryItem.get().getPrice();
        }
        return saveStockItem(stock, product, 100d, lastPricePerUnit);
    }

    public StockItem saveStockItem(Stock stock, Product product, Double maxQuantity, BigDecimal price) {
        StockItem item = stockItemRepository.saveAndFlush(stocksFactory.createItem(
                this.findById(stock.getId()).orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                productService.findById(product.getId()).orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")),
                maxQuantity,
                price));
        if (item.getStock().getItems() == null) {
            item.getStock().setItems(new ArrayList<>());
        }
        item.getStock().getItems().add(item);
        return item;
    }

    public Optional<Stock> save(Stock stock) {
        return Optional.of(stocksRepository.save(stock));
    }

    private void handleNotifications(StockItem item) {
        switch (item.getCurrentLevel()) {
            case LOW:
                notificationService.onLowLevelStockItem(item);
                break;
            case VERY_LOW:
                notificationService.onVeryLowLevelStockItem(item);
            default:
        }
    }

    private QuantityLevel getCurrentLevel(StockItem item) {
        if (item.getLevels() != null) {
            final List<QuantityLevel> orderList = Arrays.asList(
                    QuantityLevel.VERY_LOW,
                    QuantityLevel.LOW,
                    QuantityLevel.NORMAL,
                    QuantityLevel.HIGH,
                    QuantityLevel.VERY_HIGH);

            return item.getLevels().stream()
                    .sorted(Comparator.comparingInt(l -> orderList.indexOf(l.getLevel())))
                    .filter(l -> {
                        final double initialPercent = l.getFrom() == null ? -100000000000000d : l.getFrom() / 100; // percent
                        final double finalPercent = l.getTo() == null ? 1000000000000000000d : l.getTo() / 100; // percent

                        final Double minQuantity = item.getMinQuantity();
                        final Double maxQuantity = item.getMaxQuantity();

                        final double range = maxQuantity - minQuantity;

                        final Double currentQuantity = item.getQuantity();

                        return currentQuantity >= (minQuantity + initialPercent * range) &&
                                currentQuantity < (minQuantity + finalPercent * range);
                    })
                    .findFirst()
                    .map(StockItemQuantityLevel::getLevel)
                    .orElse(QuantityLevel.UNDEFINED);
        }

        return QuantityLevel.UNDEFINED;
    }

    public Optional<SellerStock> findSellerStockByUserId(String userId) {
        return stocksRepository.findSellerStockByUserId(userId);
    }

    public Optional<Stock> findByEmployee(Employee employee) {
        return stocksRepository.findByUser(employee.getUserId());
    }

    public Optional<Stock> findByCurrentLoggedEmployeeAndBranchOffice() {
        Employee employee = employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("O usuário não está logado ou não é um funcionário!"));
//        Optional<BranchOffice> currBranchOffice = branchOfficeService.getCurrentBranchOffice();
        return stocksRepository.findByUserIdBranchOffice(employee.getUserId(),
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));
    }
}
