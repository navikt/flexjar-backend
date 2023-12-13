package no.nav.helse.flex.api

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.FeedbackDbRecord
import no.nav.helse.flex.repository.FeedbackRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
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
    private val contextHolder: TokenValidationContextHolder,
    private val feedbackRepository: FeedbackRepository,
) {
    @PostMapping("/feedback")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ProtectedWithClaims(issuer = "tokenx")
    fun lagreFeedback(
        @RequestBody feedback: String,
    ) {
        val clientId = contextHolder.tokenValidationContext.getClaims("tokenx").getStringClaim("client_id")
        val (team, app) = clientId.split(":").takeLast(2)

        feedback.lagre(
            app = app,
            team = team,
        )
    }

    @PostMapping("/feedback/azure")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ProtectedWithClaims(issuer = "azureator")
    fun lagreFeedbackAzure(
        @RequestBody feedback: String,
    ) {
        val azpName = contextHolder.tokenValidationContext.getClaims("azureator").getStringClaim("azp_name")
        val (team, app) = azpName.split(":").takeLast(2)

        feedback.lagre(
            app = app,
            team = team,
        )
    }

    private fun String.lagre(
        app: String,
        team: String,
    ) {
        try {
            tilFeedbackInputDto()
        } catch (e: Exception) {
            throw IllegalArgumentException("Kunne ikke deserialisere feedback", e)
        }

        feedbackRepository.save(
            FeedbackDbRecord(
                opprettet = OffsetDateTime.now(),
                feedbackJson = this,
                app = app,
                team = team,
            ),
        )
    }
}

data class FeedbackInputDto(
    val feedback: String?,
    val svar: String?,
    val app: String,
    val feedbackId: String,
)

fun String.tilFeedbackInputDto(): FeedbackInputDto = objectMapper.readValue(this)
