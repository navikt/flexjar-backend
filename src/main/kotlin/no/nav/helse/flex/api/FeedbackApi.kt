package no.nav.helse.flex.api

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.FeedbackDbRecord
import no.nav.helse.flex.repository.FeedbackRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime

@RestController
class FeedbackApi(
    private val contextHolder: TokenValidationContextHolder,
    private val feedbackRepository: FeedbackRepository,
) {
    @PostMapping("/api/v1/feedback")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ProtectedWithClaims(issuer = "tokenx")
    fun lagreFeedbackV1(
        @RequestBody feedback: String,
    ) {
        lagreFeedbackFelles(feedback)
    }

    data class LagreFeedbackResponse(
        val id: String,
    )

    @PostMapping(value = ["/api/v2/feedback"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @ProtectedWithClaims(issuer = "tokenx")
    fun lagreFeedback(
        @RequestBody feedback: String,
    ): LagreFeedbackResponse {
        val lagretFeedback = lagreFeedbackFelles(feedback)
        return LagreFeedbackResponse(lagretFeedback.id!!)
    }

    @PutMapping(value = ["/api/v2/feedback/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ProtectedWithClaims(issuer = "tokenx")
    fun oppdaterFeedback(
        @PathVariable id: String,
        @RequestBody feedback: String,
    ) {
        val clientId = contextHolder.tokenValidationContext.getClaims("tokenx").getStringClaim("client_id")
        val (team, app) = clientId.split(":").takeLast(2)

        feedback.oppdater(
            app = app,
            team = team,
            id = id,
        )
    }

    private fun lagreFeedbackFelles(feedback: String): FeedbackDbRecord {
        val clientId = contextHolder.tokenValidationContext.getClaims("tokenx").getStringClaim("client_id")
        val (team, app) = clientId.split(":").takeLast(2)

        return feedback.lagre(
            app = app,
            team = team,
        )
    }

    @PostMapping("/api/v1/feedback/azure")
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
    ): FeedbackDbRecord {
        try {
            tilFeedbackInputDto()
        } catch (e: Exception) {
            throw IllegalArgumentException("Kunne ikke deserialisere feedback", e)
        }

        return feedbackRepository.save(
            FeedbackDbRecord(
                opprettet = OffsetDateTime.now(),
                feedbackJson = this,
                app = app,
                team = team,
            ),
        )
    }

    private fun String.oppdater(
        app: String,
        team: String,
        id: String,
    ) {
        try {
            tilFeedbackInputDto()
        } catch (e: Exception) {
            throw IllegalArgumentException("Kunne ikke deserialisere feedback", e)
        }

        val feedback =
            feedbackRepository.findByIdOrNull(id) ?: throw IllegalArgumentException("Fant ikke feedback med id $id")

        if (feedback.app != app || feedback.team != team) {
            throw IllegalArgumentException("Kan ikke oppdatere feedback som ikke tilhører samme app")
        }

        feedbackRepository.save(feedback.copy(feedbackJson = this))
    }
}

data class FeedbackInputDto(
    val feedback: String?,
    val svar: String?,
    val app: String,
    val feedbackId: String,
)

fun String.tilFeedbackInputDto(): FeedbackInputDto = objectMapper.readValue(this)
