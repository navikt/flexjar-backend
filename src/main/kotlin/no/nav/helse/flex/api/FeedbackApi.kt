package no.nav.helse.flex.api

import no.nav.helse.flex.repository.FeedbackDbRecord
import no.nav.helse.flex.repository.FeedbackRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/api/v1")
class FeedbackApi(
    private val feedbackRepository: FeedbackRepository
) {

    @PostMapping("/feedback")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ProtectedWithClaims(issuer = "tokenx")
    fun lagreFeedback(@RequestBody feedback: String) {
        feedback.lagre()
    }

    @PostMapping("/feedback/azure")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ProtectedWithClaims(issuer = "azureator")
    fun lagreFeedbackAzure(@RequestBody feedback: String) {
        feedback.lagre()
    }

    private fun String.lagre() {
        feedbackRepository.save(
            FeedbackDbRecord(
                opprettet = OffsetDateTime.now().truncatedTo(ChronoUnit.HOURS),
                feedbackJson = this
            )
        )
    }
}
