package com.secke.media_service.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.secke.media_service.Model.Media;
import com.secke.media_service.Repository.MediaRepository;
import com.secke.media_service.Service.MediaService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.core.io.UrlResource;
import java.nio.file.Paths;
import java.nio.file.Path;



@RestController
@RequestMapping("/media")
public class MediaController {

    // @Autowired
    // private MediaService mediaService;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private MediaService mediaService;

    // @PostMapping("/upload")
    // public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
    //                                      @RequestParam("productId") String productId) {
        

    //     try {
    //         Media media = mediaService.uploadImage(file, productId);

    //         // Success
    //         Map<String, String> response = new HashMap<>();
    //         response.put("message", "Upload successful");
    //         response.put("mediaId", media.getId());

    //         return ResponseEntity.status(HttpStatus.CREATED).body(response);
    //     } catch (IOException e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("Error: " + e.getMessage());
    //     }
    // }
    
    @GetMapping
    public List<Media> getAllMedia() {
        return mediaRepository.findAll();
    }

    @GetMapping("/images")
    public ResponseEntity<List<Media>> getImagesByProductId(@RequestParam String productId) {
        List<Media> images = mediaService.findMediasByProductId(productId);
        System.out.println("Retrieved images for productId " + productId + ": " + images);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Media>> getMedia(@PathVariable String id) {
        List<Media> media = mediaRepository.findByProductId(id);
        if (media == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(media);
    }

    // @DeleteMapping("/{id}")
    // public ResponseEntity<?> deleteMedia(@PathVariable String id) {
    //     List<Media> media = mediaRepository.findByProductId(id);
    //     if (media == null) {
    //         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    //     }
    //     mediaRepository.deleteById(media);
    //     return ResponseEntity.ok().build();
    // }

    @DeleteMapping
    public ResponseEntity<?> deleteAllMedia() {
        mediaRepository.deleteAll();
        return ResponseEntity.ok().build();
    }

    // retrieve the image by his name to render it onto the angul templates
    @GetMapping("/resource/{filename}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            // Assuming images are stored in the static/uploads directory
            Path imagePath = Paths.get("src/main/resources/static/uploads/" + filename);
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
