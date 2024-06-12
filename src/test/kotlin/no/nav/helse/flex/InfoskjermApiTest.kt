package no.nav.helse.flex

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.api.FeedbackDto
import no.nav.helse.flex.api.TagDto
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tilFeedbackPage

class InfoskjermApiTest : FellesTestOppsett() {
    @AfterEach
    fun slettFraDatabase() {
        feedbackRepository.deleteAll()
    }

    fun ResultActions.tilFeedback(): List<FeedbackDto> {
        return objectMapper.readValue(this.andReturn().response.contentAsString)
    }

    @Test
    fun `Henter tom liste som infoskjerm`() {
        mockMvc.perform(
            get("/api/v1/infoskjerm")
                .header(
                    "Authorization",
                    "Bearer ${
                        skapAzureJwt(
                            clientId = "flexjar-infoskjerm-client-id",
                            azpName = "flexjar-infoskjerm",
                        )
                    }",
                ),
        ).tilFeedback().also {
            it shouldHaveSize 0
        }
    }

    @Test
    fun `krever riktig app som infoskjerm`() {
        mockMvc.perform(
            get("/api/v1/infoskjerm")
                .header(
                    "Authorization",
                    "Bearer ${
                        skapAzureJwt(
                            clientId = "flexjar-frontend-client-id",
                            azpName = "flexjar-frontend",
                        )
                    }",
                ),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `Infoskjerm api trenger auth`() {
        mockMvc.perform(
            get("/api/v1/infoskjerm"),
        ).andExpect(status().isUnauthorized).andReturn().response.contentAsString
    }

    @Test
    fun `Kan hente feedback med tag infoskjerm`() {
        mockMvc.perform(
            get("/api/v1/infoskjerm")
                .header(
                    "Authorization",
                    "Bearer ${
                        skapAzureJwt(
                            clientId = "flexjar-infoskjerm-client-id",
                            azpName = "flexjar-infoskjerm",
                        )
                    }",
                ),
        ).tilFeedback().also {
            it shouldHaveSize 0
        }

        mockMvc.perform(
            post("/api/v1/feedback")
                .header("Authorization", "Bearer ${tokenxToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapOf(
                        "feedback" to "hade",
                        "app" to "spinnsyn-frontend",
                        "feedbackId" to "spinnsyn refusjon",
                        "indre" to
                            mapOf(
                                "hei" to 5,
                            ),
                    ).serialisertTilString(),
                ),
        ).andExpect(status().isAccepted)
        mockMvc.perform(
            get("/api/v1/infoskjerm")
                .header(
                    "Authorization",
                    "Bearer ${
                        skapAzureJwt(
                            clientId = "flexjar-infoskjerm-client-id",
                            azpName = "flexjar-infoskjerm",
                        )
                    }",
                ),
        ).tilFeedback().also {
            it shouldHaveSize 0
        }
        val first =
            mockMvc.perform(
                get("/api/v1/intern/feedback")
                    .header("Authorization", "Bearer ${skapAzureJwt()}"),
            ).andExpect(status().isOk).tilFeedbackPage().content.first()

        mockMvc.perform(
            post("/api/v1/intern/feedback/${first.id}/tags")
                .header("Authorization", "Bearer ${skapAzureJwt()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TagDto("infoskjerm").serialisertTilString()),
        ).andExpect(status().isCreated)

        mockMvc.perform(
            get("/api/v1/infoskjerm")
                .header(
                    "Authorization",
                    "Bearer ${
                        skapAzureJwt(
                            clientId = "flexjar-infoskjerm-client-id",
                            azpName = "flexjar-infoskjerm",
                        )
                    }",
                ),
        ).tilFeedback().also {
            it shouldHaveSize 1
        }

        mockMvc.perform(
            delete("/api/v1/intern/feedback/${first.id}/tags?tag=infoskjerm")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            get("/api/v1/infoskjerm")
                .header(
                    "Authorization",
                    "Bearer ${
                        skapAzureJwt(
                            clientId = "flexjar-infoskjerm-client-id",
                            azpName = "flexjar-infoskjerm",
                        )
                    }",
                ),
        ).tilFeedback().also {
            it shouldHaveSize 0
        }
    }
}
