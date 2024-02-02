package no.nav.helse.flex

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.api.FeedbackPage
import no.nav.helse.flex.api.TagDto
import no.nav.helse.flex.repository.FeedbackDbRecord
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tilFeedbackPage
import java.time.OffsetDateTime

class IntegrationTest : FellesTestOppsett() {
    @AfterEach
    fun slettFraDatabase() {
        feedbackRepository.deleteAll()
    }

    @Test
    fun `202 ACCEPTED blir returnert ved gyldig feedback`() {
        val feedbackInn =
            mapOf(
                "feedback" to "hade",
                "app" to "spinnsyn-frontend",
                "feedbackId" to "spinnsyn refusjon",
                "indre" to
                    mapOf(
                        "hei" to 5,
                    ),
            )

        val serialisertTilString = feedbackInn.serialisertTilString()

        mockMvc.perform(
            post("/api/v1/feedback")
                .header("Authorization", "Bearer ${tokenxToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialisertTilString),
        ).andExpect(status().isAccepted)

        val lagredeFeilmeldigner = feedbackRepository.findAll()
        lagredeFeilmeldigner shouldHaveSize 1
        val lagretFeilmelding = lagredeFeilmeldigner.first()
        lagretFeilmelding.opprettet shouldBeLessOrEqualTo OffsetDateTime.now()

        val deserialsert =
            mockMvc.perform(
                get("/api/v1/intern/feedback")
                    .header("Authorization", "Bearer ${skapAzureJwt()}"),
            ).andExpect(status().isOk).tilFeedbackPage()

        deserialsert.content shouldHaveSize 1
        deserialsert.content.first().feedback shouldBeEqualTo feedbackInn

        mockMvc.perform(
            delete("/api/v1/intern/feedback/${deserialsert.content.first().id}")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            get("/api/v1/intern/feedback")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isOk).tilFeedbackPage().also {
            it.content shouldHaveSize 1
            it.totalElements shouldBeEqualTo 1
            it.totalPages shouldBeEqualTo 1
        }

        mockMvc.perform(
            get("/api/v1/intern/feedback?medTekst=true")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isOk).tilFeedbackPage().also {
            it.content shouldHaveSize 0
            it.totalElements shouldBeEqualTo 0
            it.totalPages shouldBeEqualTo 0
        }

        mockMvc.perform(
            get("/api/v1/intern/feedback?medTekst=true&fritekst=sdfsdf")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isOk).andReturn().response.contentAsString

        // Kan filtrere på app
        mockMvc.perform(
            get("/api/v1/intern/feedback?app=spinnsyn-frontend")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isOk).tilFeedbackPage().also {
            it.content shouldHaveSize 1
            it.totalElements shouldBeEqualTo 1
            it.totalPages shouldBeEqualTo 1
        }

        mockMvc.perform(
            get("/api/v1/intern/feedback?app=facebook-frontend")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isOk).tilFeedbackPage().also {
            it.content shouldHaveSize 0
            it.totalElements shouldBeEqualTo 0
            it.totalPages shouldBeEqualTo 0
        }
    }

    @Test
    fun `Henter data som flexmedlem`() {
        val contentAsString =
            mockMvc.perform(
                get("/api/v1/intern/feedback")
                    .header("Authorization", "Bearer ${skapAzureJwt()}"),
            ).andExpect(status().isOk).andReturn().response.contentAsString

        contentAsString shouldBeEqualTo "{\"content\":[],\"totalPages\":0,\"totalElements\":0,\"size\":10,\"number\":0}"
    }

    @Test
    fun `Henter alle teams og apps`() {
        val feedbackInn =
            mapOf(
                "feedback" to "hade",
                "app" to "spinnsyn-dsfsdfsdf",
                "feedbackId" to "spinnsyn refusjon",
                "indre" to
                    mapOf(
                        "hei" to 5,
                    ),
            ).serialisertTilString()

        mockMvc.perform(
            post("/api/v1/feedback")
                .header("Authorization", "Bearer ${tokenxToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(feedbackInn),
        ).andExpect(status().isAccepted)

        mockMvc.perform(
            post("/api/v1/feedback")
                .header("Authorization", "Bearer ${tokenxToken(clientId = "dev-gcp:flex:sykepengesoknad")}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(feedbackInn),
        ).andExpect(status().isAccepted)

        mockMvc.perform(
            post("/api/v1/feedback")
                .header("Authorization", "Bearer ${tokenxToken(clientId = "dev-gcp:team-sykmelding:dine-sykmeldte")}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(feedbackInn),
        ).andExpect(status().isAccepted)

        // Kan hente apps
        mockMvc.perform(
            get("/api/v1/intern/feedback/teams")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isOk).andReturn().response.contentAsString shouldBeEqualTo
            "{\"team-sykmelding\":[\"dine-sykmeldte\"],\"flex\":[\"sykepengesoknad\",\"spinnsyn-frontend\"]}"
    }

    @Test
    fun `Henter data som annet team`() {
        val feedbackInn =
            mapOf(
                "feedback" to "hade",
                "app" to "spinnsyn-frontend",
                "feedbackId" to "spinnsyn refusjon",
                "indre" to
                    mapOf(
                        "hei" to 5,
                    ),
            ).serialisertTilString()

        feedbackRepository.save(
            FeedbackDbRecord(
                opprettet = OffsetDateTime.now(),
                feedbackJson = feedbackInn,
                team = "team_annet",
                app = "test app",
            ),
        )

        feedbackRepository.save(
            FeedbackDbRecord(
                opprettet = OffsetDateTime.now(),
                feedbackJson = feedbackInn,
                team = "flex",
                app = "other app",
            ),
        )

        val result =
            mockMvc.perform(
                get("/api/v1/intern/feedback?team=team_annet")
                    .header("Authorization", "Bearer ${skapAzureJwt()}"),
            ).andExpect(status().isOk).tilFeedbackPage()

        result.content shouldHaveSize 1
        result.content[0].team shouldBeEqualTo "team_annet"
    }

    @Test
    fun `400 blir returnert ved ugyldig feedback`() {
        val feilmeldingDto =
            mapOf(
                "blah" to "hade",
                "apklp" to "spinnsyn-frontend",
                "feedbackId" to "spinnsyn refusjon",
                "indre" to
                    mapOf(
                        "hei" to 5,
                    ),
            )

        val serialisertTilString = feilmeldingDto.serialisertTilString()

        mockMvc.perform(
            post("/api/v1/feedback")
                .header("Authorization", "Bearer ${tokenxToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialisertTilString),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `404 når feedback ikke finnes`() {
        mockMvc.perform(
            delete("/api/v1/intern/feedback/12345")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `Kan lagre og hente tags`() {
        val feedbackInn =
            mapOf(
                "feedback" to "hade",
                "app" to "spinnsyn-frontend",
                "feedbackId" to "spinnsyn refusjon",
                "indre" to
                    mapOf(
                        "hei" to 5,
                    ),
            )

        val serialisertTilString = feedbackInn.serialisertTilString()

        // Tags først
        mockMvc.perform(
            get("/api/v1/intern/feedback/tags")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isOk).andReturn().response.contentAsString shouldBeEqualTo "[]"

        mockMvc.perform(
            post("/api/v1/feedback")
                .header("Authorization", "Bearer ${tokenxToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialisertTilString),
        ).andExpect(status().isAccepted)

        val response =
            mockMvc.perform(
                get("/api/v1/intern/feedback")
                    .header("Authorization", "Bearer ${skapAzureJwt()}"),
            ).andExpect(status().isOk).andReturn().response.contentAsString

        val deserialsert: FeedbackPage = objectMapper.readValue(response)
        deserialsert.content shouldHaveSize 1
        val first = deserialsert.content.first()
        first.feedback shouldBeEqualTo feedbackInn
        first.tags.shouldHaveSize(0)

        mockMvc.perform(
            post("/api/v1/intern/feedback/${first.id}/tags")
                .header("Authorization", "Bearer ${skapAzureJwt()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TagDto("yrkesskade").serialisertTilString()),
        ).andExpect(status().isCreated)
        mockMvc.perform(
            post("/api/v1/intern/feedback/${first.id}/tags")
                .header("Authorization", "Bearer ${skapAzureJwt()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TagDto("stjerne").serialisertTilString()),
        ).andExpect(status().isCreated)

        mockMvc.perform(
            get("/api/v1/intern/feedback?fritekst=yrkesskade heihei&stjerne=true")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isOk)

        val responseNy =
            mockMvc.perform(
                get("/api/v1/intern/feedback?fritekst=yrkesskade&stjerne=true")
                    .header("Authorization", "Bearer ${skapAzureJwt()}"),
            ).andExpect(status().isOk).andReturn().response.contentAsString

        val deserialserNy: FeedbackPage = objectMapper.readValue(responseNy)
        deserialserNy.content shouldHaveSize 1
        val oppdatert = deserialserNy.content.first()
        oppdatert.tags.shouldBeEqualTo(setOf("stjerne", "yrkesskade"))

        // Tags etterpå
        mockMvc.perform(
            get("/api/v1/intern/feedback/tags")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isOk).andReturn().response.contentAsString shouldBeEqualTo "[\"yrkesskade\",\"stjerne\"]"

        // Slett tag
        mockMvc.perform(
            delete("/api/v1/intern/feedback/${first.id}/tags?tag=yrkesskade")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isNoContent)

        // Tags etterpå
        mockMvc.perform(
            get("/api/v1/intern/feedback/tags")
                .header("Authorization", "Bearer ${skapAzureJwt()}"),
        ).andExpect(status().isOk).andReturn().response.contentAsString shouldBeEqualTo "[\"stjerne\"]"
    }
}
