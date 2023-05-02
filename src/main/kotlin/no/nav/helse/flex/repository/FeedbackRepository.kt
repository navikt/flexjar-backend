package no.nav.helse.flex.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface FeedbackRepository : CrudRepository<FeedbackDbRecord, String>

@Table("feedback")
data class FeedbackDbRecord(
    @Id
    val id: String? = null,
    val opprettet: OffsetDateTime,
    val feedbackJson: String
)
