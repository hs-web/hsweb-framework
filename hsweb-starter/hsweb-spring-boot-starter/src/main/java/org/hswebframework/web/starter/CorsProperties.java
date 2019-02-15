package org.hswebframework.web.starter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "hsweb.cors"/*, ignoreInvalidFields = true*/)
public class CorsProperties {

    @Getter
    @Setter
    private List<Map<String, CorsConfiguration>> configs;

    @Getter
    @Setter
    @ToString
    public static class CorsConfiguration {

        /**
         * Wildcard representing <em>all</em> origins, methods, or headers.
         */
        public static final String ALL = "*";

        private List<String> allowedOrigins;

        private List<String> allowedMethods;

        private List<String> allowedHeaders;

        private List<String> exposedHeaders;

        private Boolean allowCredentials;

        private Long maxAge;

        CorsConfiguration applyPermitDefaultValues() {
            if (this.allowedOrigins == null) {
                this.addAllowedOrigin();
            }
            if (this.allowedMethods == null) {
                this.setAllowedMethods(Arrays.asList(
                        HttpMethod.GET.name(), HttpMethod.HEAD.name(), HttpMethod.POST.name()));
            }
            if (this.allowedHeaders == null) {
                this.addAllowedHeader();
            }
            if (this.allowCredentials == null) {
                this.setAllowCredentials(true);
            }
            if (this.maxAge == null) {
                this.setMaxAge(1800L);
            }
            return this;
        }

        /**
         * Add an origin to allow.
         */
        void addAllowedOrigin() {
            if (this.allowedOrigins == null) {
                this.allowedOrigins = new ArrayList<>(4);
            }
            this.allowedOrigins.add(CorsConfiguration.ALL);
        }

        /**
         * Add an actual request header to allow.
         */
        void addAllowedHeader() {
            if (this.allowedHeaders == null) {
                this.allowedHeaders = new ArrayList<>(4);
            }
            this.allowedHeaders.add(CorsConfiguration.ALL);
        }
    }

}
