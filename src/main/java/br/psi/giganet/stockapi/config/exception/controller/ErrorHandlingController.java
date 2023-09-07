package br.psi.giganet.stockapi.config.exception.controller;

import br.psi.giganet.stockapi.common.messages.service.LogMessageService;
import br.psi.giganet.stockapi.common.utils.model.WebRequestProjection;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.config.exception.exception.UnauthorizedException;
import br.psi.giganet.stockapi.config.exception.response.SimpleErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
public class ErrorHandlingController extends ResponseEntityExceptionHandler {

    @Autowired
    private LogMessageService logService;

    private final ObjectMapper mapper = new ObjectMapper();

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) throws IOException {
        SimpleErrorResponse response = new SimpleErrorResponse(ex.getMessage());
        logService.send(mapper.writeValueAsString(response));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> handleRuntimeException(IllegalArgumentException ex) throws IOException {
        SimpleErrorResponse response = new SimpleErrorResponse(ex.getMessage());
        logService.send(mapper.writeValueAsString(response));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler({DateTimeParseException.class})
    public ResponseEntity<Object> handleDateTimeParseException(DateTimeParseException ex, WebRequest request) throws IOException {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "DateTimeParseException");
        errors.put("description", ex.getLocalizedMessage());
        errors.put("request", new WebRequestProjection(request));
        logService.send(mapper.writeValueAsString(errors));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SimpleErrorResponse("A data informada não está em um formato válido."));
    }

    @ExceptionHandler({DataAccessException.class})
    public ResponseEntity<Object> handleSqlException(DataAccessException ex, WebRequest request) throws IOException {
        String responseMessage = "Não foi possível salvar o recurso. Erro do banco de dados. ";
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Erro SQL - DataAccessException");
        errors.put("request", new WebRequestProjection(request));
        if (ex.getMostSpecificCause() instanceof SQLException) {
            SQLException sqlException = (SQLException) ex.getMostSpecificCause();
            errors.put("code", sqlException.getSQLState());
            errors.put("description", sqlException.getLocalizedMessage());
            responseMessage += "Codigo " + sqlException.getSQLState();
        } else {
            errors.put("description", ex.getLocalizedMessage());
        }
        SimpleErrorResponse response = new SimpleErrorResponse(responseMessage);
        logService.send(mapper.writeValueAsString(errors));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(
            Exception ex, WebRequest request) throws IOException {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Acesso não autorizado");
        errors.put("description", ex.getLocalizedMessage());
        errors.put("request", new WebRequestProjection(request));
        logService.send(mapper.writeValueAsString(errors));

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new SimpleErrorResponse("Você não tem permissões suficientes para realizar esta operação"));
    }

    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<Object> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) throws IOException {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        errors.put("description", ex.getLocalizedMessage());
        errors.put("request", new WebRequestProjection(request));
        logService.send(mapper.writeValueAsString(errors));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({RestClientException.class})
    public ResponseEntity<Object> handleRestClientException(RestClientException ex, WebRequest request) throws IOException {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Um erro interno ocorreu");
        errors.put("description", ex.getLocalizedMessage());
        errors.put("message", ex.getMessage());
        errors.put("request", new WebRequestProjection(request));
        logService.send(mapper.writeValueAsString(errors));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleErrorResponse("Um erro interno ocorreu"));
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) throws IOException {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Um erro interno ocorreu");
        errors.put("description", ex.getLocalizedMessage());
        errors.put("request", new WebRequestProjection(request));
        ex.printStackTrace();
        logService.send(mapper.writeValueAsString(errors));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleErrorResponse("Um erro interno ocorreu"));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", status.value());

        //Get all errors
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        body.put("errors", errors);

        return new ResponseEntity<>(body, headers, status);

    }
}
