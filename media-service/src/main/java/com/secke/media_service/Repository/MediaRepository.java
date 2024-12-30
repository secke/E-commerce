package com.secke.media_service.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.secke.media_service.Model.Media;


public interface MediaRepository extends MongoRepository<Media, String> {
    //ToDO: jsp test webhook
    List<Media> findByProductId(String productId);

}