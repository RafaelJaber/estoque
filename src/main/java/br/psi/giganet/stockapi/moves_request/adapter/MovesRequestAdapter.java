package br.psi.giganet.stockapi.moves_request.adapter;

import br.psi.giganet.stockapi.employees.adapter.EmployeeAdapter;
import br.psi.giganet.stockapi.moves_request.controller.request.BasicInsertRequestedMoveRequest;
import br.psi.giganet.stockapi.moves_request.controller.request.InsertRequestedMoveRequest;
import br.psi.giganet.stockapi.moves_request.controller.response.RequestedMoveProjection;
import br.psi.giganet.stockapi.moves_request.controller.response.RequestedMoveResponse;
import br.psi.giganet.stockapi.moves_request.model.RequestedMove;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.schedules.controller.response.ScheduledMoveProjection;
import br.psi.giganet.stockapi.schedules.model.ScheduledMove;
import br.psi.giganet.stockapi.stock.adapter.StockAdapter;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock_moves.adapter.StockMovesAdapter;
import br.psi.giganet.stockapi.stock_moves.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MovesRequestAdapter {

    @Autowired
    private StockFactory stockFactory;

    @Autowired
    private ProductAdapter productAdapter;

    @Autowired
    private StockAdapter stockAdapter;

    @Autowired
    private StockMovesAdapter stockMovesAdapter;

    @Autowired
    private EmployeeAdapter employeeAdapter;

    public List<RequestedMove> transform(InsertRequestedMoveRequest request) {
        Stock from = request.getFrom() != null ? stockFactory.create(request.getFrom()) : null;
        Stock to = request.getTo() != null ? stockFactory.create(request.getTo()) : null;

        return request.getItems().stream()
                .map(item -> {
                    Product product = productAdapter.create(item.getProduct());

                    RequestedMove move = new RequestedMove();
                    move.setFrom(stockFactory.createItem(from, product));
                    move.setTo(stockFactory.createItem(to, product));
                    move.setNote(request.getNote());
                    move.setOrigin(MoveOrigin.LOGGED_USER);
                    move.setProduct(product);
                    move.setQuantity(item.getQuantity());
                    move.setStatus(MoveStatus.REQUESTED);

                    return move;
                })
                .collect(Collectors.toList());
    }

    public List<RequestedMove> transform(BasicInsertRequestedMoveRequest request, String userId) {
        Stock from = request.getFrom() != null ? stockFactory.create(request.getFrom()) : null;
        Stock to = stockFactory.create(userId);

        return request.getItems().stream()
                .map(item -> {
                    Product product = productAdapter.create(item.getProduct());

                    RequestedMove move = new RequestedMove();
                    move.setFrom(stockFactory.createItem(from, product));
                    move.setTo(stockFactory.createItem(to, product));
                    move.setNote(request.getNote());
                    move.setOrigin(MoveOrigin.TECHNICIAN_APP);
                    move.setProduct(product);
                    move.setQuantity(item.getQuantity());
                    move.setStatus(MoveStatus.REQUESTED);

                    return move;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public RequestedMoveProjection transform(RequestedMove move) {
        RequestedMoveProjection response = new RequestedMoveProjection();
        response.setId(move.getId());
        response.setDate(move.getCreatedDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response.setStatus(move.getStatus());

        response.setQuantity(move.getQuantity());
        response.setProduct(productAdapter.transformWithoutUnit(move.getProduct()));
        response.setFrom(move.getFrom() != null ? stockAdapter.transform(move.getFrom().getStock()) : null);
        response.setTo(move.getTo() != null ? stockAdapter.transform(move.getTo().getStock()) : null);
        response.setDescription(move.getDescription());

        return response;
    }

    @Transactional
    public RequestedMoveResponse transformToResponse(RequestedMove move) {
        RequestedMoveResponse response = new RequestedMoveResponse();
        response.setId(move.getId());
        response.setDate(move.getCreatedDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response.setStatus(move.getStatus());

        response.setQuantity(move.getQuantity());
        response.setProduct(productAdapter.transformWithoutUnit(move.getProduct()));
        response.setFrom(move.getFrom() != null ? stockAdapter.transform(move.getFrom().getStock()) : null);
        response.setTo(move.getTo() != null ? stockAdapter.transform(move.getTo().getStock()) : null);
        response.setRequester(employeeAdapter.transform(move.getRequester()));
        response.setDescription(move.getDescription());
        response.setNote(move.getNote());

        if(move.getMove() != null){
            response.setMove(stockMovesAdapter.transform(move.getMove()));
        }

        return response;
    }


}
