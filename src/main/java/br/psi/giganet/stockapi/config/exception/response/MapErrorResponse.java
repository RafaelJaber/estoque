package br.psi.giganet.stockapi.config.exception.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MapErrorResponse {

    private Map<String, String> errors;

}
