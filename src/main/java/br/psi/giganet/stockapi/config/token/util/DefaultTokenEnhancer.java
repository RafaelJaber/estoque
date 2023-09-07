package br.psi.giganet.stockapi.config.token.util;

import br.psi.giganet.stockapi.config.project_property.ApplicationProperties;
import br.psi.giganet.stockapi.config.security.model.SystemUser;
import br.psi.giganet.stockapi.employees.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

public class DefaultTokenEnhancer implements TokenEnhancer {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        SystemUser systemUser = (SystemUser) authentication.getPrincipal();
        Map<String, Object> addInfo = new HashMap<>();

        if (systemUser.getUser() instanceof Employee) {
            Map<String, String> userDetails = new HashMap<>();

            Employee employee = ((Employee) systemUser.getUser());
            userDetails.put("id", employee.getId().toString());
            userDetails.put("name", employee.getName());
            userDetails.put("email", employee.getEmail());

            addInfo.put("employee", userDetails);
        }

        Map<String, String> info = new HashMap<>();
        info.put("name", applicationProperties.getApiTitle());
        info.put("version", applicationProperties.getApiVersion());

        addInfo.put("info", info);

        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(addInfo);

        return accessToken;
    }

}
