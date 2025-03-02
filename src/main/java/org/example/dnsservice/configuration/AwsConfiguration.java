package org.example.dnsservice.configuration;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

@Configuration
@EnableConfigurationProperties(value = {R53Properties.class})
public class AwsConfiguration {

  private final String useLocalR53PropertyKey = "aws.r53.use-local";

  @Bean
  @ConditionalOnProperty(
      value = {useLocalR53PropertyKey},
      havingValue = "false")
  public Route53AsyncClient r53Client(R53Properties r53Properties) {
    return Route53AsyncClient.builder()
        .credentialsProvider(getStaticCredentialsProvider(r53Properties))
        .endpointOverride(URI.create(r53Properties.endpointOverride()))
        .region(Region.of(r53Properties.region()))
        .build();
  }

  private StaticCredentialsProvider getStaticCredentialsProvider(R53Properties r53Properties) {
    var awsCredentials =
        AwsBasicCredentials.create(r53Properties.accessKey(), r53Properties.secretKey());
    return StaticCredentialsProvider.create(awsCredentials);
  }

  private AwsCredentialsProvider roleAssumingCredentialsProviderChain(R53Properties r53Properties)
      throws UnknownHostException {
    return AwsCredentialsProviderChain.of(
        assumeRoleCredentialsProvider(r53Properties), AnonymousCredentialsProvider.create());
  }

  private StsAssumeRoleCredentialsProvider assumeRoleCredentialsProvider(
      R53Properties r53Properties) throws UnknownHostException {
    var userCredentialsProvider =
        StaticCredentialsProvider.create(
            AwsBasicCredentials.create(r53Properties.accessKey(), r53Properties.secretKey()));

    var stsClient =
        StsClient.builder()
            .credentialsProvider(userCredentialsProvider)
            .region(Region.of(r53Properties.region()))
            .build();

    var assumeRoleRefreshRequest =
        AssumeRoleRequest.builder()
            .roleArn(r53Properties.role())
            .roleSessionName(InetAddress.getLocalHost().getHostName())
            .durationSeconds(60 * 60 * 10)
            .build();

    return StsAssumeRoleCredentialsProvider.builder()
        .stsClient(stsClient)
        .refreshRequest(assumeRoleRefreshRequest)
        .build();
  }
}
