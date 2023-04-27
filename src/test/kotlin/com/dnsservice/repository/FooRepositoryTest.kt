package com.dnsservice.repository

import com.dnsservice.entity.FooEntity
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.util.UUID

@DataJpaTest
@AutoConfigureEmbeddedDatabase
class FooRepositoryTest {

    @Autowired
    private lateinit var fooRepository: FooRepository

    @Test
    fun `should save and get foo from db`() {
        val id = UUID.randomUUID()
        fooRepository.save(FooEntity(id))
        fooRepository.findById(id).run {
            Assertions.assertNotNull(this)
        }
    }
}
