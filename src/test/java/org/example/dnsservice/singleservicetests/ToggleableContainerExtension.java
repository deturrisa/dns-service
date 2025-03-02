package org.example.dnsservice.singleservicetests;

import java.util.Arrays;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToggleableContainerExtension
    implements BeforeAllCallback, AfterAllCallback, AfterEachCallback, BeforeEachCallback {

  private static final Logger log = LoggerFactory.getLogger(ToggleableContainerExtension.class);

  private final ExternalPlatform type;

  public ToggleableContainerExtension(ExternalPlatform type) {
    this.type = type;
  }

  protected void beforeAllInternal(ExtensionContext context) {}

  protected void beforeEachInternal(ExtensionContext context) {}

  protected void afterAllInternal(ExtensionContext context) {}

  protected void afterEachInternal(ExtensionContext context) {}

  @Override
  public void beforeEach(ExtensionContext context) {
    if (isFirstLevelClass(context) && isExtensionEnabled(context)) {
      beforeEachInternal(context);
    }
  }

  @Override
  public void afterEach(ExtensionContext context) {
    if (isFirstLevelClass(context) && isExtensionEnabled(context)) {
      afterEachInternal(context);
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
    if (isFirstLevelClass(context) && isExtensionEnabled(context)) {
      afterAllInternal(context);
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    if (isFirstLevelClass(context) && isExtensionEnabled(context)) {
      beforeAllInternal(context);
    }
  }

  private Boolean isFirstLevelClass(ExtensionContext context) {
    return context
        .getParent()
        .get()
        .getClass()
        .getSimpleName()
        .equals("JupiterEngineExtensionContext");
  }

  private boolean isExtensionEnabled(ExtensionContext context) {
    var testClass =
        context
            .getTestClass()
            .orElseThrow(() -> new ExtensionConfigurationException("Could not find test class"));
    var annotation = testClass.getAnnotation(SingleServiceTest.class);
    if (annotation == null) {
      throw new ExtensionConfigurationException("Could not find @SingleServiceTest annotation");
    }
    return Arrays.asList(annotation.value()).contains(type);
  }
}
