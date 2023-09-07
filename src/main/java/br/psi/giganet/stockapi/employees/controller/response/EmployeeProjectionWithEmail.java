package br.psi.giganet.stockapi.employees.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProjectionWithEmail extends EmployeeProjection {

    private String email;
}
