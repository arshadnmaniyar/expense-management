package org.example.expensecommand.config;

import org.example.expensecommand.logging.RequestCorrelationFilter;
import org.example.expensecommand.logging.RequestResponseLoggingFilter;
import org.example.expensecommand.domain.requestResponseLog.RequestResponseLogRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public RequestCorrelationFilter requestCorrelationFilter() {
        return new RequestCorrelationFilter();
    }

    @Bean
    public RequestResponseLoggingFilter requestResponseLoggingFilter(RequestResponseLogRepository repo) {
        return new RequestResponseLoggingFilter(repo);
    }

    @Bean
    public FilterRegistrationBean<RequestCorrelationFilter> correlationFilterRegistration(RequestCorrelationFilter filter) {
        FilterRegistrationBean<RequestCorrelationFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(-1); // run FIRST - sets MDC traceId and requestId
        reg.addUrlPatterns("/*");
        return reg;
    }

    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilterRegistration(RequestResponseLoggingFilter filter) {
        FilterRegistrationBean<RequestResponseLoggingFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(0); // run SECOND - reads MDC values and wraps request/response
        reg.addUrlPatterns("/*");
        return reg;
    }
}
