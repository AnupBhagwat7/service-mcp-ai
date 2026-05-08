package com.myjavablog.servicemcpai.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiCategoryListResponse(
        @JsonProperty(required = true) List<AiCategoryResponse> categories
) {
}