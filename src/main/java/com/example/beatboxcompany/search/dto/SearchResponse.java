package com.example.beatboxcompany.search.dto;

import com.example.beatboxcompany.search.entity.SearchIndex;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponse {

    private List<SearchIndex> topResults;

    private List<SearchIndex> songs;

    private List<SearchIndex> artists;

    private List<SearchIndex> albums;

    private List<SearchIndex> playlists;

    private List<SearchIndex> users;
}