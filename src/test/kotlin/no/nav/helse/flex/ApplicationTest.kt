package no.nav.helse.flex

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class ApplicationTest : FellesTestOppsett() {

    @Test
    fun sjekkReadiness() {
        mockMvc.perform(MockMvcRequestBuilders.get("/internal/health/readiness"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UP"))
    }

    @Test
    fun sjekkLiveness() {
        mockMvc.perform(MockMvcRequestBuilders.get("/internal/health/liveness"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UP"))
    }
}
