package no.nav.helse.flex.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface FeedbackRepository : CrudRepository<FeedbackDbRecord, String> {
    fun getAllByTeam(team: String): List<FeedbackDbRecord>
}

@Table("feedback")
data class FeedbackDbRecord(
    @Id
    val id: String? = null,
    val opprettet: OffsetDateTime,
    val feedbackJson: String,
    val team: String,
    val app: String? = null,
    val tags: String? = null
)
