package com.secke.user_service.Service;

import com.secke.user_service.Error.ResourceNotFoundException;
import com.secke.user_service.Feign.UserInterface;
import com.secke.user_service.Model.Product;
import com.secke.user_service.Model.User;
import com.secke.user_service.Model.UserCustomize;
import com.secke.user_service.Repository.UserRepository;
import com.secke.user_service.kafka.Producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.imageio.ImageIO;

@Service
public class UserService {
    @Autowired
    UserRepository userRepo;

    @Autowired
    UserInterface userInterface;

    @Autowired
    Producer producer;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
//    @Autowired
//    PasswordEncoder passwordEncoder;

    public List<UserCustomize> getAllUsers() {
        return userRepo.findAll().stream().map(UserCustomize::new).toList();
//        try {
//        } catch (ForbiddenException fex) {
//            throw new ForbiddenException("Access denied for this user");
//        }
    }

    public User addUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }
        producer.sendUserRegistrationEvent(user);
        return  userRepo.save(user);
    }

    public User getUserById(String id) {
        return userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

    }
    public User getByName(String name) {
        return userRepo.findByName(name);
    }

    // public User getByEmail(String email) {
    //     return userRepo.findByEmail(email);
    // }

    public User getByNameOrEmail(String identifier) {
        return userRepo.findByNameOrEmail(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with name or email: " + identifier));
    }

    public void deleteUser(String id) {
        User user = userRepo.findById(id).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        } else {
            userRepo.deleteById(id);
        }
    }

    public User updateUser(String id, User userDetails) {
        User user = getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        user.setEmail(userDetails.getEmail());
        user.setName(userDetails.getName());
        user.setRole(userDetails.getRole());
        if (userDetails.getPassword() != null){
            user.setPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
        }
        return userRepo.save(user);
//        try {
//
//        }
    }

    public List<Product> getUserProducts(String userId) {
        return userInterface.getProductByUserId(userId).getBody();
    }

//    private boolean validUser(User user) {
//        if (user.getName() == null || user.getName().trim().isEmpty()
//                || product.getPrice() == null || product.getPrice() < 0
//        ){
//            return false;
//        }
//        return true;
//    }

    public User uploadAvatar(MultipartFile file, String userId) throws IOException {
        System.out.println("uploading avatar");
        if (file.getSize() > 2 * 1024 * 1024) { // Limite de 2MB
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds the 2MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Only image files are allowed.");
        }

        if (ImageIO.read(file.getInputStream()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is not a valid image.");
        }

        String uploadDir = System.getProperty("user.dir") + "/Avatars/";
        Files.createDirectories(Paths.get(uploadDir)); 

        String fileName = file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        File destinationFile = new File(filePath);
        file.transferTo(destinationFile);

        User user = getUserById(userId);

        user.setAvatar(fileName);
        return userRepo.save(user);
    }

    public User updateUsername(String id, String username) {
        User existingUser = userRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        existingUser.setName(username);

        return userRepo.save(existingUser);
    }
    
    // public User partialUpdate(String id, Map<String, Object> updates) {
    //     User existingUser = userRepo.findById(id)
    //         .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
    //     updates.forEach((key, value) -> {
    //         switch(key) {
    //             case "name":
    //                 String newName = (String) value;
    //                 if (newName == null || newName.trim().length() < 3) {
    //                     throw new IllegalArgumentException("Le nom doit contenir au moins 3 caractères");
    //                 }
    //                 existingUser.setName(newName.trim());
    //                 break;
    //             // others champs if g le temps
    //         }
    //     });
    
    //     return userRepo.save(existingUser);
    // }
}
