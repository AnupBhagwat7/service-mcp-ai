package com.myjavablog.servicemcpai.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiCategoryResponse(
        @JsonProperty(required = true) Long categoryId,
        @JsonProperty(required = true) String sourceDescription,
        @JsonProperty(required = true) String observation
) {
}