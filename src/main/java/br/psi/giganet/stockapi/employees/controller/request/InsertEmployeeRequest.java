package br.psi.giganet.stockapi.employees.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InsertEmployeeRequest {

    @NotEmpty(message = "Email não pode ser nulo")
    @Email(message = "Email inválido")
    private String email;

    @Size(min = 6, message = "Senha deve conter no mínimo 6 caracteres")
    @NotNull(message = "Senha não pode ser nula")
    private String password;

    @NotEmpty(message = "Nome não pode ser nulo")
    private String name;

}
