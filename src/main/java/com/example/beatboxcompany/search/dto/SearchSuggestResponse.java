package com.example.beatboxcompany.search.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SearchSuggestResponse {
    private String keyword;
    private List<String> suggestions;
}