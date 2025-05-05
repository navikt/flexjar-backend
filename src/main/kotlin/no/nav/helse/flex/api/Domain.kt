package no.nav.helse.flex.api

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.FeedbackDbRecord
import java.time.OffsetDateTime

fun FeedbackDbRecord.toDto(): FeedbackDto =
    FeedbackDto(
        feedback =
            objectMapper.readValue<HashMap<String, Any>>(this.feedbackJson).also {
                if (this.app != null) {
                    it["app"] = this.app
                }
            },
        opprettet = this.opprettet,
        id = this.id!!,
        team = this.team,
        app = this.app,
        tags = this.tags?.split(",")?.toSet() ?: emptySet(),
    )

data class FeedbackDto(
    val feedback: Map<String, Any>,
    val opprettet: OffsetDateTime,
    val id: String,
    val team: String,
    val app: String?,
    val tags: Set<String>,
)

data class FeedbackPage(
    val content: List<FeedbackDto>,
    val totalPages: Int,
    val totalElements: Int,
    val size: Int,
    val number: Int,
)

data class TagDto(
    val tag: String,
)
