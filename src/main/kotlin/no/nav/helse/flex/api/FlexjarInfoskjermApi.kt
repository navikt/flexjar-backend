package no.nav.helse.flex.api

import no.nav.helse.flex.clientidvalidation.ClientIdValidation
import no.nav.helse.flex.repository.FeedbackDbRecord
import no.nav.helse.flex.repository.FeedbackRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class FlexjarInfoskjermApi(
    private val feedbackRepository: FeedbackRepository,
    private val clientIdValidation: ClientIdValidation,
) {
    @GetMapping("/api/v1/infoskjerm")
    @ResponseBody
    @ProtectedWithClaims(issuer = "azureator")
    fun hentFeedbackPageable(
        @RequestParam(defaultValue = "flex") team: String,
    ): List<FeedbackDto> {
        clientIdValidation.validateClientId(
            ClientIdValidation.NamespaceAndApp(
                namespace = "flex",
                app = "flexjar-infoskjerm",
            ),
        )
        return feedbackRepository.finnForInfoskjerm(team).map(FeedbackDbRecord::toDto)
    }
}
