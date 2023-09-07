package br.psi.giganet.stockapi.config.cors;

import br.psi.giganet.stockapi.config.project_property.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CORSFilter implements Filter {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        response.setHeader("Access-Control-Allow-Credentials", "true");
        
        if(applicationProperties.getUiCredentials().getUrl().equals("*")){
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        } else {
            response.setHeader("Access-Control-Allow-Origin", applicationProperties.getUiCredentials().getUrl());
        }

        if (("OPTIONS").equals(request.getMethod())
                && (applicationProperties.getUiCredentials().getUrl().equals(request.getHeader("Origin"))
                || applicationProperties.getUiCredentials().getUrl().equals("*"))) {
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, PATCH, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, Office-Id");
            response.setHeader("Access-Control-Max-Age", "3600");

            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, resp);
        }

    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

}
