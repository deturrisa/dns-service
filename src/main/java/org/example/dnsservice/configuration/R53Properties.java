package org.example.dnsservice.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import software.amazon.awssdk.annotations.NotNull;

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
@NotBlank String hostedZoneId,
@NotNull Long ttl,
@NotNull Long weight
){}
