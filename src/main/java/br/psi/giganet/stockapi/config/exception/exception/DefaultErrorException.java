package br.psi.giganet.stockapi.config.exception.exception;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class DefaultErrorException extends Exception {

    @JsonIgnore
    private HttpStatus status;

    public DefaultErrorException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
