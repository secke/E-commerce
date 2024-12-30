package com.secke.media_service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.secke.media_service.Model.Media;
import com.secke.media_service.Repository.MediaRepository;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

@Service
public class MediaService {
    @Autowired
    private MediaRepository mediaRepository;

    public Media uploadImage(MultipartFile file, String productId) throws IOException {
        if (file.getSize() > 2 * 1024 * 1024) { // 2MB limit
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds the 2MB limit");
        }

        // Validate file type if not an image
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Only image files are allowed.");
            }
            
            // Optionally, you can further check the file with ImageIO
            if (ImageIO.read(file.getInputStream()) == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is not a valid image.");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading the image file.");
        }

        // Définir le chemin
        String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
        Files.createDirectories(Paths.get(uploadDir));

        String filePath = uploadDir + file.getOriginalFilename();

        System.out.println("Saving file to: " + filePath);

        File destinationFile = new File(filePath);
        file.transferTo(destinationFile);

        // Sauvegarder dans MongoDB
        Media media = new Media();
        media.setImagePath(filePath);
        media.setProductId(productId);

        return mediaRepository.save(media);
    }

    public List<Media> findMediasByProductId(String productId) {
        try {
            return mediaRepository.findByProductId(productId);
        } catch (Exception e) {
            return Collections.emptyList(); // Return empty list if no images found
        }
    }

}

