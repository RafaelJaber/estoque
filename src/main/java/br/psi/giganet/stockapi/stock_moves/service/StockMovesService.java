package br.psi.giganet.stockapi.stock_moves.service;

import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.common.reports.model.ReportFormat;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.entries.model.EntryItem;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock.model.CustomerStock;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.StockItem;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock.service.StockService;
import br.psi.giganet.stockapi.stock_moves.controller.request.AdvancedExportStockMoveRequest;
import br.psi.giganet.stockapi.stock_moves.controller.request.ExportMovesReportRequest;
import br.psi.giganet.stockapi.stock_moves.controller.request.UpdateMoveRequest;
import br.psi.giganet.stockapi.stock_moves.controller.response.StockMoveSimpleReportProjection;
import br.psi.giganet.stockapi.stock_moves.factory.MoveFactory;
import br.psi.giganet.stockapi.stock_moves.model.*;
import br.psi.giganet.stockapi.stock_moves.repository.AdvancedStockMovesRepository;
import br.psi.giganet.stockapi.stock_moves.repository.StockMovesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockMovesService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private StockMovesRepository stockMovesRepository;

    @Autowired
    private AdvancedStockMovesRepository advancedStockMovesRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockFactory stockFactory;

    @Autowired
    private MoveFactory moveFactory;

    @Autowired
    private DetachedMoveService detachedMoveService;

    @Autowired
    private EntryItemMoveService entryItemMoveService;

    @Autowired
    private TechnicianMoveService technicianMoveService;

    @Autowired
    private SaleMoveService saleMoveService;

    @Autowired
    private ScheduledStockMoveService scheduledStockMoveService;

    @Autowired
    private BranchOfficeService branchOfficeService;

    @Autowired
    private StockMovesReportService stockMovesReportService;

    public Page<? extends StockMove> findAll(List<String> queries, Integer page, Integer pageSize) {
        return this.advancedStockMovesRepository.findAll(
                queries,
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllByDescription(String description, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllByDescription(description,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public List<? extends StockMove> findAllTechnicianStockMoveByOrderId(String externalOrderId) {
        return this.stockMovesRepository.findAllTechnicianStockMoveByOrderId(
                externalOrderId,
                Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public List<? extends StockMove> findAllTechnicianStockMoveByOrderIdAndActivationId(
            String externalOrderId, String externalActivationId) {
        return this.stockMovesRepository.findAllTechnicianStockMoveByOrderIdAndActivationId(
                externalOrderId,
                externalActivationId,
                Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public List<? extends StockMove> findAllPendingTechnicianStockMoveByOrderId(String externalOrderId) {
        return this.stockMovesRepository.findAllPendingTechnicianStockMoveByOrderId(
                externalOrderId,
                Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public Page<StockMove> findAllByUserAndDescription(String userId, String description, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllByDescriptionAndStock(
                stockService.findByUser(userId)
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                description,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllRealizedByDescription(String userId, String description, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllRealizedByDescriptionAndStock(
                stockService.findByUser(userId)
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                description,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingByStockFrom(String userId, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingByStockFrom(
                stockService.findByUser(userId)
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingByStockTo(String userId, List<MoveReason> reasons, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingByStockToAndMoveReasonIn(
                stockService.findByUser(userId)
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                reasons,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingByStockTo(String userId, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingByStockTo(
                stockService.findByUser(userId)
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllByDescriptionAndTechnician(TechnicianStock technicianStock, String description, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllByDescriptionAndStock(
                stockService.findByUserId(technicianStock.getTechnician().getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                description,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllRealizedByDescription(String description, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllRealizedByDescription(description,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllRealizedByDescriptionAndTechnician(TechnicianStock technicianStock, String description, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllRealizedByDescriptionAndStock(
                stockService.findByUserId(technicianStock.getTechnician().getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                description,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    @Deprecated(forRemoval = true)
    public Page<StockMove> findAllPendingByCityStockFrom(CityOptions city, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingByCityStockFrom(city,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingByStockTypeFromAndBranchOffice(List<StockType> types, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingByStockTypeFromAndBranchOffice(
                types,
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPending(String description, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPending(description,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingFromServiceOrder(String description, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingFromServiceOrder(description,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingFromServiceOrderAndBranchOffice(String description, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingFromServiceOrderAndBranchOffice(
                description,
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingAndType(String description, MoveType type, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingAndType(description, type,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingByStockFrom(Stock from, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingByStockFrom(from,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingByStockFrom(TechnicianStock from, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingByStockFrom(
                stockService.findByUserId(from.getTechnician().getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingByStockTo(Stock to, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingByStockTo(to,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingByStockTo(TechnicianStock to, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingByStockTo(
                stockService.findByUserId(to.getTechnician().getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingByStockTo(TechnicianStock to, List<MoveReason> reasons, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingByStockToAndMoveReasonIn(
                stockService.findByUserId(to.getTechnician().getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                reasons,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    @Deprecated(forRemoval = true)
    public Page<StockMove> findAllPendingByCityStockTo(CityOptions city, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingByCityStockTo(city,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<StockMove> findAllPendingByStockTypeToAndBranchOffice(List<StockType> types, Integer page, Integer pageSize) {
        return this.stockMovesRepository.findAllPendingByStockTypeToAndBranchOffice(
                types,
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Optional<StockMove> findById(Long id) {
        return stockMovesRepository.findById(id);
    }

    public Boolean existsAnyPendingMoveByStock(Stock stock) {
        return stockMovesRepository.existsAnyPendingMoveByStock(stock);
    }

    public Page<? extends StockMoveSimpleReportProjection> findAllStockMovesSimpleReport(List<String> groupProperties, List<String> queries,
                                                                                         Integer page, Integer pageSize) {
        return this.advancedStockMovesRepository.findAllStockMovesSimpleReport(
                groupProperties,
                queries,
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name")));
    }

    /**
     *
     * @param queries
     * @param product
     * @param from
     * @param to
     * @param page
     * @param size
     * @return
     */
    public Page<? extends StockMove> detailsMoves(List<String> queries, Long product, Long from, Long to, Integer page, Integer size) {
        HashMap<String, Object> criteries = new HashMap<>();
        if (product != null && product > 0) {
            criteries.put("product", product);
        }
        if (from != null && from > 0) {
            criteries.put("from", from);
        }
        if (to != null && to > 0) {
            criteries.put("to", to);
        }

        return this.advancedStockMovesRepository.findAll(
                queries,
                criteries,
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "note")));
    }

    @Transactional
    public Optional<? extends StockMove> update(Long id, UpdateMoveRequest request) {
        return this.findById(id)
                .map(move -> {
                    Product product = this.productRepository.findById(String.valueOf(request.getProduct()))
                            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

                    move.setProduct(product);
                    move.setDescription(request.getDescription());
                    return this.stockMovesRepository.save(move);
                });
    }

    @Transactional
    public List<DetachedStockMove> insertDetachedMove(List<DetachedStockMove> moves) {
        return moves.stream()
                .map(move -> {
                    move.setFrom(move.getFrom() != null ?
                            stockService.findByStockAndProductId(move.getFrom().getStock(), move.getProduct().getId())
                                    .orElseGet(() -> stockService.saveStockItem(move.getFrom().getStock(), move.getProduct())) : null);

                    move.setTo(move.getTo() != null ?
                            stockService.findByStockAndProductId(move.getTo().getStock(), move.getProduct().getId())
                                    .orElseGet(() -> stockService.saveStockItem(move.getTo().getStock(), move.getProduct())) : null);

                    move.setRequester(employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));

                    final DetachedStockMove savedMove = stockMovesRepository.save(detachedMoveService.create(move)
                            .orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar esta movimentação")));

                    if (savedMove.getFrom() != null) {
                        stockService.updateStockItem(savedMove.getFrom());
                    }
                    if (savedMove.getTo() != null) {
                        stockService.updateStockItem(savedMove.getTo());
                    }

                    return savedMove;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TechnicianStockMove> insertTechnicianMove(List<TechnicianStockMove> moves) {
        return moves.stream()
                .map(move -> {
                    if (move.getFrom() != null && move.getFrom().getStock() instanceof TechnicianStock) {
                        Stock stockFrom = stockService.findByUserId(
                                ((TechnicianStock) move.getFrom().getStock())
                                        .getTechnician()
                                        .getUserId())
                                .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado"));
                        move.setFrom(stockFrom.find(move.getProduct())
                                .orElseGet(() -> stockService.saveStockItem(stockFrom, move.getProduct())));

                    }

                    if (move.getTo() != null && move.getTo().getStock() instanceof TechnicianStock) {
                        Stock stockTo = stockService.findByUserId(
                                ((TechnicianStock) move.getTo().getStock())
                                        .getTechnician()
                                        .getUserId())
                                .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado"));
                        move.setTo(stockTo.find(move.getProduct())
                                .orElseGet(() -> stockService.saveStockItem(stockTo, move.getProduct())));

                    }

                    if (move.getFrom() != null) {

                        if (move.getFrom().getStock().getId() != null) {
                            move.setFrom(stockService.findByStockAndProductId(move.getFrom().getStock(), move.getProduct().getId())
                                    .orElseGet(() -> stockService.saveStockItem(move.getFrom().getStock(), move.getProduct())));

                        } else if (move.getFrom().getStock().isCustomer()) {
                            CustomerStock customerStock = move.getTo().getStock().getBranchOffice().customer()
                                    .orElseThrow(() -> new IllegalArgumentException("Estoque do cliente não foi encontrado"));
                            move.setFrom(customerStock.find(move.getProduct())
                                    .orElseGet(() -> stockService.saveStockItem(customerStock, move.getProduct())));
                        }

                    }


                    if (move.getTo() != null) {

                        if (move.getTo().getStock().getId() != null) {
                            move.setTo(stockService.findByStockAndProductId(move.getTo().getStock(), move.getProduct().getId())
                                    .orElseGet(() -> stockService.saveStockItem(move.getTo().getStock(), move.getProduct())));

                        } else if (move.getTo().getStock().isCustomer()) {
                            CustomerStock customerStock = move.getFrom().getStock().getBranchOffice().customer()
                                    .orElseThrow(() -> new IllegalArgumentException("Estoque do cliente não foi encontrado"));
                            move.setTo(customerStock.find(move.getProduct())
                                    .orElseGet(() -> stockService.saveStockItem(customerStock, move.getProduct())));

                        }

                    }

                    move.setRequester(employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));

                    final TechnicianStockMove savedMove = stockMovesRepository.save(technicianMoveService.create(move)
                            .orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar esta movimentação")));

                    if (savedMove.getFrom() != null) {
                        stockService.updateStockItem(savedMove.getFrom());
                    }
                    if (savedMove.getTo() != null) {
                        stockService.updateStockItem(savedMove.getTo());
                    }

                    return savedMove;

                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ScheduledStockMove> insertScheduledMove(List<ScheduledStockMove> moves) {
        return moves.stream()
                .map(move ->
                        stockMovesRepository.save(
                                scheduledStockMoveService.create(move)
                                        .orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar esta movimentação")))
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public List<? extends StockMove> approveAllByExternalOrderId(String orderId) {
        return stockMovesRepository.findAllPendingTechnicianStockMoveByOrderId(
                orderId,
                Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(move -> approve(move.getId()).orElseThrow(
                        () -> new IllegalArgumentException("Movimentação " + move.getId() + " não foi encontrada")))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<? extends StockMove> approve(List<Long> moves) {
        return moves.stream()
                .sorted(Long::compareTo)
                .map(id -> approve(id).orElseThrow(
                        () -> new IllegalArgumentException("Movimentação " + id + " não foi encontrada")))
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<? extends StockMove> approve(Long id) {
        return this.findById(id)
                .flatMap(move -> {
                    move.setResponsible(employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));

                    Optional<? extends StockMove> savedMove;
                    if (move instanceof TechnicianStockMove) {
                        savedMove = technicianMoveService.approveAndExecute((TechnicianStockMove) move);
                        final StockMove saved = savedMove.orElseThrow(() -> new IllegalArgumentException(
                                "Não foi possível executar a movimentação de retorno para a manutenção"));

                        // if this move is about returned item, it should return it to maintenance immediately
                        if (saved.getReason().equals(MoveReason.SERVICE_ORDER) &&
                                saved.getFrom().getStock().getType().equals(StockType.CUSTOMER)) {

                            Stock maintenanceStock = saved.getTo().getStock()
                                    .getBranchOffice()
                                    .maintenance()
                                    .orElseThrow(() -> new IllegalArgumentException("Estoque de manutenção não encontrado"));

                            StockItem maintenanceStockItem = stockService.findByStockAndProductId(maintenanceStock,
                                    saved.getProduct().getId())
                                    .orElseGet(() -> stockService.saveStockItem(maintenanceStock, saved.getProduct()));

                            TechnicianStockMove returnToMaintenance = moveFactory.createTechnicianStockMove(
                                    saved.getTo(),
                                    maintenanceStockItem,
                                    saved.getQuantity(),
                                    MoveType.BETWEEN_STOCKS,
                                    move.getOrigin());
                            returnToMaintenance.setCustomerName(((TechnicianStockMove) saved).getCustomerName());
                            returnToMaintenance.setRequester(saved.getRequester());

                            stockMovesRepository.save(technicianMoveService.create(returnToMaintenance)
                                    .orElseThrow(() -> new IllegalArgumentException(
                                            "Não foi possível salvar a movimentação de retorno para manutenção")));
                        }

                    } else if (move instanceof DetachedStockMove) {
                        savedMove = detachedMoveService.approveAndExecute((DetachedStockMove) move);
                    } else if (move instanceof ScheduledStockMove) {
                        savedMove = scheduledStockMoveService.approveAndExecute((ScheduledStockMove) move);
                    } else {
                        throw new IllegalArgumentException("Não é possível rejeitar esta movimentação. Tipo não suportado");
                    }

                    savedMove.map(saved -> stockMovesRepository.save(saved))
                            .orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar a movimentação"));

                    if (savedMove.get().getFrom() != null) {
                        stockService.updateStockItem(savedMove.get().getFrom());
                    }
                    if (savedMove.get().getTo() != null) {
                        stockService.updateStockItem(savedMove.get().getTo());
                    }

                    return savedMove;
                });
    }

    @Transactional
    public Optional<? extends StockMove> cancel(Long id) {
        return this.findById(id)
                .map(move -> {
                    move.setResponsible(employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));

                    Optional<? extends StockMove> savedMove;
                    if (move instanceof TechnicianStockMove) {
                        savedMove = technicianMoveService.cancel((TechnicianStockMove) move);
                    } else if (move instanceof DetachedStockMove) {
                        savedMove = detachedMoveService.cancel((DetachedStockMove) move);
                    } else if (move instanceof ScheduledStockMove) {
                        savedMove = scheduledStockMoveService.cancel((ScheduledStockMove) move);
                    } else {
                        throw new IllegalArgumentException("Não é possível rejeitar esta movimentação. Tipo não suportado");
                    }
                    savedMove.orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar esta movimentação"));

                    return stockMovesRepository.save(savedMove.get());
                });
    }

    @Transactional
    public List<? extends StockMove> rejectAllByExternalOrderId(String orderId) {
        return stockMovesRepository.findAllPendingTechnicianStockMoveByOrderId(
                orderId,
                Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(move -> reject(move.getId()).orElseThrow(
                        () -> new IllegalArgumentException("Movimentação " + move.getId() + " não foi encontrada")))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<? extends StockMove> reject(List<Long> moves) {
        return moves.stream()
                .sorted(Long::compareTo)
                .map(id -> reject(id).orElseThrow(
                        () -> new IllegalArgumentException("Movimentação " + id + " não foi encontrada")))
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<? extends StockMove> reject(Long id) {
        return this.findById(id)
                .map(move -> {
                    move.setResponsible(employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));

                    Optional<? extends StockMove> savedMove;
                    if (move instanceof TechnicianStockMove) {
                        savedMove = technicianMoveService.reject((TechnicianStockMove) move);
                    } else if (move instanceof DetachedStockMove) {
                        savedMove = detachedMoveService.reject((DetachedStockMove) move);
                    } else if (move instanceof ScheduledStockMove) {
                        savedMove = scheduledStockMoveService.reject((ScheduledStockMove) move);
                    } else {
                        throw new IllegalArgumentException("Não é possível rejeitar esta movimentação. Tipo não suportado");
                    }
                    savedMove.orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar esta movimentação"));

                    return stockMovesRepository.save(savedMove.get());
                });
    }

    public Optional<EntryItemStockMove> addItemsToStock(EntryItem entryItem, StockItem stock) {
        final EntryItemStockMove move = moveFactory.createEntryItemStockMove(entryItem, stock);
        return entryItemMoveService.create(move)
                .map(moved -> stockMovesRepository.save(moved));
    }

    public List<StockMove> insertSaleMove(List<SaleStockMove> salesStockMoves) {
        return salesStockMoves.stream()
                .map(move -> {
                    if (move.getFrom() != null) {
                        if (move.getFrom().getStock().getId() != null) {
                            move.setFrom(stockService.findByStockAndProductId(move.getFrom().getStock(), move.getProduct().getId())
                                    .orElseGet(() -> stockService.saveStockItem(move.getFrom().getStock(), move.getProduct())));

                        } else if (move.getFrom().getStock().isCustomer()) {
                            CustomerStock customerStock = move.getTo().getStock().getBranchOffice().customer()
                                    .orElseThrow(() -> new IllegalArgumentException("Estoque do cliente não foi encontrado"));
                            move.setFrom(customerStock.find(move.getProduct())
                                    .orElseGet(() -> stockService.saveStockItem(customerStock, move.getProduct())));
                        }

                    }

                    if (move.getTo() == null) {
                        CustomerStock customerStock = move.getFrom().getStock().getBranchOffice().customer()
                                .orElseThrow(() -> new IllegalArgumentException("Estoque do cliente não foi encontrado"));
                        move.setTo(customerStock.find(move.getProduct())
                                .orElseGet(() -> stockService.saveStockItem(customerStock, move.getProduct())));
                    }

                    move.setRequester(employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));

                    final SaleStockMove savedMove = stockMovesRepository.save(saleMoveService.create(move)
                            .orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar esta movimentação")));
//
                    if (savedMove.getFrom() != null) {
                        stockService.updateStockItem(savedMove.getFrom());
                    }
                    if (savedMove.getTo() != null) {
                        stockService.updateStockItem(savedMove.getTo());
                    }
                    return move;
                })
                .collect(Collectors.toList());
    }

    public File stockMovesAdvancedReport(AdvancedExportStockMoveRequest request, ReportFormat format) throws Exception {
        return stockMovesReportService.createStockMovesAdvancedReport(request.getColumns(), request.getData(), format);
    }

    public File movesReportDownload(ExportMovesReportRequest request, ReportFormat format) throws Exception {
        return stockMovesReportService.createStockMovesSimpleReport(request.getColumns(), request.getData(), format);
    }
}
