package br.psi.giganet.stockapi.stock.adapter;

import br.psi.giganet.stockapi.common.adapter.AbstractAdapter;
import br.psi.giganet.stockapi.patrimonies.adapter.PatrimonyAdapter;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import br.psi.giganet.stockapi.stock.controller.request.UpdateStockItemParametersRequest;
import br.psi.giganet.stockapi.stock.controller.response.*;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.StockItem;
import br.psi.giganet.stockapi.stock.model.StockItemQuantityLevel;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.repository.GeneralStockItem;
import br.psi.giganet.stockapi.stock_moves.adapter.StockMovesAdapter;
import br.psi.giganet.stockapi.technician.adapter.TechnicianAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class StockAdapter extends AbstractAdapter<Stock> {

    @Autowired
    private ProductAdapter productAdapter;
    @Autowired
    private StockMovesAdapter movesAdapter;

    @Autowired
    private StockFactory stockFactory;
    @Autowired
    private PatrimonyAdapter patrimonyAdapter;

    @Autowired
    private TechnicianAdapter technicianAdapter;

    public StockItem transform(UpdateStockItemParametersRequest request) {
        StockItem item = new StockItem();
        item.setStock(stockFactory.create(request.getStock()));
        item.setQuantity(request.getQuantity());
        item.setLastPricePerUnit(request.getPricePerUnit());
        item.setMaxQuantity(request.getMaxQuantity());
        item.setMinQuantity(request.getMinQuantity());
        item.setBlockedQuantity(request.getBlockedQuantity());
        item.setId(request.getId());

        if (request.getLevels() != null) {
            item.setLevels(
                    request.getLevels()
                            .stream()
                            .filter(level -> level.getFrom() != null || level.getTo() != null)
                            .map(levelRequest -> {
                                StockItemQuantityLevel level = new StockItemQuantityLevel();
                                level.setId(levelRequest.getId());
                                level.setStockItem(item);
                                level.setFrom(levelRequest.getFrom());
                                level.setTo(levelRequest.getTo());
                                level.setLevel(levelRequest.getLevel());
                                return level;
                            })
                            .collect(Collectors.toList())
            );
        } else {
            item.setLevels(new ArrayList<>());
        }

        return item;
    }

    @Transactional
    public StockProjection transform(Stock stock) {
        StockProjection response = new StockProjection();
        response.setId(stock.getId());
        response.setType(stock.getType());
        response.setName(stock.getName());

        return response;
    }

    @Transactional
    public StockProjection transform(TechnicianStock stock) {
        TechnicianStockProjection response = new TechnicianStockProjection();
        response.setId(stock.getId());
        response.setType(stock.getType());
        response.setName(stock.getName());
        response.setTechnician(technicianAdapter.transformToProjection(stock.getTechnician()));

        return response;
    }

    @Transactional
    public StockItemProjection transform(StockItem item) {
        StockItemProjection response = new StockItemProjection();
        response.setId(item.getId());
        response.setMaxQuantity(item.getMaxQuantity());
        response.setMinQuantity(item.getMinQuantity());
        response.setQuantity(item.getQuantity());
        response.setAvailableQuantity(item.getAvailableQuantity());
        response.setBlockedQuantity(item.getBlockedQuantity());
        response.setLastPricePerUnit(item.getLastPricePerUnit());
        response.setProduct(productAdapter.transform(item.getProduct()));
        return response;
    }

    @Transactional
    public StockItemProjection transformWithCurrentStockLevel(StockItem item) {
        StockItemProjectionWithCurrentLevel response = new StockItemProjectionWithCurrentLevel();
        response.setId(item.getId());
        response.setMaxQuantity(item.getMaxQuantity());
        response.setMinQuantity(item.getMinQuantity());
        response.setQuantity(item.getQuantity());
        response.setAvailableQuantity(item.getAvailableQuantity());
        response.setBlockedQuantity(item.getBlockedQuantity());
        response.setLastPricePerUnit(item.getLastPricePerUnit());
        response.setProduct(productAdapter.transform(item.getProduct()));
        response.setCurrentLevel(item.getCurrentLevel());

        return response;
    }

    @Transactional
    public StockItemProjection transformWithLevels(StockItem item) {
        StockItemProjectionWithLevels response = new StockItemProjectionWithLevels();
        response.setId(item.getId());
        response.setMaxQuantity(item.getMaxQuantity());
        response.setMinQuantity(item.getMinQuantity());
        response.setQuantity(item.getQuantity());
        response.setAvailableQuantity(item.getAvailableQuantity());
        response.setBlockedQuantity(item.getBlockedQuantity());
        response.setLastPricePerUnit(item.getLastPricePerUnit());
        response.setProduct(productAdapter.transform(item.getProduct()));
        response.setStock(item.getStock().getId());
        response.setLevels(item.getLevels() == null ? Collections.emptyList() :
                item.getLevels().stream()
                        .map(level -> {
                            QuantityLevelProjection levelProjection = new QuantityLevelProjection();
                            levelProjection.setId(level.getId());
                            levelProjection.setLevel(level.getLevel());
                            levelProjection.setFrom(level.getFrom());
                            levelProjection.setTo(level.getTo());

                            return levelProjection;
                        })
                        .collect(Collectors.toList()));

        return response;
    }

    @Transactional
    public GeneralStockItemResponse transform(GeneralStockItem item) {
        GeneralStockItemResponse response = new GeneralStockItemResponse();
        response.setProduct(productAdapter.transform(item.getProduct()));
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        return response;
    }

    @Transactional
    public StockItemResponse transformToResponse(StockItem item) {
        StockItemResponse response = new StockItemResponse();
        response.setId(item.getId());
        response.setMaxQuantity(item.getMaxQuantity());
        response.setMinQuantity(item.getMinQuantity());
        response.setQuantity(item.getQuantity());
        response.setAvailableQuantity(item.getAvailableQuantity());
        response.setBlockedQuantity(item.getBlockedQuantity());
        response.setProduct(productAdapter.transform(item.getProduct()));
        response.setLastPricePerUnit(item.getLastPricePerUnit());
        response.setStock(item.getStock().getId());
        response.setLastEntryMoves(
                item.getEntryMoves() == null ? null :
                        item.getEntryMoves().stream()
                                .sorted((m1, m2) -> m2.getCreatedDate().compareTo(m1.getCreatedDate()))
                                .limit(50)
                                .map(movesAdapter::transform)
                                .collect(Collectors.toList()));
        response.setLastOutgoingMoves(
                item.getOutgoingMoves() == null ? null :
                        item.getOutgoingMoves().stream()
                                .sorted((m1, m2) -> m2.getCreatedDate().compareTo(m1.getCreatedDate()))
                                .limit(50)
                                .map(movesAdapter::transform)
                                .collect(Collectors.toList()));
        return response;
    }

    public StockProjection transformWithStockItems(Stock stock) {
        StockProjection projection = this.transform(stock);
        projection.setStockItems(
                stock.getItems() == null ? null :
                        stock.getItems().stream()
                                .filter(stockItem -> stockItem.getQuantity() > 0)
                                .map(this::transform)
                                .collect(Collectors.toList()));
        return projection;
    }
}
