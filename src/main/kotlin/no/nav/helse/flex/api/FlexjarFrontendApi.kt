package no.nav.helse.flex.api

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.clientidvalidation.ClientIdValidation
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.FeedbackRepository
import no.nav.helse.flex.repository.PagingFeedbackRepository
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
    private val pagingFeedbackRepository: PagingFeedbackRepository,
    private val clientIdValidation: ClientIdValidation

) {

    @GetMapping("/feedback")
    @ResponseBody
    @ProtectedWithClaims(issuer = "azureator")
    fun hentFeedback(@RequestParam team: String?): List<FeedbackDto> {
        clientIdValidation.validateClientId(
            ClientIdValidation.NamespaceAndApp(
                namespace = "flex",
                app = "flexjar-frontend"
            )
        )

        return feedbackRepository.getAllByTeam(team ?: "flex").toList()
            .map {
                FeedbackDto(
                    feedback = objectMapper.readValue(it.feedbackJson),
                    opprettet = it.opprettet,
                    id = it.id!!,
                    team = it.team,
                    app = it.app
                )
            }
    }

    @GetMapping("/feedback-pagable")
    @ResponseBody
    @ProtectedWithClaims(issuer = "azureator")
    fun hentFeedbackPageable(
        @RequestParam(defaultValue = "flex") team: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "false") medTekst: Boolean
    ): FeedbackPage {
        clientIdValidation.validateClientId(
            ClientIdValidation.NamespaceAndApp(
                namespace = "flex",
                app = "flexjar-frontend"
            )
        )

        val dbRecords = pagingFeedbackRepository.findPaginated(
            page = page,
            size = size,
            team = team,
            medTekst = medTekst
        )
        return FeedbackPage(
            content = dbRecords.first.map {
                FeedbackDto(
                    feedback = objectMapper.readValue(it.feedbackJson),
                    opprettet = it.opprettet,
                    id = it.id!!,
                    team = it.team,
                    app = it.app

                )
            },
            totalPages = Math.ceil(dbRecords.second.toDouble() / size).toInt(),
            totalElements = dbRecords.second.toInt(),
            size = size,
            number = page

        )
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
    val id: String,
    val team: String,
    val app: String?
)

data class FeedbackPage(
    val content: List<FeedbackDto>,
    val totalPages: Int,
    val totalElements: Int,
    val size: Int,
    val number: Int
)
