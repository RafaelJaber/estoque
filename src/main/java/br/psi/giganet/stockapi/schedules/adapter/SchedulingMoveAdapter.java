package br.psi.giganet.stockapi.schedules.adapter;

import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import br.psi.giganet.stockapi.schedules.controller.request.InsertScheduledMoveRequest;
import br.psi.giganet.stockapi.schedules.controller.request.UpdateScheduledMoveRequest;
import br.psi.giganet.stockapi.schedules.controller.response.ScheduledMoveItemResponse;
import br.psi.giganet.stockapi.schedules.controller.response.ScheduledMoveItemWithAvailableQuantityResponse;
import br.psi.giganet.stockapi.schedules.controller.response.ScheduledMoveProjection;
import br.psi.giganet.stockapi.schedules.controller.response.ScheduledMoveResponse;
import br.psi.giganet.stockapi.schedules.model.ScheduledMove;
import br.psi.giganet.stockapi.schedules.model.ScheduledMoveItem;
import br.psi.giganet.stockapi.schedules.model.ScheduledStatus;
import br.psi.giganet.stockapi.stock.adapter.StockAdapter;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock_moves.adapter.StockMovesAdapter;
import br.psi.giganet.stockapi.stock_moves.model.MoveOrigin;
import br.psi.giganet.stockapi.stock_moves.model.MoveReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class SchedulingMoveAdapter {

    @Autowired
    private StockFactory stockFactory;

    @Autowired
    private ProductAdapter productAdapter;

    @Autowired
    private StockAdapter stockAdapter;

    @Autowired
    private StockMovesAdapter stockMovesAdapter;

    public ScheduledMove transform(InsertScheduledMoveRequest request) {
        ScheduledMove scheduled = new ScheduledMove();
        scheduled.setFrom(request.getFrom() != null ? stockFactory.create(request.getFrom()) : null);
        scheduled.setTo(request.getTo() != null ? stockFactory.create(request.getTo()) : null);
        scheduled.setDate(ZonedDateTime.parse(request.getDate()));
        scheduled.setExecution(request.getExecution());
        scheduled.setStatus(ScheduledStatus.SCHEDULED);
        scheduled.setOrigin(MoveOrigin.SCHEDULE);
        scheduled.setType(request.getType());
        scheduled.setNote(request.getNote());

        scheduled.setItems(
                request.getItems().stream()
                        .map(item -> {
                            ScheduledMoveItem move = new ScheduledMoveItem();

                            move.setType(request.getType());
                            move.setDescription(request.getNote());
                            move.setOrigin(MoveOrigin.SCHEDULE);
                            move.setQuantity(item.getQuantity());
                            move.setReason(MoveReason.DETACHED);
                            move.setProduct(productAdapter.create(item.getProduct()));
                            move.setFrom(scheduled.getFrom() != null ? stockFactory.createItem(scheduled.getFrom(), move.getProduct()) : null);
                            move.setTo(scheduled.getTo() != null ? stockFactory.createItem(scheduled.getTo(), move.getProduct()) : null);

                            move.setScheduled(scheduled);

                            return move;
                        })
                        .collect(Collectors.toList()));

        return scheduled;
    }

    public ScheduledMove transform(UpdateScheduledMoveRequest request) {
        ScheduledMove scheduled = new ScheduledMove();
        scheduled.setId(request.getId());
        scheduled.setFrom(request.getFrom() != null ? stockFactory.create(request.getFrom()) : null);
        scheduled.setTo(request.getTo() != null ? stockFactory.create(request.getTo()) : null);
        scheduled.setDate(ZonedDateTime.parse(request.getDate()));
        scheduled.setExecution(request.getExecution());
        scheduled.setStatus(ScheduledStatus.SCHEDULED);
        scheduled.setOrigin(MoveOrigin.SCHEDULE);
        scheduled.setType(request.getType());
        scheduled.setNote(request.getNote());

        scheduled.setItems(
                request.getItems().stream()
                        .map(item -> {
                            ScheduledMoveItem move = new ScheduledMoveItem();
                            move.setId(item.getId());

                            move.setType(request.getType());
                            move.setDescription(request.getNote());
                            move.setOrigin(MoveOrigin.SCHEDULE);
                            move.setReason(MoveReason.DETACHED);
                            move.setQuantity(item.getQuantity());
                            move.setProduct(productAdapter.create(item.getProduct()));
                            move.setFrom(scheduled.getFrom() != null ? stockFactory.createItem(scheduled.getFrom(), move.getProduct()) : null);
                            move.setTo(scheduled.getTo() != null ? stockFactory.createItem(scheduled.getTo(), move.getProduct()) : null);

                            move.setScheduled(scheduled);

                            return move;
                        })
                        .collect(Collectors.toList()));

        return scheduled;
    }

    @Transactional
    public ScheduledMoveProjection transform(ScheduledMove move) {
        ScheduledMoveProjection projection = new ScheduledMoveProjection();
        projection.setId(move.getId());
        projection.setDate(move.getDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        projection.setDescription(move.getDescription());
        projection.setOrigin(move.getOrigin());
        projection.setType(move.getType());
        projection.setExecution(move.getExecution());
        projection.setStatus(move.getStatus());

        return projection;
    }

    @Transactional
    public ScheduledMoveResponse transformToResponse(ScheduledMove move) {
        return transformToResponse(move, false);
    }

    @Transactional
    public ScheduledMoveResponse transformToResponse(ScheduledMove move, boolean withCurrentQuantity) {
        ScheduledMoveResponse response = new ScheduledMoveResponse();
        response.setId(move.getId());
        response.setDate(move.getDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response.setDescription(move.getDescription());
        response.setOrigin(move.getOrigin());
        response.setType(move.getType());
        response.setExecution(move.getExecution());
        response.setStatus(move.getStatus());

        response.setFrom(move.getFrom() != null ? stockAdapter.transform(move.getFrom()) : null);
        response.setTo(move.getTo() != null ? stockAdapter.transform(move.getTo()) : null);

        response.setItems(
                move.getItems() != null ?
                        move.getItems().stream()
                                .map(item -> transformToResponse(item, withCurrentQuantity))
                                .collect(Collectors.toList()) :
                        Collections.emptyList());

        return response;
    }

    @Transactional
    public ScheduledMoveItemResponse transformToResponse(ScheduledMoveItem item, boolean withCurrentQuantity) {
        ScheduledMoveItemResponse itemResponse;
        if (withCurrentQuantity) {
            itemResponse = new ScheduledMoveItemWithAvailableQuantityResponse();
            ((ScheduledMoveItemWithAvailableQuantityResponse) itemResponse).setAvailableQuantity(item.getFrom().getAvailableQuantity());
        } else {
            itemResponse = new ScheduledMoveItemResponse();
        }

        itemResponse.setId(item.getId());
        itemResponse.setProduct(productAdapter.transform(item.getProduct()));
        itemResponse.setMove(item.getMove() != null ? stockMovesAdapter.transform(item.getMove()) : null);
        itemResponse.setQuantity(item.getQuantity());

        return itemResponse;

    }

}
