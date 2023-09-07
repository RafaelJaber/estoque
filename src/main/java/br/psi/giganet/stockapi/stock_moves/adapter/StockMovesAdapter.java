package br.psi.giganet.stockapi.stock_moves.adapter;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.adapter.EmployeeAdapter;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.stock.adapter.StockAdapter;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.stock_moves.controller.request.BasicInsertMoveFromServiceOrderRequest;
import br.psi.giganet.stockapi.stock_moves.controller.request.BasicInsertMoveRequest;
import br.psi.giganet.stockapi.stock_moves.controller.request.InsertMoveRequest;
import br.psi.giganet.stockapi.stock_moves.controller.response.*;
import br.psi.giganet.stockapi.stock_moves.controller.response.enums.ServiceOrderMoveType;
import br.psi.giganet.stockapi.stock_moves.factory.MoveFactory;
import br.psi.giganet.stockapi.stock_moves.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class StockMovesAdapter {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductAdapter productAdapter;

    @Autowired
    private EmployeeAdapter employeeAdapter;

    @Autowired
    private StockAdapter stockAdapter;

    @Autowired
    private StockFactory stockFactory;

    @Autowired
    private MoveFactory moveFactory;

    public List<DetachedStockMove> transform(InsertMoveRequest request) {
        Stock from = request.getFrom() != null ? stockFactory.create(request.getFrom()) : null;
        Stock to = request.getTo() != null ? stockFactory.create(request.getTo()) : null;

        return request.getItems().stream()
                .map(item -> {
                    Product product = productAdapter.create(item.getProduct());
                    return moveFactory.createDetachedStockMove(
                            from != null ? stockFactory.createItem(from, product) : null,
                            to != null ? stockFactory.createItem(to, product) : null,
                            item.getQuantity(),
                            request.getType(),
                            request.getNote());
                })
                .collect(Collectors.toList());
    }

    public List<DetachedStockMove> transform(InsertMoveRequest request, String userId) {
        Stock from = stockRepository.findByUser(userId)
                .orElseThrow(() -> new IllegalArgumentException("O estoque de origem não pode ser nulo!"));

        request.setFrom(from.getId());

        return transform(request);
    }

    public List<SaleStockMove> transformToSaleMoves(InsertMoveRequest request, String userId) {
        Stock from = stockRepository.findByUser(userId)
                .orElseThrow(() -> new IllegalArgumentException("O estoque de origem não pode ser nulo!"));

        return request.getItems().stream()
                .map(item -> {
                    Product product = productAdapter.create(item.getProduct());
                    return moveFactory.createSaleStockMove(
                            from != null ? stockFactory.createItem(from, product) : null,
                            item.getQuantity(),
                            request.getType(),
                            request.getNote());
                })
                .collect(Collectors.toList());
    }

    public List<SaleStockMove> transformToSaleMoves(InsertMoveRequest request) {
        Stock from = stockRepository.findById(request.getFrom())
                .orElseThrow(() -> new IllegalArgumentException("O estoque de origem não pode ser nulo!"));

        return request.getItems().stream()
                .map(item -> {
                    Product product = productAdapter.create(item.getProduct());
                    return moveFactory.createSaleStockMove(
                            from != null ? stockFactory.createItem(from, product) : null,
                            item.getQuantity(),
                            request.getType(),
                            request.getNote());
                })
                .collect(Collectors.toList());
    }

    public List<TechnicianStockMove> transform(BasicInsertMoveRequest request, String userId) {
        TechnicianStock technicianStock = stockFactory.create(userId);
        Stock to = request.getTo() != null ? stockFactory.create(request.getTo()) : null;

        return request.getItems().stream()
                .map(item -> {
                    Product product = productAdapter.create(item.getProduct());

                    if (request.getType().equals(MoveType.ENTRY_ITEM)) {
                        return moveFactory.createTechnicianStockMove(
                                null,
                                stockFactory.createItem(technicianStock, product),
                                item.getQuantity(),
                                request.getType(),
                                MoveOrigin.TECHNICIAN_APP);

                    } else {
                        return moveFactory.createTechnicianStockMove(
                                stockFactory.createItem(technicianStock, product),
                                to != null ? stockFactory.createItem(to, product) : null,
                                item.getQuantity(),
                                request.getType(),
                                MoveOrigin.TECHNICIAN_APP);
                    }
                })
                .collect(Collectors.toList());
    }

    public List<TechnicianStockMove> transform(BasicInsertMoveFromServiceOrderRequest request, String userId) {
        TechnicianStock technicianStock = stockFactory.create(userId);
        Stock customersStock = stockFactory.create(null, StockType.CUSTOMER);
        final ExternalOrderType orderType;
        switch (request.getOrderType()) {
            case "I":
                orderType = ExternalOrderType.INSTALLATION;
                break;
            case "R":
                orderType = ExternalOrderType.REPAIR;
                break;
            case "CR":
                orderType = ExternalOrderType.CANCELLATION;
                break;
            case "M":
                orderType = ExternalOrderType.ADDRESS_CHANGE;
                break;
            case "SP":
                orderType = ExternalOrderType.SECOND_POINT;
                break;
            default:
                orderType = null;
                break;
        }


        if ((request.getEntryItems() == null || request.getEntryItems().isEmpty()) &&
                (request.getOutgoingItems() == null || request.getOutgoingItems().isEmpty())) {
            throw new IllegalArgumentException("É necessário informar pelo menos 1 item a ser movimentado");
        }

        if (request.getEntryItems() == null) {
            request.setEntryItems(new ArrayList<>());
        }
        if (request.getOutgoingItems() == null) {
            request.setOutgoingItems(new ArrayList<>());
        }

        return Stream.concat(
                request.getOutgoingItems().stream()
                        .map(item -> {
                            Product product = productAdapter.create(item.getProduct());
                            return moveFactory.createTechnicianStockMove(
                                    stockFactory.createItem(technicianStock, product),
                                    stockFactory.createItem(customersStock, product),
                                    item.getQuantity(),
                                    MoveReason.SERVICE_ORDER,
                                    MoveType.BETWEEN_STOCKS,
                                    MoveOrigin.TECHNICIAN_APP,
                                    request.getOrderId(),
                                    request.getActivationId(),
                                    request.getCustomerName(),
                                    orderType,
                                    "Ordem de serviço: " + request.getOrderId());
                        }),
                request.getEntryItems().stream()
                        .map(item -> {
                            Product product = productAdapter.create(item.getProduct());
                            return moveFactory.createTechnicianStockMove(
                                    stockFactory.createItem(customersStock, product),
                                    stockFactory.createItem(technicianStock, product),
                                    item.getQuantity(),
                                    MoveReason.SERVICE_ORDER,
                                    MoveType.BETWEEN_STOCKS,
                                    MoveOrigin.TECHNICIAN_APP,
                                    request.getOrderId(),
                                    request.getActivationId(),
                                    request.getCustomerName(),
                                    orderType,
                                    "Ordem de serviço: " + request.getOrderId());
                        }))
                .collect(Collectors.toList());
    }

    @Transactional
    public StockMoveProjection transform(StockMove move) {
        StockMoveProjection projection = new StockMoveProjection();
        projection.setId(move.getId());
        projection.setStatus(move.getStatus());
        projection.setDate(move.getCreatedDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        projection.setDescription(move.getDescription());
        projection.setOrigin(move.getOrigin());
        projection.setType(move.getType());
        projection.setQuantity(move.getQuantity());

        return projection;
    }

    @Transactional
    public ServiceOrderMoveResponse transformToServiceOrderMoveResponse(TechnicianStockMove move) {
        ServiceOrderMoveResponse projection = new ServiceOrderMoveResponse();
        projection.setId(move.getId());
        projection.setStatus(move.getStatus());
        projection.setDate(move.getCreatedDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        projection.setLastModifiedDate(move.getLastModifiedDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Stock stockFrom = move.getFrom().getStock();
        projection.setFrom(stockFrom.isCustomer() && move.getCustomerName() != null ?
                move.getCustomerName() : stockFrom.getName());

        Stock stockTo = move.getTo().getStock();
        projection.setTo(stockTo.isCustomer() && move.getCustomerName() != null ?
                move.getCustomerName() : stockTo.getName());

        projection.setType(stockFrom.isCustomer() ?
                ServiceOrderMoveType.FROM_CUSTOMER_TO_TECHNICIAN :
                ServiceOrderMoveType.FROM_TECHNICIAN_TO_CUSTOMER);

        projection.setProduct(productAdapter.transformWithoutUnit(move.getProduct()));
        projection.setQuantity(move.getQuantity());
        projection.setDescription(move.getDescription());

        return projection;
    }

    @Transactional
    public AdvancedStockMoveProjection transformToAdvancedStockMoveProjection(StockMove move) {
        AdvancedStockMoveProjection response = new AdvancedStockMoveProjection();
        response.setId(move.getId());
        response.setStatus(move.getStatus());
        response.setDate(move.getCreatedDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response.setDescription(move.getDescription());

        if (move.getReason().equals(MoveReason.SERVICE_ORDER)) {
            TechnicianStockMove technicianStockMove = (TechnicianStockMove) move;
            Stock stockFrom = move.getFrom().getStock();
            response.setFrom(stockFrom.isCustomer() && technicianStockMove.getCustomerName() != null ?
                    technicianStockMove.getCustomerName() : stockFrom.getName());

            Stock stockTo = move.getTo().getStock();
            response.setTo(stockTo.isCustomer() && technicianStockMove.getCustomerName() != null ?
                    technicianStockMove.getCustomerName() : stockTo.getName());

        } else {
            response.setFrom(move.getFrom() != null ? move.getFrom().getStock().getName() : null);
            response.setTo(move.getTo() != null ? move.getTo().getStock().getName() : null);

        }


        response.setProduct(productAdapter.transformWithoutUnit(move.getProduct()));
        response.setQuantity(move.getQuantity());

        return response;
    }

    @Transactional
    public AdvancedStockMoveProjection transformToAdvancedStockMoveProjectionWithCustomerName(StockMove move) {
        AdvancedStockMoveProjectionWithCustomerName response = new AdvancedStockMoveProjectionWithCustomerName();
        response.setId(move.getId());
        response.setStatus(move.getStatus());
        response.setDate(move.getCreatedDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response.setDescription(move.getDescription());

        if (move.getReason().equals(MoveReason.SERVICE_ORDER)) {
            TechnicianStockMove technicianStockMove = (TechnicianStockMove) move;
            Stock stockFrom = move.getFrom().getStock();
            response.setFrom(stockFrom.isCustomer() && technicianStockMove.getCustomerName() != null ?
                    technicianStockMove.getCustomerName() : stockFrom.getName());

            Stock stockTo = move.getTo().getStock();
            response.setTo(stockTo.isCustomer() && technicianStockMove.getCustomerName() != null ?
                    technicianStockMove.getCustomerName() : stockTo.getName());

        } else {
            response.setFrom(move.getFrom() != null ? move.getFrom().getStock().getName() : null);
            response.setTo(move.getTo() != null ? move.getTo().getStock().getName() : null);

        }

        if(move instanceof TechnicianStockMove){
            response.setCustomerName(((TechnicianStockMove) move).getCustomerName());
        }

        response.setProduct(productAdapter.transformWithoutUnit(move.getProduct()));
        response.setQuantity(move.getQuantity());

        return response;
    }

    @Transactional
    public StockMoveProjection transformWithProductWithoutUnit(StockMove move) {
        StockMoveProjectionWithProduct projection = new StockMoveProjectionWithProduct();
        projection.setId(move.getId());
        projection.setStatus(move.getStatus());
        projection.setDate(move.getCreatedDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        projection.setDescription(move.getDescription());
        projection.setOrigin(move.getOrigin());
        projection.setType(move.getType());
        projection.setQuantity(move.getQuantity());
        projection.setProduct(productAdapter.transformWithoutUnit(move.getProduct()));

        return projection;
    }

    @Transactional
    public StockMoveResponse transformToResponse(StockMove move) {
        StockMoveResponse response = new StockMoveResponse();
        response.setId(move.getId());
        response.setDate(move.getCreatedDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response.setLastModifiedDate(move.getLastModifiedDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response.setDescription(move.getDescription());
        response.setStatus(move.getStatus());
        response.setOrigin(move.getOrigin());
        response.setType(move.getType());
        response.setQuantity(move.getQuantity());
        response.setRequester(move.getRequester() != null ? employeeAdapter.transform(move.getRequester()) : null);
        response.setResponsible(move.getResponsible() != null ? employeeAdapter.transform(move.getResponsible()) : null);
        response.setNote(move.getNote());

        response.setFrom(move.getFrom() != null ? stockAdapter.transform(move.getFrom().getStock()) : null);
        response.setTo(move.getTo() != null ? stockAdapter.transform(move.getTo().getStock()) : null);

        response.setProduct(productAdapter.transform(move.getProduct()));

        return response;
    }
}
