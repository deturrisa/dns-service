package com.dnsservice.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories("com.dnsservice.repository")
@EntityScan("com.dnsservice.entity")
class DatabaseConfiguration
