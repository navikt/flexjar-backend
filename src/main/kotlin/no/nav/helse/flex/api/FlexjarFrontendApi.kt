package no.nav.helse.flex.api

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.clientidvalidation.ClientIdValidation
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.FeedbackRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
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
    fun lagreFeedback(): List<FeedbackDto> {
        clientIdValidation.validateClientId(
            ClientIdValidation.NamespaceAndApp(
                namespace = "flex",
                app = "flexjar-frontend"
            )
        )
        return feedbackRepository.findAll().toList()
            .map { FeedbackDto(feedback = objectMapper.readValue(it.feedbackJson), opprettet = it.opprettet) }
    }
}

data class FeedbackDto(
    val feedback: Map<String, Any>,
    val opprettet: OffsetDateTime
)
