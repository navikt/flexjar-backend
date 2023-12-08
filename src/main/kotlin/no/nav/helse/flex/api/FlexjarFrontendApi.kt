package no.nav.helse.flex.api

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.clientidvalidation.ClientIdValidation
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.FeedbackDbRecord
import no.nav.helse.flex.repository.FeedbackRepository
import no.nav.helse.flex.repository.PagingFeedbackRepository
import no.nav.helse.flex.serialisertTilString
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.*
import kotlin.NoSuchElementException
import kotlin.math.ceil

@RestController
class FlexjarFrontendApi(
    private val feedbackRepository: FeedbackRepository,
    private val pagingFeedbackRepository: PagingFeedbackRepository,
    private val clientIdValidation: ClientIdValidation

) {

    @GetMapping("/api/v1/intern/feedback")
    @ResponseBody
    @ProtectedWithClaims(issuer = "azureator")
    fun hentFeedbackPageable(
        @RequestParam(defaultValue = "flex") team: String,
        @RequestParam page: Int?,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "false") medTekst: Boolean,
        @RequestParam(defaultValue = "false") stjerne: Boolean,
        @RequestParam fritekst: String?
    ): FeedbackPage {
        clientIdValidation.validateClientId(
            ClientIdValidation.NamespaceAndApp(
                namespace = "flex",
                app = "flexjar-frontend"
            )
        )

        val dbRecords = pagingFeedbackRepository.findPaginated(
            pageInn = page,
            size = size,
            team = team,
            medTekst = medTekst,
            fritekst = fritekst,
            stjerne = stjerne
        )

        return FeedbackPage(
            content = dbRecords.first.map(FeedbackDbRecord::toDto),
            totalPages = ceil(dbRecords.second.toDouble() / size).toInt(),
            totalElements = dbRecords.second.toInt(),
            size = size,
            number = dbRecords.third
        )
    }

    @DeleteMapping("/api/v1/intern/feedback/{id}")
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

    @PostMapping("/api/v1/intern/feedback/{id}/tags")
    @ResponseBody
    @ProtectedWithClaims(issuer = "azureator")
    fun lagreTag(@PathVariable id: String, @RequestBody tag: TagDto): ResponseEntity<Void> {
        clientIdValidation.validateClientId(
            ClientIdValidation.NamespaceAndApp(
                namespace = "flex",
                app = "flexjar-frontend"
            )
        )

        try {
            val feedback = feedbackRepository.findById(id).get()
            val feedbackDto = feedback.toDto()

            val tags = feedbackDto.tags.toMutableSet()
            tags.add(tag.tag.lowercase(Locale.getDefault()))

            val feedbackMedTag = feedback.copy(
                tags = tags.joinToString(",")
            )
            feedbackRepository.save(feedbackMedTag)

            return ResponseEntity<Void>(HttpStatus.CREATED)
        } catch (e: NoSuchElementException) {
            return ResponseEntity<Void>(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            return ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/api/v1/intern/feedback/tags")
    @ResponseBody
    @ProtectedWithClaims(issuer = "azureator")
    fun hentAlleTags(): Set<String> {
        clientIdValidation.validateClientId(
            ClientIdValidation.NamespaceAndApp(
                namespace = "flex",
                app = "flexjar-frontend"
            )
        )

        return feedbackRepository.finnAlleDistinctTags().map { it?.split(",")?.toSet() ?: emptySet() }.flatten().toSet()
    }

    @DeleteMapping("/api/v1/intern/feedback/{id}/tags")
    @ResponseBody
    @ProtectedWithClaims(issuer = "azureator")
    fun slettTag(@PathVariable id: String, @RequestParam tag: String): ResponseEntity<Void> {
        clientIdValidation.validateClientId(
            ClientIdValidation.NamespaceAndApp(
                namespace = "flex",
                app = "flexjar-frontend"
            )
        )

        try {
            val feedback = feedbackRepository.findById(id).get()
            val feedbackDto = feedback.toDto()

            val tags = feedbackDto.tags.toMutableSet()
            tags.remove(tag.lowercase(Locale.getDefault()))

            val feedbackMedTag = feedback.copy(
                tags = if (tags.isEmpty()) {
                    null
                } else {
                    tags.joinToString(",")
                }
            )
            feedbackRepository.save(feedbackMedTag)

            return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
        } catch (e: NoSuchElementException) {
            return ResponseEntity<Void>(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            return ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}

fun FeedbackDbRecord.toDto(): FeedbackDto {
    return FeedbackDto(
        feedback = objectMapper.readValue(this.feedbackJson),
        opprettet = this.opprettet,
        id = this.id!!,
        team = this.team,
        app = this.app,
        tags = this.tags?.split(",")?.toSet() ?: emptySet()
    )
}

data class FeedbackDto(
    val feedback: Map<String, Any>,
    val opprettet: OffsetDateTime,
    val id: String,
    val team: String,
    val app: String?,
    val tags: Set<String>
)

data class FeedbackPage(
    val content: List<FeedbackDto>,
    val totalPages: Int,
    val totalElements: Int,
    val size: Int,
    val number: Int
)

data class TagDto(
    val tag: String
)
