package no.nav.helse.flex

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.api.FeedbackDto
import no.nav.helse.flex.repository.FeedbackRepository
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
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
        val feedbackInn = mapOf(
            "feedback" to "hade",
            "app" to "spinnsyn-frontend",
            "feedbackId" to "spinnsyn refusjon",
            "indre" to mapOf(
                "hei" to 5
            )
        )

        val serialisertTilString = feedbackInn.serialisertTilString()

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

        val deserialsert: List<FeedbackDto> = objectMapper.readValue(response)
        deserialsert shouldHaveSize 1
        deserialsert.first().feedback shouldBeEqualTo feedbackInn

        mockMvc.perform(
            delete("/api/v1/intern/feedback/${deserialsert.first().id}")
                .header("Authorization", "Bearer ${skapAzureJwt()}")
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            delete("/api/v1/intern/feedback/${deserialsert.first().id}")
                .header("Authorization", "Bearer ${skapAzureJwt()}")
        ).andExpect(status().isNotFound)

        val responseNy = mockMvc.perform(
            get("/api/v1/intern/feedback")
                .header("Authorization", "Bearer ${skapAzureJwt()}")
        ).andExpect(status().isOk).andReturn().response.contentAsString

        val deserialserNy: List<FeedbackDto> = objectMapper.readValue(responseNy)
        deserialserNy shouldHaveSize 0
    }

    @Test
    fun `Henter data som flexmedlem`() {
        val contentAsString = mockMvc.perform(
            get("/api/v1/intern/feedback")
                .header("Authorization", "Bearer ${skapAzureJwt()}")
        ).andExpect(status().isOk).andReturn().response.contentAsString

        contentAsString shouldBeEqualTo "[]"
    }

    @Test
    fun `400 ACCEPTED blir returnert ved ugyldig feedback`() {
        val feilmeldingDto = mapOf(
            "blah" to "hade",
            "apklp" to "spinnsyn-frontend",
            "feedbackId" to "spinnsyn refusjon",
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
        ).andExpect(status().isBadRequest)
    }
}
