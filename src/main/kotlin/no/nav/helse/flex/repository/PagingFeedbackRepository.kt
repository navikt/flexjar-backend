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
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    fun findPaginated(
        pageInn: Int?,
        size: Int,
        team: String,
        medTekst: Boolean,
        app: String?,
        fritekst: List<String>,
        stjerne: Boolean,
        tags: List<String>,
    ): Triple<List<FeedbackDbRecord>, Long, Int> {
        val whereClause =
            "WHERE team = :team" +
                if (medTekst) {
                    " AND feedback_json::json->>'feedback' <> ''"
                } else {
                    ""
                } +
                if (stjerne) {
                    " AND tags like '%stjerne%'"
                } else {
                    ""
                } +

                if (tags.isNotEmpty()) {
                    tags
                        .mapIndexed {
                            index,
                            _,
                            ->
                            " AND tags like :tags$index"
                        }.joinToString(" ")
                } else {
                    ""
                } +
                if (app != null) {
                    " AND app = :app"
                } else {
                    ""
                } +
                if (fritekst.isNotEmpty()) {
                    fritekst
                        .mapIndexed {
                            index,
                            _,
                            ->
                            " AND (feedback_json ilike :fritekst$index OR tags ilike :fritekstTags$index )"
                        }.joinToString(" ")
                } else {
                    ""
                }

        val mapSqlParameterSource = MapSqlParameterSource()
        mapSqlParameterSource.addValue("team", team)

        fritekst.forEachIndexed { index, s ->
            mapSqlParameterSource.addValue("fritekst$index", "%$s%")
            mapSqlParameterSource.addValue("fritekstTags$index", "%$s%")
        }

        tags.forEachIndexed { index, s ->
            mapSqlParameterSource.addValue("tags$index", "%$s%")
        }

        if (app != null) {
            mapSqlParameterSource.addValue("app", app)
        }

        val rowCountSql = "SELECT count(*) AS row_count FROM feedback $whereClause"
        val total =
            jdbcTemplate.queryForObject(
                rowCountSql,
                mapSqlParameterSource,
                Int::class.java,
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

        val pageItems: List<FeedbackDbRecord> =
            jdbcTemplate.query(
                query,
                mapSqlParameterSource,
                FeedbackDbRecordRowMapper(),
            )

        return Triple(pageItems, total.toLong(), page)
    }
}

class FeedbackDbRecordRowMapper : RowMapper<FeedbackDbRecord> {
    override fun mapRow(
        rs: ResultSet,
        rowNum: Int,
    ): FeedbackDbRecord =
        FeedbackDbRecord(
            id = rs.getString("id"),
            opprettet = rs.getObject("opprettet", OffsetDateTime::class.java),
            feedbackJson = rs.getString("feedback_json"),
            team = rs.getString("team"),
            app = rs.getString("app"),
            tags = rs.getString("tags"),
        )
}
