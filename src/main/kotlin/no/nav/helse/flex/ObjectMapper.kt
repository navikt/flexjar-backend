package no.nav.helse.flex

import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

val objectMapper: ObjectMapper =
    JsonMapper
        .builder()
        .addModule(kotlinModule())
        .build()

fun Any.serialisertTilString(): String = objectMapper.writeValueAsString(this)
