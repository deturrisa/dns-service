package org.example.dnsservice.configuration;

import java.util.Properties;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

public class YamlPropertySourceFactory implements PropertySourceFactory {

  @Override
  public PropertiesPropertySource createPropertySource(String name, EncodedResource resource) {
    var propertiesFromYaml = loadYamlIntoProperties(resource);

    var sourceName = getSourceName(name, resource);

    return new PropertiesPropertySource(sourceName, propertiesFromYaml);
  }

  private static String getSourceName(String name, EncodedResource resource) {
    String sourceName;
    if (name == null) {
      sourceName = resource.getResource().getFilename();
    } else {
      sourceName = name;
    }
    return sourceName;
  }

  private Properties loadYamlIntoProperties(EncodedResource resource) {
    var factory = new YamlPropertiesFactoryBean();
    factory.setResources(resource.getResource());
    factory.afterPropertiesSet();
    return factory.getObject();
  }
}
