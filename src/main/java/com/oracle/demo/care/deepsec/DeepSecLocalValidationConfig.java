package com.oracle.demo.care.deepsec;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(DeepSecLocalValidationConfig.DeepSecLocalProperties.class)
@ConditionalOnProperty(name = "care.deepsec.local-direct-logon", havingValue = "true")
public class DeepSecLocalValidationConfig {
    @Bean
    @Primary
    DataSource directEndUserDataSource(DeepSecLocalProperties properties) {
        return new DirectEndUserDataSource(
                properties.jdbcUrl(),
                properties.defaultEndUser(),
                properties.endUserPassword());
    }

    @ConfigurationProperties(prefix = "care.deepsec")
    public record DeepSecLocalProperties(
            String jdbcUrl,
            String defaultEndUser,
            String endUserPassword
    ) {
    }
}
