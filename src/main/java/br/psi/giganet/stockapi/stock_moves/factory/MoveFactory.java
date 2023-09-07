package br.psi.giganet.stockapi.stock_moves.factory;

import br.psi.giganet.stockapi.entries.model.EntryItem;
import br.psi.giganet.stockapi.moves_request.model.RequestedMove;
import br.psi.giganet.stockapi.schedules.model.ScheduledMoveItem;
import br.psi.giganet.stockapi.stock.model.StockItem;
import br.psi.giganet.stockapi.stock_moves.model.*;
import org.springframework.stereotype.Component;

@Component
public class MoveFactory {

    public DetachedStockMove createDetachedStockMove(
            StockItem from,
            StockItem to,
            Double quantity,
            MoveType type) {

        DetachedStockMove move = new DetachedStockMove();
        move.setFrom(from);
        move.setTo(to);
        move.setProduct(from != null ?
                from.getProduct() : to != null ?
                to.getProduct() : null);
        move.setQuantity(quantity);
        move.setType(type);
        move.setStatus(MoveStatus.REQUESTED);
        move.setOrigin(MoveOrigin.LOGGED_USER);
        move.setReason(MoveReason.DETACHED);
        return move;
    }

    public DetachedStockMove createDetachedStockMove(
            StockItem from,
            StockItem to,
            Double quantity,
            MoveType type,
            String note) {

        DetachedStockMove move = new DetachedStockMove();
        move.setFrom(from);
        move.setTo(to);
        move.setProduct(from != null ?
                from.getProduct() : to != null ?
                to.getProduct() : null);
        move.setQuantity(quantity);
        move.setType(type);
        move.setStatus(MoveStatus.REQUESTED);
        move.setOrigin(MoveOrigin.LOGGED_USER);
        move.setReason(MoveReason.DETACHED);
        move.setNote(note);
        return move;
    }

    public SaleStockMove createSaleStockMove(
            StockItem from,
            Double quantity,
            MoveType type,
            String note) {

        SaleStockMove move = new SaleStockMove();
        move.setFrom(from);
        move.setProduct(from != null ?
                from.getProduct() : null);
        move.setQuantity(quantity);
        move.setType(type);
        move.setStatus(MoveStatus.REALIZED);
        move.setOrigin(MoveOrigin.LOGGED_USER);
        move.setReason(MoveReason.DETACHED);
        move.setNote(note);
        return move;
    }

    public TechnicianStockMove createTechnicianStockMove(
            StockItem from,
            StockItem to,
            Double quantity,
            MoveType type,
            MoveOrigin origin) {

        TechnicianStockMove move = new TechnicianStockMove();
        move.setFrom(from);
        move.setTo(to);
        move.setProduct(from != null ? from.getProduct() : to.getProduct());
        move.setQuantity(quantity);
        move.setType(type);
        move.setStatus(MoveStatus.REQUESTED);
        move.setOrigin(origin);
        move.setReason(MoveReason.DETACHED);
        return move;
    }

    public TechnicianStockMove createTechnicianStockMove(
            StockItem from,
            StockItem to,
            Double quantity,
            MoveReason reason,
            MoveType type,
            MoveOrigin origin,
            String orderId,
            String activationId,
            String customerName,
            ExternalOrderType orderType,
            String note) {

        TechnicianStockMove move = new TechnicianStockMove();
        move.setFrom(from);
        move.setTo(to);
        move.setProduct(from != null ? from.getProduct() : to.getProduct());
        move.setQuantity(quantity);
        move.setType(type);
        move.setStatus(MoveStatus.REQUESTED);
        move.setOrigin(origin);
        move.setReason(reason);
        move.setNote(note);
        move.setOrderId(orderId);
        move.setActivationId(activationId);
        move.setOrderType(orderType);
        move.setCustomerName(customerName);
        return move;
    }

    public EntryItemStockMove createEntryItemStockMove(
            EntryItem entryItem,
            StockItem stockItem) {

        EntryItemStockMove move = new EntryItemStockMove();
        move.setTo(stockItem);
        move.setProduct(entryItem.getProduct());
        move.setRequester(entryItem.getEntry().getResponsible());
        move.setResponsible(entryItem.getEntry().getResponsible());
        move.setQuantity(entryItem.getQuantity());
        move.setType(MoveType.ENTRY_ITEM);
        move.setStatus(MoveStatus.APPROVED);
        move.setOrigin(MoveOrigin.LOGGED_USER);
        move.setReason(MoveReason.ENTRY_FROM_PURCHASE_ORDER);
        return move;
    }


    public ScheduledStockMove createScheduleStockMove(ScheduledMoveItem scheduledMoveItem) {
        ScheduledStockMove move = new ScheduledStockMove();
        move.setFrom(scheduledMoveItem.getFrom());
        move.setTo(scheduledMoveItem.getTo());
        move.setProduct(scheduledMoveItem.getProduct());
        move.setDescription(scheduledMoveItem.getDescription());
        move.setQuantity(scheduledMoveItem.getQuantity());
        move.setType(scheduledMoveItem.getType());
        move.setReason(scheduledMoveItem.getReason());
        move.setOrigin(MoveOrigin.SCHEDULE);
        move.setStatus(MoveStatus.REQUESTED);
        move.setRequester(scheduledMoveItem.getScheduled().getResponsible());
        move.setScheduledMoveItem(scheduledMoveItem);
        move.setBranchOffice(scheduledMoveItem.getScheduled().getBranchOffice());

        return move;
    }

    public DetachedStockMove createRequestedStockMove(RequestedMove requestedMove) {
        DetachedStockMove move = new DetachedStockMove();
        move.setFrom(requestedMove.getFrom());
        move.setTo(requestedMove.getTo());
        move.setProduct(requestedMove.getProduct());
        move.setDescription(requestedMove.getDescription());
        move.setQuantity(requestedMove.getQuantity());
        move.setOrigin(MoveOrigin.LOGGED_USER);
        move.setType(MoveType.BETWEEN_STOCKS);
        move.setReason(MoveReason.REQUEST);
        move.setStatus(MoveStatus.REQUESTED);
        move.setResponsible(requestedMove.getRequester());
        move.setBranchOffice(requestedMove.getBranchOffice());

        return move;
    }


}
