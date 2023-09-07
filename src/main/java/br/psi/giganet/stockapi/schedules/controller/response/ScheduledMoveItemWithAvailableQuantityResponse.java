package br.psi.giganet.stockapi.schedules.controller.response;

import lombok.Data;

@Data
public class ScheduledMoveItemWithAvailableQuantityResponse extends ScheduledMoveItemResponse {

    private Double availableQuantity;

}
