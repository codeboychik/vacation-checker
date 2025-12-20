package com.example.vacationchecker.slack;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlackResponse {

    @JsonProperty("response_type")
    private final String responseType;

    private final List<Map<String, Object>> blocks;

    public SlackResponse(String responseType, List<Map<String, Object>> blocks) {
        this.responseType = responseType;
        this.blocks = blocks;
    }

    public String getResponseType() {
        return responseType;
    }

    public List<Map<String, Object>> getBlocks() {
        return blocks;
    }
}
