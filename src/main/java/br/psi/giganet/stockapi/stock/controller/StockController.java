package br.psi.giganet.stockapi.stock.controller;

import br.psi.giganet.stockapi.common.reports.model.ReportFormat;
import br.psi.giganet.stockapi.common.utils.controller.DownloadFileControllerUtil;
import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.stock.adapter.StockAdapter;
import br.psi.giganet.stockapi.stock.controller.request.UpdateStockItemParametersRequest;
import br.psi.giganet.stockapi.stock.controller.response.GeneralStockItemResponse;
import br.psi.giganet.stockapi.stock.controller.response.StockItemProjection;
import br.psi.giganet.stockapi.stock.controller.response.StockProjection;
import br.psi.giganet.stockapi.stock.controller.security.RoleStocksRead;
import br.psi.giganet.stockapi.stock.controller.security.RoleStocksWrite;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/stocks")
public class StockController {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockAdapter stockAdapter;

    @Autowired
    private StockFactory stockFactory;

    @GetMapping("/{stock}/items")
    @RoleStocksRead
    public Page<StockItemProjection> findAllStockItemsByStock(
            @PathVariable Long stock,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String code,
            @RequestParam(defaultValue = "false") Boolean filterEmpty,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return filterEmpty ?
                stockService.findByStockAndNameContainingAndCodeFilteringEmpties(stockFactory.create(stock), name, code, page, pageSize)
                        .map(stockAdapter::transform) :
                stockService.findByStockAndNameContainingAndCode(stockFactory.create(stock), name, code, page, pageSize)
                        .map(stockAdapter::transform);
    }

    @GetMapping("/{stock}/items/available")
    @RoleStocksRead
    public Page<StockItemProjection> findAllAvailableStockItemsByStock(
            @PathVariable Long stock,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String code,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return stockService.findByStockAndNameContainingAndCodeFilteringAvailable(
                stockFactory.create(stock), name, code, page, pageSize)
                .map(stockAdapter::transform);
    }

    @GetMapping(value = "/{stock}/items", params = {"withCurrentLevel"})
    @RoleStocksRead
    public Page<StockItemProjection> findAllStockItemsByStockWithCurrentLevel(
            @PathVariable Long stock,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String code,
            @RequestParam(defaultValue = "false") Boolean filterEmpty,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {

        return filterEmpty ?
                stockService.findByStockAndNameContainingAndCodeFilteringEmpties(stockFactory.create(stock), name, code, page, pageSize)
                        .map(stockAdapter::transformWithCurrentStockLevel) :
                stockService.findByStockAndNameContainingAndCode(stockFactory.create(stock), name, code, page, pageSize)
                        .map(stockAdapter::transformWithCurrentStockLevel);
    }

    @GetMapping("/available")
    @RoleStocksRead
    public Page<StockProjection> findAllAvailableToMove(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return stockService.findAllAvailableToMove(page, pageSize)
                .map(stockAdapter::transform);
    }

    @GetMapping
    @RoleStocksRead
    public Page<StockProjection> findAll(
            @RequestParam(required = false) StockType type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return type != null ?
                stockService.findByStockTypeAndCurrentBranchOffice(type, page, pageSize).map(stockAdapter::transform) :
                stockService.findAllByCurrentBranchOffice(page, pageSize).map(stockAdapter::transform);
    }

    @GetMapping("/technicians")
    @RoleStocksRead
    public Page<StockProjection> findAllTechnicianStocks(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return stockService.findByStockType(StockType.TECHNICIAN, page, pageSize)
                .map(stock -> stockAdapter.transform((TechnicianStock) stock));
    }

    @GetMapping("/general")
    @RoleStocksRead
    public Page<GeneralStockItemResponse> findByProductGroupByProduct(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return stockService.findByProductGroupByProductAndCurrentBranchOffice(name, page, pageSize)
                .map(stockAdapter::transform);
    }

    @GetMapping("/{id}")
    @RoleStocksRead
    public StockProjection findByStockId(@PathVariable Long id) {
        return stockService.findById(id)
                .map(stockAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque não encontrado"));
    }

    @GetMapping("/my")
    @RoleStocksRead
    public StockProjection findByEmployee() {
        return stockService.findByCurrentLoggedEmployeeAndBranchOffice()
                .map(stockAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque não encontrado"));
    }

    @GetMapping("/{stock}/items/codes/{code}")
    @RoleStocksRead
    public StockItemProjection findByStockAndCode(
            @PathVariable Long stock,
            @PathVariable String code) {
        return stockService.findByStockAndCode(stockFactory.create(stock), code)
                .map(stockAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque não encontrado"));
    }

    @GetMapping("/{stock}/items/{id}")
    @RoleStocksRead
    public StockItemProjection findByStockItemId(
            @PathVariable Long stock,
            @PathVariable Long id) {
        return stockService.findByStockItemId(id, stockFactory.create(stock))
                .map(stockAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque não encontrado"));
    }

    @GetMapping(value = "/{stock}/items/{id}", params = {"withLevels"})
    @RoleStocksRead
    public StockItemProjection findByStockItemIdWithLevels(
            @PathVariable Long stock,
            @PathVariable Long id) {
        return stockService.findByStockItemId(id, stockFactory.create(stock))
                .map(stockAdapter::transformWithLevels)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque não encontrado"));
    }

    @PutMapping("/{stock}/items/{id}")
    @RoleStocksWrite
    public StockItemProjection updateStockItemParameters(
            @PathVariable Long stock,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockItemParametersRequest request) {
        return stockService.updateParameters(id, stockFactory.create(stock), stockAdapter.transform(request))
                .map(stockAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque não encontrado"));
    }

    @GetMapping("/reports/current-situation/{id}")
    @RoleStocksRead
    public void getStockSituationReport(@PathVariable Long id, @RequestParam ReportFormat format, HttpServletResponse response) throws Exception {
        DownloadFileControllerUtil.appendFile(
                stockService.getStockSituationReport(stockFactory.create(id), format),
                response);
    }

}
