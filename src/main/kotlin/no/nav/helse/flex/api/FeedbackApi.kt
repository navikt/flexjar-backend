package no.nav.helse.flex.api

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.objectMapper
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
        try {
            tilFeedbackInputDto()
        } catch (e: Exception) {
            throw IllegalArgumentException("Kunne ikke deserialisere feedback", e)
        }

        feedbackRepository.save(
            FeedbackDbRecord(
                opprettet = OffsetDateTime.now(),
                feedbackJson = this
            )
        )
    }
}

data class FeedbackInputDto(
    val feedback: String?,
    val svar: String?,
    val app: String,
    val feedbackId: String
)
fun String.tilFeedbackInputDto(): FeedbackInputDto = objectMapper.readValue(this)
