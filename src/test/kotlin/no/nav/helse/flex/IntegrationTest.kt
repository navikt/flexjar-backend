package no.nav.helse.flex

import no.nav.helse.flex.repository.FeedbackRepository
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

class IntegrationTest : FellesTestOppsett() {

    @Autowired
    private lateinit var feedbackRepository: FeedbackRepository

    @AfterEach
    fun slettFraDatabase() {
        feedbackRepository.deleteAll()
    }

    @Test
    fun `202 ACCEPTED blir returnert ved gyldig feedback`() {
        val feilmeldingDto = mapOf(
            "hei" to "hade"
        )

        val serialisertTilString = feilmeldingDto.serialisertTilString()

        mockMvc.perform(
            post("/api/v1/feedback")
                .header("Authorization", "Bearer ${tokenxToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialisertTilString)
        ).andExpect(status().isAccepted)

        val lagredeFeilmeldigner = feedbackRepository.findAll()
        lagredeFeilmeldigner shouldHaveSize 1
        val lagretFeilmelding = lagredeFeilmeldigner.first()
        lagretFeilmelding.opprettet shouldBeLessOrEqualTo OffsetDateTime.now()
    }
}
