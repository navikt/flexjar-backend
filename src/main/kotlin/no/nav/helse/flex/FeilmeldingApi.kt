package no.nav.helse.flex

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/syk/feilmeldinger/api/v1")
@RestController
class FeilmeldingApi {

    private val log = logger()

    @PostMapping("/feilmelding")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun lagreFeilmelding() =
        log.info("Feilmelding mottatt")

    @GetMapping("/test")
    @ResponseStatus(HttpStatus.OK)
    fun testIngress() =
        log.info("Get-kall mottatt")
}
