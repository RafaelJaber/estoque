package br.psi.giganet.stockapi.config.security.service;

import br.psi.giganet.stockapi.config.project_property.ApplicationProperties;
import br.psi.giganet.stockapi.config.security.model.User;
import br.psi.giganet.stockapi.config.security.repository.AbstractUserRepository;
import br.psi.giganet.stockapi.employees.adapter.EmployeeAdapter;
import br.psi.giganet.stockapi.employees.model.Employee;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AbstractUserService {

    @Autowired
    private AbstractUserRepository<User> repository;

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EmployeeAdapter employeeAdapter;

    public Optional<User> handleLoginCheck(String email) {
        return repository.handleLoginCheck(email);
    }

    public Optional<User> remoteAuthenticateHandler(String user, String password) {
        final String url = this.properties.getAuthorizationApiUrl() + "/auth/sign-in";
        Map<String, String> request = new HashMap<>();
        request.put("user", user);
        request.put("password", password);

        try {
            ResponseEntity<SmartnetSignInResponse> response = this.restTemplate.postForEntity(url, request, SmartnetSignInResponse.class);
            boolean isAuthenticated = (response.getStatusCode().is2xxSuccessful() && response.getBody() != null);

            if (isAuthenticated) {
                Optional<User> savedUser = this.repository.findByEmail(user);
                if (savedUser.isEmpty()) {
                    SmartnetSignInUser signInUser = response.getBody().getBody().getUsuario();
                    Employee newUser = employeeAdapter.createDefaultUser(
                            signInUser.getId(),
                            signInUser.getNome(),
                            signInUser.getEmail(),
                            password);
                    savedUser = Optional.of(this.repository.save(newUser));
                }
                return savedUser;
            }

        } catch (HttpClientErrorException e) {
            System.out.println(e);
        }

        return Optional.empty();
    }

    @Data
    @NoArgsConstructor
    private static class SmartnetSignInResponse {
        private Integer status;
        private String message;
        private Integer length;
        private SmartnetSignInBodyResponse body;
    }

    @Data
    @NoArgsConstructor
    private static class SmartnetSignInBodyResponse {
        private String access_token;
        private String refresh_token;
        private SmartnetSignInUser usuario;
    }

    @Data
    @NoArgsConstructor
    private static class SmartnetSignInUser {
        private String id;
        private String nome;
        private String email;
    }
}
