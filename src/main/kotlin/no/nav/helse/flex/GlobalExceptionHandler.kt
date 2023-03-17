package no.nav.helse.flex

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    private val log = logger()

    @ExceptionHandler(java.lang.Exception::class)
    fun handleException(e: Exception): ResponseEntity<Any> {
        log.warn("Returnerer 202 ACCEPTED selv om kallet feilet med ${e.message}.")
        return ResponseEntity.accepted().build()
    }
}
