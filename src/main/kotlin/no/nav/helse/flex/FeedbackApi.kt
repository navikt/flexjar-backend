package no.nav.helse.flex

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
    private val feedbackRepository: FeedbackRepository
) {

    @PostMapping("/feedback")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun lagreFeedback(@RequestBody feedback: String) {
        feedbackRepository.save(
            FeedbackDbRecord(
                opprettet = OffsetDateTime.now(),
                feedbackJson = feedback
            )
        )
    }
}
