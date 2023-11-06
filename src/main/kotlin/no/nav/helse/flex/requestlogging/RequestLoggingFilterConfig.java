package no.nav.helse.flex.requestlogging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingFilterConfig {

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter
          = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(false);
        filter.setIncludePayload(false);
        filter.setIncludeHeaders(true);
        filter.setHeaderPredicate(header -> header.equalsIgnoreCase("x-request-id"));
        return filter;
    }
}