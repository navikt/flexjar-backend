package no.nav.helse.flex.utvikling

import no.nav.helse.flex.Application
import no.nav.helse.flex.testoppsett.startPostgresContainer
import org.springframework.boot.runApplication

fun main(args: Array<String>) {
    startPostgresContainer()

    System.setProperty("server.port", "80")
    System.setProperty("spring.profiles.active", "dev")
    runApplication<Application>(*args)
}
