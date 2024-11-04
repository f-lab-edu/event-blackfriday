package com.jaeyeon.blackfriday

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class BlackfridayApplication

fun main(args: Array<String>) {
    runApplication<BlackfridayApplication>(*args)
}
