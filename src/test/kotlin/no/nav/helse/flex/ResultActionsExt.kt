import no.nav.helse.flex.api.FeedbackApi
import no.nav.helse.flex.api.FeedbackPage
import no.nav.helse.flex.objectMapper
import org.springframework.test.web.servlet.ResultActions
import tools.jackson.module.kotlin.readValue

fun ResultActions.tilFeedbackResponse(): FeedbackApi.LagreFeedbackResponse =
    objectMapper.readValue(this.andReturn().response.contentAsString)

fun ResultActions.tilFeedbackPage(): FeedbackPage = objectMapper.readValue(this.andReturn().response.contentAsString)
