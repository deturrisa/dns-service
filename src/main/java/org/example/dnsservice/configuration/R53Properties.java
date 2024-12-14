package org.example.dnsservice.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aws.r53")
public record R53Properties(
@NotBlank String accessKey,
@NotBlank String secretKey,
@NotBlank String region,
@NotBlank String role,
@NotBlank String localAddress,
@NotBlank String useLocal,
@NotBlank String endpointOverride,
@NotBlank String hostedZoneId
){}
