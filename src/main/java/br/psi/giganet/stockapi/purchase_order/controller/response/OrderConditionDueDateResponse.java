package br.psi.giganet.stockapi.purchase_order.controller.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderConditionDueDateResponse {

    private Long id;
    private LocalDate dueDate;

}
