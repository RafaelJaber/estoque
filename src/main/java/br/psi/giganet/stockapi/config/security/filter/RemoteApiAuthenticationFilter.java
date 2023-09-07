package br.psi.giganet.stockapi.config.security.filter;

import br.psi.giganet.stockapi.common.messages.service.LogMessageService;
import br.psi.giganet.stockapi.common.utils.model.WebRequestProjection;
import br.psi.giganet.stockapi.config.contenxt.BranchOfficeContext;
import br.psi.giganet.stockapi.config.exception.response.SimpleErrorResponse;
import br.psi.giganet.stockapi.config.project_property.ApplicationProperties;
import br.psi.giganet.stockapi.config.security.adapter.SystemUserAdapter;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.stock.service.StockService;
import br.psi.giganet.stockapi.technician.service.TechnicianService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class RemoteApiAuthenticationFilter implements Filter {

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private LogMessageService logService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private TechnicianService technicianService;

    @Autowired
    private StockService stockService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        final String securityPath = "/basic/";
        if (!request.getRequestURI().contains(securityPath)) {
            chain.doFilter(req, resp);
            return;
        }

        List<ApplicationProperties.WebHook> apps = Arrays.asList(properties.getWebhooks().getPurchaseApi(), properties.getWebhooks().getSmartnetApi());

        String authorization = request.getHeader("Authorization");
        if (authorization != null && !authorization.isBlank()) {
            String token = authorization.replaceAll("Basic ", "");
            String[] data = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8).split(":");

            if (data.length != 2) {
                unauthorizedHandler(request, response);

            } else if (apps.stream().anyMatch(app -> app.getName().equals(data[0]) && app.checkCredentials(data[1]))) {

                String userId = request.getHeader("User-Id");
                Optional<Employee> employee = employeeService.findByExternalEmployeeByUserId(userId);
                if (employee.isPresent()) {
                    UserDetails details = SystemUserAdapter.create(employee.get());
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                            details,
                            details.getPassword(),
                            details.getAuthorities()));

                    stockService.findByEmployee(employee.get())
                            .ifPresent(stock -> BranchOfficeContext.setCurrentBranchOffice(stock.getBranchOffice()));

                    chain.doFilter(req, resp);
                    return;
                }
            }
        }
        unauthorizedHandler(request, response);
    }

    private void unauthorizedHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), new SimpleErrorResponse("Acesso não autorizado"));

        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Acesso não autorizado");
        errors.put("description", "Usuário/Senha incorretos para autenticação básica");
        errors.put("request", new WebRequestProjection(request));
        logService.send(mapper.writeValueAsString(errors));
    }

}