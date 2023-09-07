package br.psi.giganet.stockapi.stock_moves.controller;

import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.common.reports.model.ReportFormat;
import br.psi.giganet.stockapi.common.utils.controller.DownloadFileControllerUtil;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.stock.controller.security.RoleStocksRead;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock_moves.adapter.StockMovesAdapter;
import br.psi.giganet.stockapi.stock_moves.controller.request.*;
import br.psi.giganet.stockapi.stock_moves.controller.response.AdvancedStockMoveProjection;
import br.psi.giganet.stockapi.stock_moves.controller.response.ServiceOrderMoveResponse;
import br.psi.giganet.stockapi.stock_moves.controller.response.StockMoveProjection;
import br.psi.giganet.stockapi.stock_moves.controller.response.StockMoveSimpleReportProjection;
import br.psi.giganet.stockapi.stock_moves.controller.security.RoleMovesRead;
import br.psi.giganet.stockapi.stock_moves.controller.security.RoleMovesWrite;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.stock_moves.model.TechnicianStockMove;
import br.psi.giganet.stockapi.stock_moves.service.StockMovesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/moves")
public class MovesController {

    @Autowired
    private StockMovesService movesService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private StockMovesAdapter stockMovesAdapter;
    @Autowired
    private StockFactory stockFactory;

    @GetMapping
    @RoleMovesRead
    public Page<StockMoveProjection> findAllByDescription(
            @RequestParam(defaultValue = "") String description,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAllByDescription(description, page, pageSize)
                .map(stockMovesAdapter::transform);
    }

    @GetMapping(params = {"advanced"})
    @RoleMovesRead
    public Page<AdvancedStockMoveProjection> findAllAdvanced(
            @RequestParam(defaultValue = "", name = "search") List<String> queries,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAll(queries, page, pageSize)
                .map(stockMovesAdapter::transformToAdvancedStockMoveProjection);
    }

    @PostMapping("/exports")
    @RoleMovesRead
    public void findAllAdvancedExport(
            @RequestBody AdvancedExportStockMoveRequest request,
            @RequestParam ReportFormat format,
            HttpServletResponse response) throws Exception {
        DownloadFileControllerUtil.appendFile(
                movesService.stockMovesAdvancedReport(request, format),
                response);
    }

    @GetMapping("/report")
    @RoleMovesRead
    public Page<? extends StockMoveSimpleReportProjection> findAllStockMovesSimpleReport(
            @RequestParam(name = "groupProperties") List<String> groupProperties,
            @RequestParam(defaultValue = "", name = "search") List<String> queries,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return movesService.findAllStockMovesSimpleReport(groupProperties, queries, page, pageSize);
    }

    @GetMapping("/report/details")
    @RoleMovesRead
    public Page<? extends StockMoveProjection> findAllStockMovesSimpleReport(
            @RequestParam(defaultValue = "", name = "search") List<String> queries,
            @RequestParam(defaultValue = "0", name = "product") Long product,
            @RequestParam(defaultValue = "0", name = "from") Long from,
            @RequestParam(defaultValue = "0", name = "to") Long to,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "1000") Integer pageSize) {
        return movesService.detailsMoves(queries, product, from, to, page, pageSize)
                .map(stockMovesAdapter::transformToResponse);
    }

    @PostMapping("/report/exports")
    @RoleStocksRead
    public void findAllStockMovesSimpleReport(
            @RequestBody ExportMovesReportRequest request,
            @RequestParam ReportFormat format,
            HttpServletResponse response) throws Exception {
        DownloadFileControllerUtil.appendFile(
                movesService.movesReportDownload(request, format),
                response);
    }

    @GetMapping("/realized")
    @RoleMovesRead
    public Page<StockMoveProjection> findAllRealizedByDescription(
            @RequestParam(defaultValue = "") String description,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAllRealizedByDescription(description, page, pageSize)
                .map(stockMovesAdapter::transform);
    }

    @GetMapping("/pending")
    @RoleMovesRead
    public Page<StockMoveProjection> findAllPendingByDescription(
            @RequestParam(defaultValue = "") String description,
            @RequestParam(required = false) MoveType type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return type != null ?
                movesService.findAllPendingAndType(description, type, page, pageSize)
                        .map(stockMovesAdapter::transform) :
                movesService.findAllPending(description, page, pageSize)
                        .map(stockMovesAdapter::transform);
    }

    @GetMapping("/pending/service-orders")
    @RoleMovesRead
    public Page<ServiceOrderMoveResponse> findAllPendingFromServiceOrdersByDescription(
            @RequestParam(defaultValue = "") String description,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAllPendingFromServiceOrderAndBranchOffice(description, page, pageSize)
                .map(move -> stockMovesAdapter.transformToServiceOrderMoveResponse((TechnicianStockMove) move));
    }

    @GetMapping("/from/{stock}/pending")
    @RoleMovesRead
    public Page<StockMoveProjection> findAllPendingByStockFrom(
            @PathVariable Long stock,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAllPendingByStockFrom(stockFactory.create(stock), page, pageSize)
                .map(stockMovesAdapter::transform);
    }

    @GetMapping("/to/employee/pending")
    @RoleMovesRead
    public Page<AdvancedStockMoveProjection> findAllPendingByStockTo(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        String userId = employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não logado"))
                .getUserId();
        return movesService.findAllPendingByStockTo(userId, page, pageSize)
                .map(stockMovesAdapter::transformToAdvancedStockMoveProjectionWithCustomerName);
    }


    @GetMapping("/from/city/{city}/pending")
    @RoleMovesRead
    @Deprecated(forRemoval = true)
    public Page<AdvancedStockMoveProjection> findAllPendingByCityStockFrom(
            @PathVariable CityOptions city,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAllPendingByCityStockFrom(city, page, pageSize)
                .map(stockMovesAdapter::transformToAdvancedStockMoveProjection);
    }

    @GetMapping("/from/pending")
    @RoleMovesRead
    public Page<AdvancedStockMoveProjection> findAllPendingByStockTypeFrom(
            @RequestParam(defaultValue = "") List<StockType> types,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAllPendingByStockTypeFromAndBranchOffice(
                types, page, pageSize)
                .map(stockMovesAdapter::transformToAdvancedStockMoveProjection);
    }

    @GetMapping("/to/city/{city}/pending")
    @RoleMovesRead
    @Deprecated(forRemoval = true)
    public Page<AdvancedStockMoveProjection> findAllPendingByCityStockTo(
            @PathVariable CityOptions city,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAllPendingByCityStockTo(city, page, pageSize)
                .map(stockMovesAdapter::transformToAdvancedStockMoveProjectionWithCustomerName);
    }


    @GetMapping("/to/pending")
    @RoleMovesRead
    public Page<AdvancedStockMoveProjection> findAllPendingByStockTypeTo(
            @RequestParam(defaultValue = "") List<StockType> types,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAllPendingByStockTypeToAndBranchOffice(
                types, page, pageSize)
                .map(stockMovesAdapter::transformToAdvancedStockMoveProjectionWithCustomerName);
    }

    @GetMapping("/to/{stock}/pending")
    @RoleMovesRead
    public Page<StockMoveProjection> findAllPendingByStockTo(
            @PathVariable Long stock,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAllPendingByStockTo(stockFactory.create(stock), page, pageSize)
                .map(stockMovesAdapter::transform);
    }

    @GetMapping("/{id}")
    @RoleMovesRead
    public StockMoveProjection findById(@PathVariable Long id) {
        return movesService.findById(id)
                .map(stockMovesAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada"));
    }

    @PostMapping
    @RoleMovesWrite
    @ResponseStatus(HttpStatus.CREATED)
    public List<StockMoveProjection> insert(@Valid @RequestBody InsertMoveRequest request) {
        if (request.getType().equals(MoveType.SALE)) {
            return movesService.insertSaleMove(stockMovesAdapter.transformToSaleMoves(request))
                    .stream()
                    .map(stockMovesAdapter::transform)
                    .collect(Collectors.toList());
        }
        return movesService.insertDetachedMove(stockMovesAdapter.transform(request)).stream()
                .map(stockMovesAdapter::transform)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/update")
    @RoleMovesWrite
    @ResponseStatus(HttpStatus.OK)
    public List<StockMoveProjection> update(@PathVariable Long id, @Valid @RequestBody UpdateMoveRequest request) {
        return movesService.update(id, request)
                .stream()
                .map(stockMovesAdapter::transform)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/approve")
    @RoleMovesWrite
    public StockMoveProjection approveMove(@PathVariable Long id) {
        return movesService.approve(id)
                .map(stockMovesAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada"));
    }

    @PostMapping("/batch/approve")
    @RoleMovesWrite
    public List<StockMoveProjection> approveMovesInBatch(@RequestBody @Valid EvaluateMovesInBatchRequest request) {
        return movesService.approve(request.getMoves())
                .stream()
                .map(stockMovesAdapter::transform)
                .collect(Collectors.toList());
    }

    @PostMapping("/batch/reject")
    @RoleMovesWrite
    public List<StockMoveProjection> rejectMovesInBatch(@RequestBody @Valid EvaluateMovesInBatchRequest request) {
        return movesService.reject(request.getMoves())
                .stream()
                .map(stockMovesAdapter::transform)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/reject")
    @RoleMovesWrite
    public StockMoveProjection rejectMove(@PathVariable Long id) {
        return movesService.reject(id)
                .map(stockMovesAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada"));
    }

    @DeleteMapping("/{id}")
    @RoleMovesWrite
    public StockMoveProjection cancelMove(@PathVariable Long id) {
        return movesService.cancel(id)
                .map(stockMovesAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada"));
    }
}
