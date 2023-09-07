package br.psi.giganet.stockapi.config.security.filter;

import br.psi.giganet.stockapi.branch_offices.factory.BranchOfficeFactory;
import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.common.messages.service.LogMessageService;
import br.psi.giganet.stockapi.common.utils.model.WebRequestProjection;
import br.psi.giganet.stockapi.config.contenxt.BranchOfficeContext;
import br.psi.giganet.stockapi.config.exception.response.SimpleErrorResponse;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class BranchOfficeFilter implements Filter {

    @Autowired
    private LogMessageService logService;

    @Autowired
    private BranchOfficeService branchOfficeService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private BranchOfficeFactory branchOfficeFactory;

    @Override
    @Transactional
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        if (isWhitelist(request.getRequestURI())) {
            chain.doFilter(req, resp);
            return;
        }

        String officeId = request.getHeader("Office-Id");
        if (officeId == null) {
            unauthorizedHandler(request, response);
            return;
        }

        Optional<BranchOffice> branchOffice = branchOfficeService.findById(Long.parseLong(officeId));
        Optional<Employee> employee = employeeService.getCurrentLoggedEmployee();
        if (branchOffice.isEmpty() || employee.isEmpty() || !employee.get().hasAccess(branchOffice.get())) {
            unauthorizedHandler(request, response);
            return;
        }

        BranchOfficeContext.setCurrentBranchOffice(branchOffice.get());
        chain.doFilter(req, resp);
    }


    private boolean isWhitelist(String requestURI) {
        return Stream.of("/oauth/token", "/tokens/revoke", "/ws", "/basic", "/actuator", "/webhooks", "/branch-offices")
                .anyMatch(requestURI::contains);
    }

    private void unauthorizedHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), new SimpleErrorResponse("Acesso não autorizado. Filial não encontrada"));

        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Acesso não autorizado. Filial não encontrada");
        errors.put("description", "Filial não informada no cabeçalho e/ou não encontrada");
        errors.put("request", new WebRequestProjection(request));
        logService.send(mapper.writeValueAsString(errors));
    }

}