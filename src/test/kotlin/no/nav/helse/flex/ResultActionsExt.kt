import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.api.FeedbackApi
import no.nav.helse.flex.api.FeedbackPage
import no.nav.helse.flex.objectMapper
import org.springframework.test.web.servlet.ResultActions

fun ResultActions.tilFeedbackResponse(): FeedbackApi.LagreFeedbackResponse =
    objectMapper.readValue(this.andReturn().response.contentAsString)

fun ResultActions.tilFeedbackPage(): FeedbackPage = objectMapper.readValue(this.andReturn().response.contentAsString)
