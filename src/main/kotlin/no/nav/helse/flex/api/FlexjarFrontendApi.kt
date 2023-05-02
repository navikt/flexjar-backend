package no.nav.helse.flex.api

import no.nav.helse.flex.clientidvalidation.ClientIdValidation
import no.nav.helse.flex.repository.FeedbackDbRecord
import no.nav.helse.flex.repository.FeedbackRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/flexjarfrontend")
class FlexjarFrontendApi(
    private val feedbackRepository: FeedbackRepository,
    private val clientIdValidation: ClientIdValidation

) {

    @GetMapping("/feedback")
    @ResponseBody
    @ProtectedWithClaims(issuer = "azureator")
    fun lagreFeedback(@RequestBody feedback: String): List<FeedbackDbRecord> {
        clientIdValidation.validateClientId(
            ClientIdValidation.NamespaceAndApp(
                namespace = "flex",
                app = "flexjar-frontend"
            )
        )
        return feedbackRepository.findAll().toList() // TODO returnere litt som json
    }
}
