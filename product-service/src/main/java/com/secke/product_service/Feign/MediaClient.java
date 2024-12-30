package com.secke.product_service.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secke.product_service.Model.Media;

import java.util.List;

@FeignClient(name = "MEDIA-SERVICE")
public interface MediaClient {
    
    @GetMapping("/media/images")
    List<Media> getImagesByProductId(@RequestParam("productId") String productId);
}
