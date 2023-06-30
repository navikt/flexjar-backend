package no.nav.helse.flex.api

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.clientidvalidation.ClientIdValidation
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.FeedbackRepository
import no.nav.helse.flex.serialisertTilString
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime

@RestController
@RequestMapping("/api/v1/intern")
class FlexjarFrontendApi(
    private val feedbackRepository: FeedbackRepository,
    private val clientIdValidation: ClientIdValidation

) {

    @GetMapping("/feedback")
    @ResponseBody
    @ProtectedWithClaims(issuer = "azureator")
    fun hentFeedback(): List<FeedbackDto> {
        clientIdValidation.validateClientId(
            ClientIdValidation.NamespaceAndApp(
                namespace = "flex",
                app = "flexjar-frontend"
            )
        )
        return feedbackRepository.findAll().toList()
            .map {
                FeedbackDto(
                    feedback = objectMapper.readValue(it.feedbackJson),
                    opprettet = it.opprettet,
                    id = it.id!!
                )
            }
    }

    @DeleteMapping("/feedback/{id}")
    @ResponseBody
    @ProtectedWithClaims(issuer = "azureator")
    fun slettFeedback(@PathVariable id: String): ResponseEntity<Void> {
        clientIdValidation.validateClientId(
            ClientIdValidation.NamespaceAndApp(
                namespace = "flex",
                app = "flexjar-frontend"
            )
        )

        try {
            val feedback = feedbackRepository.findById(id).get()
            val json = feedback.feedbackJson
            val jsonMap = objectMapper.readValue<MutableMap<String, Any>>(json)

            jsonMap.replace("feedback", "") ?: return ResponseEntity<Void>(HttpStatus.BAD_REQUEST)
            val feedbackUtenFeedback = feedback.copy(
                feedbackJson = jsonMap.serialisertTilString()
            )
            feedbackRepository.save(feedbackUtenFeedback)

            return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
        } catch (e: NoSuchElementException) {
            return ResponseEntity<Void>(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            return ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}

data class FeedbackDto(
    val feedback: Map<String, Any>,
    val opprettet: OffsetDateTime,
    val id: String
)
