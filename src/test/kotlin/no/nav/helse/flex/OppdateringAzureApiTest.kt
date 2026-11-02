package no.nav.helse.flex

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tilFeedbackPage
import tilFeedbackResponse

class OppdateringAzureApiTest : FellesTestOppsett() {
    @AfterEach
    fun slettFraDatabase() {
        feedbackRepository.deleteAll()
    }

    @Test
    fun `Vi kan oppdatere en feedback`() {
        val feedbackInn =
            mapOf(
                "app" to "spinnsyn-frontend",
                "feedbackId" to "spinnsyn refusjon",
                "indre" to
                    mapOf(
                        "hei" to 5,
                    ),
            )

        val feedbackResponse =
            mockMvc
                .perform(
                    post("/api/azure/v2/feedback")
                        .header("Authorization", "Bearer ${skapAzureJwt()}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackInn.serialisertTilString()),
                ).andExpect(status().isCreated)
                .tilFeedbackResponse()

        mockMvc
            .perform(
                get("/api/v1/intern/feedback")
                    .header("Authorization", "Bearer ${skapAzureJwt()}"),
            ).andExpect(status().isOk)
            .tilFeedbackPage()
            .also {
                it.totalPages shouldBe 1
                it.totalElements shouldBe 1
                it.content
                    .first()
                    .feedback["feedback"]
                    .shouldBeNull()
                it.content.first().id shouldBe feedbackResponse.id
            }

        val oppdatertFeedback =
            feedbackInn.toMutableMap().also {
                it["feedback"] = "min oppdaterte tekst"
            }

        mockMvc
            .perform(
                put("/api/azure/v2/feedback/${feedbackResponse.id}")
                    .header("Authorization", "Bearer ${skapAzureJwt()}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(oppdatertFeedback.serialisertTilString()),
            ).andExpect(status().isNoContent)

        mockMvc
            .perform(
                get("/api/v1/intern/feedback")
                    .header("Authorization", "Bearer ${skapAzureJwt()}"),
            ).andExpect(status().isOk)
            .tilFeedbackPage()
            .also {
                it.totalPages shouldBe 1
                it.totalElements shouldBe 1
                it.content.first().feedback["feedback"] shouldBe "min oppdaterte tekst"
                it.content.first().id shouldBe feedbackResponse.id
            }
    }

    @Test
    fun `Vi kan ikke oppdatere en feedback fra en annen app`() {
        val feedbackInn =
            mapOf(
                "app" to "spinnsyn-frontend",
                "feedbackId" to "spinnsyn refusjon",
                "indre" to
                    mapOf(
                        "hei" to 5,
                    ),
            )

        val feedbackResponse =
            mockMvc
                .perform(
                    post("/api/azure/v2/feedback")
                        .header("Authorization", "Bearer ${skapAzureJwt(azpName = "dev-gcp:flex:spinnsyn-frontend")}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackInn.serialisertTilString()),
                ).andExpect(status().isCreated)
                .tilFeedbackResponse()

        mockMvc
            .perform(
                put("/api/azure/v2/feedback/${feedbackResponse.id}")
                    .header("Authorization", "Bearer ${skapAzureJwt(azpName = "dev-gcp:flex:ditt-sykefravaer")}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(feedbackInn.serialisertTilString()),
            ).andExpect(status().isBadRequest)
    }
}
