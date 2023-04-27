package com.dnsservice.entity

import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "foo")
data class FooEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
)
