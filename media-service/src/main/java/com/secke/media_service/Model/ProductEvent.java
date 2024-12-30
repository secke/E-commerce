package com.secke.media_service.Model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductEvent {
    private String id;
    // private String name;
    // private String description;
    // private double price;
    // private int quantity;
    // private String userId;
    private List<FileData> files; 


    @Getter @Setter
    public static class FileData {
        private String fileName;
        private String mimeType;
        private byte[] content;

        // Constructeurs, getters, setters...
    }

    @Override
    public String toString() {
        return "ProductEvent{" +
                "id='" + id + '\'' +
                ", files=" + files +
                '}';
    }
}
