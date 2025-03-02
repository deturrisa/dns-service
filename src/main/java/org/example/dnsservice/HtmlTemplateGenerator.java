package org.example.dnsservice;

import org.example.dnsservice.context.ServerContext;
import org.example.dnsservice.model.EntryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
public class HtmlTemplateGenerator {

  private final SpringTemplateEngine templateEngine;

  @Value("${spring.thymeleaf.templates.file}")
  private String templateFileName;

  @Autowired
  public HtmlTemplateGenerator(SpringTemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  public String generate(EntryStore entryStore) {
    return templateEngine.process(templateFileName, ServerContext.create(entryStore));
  }
}
