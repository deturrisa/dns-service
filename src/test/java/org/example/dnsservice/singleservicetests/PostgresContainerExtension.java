package org.example.dnsservice.singleservicetests;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainerExtension extends ToggleableContainerExtension {

  private static final String POSTGRES_DB = "POSTGRES_DB";
  private static final String POSTGRES_USER = "POSTGRES_USER";
  private static final String POSTGRES_USER_PASSWORD = "POSTGRES_USER_PASSWORD";

  private final Logger log = LoggerFactory.getLogger(PostgresContainerExtension.class);

  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:12.13")
          .withDatabaseName("dns_service")
          .withUsername("username")
          .withPassword("password")
          .withExposedPorts(5432);

  public PostgresContainerExtension() {
    super(ExternalPlatform.POSTGRES);
  }

  @Override
  protected void afterAllInternal(ExtensionContext context) {
    log.info("### Stopping Postgres container.");
    postgres.stop();
    log.info("### Postgres container stopped.");
  }

  private void setDatabaseConnectionProperties() {
    System.setProperty(POSTGRES_DB, postgres.getJdbcUrl());
    System.setProperty(POSTGRES_USER, postgres.getUsername());
    System.setProperty(POSTGRES_USER_PASSWORD, postgres.getPassword());
  }

  @Override
  protected void beforeAllInternal(ExtensionContext context) {
    log.info("### Starting up Postgres container.");
    postgres.start();
    setDatabaseConnectionProperties();
    log.info(
        "### Postgres container startup finished with properties: {} {} {}",
        POSTGRES_DB,
        System.getProperty(POSTGRES_DB),
        POSTGRES_USER,
        System.getProperty(POSTGRES_USER),
        POSTGRES_USER_PASSWORD,
        System.getProperty(POSTGRES_USER_PASSWORD));
  }
}
