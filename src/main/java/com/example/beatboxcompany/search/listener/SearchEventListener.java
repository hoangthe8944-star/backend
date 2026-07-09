package com.example.beatboxcompany.search.listener;

import com.example.beatboxcompany.search.entity.SearchIndex;
import com.example.beatboxcompany.search.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchEventListener {

    private final SearchRepository searchRepository;

    // Trong thực tế, đây sẽ là @KafkaListener hoặc @RabbitListener
    public void handleEntityCreated(SearchIndex searchIndex) {
        searchRepository.save(searchIndex);
    }

    public void handleEntityUpdated(SearchIndex searchIndex) {
        searchRepository.save(searchIndex);
    }

    public void handleEntityDeleted(String referenceId) {
        // Logic xóa index dựa trên referenceId
    }
}