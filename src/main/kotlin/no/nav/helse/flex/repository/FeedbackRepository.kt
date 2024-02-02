package no.nav.helse.flex.repository

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface FeedbackRepository : CrudRepository<FeedbackDbRecord, String> {
    @Query("SELECT DISTINCT f.tags FROM feedback f")
    fun finnAlleDistinctTags(): List<String?>

    @Query("SELECT DISTINCT f.app FROM feedback f")
    fun finnAlleDistinctApps(): List<String?>
}

@Table("feedback")
data class FeedbackDbRecord(
    @Id
    val id: String? = null,
    val opprettet: OffsetDateTime,
    val feedbackJson: String,
    val team: String,
    val app: String? = null,
    val tags: String? = null,
)
