package com.example.beatboxcompany.search.repository;

import com.example.beatboxcompany.search.entity.SearchIndex;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface SearchRepository extends MongoRepository<SearchIndex, String> {

    @Query("""
    {
        "$or":[
            {"title":{"$regex":?0,"$options":"i"}},
            {"subtitle":{"$regex":?0,"$options":"i"}},
            {"description":{"$regex":?0,"$options":"i"}}
        ]
    }
    """)
    List<SearchIndex> findByKeyword(String keyword);

    void deleteByReferenceId(String referenceId);
}