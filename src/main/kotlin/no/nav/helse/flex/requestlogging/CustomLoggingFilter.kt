package no.nav.helse.flex.requestlogging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class CustomLoggingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        filterChain.doFilter(request, response)

        val requestUri = request.requestURI
        if (requestUri.startsWith("/internal")) return
        val requestId = request.getHeader("X-Request-ID")
        val status = response.status

        logger.info("${request.method} $requestUri, X-Request-ID: $requestId, Response Status: $status")
    }
}
