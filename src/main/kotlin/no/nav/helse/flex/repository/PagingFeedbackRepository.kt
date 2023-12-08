package no.nav.helse.flex.repository

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.sql.ResultSet
import java.time.OffsetDateTime
import kotlin.math.ceil

@Service
class PagingFeedbackRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate

) {

    fun findPaginated(
        pageInn: Int?,
        size: Int,
        team: String,
        medTekst: Boolean,
        fritekst: String?,
        stjerne: Boolean
    ): Triple<List<FeedbackDbRecord>, Long, Int> {
        val whereClause = "WHERE team = :team" +
            if (medTekst) {
                " AND feedback_json::json->>'feedback' <> ''"
            } else {
                ""
            } +
            if (stjerne) {
                " AND tags like '%stjerne'"
            } else {
                ""
            } +
            if (fritekst != null) {
                " AND (feedback_json like :fritekst OR tags like :fritekstTags )"
            } else {
                ""
            }

        val mapSqlParameterSource = MapSqlParameterSource()
        mapSqlParameterSource.addValue("team", team)
        if (fritekst != null) {
            mapSqlParameterSource.addValue("fritekst", "%$fritekst%")
            mapSqlParameterSource.addValue("fritekstTags", "%$fritekst%")
        }

        val rowCountSql = "SELECT count(*) AS row_count FROM feedback $whereClause"
        val total = jdbcTemplate.queryForObject(
            rowCountSql,
            mapSqlParameterSource,
            Int::class.java
        ) ?: 0
        val totalPages = ceil(total.toDouble() / size).toInt()

        val page = pageInn ?: (totalPages - 1).coerceAtLeast(0)

        val offset = page.toBigInteger() * size.toBigInteger() // If your page number starts at 0
        val query =
            "SELECT * FROM feedback " +
                whereClause +
                " ORDER BY opprettet ASC" +
                " LIMIT " + size +
                " OFFSET " + offset

        val pageItems: List<FeedbackDbRecord> = jdbcTemplate.query(
            query,
            mapSqlParameterSource,
            FeedbackDbRecordRowMapper()
        )

        return Triple(pageItems, total.toLong(), page)
    }
}

class FeedbackDbRecordRowMapper : RowMapper<FeedbackDbRecord> {
    override fun mapRow(rs: ResultSet, rowNum: Int): FeedbackDbRecord {
        return FeedbackDbRecord(
            id = rs.getString("id"),
            opprettet = rs.getObject("opprettet", OffsetDateTime::class.java),
            feedbackJson = rs.getString("feedback_json"),
            team = rs.getString("team"),
            app = rs.getString("app"),
            tags = rs.getString("tags")
        )
    }
}
