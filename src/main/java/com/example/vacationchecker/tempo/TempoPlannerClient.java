package com.example.vacationchecker.tempo;

import com.example.vacationchecker.config.CapacityPlannerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class TempoPlannerClient {

    private static final String DEFAULT_PLANS_PATH = "/4/plans";

    private final CapacityPlannerProperties properties;
    private final RestClient restClient;

    public TempoPlannerClient(CapacityPlannerProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        RestClient.Builder clientBuilder = builder;
        if (StringUtils.hasText(properties.baseUrl())) {
            clientBuilder = clientBuilder.baseUrl(properties.baseUrl());
        }
        this.restClient = clientBuilder.build();
    }

    public List<TempoPlan> fetchPlans(String assigneeKey, LocalDate startDate, LocalDate endDate) {
        if (!StringUtils.hasText(properties.baseUrl())) {
            return List.of();
        }
        TempoPlansResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(resolvePlansPath())
                        .queryParam("from", startDate)
                        .queryParam("to", endDate)
                        .queryParam("accountIds", assigneeKey)
                        .queryParamIfPresent("scheduleId", Optional.ofNullable(properties.scheduleId()).filter(StringUtils::hasText))
                        .build())
                .headers(headers -> applyAuthHeader(headers))
                .retrieve()
                .body(TempoPlansResponse.class);

        if (response == null) {
            return List.of();
        }
        return response.extractPlans();
    }

    private String resolvePlansPath() {
        return StringUtils.hasText(properties.plansPath()) ? properties.plansPath() : DEFAULT_PLANS_PATH;
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!StringUtils.hasText(properties.token())) {
            return;
        }
        headers.setBearerAuth(properties.token().trim());
    }
}
