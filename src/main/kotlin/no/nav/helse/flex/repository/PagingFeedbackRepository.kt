package no.nav.helse.flex.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import java.sql.ResultSet
import java.time.OffsetDateTime

@Service
class PagingFeedbackRepository(
    private val jdbcTemplate: JdbcTemplate

) {

    fun findPaginated(pageable: Pageable, team: String): Page<FeedbackDbRecord> {
        // Replace with your actual criteria and parameters
        // Replace with your actual criteria and parameters
        val whereClause = "WHERE team = ?"
        val criteria = arrayOf(team)

        val rowCountSql = "SELECT count(*) AS row_count FROM feedback $whereClause"
        val total = jdbcTemplate.queryForObject(
            rowCountSql,
            Int::class.java,
            *criteria
        )

        val query = (
            "SELECT * FROM feedback " +
                whereClause +
                " ORDER BY opprettet " +
                " LIMIT " + pageable.pageSize
            ) +
            " OFFSET " + pageable.offset

        val pageItems: List<FeedbackDbRecord> = jdbcTemplate.query(
            query,
            FeedbackDbRecordRowMapper(),
            *criteria
        )

        return PageImpl(pageItems, pageable, total.toLong())
    }
}

class FeedbackDbRecordRowMapper : RowMapper<FeedbackDbRecord> {
    override fun mapRow(rs: ResultSet, rowNum: Int): FeedbackDbRecord {
        return FeedbackDbRecord(
            id = rs.getString("id"),
            opprettet = rs.getObject("opprettet", OffsetDateTime::class.java),
            feedbackJson = rs.getString("feedback_json"),
            team = rs.getString("team"),
            app = rs.getString("app")
        )
    }
}
