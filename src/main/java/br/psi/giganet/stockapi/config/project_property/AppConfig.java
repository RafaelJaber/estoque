package br.psi.giganet.stockapi.config.project_property;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableScheduling
public class AppConfig {
}
