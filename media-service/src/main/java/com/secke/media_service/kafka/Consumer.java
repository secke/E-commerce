package com.secke.media_service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
// import org.springframework.mock.web.MockMultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secke.media_service.Model.ProductEvent;
import com.secke.media_service.Service.MediaService;
import com.secke.media_service.Model.Media;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class Consumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MediaService mediaService;

    @KafkaListener(topics = "product-creation", groupId = "product-service-group")
    public void handleProductCreationEvent(String productJson) {
        try {
            // Désérialiser le JSON en objet ProductEvent
            ProductEvent product = objectMapper.readValue(productJson, ProductEvent.class);
            System.out.println("Received product creation event: " + product);

            // Traiter les fichiers reçus
            if (product.getFiles() != null && !product.getFiles().isEmpty()) {
                for (ProductEvent.FileData fileData : product.getFiles()) {
                    byte[] imageBytes = fileData.getContent();  // Obtenez le contenu du fichier
                    String fileName = fileData.getFileName(); // Obtenez le nom du fichier
                    String mimeType = fileData.getMimeType(); // Obtenez le type MIME

                    // Créer un MultipartFile à partir des données en byte[]
                    MultipartFile file = new MockMultipartFile("file", fileName, mimeType, new ByteArrayInputStream(imageBytes));

                    // Utilisez le productId pour associer l'image au produit
                    String productId = product.getId();

                    try {
                        // Appeler le service MediaService pour sauvegarder l'image
                        Media media = mediaService.uploadImage(file, productId);
                        System.out.println("Successfully uploaded media: " + media.getId());
                    } catch (IOException e) {
                        System.err.println("Error uploading image: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to deserialize product event: " + e.getMessage());
        }
    }
}
