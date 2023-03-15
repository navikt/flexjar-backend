package no.nav.helse.flex

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class IntegrationTest : FellesTestOppsett() {

    @Test
    fun postFeilmelding() {
        mockMvc.perform(MockMvcRequestBuilders.post("/syk/feilmeldinger/api/v1/feilmelding"))
            .andExpect(MockMvcResultMatchers.status().isAccepted)
    }
}
