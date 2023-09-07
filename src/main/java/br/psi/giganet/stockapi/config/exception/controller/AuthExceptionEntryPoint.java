package br.psi.giganet.stockapi.config.exception.controller;

import br.psi.giganet.stockapi.common.messages.service.LogMessageService;
import br.psi.giganet.stockapi.common.utils.model.WebRequestProjection;
import br.psi.giganet.stockapi.config.exception.response.SimpleErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class AuthExceptionEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private LogMessageService logService;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException arg2) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), new SimpleErrorResponse("Acesso não autorizado"));

        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Acesso não autorizado");
        errors.put("description", arg2.getLocalizedMessage());
        errors.put("request", new WebRequestProjection(request));
        logService.send(mapper.writeValueAsString(errors));
    }
}