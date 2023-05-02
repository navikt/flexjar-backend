package no.nav.helse.flex

import no.nav.helse.flex.repository.FeedbackRepository
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldStartWith
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
            "hei" to "hade",
            "indre" to mapOf(
                "hei" to 5
            )
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

        val response = mockMvc.perform(
            get("/api/v1/intern/feedback")
                .header("Authorization", "Bearer ${skapAzureJwt()}")
        ).andExpect(status().isOk).andReturn().response.contentAsString

        response shouldStartWith "[{\"feedback\":{\"hei\":\"hade\",\"indre\":{\"hei\":5}},\"opprettet\":"
    }

    @Test
    fun `Henter data som flexmedlem`() {
        val contentAsString = mockMvc.perform(
            get("/api/v1/intern/feedback")
                .header("Authorization", "Bearer ${skapAzureJwt()}")
        ).andExpect(status().isOk).andReturn().response.contentAsString

        contentAsString shouldBeEqualTo "[]"
    }
}
