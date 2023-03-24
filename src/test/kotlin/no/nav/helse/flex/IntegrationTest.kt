package no.nav.helse.flex

import org.amshove.kluent.shouldBeEqualTo
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
    private lateinit var feilmeldingRepository: FeilmeldingRepository

    @AfterEach
    fun slettFraDatabase() {
        feilmeldingRepository.deleteAll()
    }

    @Test
    fun `202 ACCEPTED blir returnert ved gyldig feilmelding`() {
        val feilmeldingDto = FeilmeldingDto(
            requestId = "uuid-1",
            app = FrontendApp.SPINNSYN_FRONTEND.navn,
            payload = "{\"foo\": \"foo\", \"bar\": \"bar\"}",
            method = "GET",
            responseCode = 200,
            contentLength = 215512
        )

        val serialisertTilString = feilmeldingDto.serialisertTilString()

        mockMvc.perform(
            post("/syk/feilmeldinger/api/v1/feilmelding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialisertTilString)
        ).andExpect(status().isAccepted)

        val lagredeFeilmeldigner = feilmeldingRepository.findAll()
        lagredeFeilmeldigner shouldHaveSize 1
        val lagretFeilmelding = lagredeFeilmeldigner.first()
        lagretFeilmelding.opprettet shouldBeLessOrEqualTo OffsetDateTime.now()
        lagretFeilmelding.requestId shouldBeEqualTo feilmeldingDto.requestId
        lagretFeilmelding.app shouldBeEqualTo feilmeldingDto.app
        lagretFeilmelding.payload shouldBeEqualTo feilmeldingDto.payload
        lagretFeilmelding.method shouldBeEqualTo feilmeldingDto.method
        lagretFeilmelding.responseCode shouldBeEqualTo feilmeldingDto.responseCode
        lagretFeilmelding.contentLength shouldBeEqualTo feilmeldingDto.contentLength
    }

    @Test
    fun `202 ACCEPTED blir returnert selv om appliasjon ikke i listen over tillatte applikasjoner`() {
        val feilmeldingDto = FeilmeldingDto(
            requestId = "uuid-1",
            app = "UKJENT",
            payload = "{\"foo\": \"foo\", \"bar\": \"bar\"}",
            method = "GET",
            responseCode = 200,
            contentLength = 215512
        )

        mockMvc.perform(
            post("/syk/feilmeldinger/api/v1/feilmelding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(feilmeldingDto.serialisertTilString())
        ).andExpect(status().isAccepted)

        feilmeldingRepository.findAll() shouldHaveSize 0
    }

    @Test
    fun `202 ACCEPTED blir returnert selv om JSON ikke stemmer med DTO`() {
        mockMvc.perform(
            post("/syk/feilmeldinger/api/v1/feilmelding")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .content("{\"foo\": \"foo\", \"bar\": \"bar\"}")
        ).andExpect(status().isAccepted)

        feilmeldingRepository.findAll() shouldHaveSize 0
    }

    @Test
    fun `202 ACCEPTED blir returnert selv om JSON er ugyldig`() {
        mockMvc.perform(
            post("/syk/feilmeldinger/api/v1/feilmelding")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .content("JSON")
        ).andExpect(status().isAccepted)

        feilmeldingRepository.findAll() shouldHaveSize 0
    }
}
