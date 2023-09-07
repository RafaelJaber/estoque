package br.psi.giganet.stockapi.config.security;

import br.psi.giganet.stockapi.config.exception.controller.AuthExceptionEntryPoint;
import br.psi.giganet.stockapi.config.project_property.ApplicationProperties;
import br.psi.giganet.stockapi.config.security.filter.BranchOfficeFilter;
import br.psi.giganet.stockapi.config.security.filter.RemoteApiAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.web.session.ConcurrentSessionFilter;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private DefaultTokenServices tokenServices;

    @Autowired
    private AuthExceptionEntryPoint authExceptionEntryPoint;

    @Autowired
    private RemoteApiAuthenticationFilter apiAuthenticationFilter;

    @Autowired
    private BranchOfficeFilter branchOfficeFilter;

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/webhooks")
                .permitAll()
                .and() // external APIs
                .addFilterAfter(apiAuthenticationFilter, ConcurrentSessionFilter.class)
                .addFilterAfter(branchOfficeFilter, RemoteApiAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/**/basic/**")
                .permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/ws")
                .permitAll()
                .and()
                .authorizeRequests()
                .antMatchers( // documentation and monitoring
                        HttpMethod.GET,
                        "/actuator/health",
                        "/actuator/info"
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(authExceptionEntryPoint)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.stateless(true);
        resources.tokenServices(tokenServices);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
