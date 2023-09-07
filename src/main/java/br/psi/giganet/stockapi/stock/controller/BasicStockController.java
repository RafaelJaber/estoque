package br.psi.giganet.stockapi.stock.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.stock.adapter.StockAdapter;
import br.psi.giganet.stockapi.stock.controller.response.StockItemProjection;
import br.psi.giganet.stockapi.stock.controller.response.StockProjection;
import br.psi.giganet.stockapi.stock.controller.security.RoleStocksRead;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/basic/stocks")
public class BasicStockController {
    public static final String TAG = BasicStockController.class.getCanonicalName();
    @Autowired
    private StockService stockService;

    @Autowired
    private StockAdapter stockAdapter;

    @Autowired
    private StockFactory stockFactory;

    @GetMapping("/technicians/{userId}/items")
    @RoleStocksRead
    public Page<StockItemProjection> findAllStockItemsFromTechnicianStockByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String code,
            @RequestParam(defaultValue = "false") Boolean filterEmpty,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return filterEmpty ?
                stockService.findByStockAndNameContainingAndCodeFilteringEmpties(stockFactory.create(userId), name, code, page, pageSize)
                        .map(stockAdapter::transform) :
                stockService.findByStockAndNameContainingAndCode(stockFactory.create(userId), name, code, page, pageSize)
                        .map(stockAdapter::transform);
    }

    @GetMapping("/my")
    public StockProjection findByEmployee() {
        return stockService.findByCurrentLoggedEmployeeAndBranchOffice()
                .map(stockAdapter::transformWithStockItems)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque não encontrado"));
    }

    @GetMapping
    @RoleStocksRead
    public Page<StockProjection> findAll(
            @RequestParam(required = false) StockType type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return type != null ?
                stockService.findByStockType(type, page, pageSize).map(stockAdapter::transform) :
                stockService.findAll(page, pageSize).map(stockAdapter::transform);
    }

    @GetMapping("/technicians/available")
    @RoleStocksRead
    public Page<StockProjection> findAllAvailableToMoveByTechnician(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return  stockService.findAllAvailableToMoveByTechnician(page, pageSize).map(stockAdapter::transform);
    }

}
