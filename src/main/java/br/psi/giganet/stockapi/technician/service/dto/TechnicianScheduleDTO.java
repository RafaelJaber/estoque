package br.psi.giganet.stockapi.technician.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
public class TechnicianScheduleDTO {

    private LocalDate date;
    private String subtype;
    private String address;
    private String box;
    private PlainDTO plain;

}
