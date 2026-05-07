package com.myjavablog.servicemcpai.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiCategoryResponse(
        @JsonProperty(required = true) Long id,
        @JsonProperty(required = true) String observation
) {
}