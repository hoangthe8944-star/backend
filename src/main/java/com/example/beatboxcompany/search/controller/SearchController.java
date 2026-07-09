package com.example.beatboxcompany.search.controller;

import com.example.beatboxcompany.search.dto.SearchResponse;
import com.example.beatboxcompany.search.dto.SearchSuggestResponse;
import com.example.beatboxcompany.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> search(@RequestParam String q) {
        if (q == null || q.length() < 2) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(searchService.search(q));
    }

    @GetMapping("/suggest")
    public ResponseEntity<SearchSuggestResponse> suggest(@RequestParam String q) {
        if (q == null || q.length() < 2) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(searchService.suggest(q));
    }
}