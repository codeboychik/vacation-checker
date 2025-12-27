package com.example.vacationchecker.tempo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TempoPlansResponse(
        List<TempoPlan> plans,
        List<TempoPlan> results,
        @JsonAlias({"values", "items"})
        List<TempoPlan> values
) {

    public List<TempoPlan> extractPlans() {
        List<TempoPlan> collected = new ArrayList<>();
        if (plans != null) {
            collected.addAll(plans);
        }
        if (results != null) {
            collected.addAll(results);
        }
        if (values != null) {
            collected.addAll(values);
        }
        if (collected.isEmpty()) {
            return Collections.emptyList();
        }
        return List.copyOf(collected);
    }
}
