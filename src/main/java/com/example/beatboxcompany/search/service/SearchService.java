package com.example.beatboxcompany.search.service;

import com.example.beatboxcompany.search.dto.SearchResponse;
import com.example.beatboxcompany.search.dto.SearchSuggestResponse;

public interface SearchService {
    SearchResponse search(String keyword);
    SearchSuggestResponse suggest(String keyword);
}