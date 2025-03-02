package org.example.dnsservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories("org.example.dnsservice.repository")
@EntityScan("org.example.dnsservice.entity")
public class DatabaseConfiguration {
  @Bean
  @Primary
  public PlatformTransactionManager transactionManager() {
    return new JpaTransactionManager();
  }

  @Bean
  public HibernatePropertiesCustomizer jsonFormatMapperCustomizer() {
    return properties ->
        properties.put(
            AvailableSettings.JSON_FORMAT_MAPPER,
            new JacksonJsonFormatMapper(new ObjectMapper().findAndRegisterModules()));
  }
}
