package com.dnsservice.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FooServiceTest {

    private val service : FooService by lazy {
        FooService()
    }

    @Test
    fun `Should do something`(){
        service.doSomething().run{
            Assertions.assertEquals("i am foo", this)
        }
    }

}