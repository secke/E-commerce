package com.secke.user_service.Controller;


import com.secke.user_service.Model.*;
import com.secke.user_service.Service.JwtService;
import com.secke.user_service.Service.UserService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.validation.Valid;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserController {
    // Add a rate limiter
    private Bucket bucket;

    public UserController() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }
    @Autowired
    private UserService userServ;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    //    System.out.println("THE REQ DATA :" +request);
        try {
            User user = userServ.getByNameOrEmail(request.getName());

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    user.getName(),
                    request.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(user, userDetails);

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }
    }


    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<?> register(
        @ModelAttribute UserDTO userDTO, 
        @RequestParam(required = false) MultipartFile file
    ) throws IOException {

        System.out.println("register called with userDTO: " + userDTO.getUsername());
        try {
            User user = new User(
                userDTO.getUsername(), 
                userDTO.getEmail(), 
                userDTO.getPassword(), 
                userDTO.getRole(), 
                file.getOriginalFilename()
            );
            user = userServ.addUser(user);
            
            // Only upload avatar if file is not null
            if (file != null && !file.isEmpty()) {
                userServ.uploadAvatar(file, user.getId());
            }

            String token = jwtService.generateToken(user, new UserPrincipal(user));
    
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    

    @PreAuthorize("hasAuthority('SELLER')")
    @GetMapping
    public ResponseEntity<List<UserCustomize>> getAllUsers() {
        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok(userServ.getAllUsers());
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<UserCustomize> createUser(@Valid @RequestBody User user) {
        if (bucket.tryConsume(1)) {
            User createdUser = userServ.addUser(user);
            return new ResponseEntity<>(new UserCustomize(createdUser), HttpStatus.CREATED);
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    // @PreAuthorize("hasAuthority('ADMIN') or authentication.principal.getId() == #id")
    @GetMapping("/{id}")
    public ResponseEntity<UserCustomize> getUserById(@PathVariable String id) {
            if (bucket.tryConsume(1)) {
                User user = userServ.getUserById(id);
                // we use the userCustomize to avoid to return the sensitives data in the resp like the password
                UserCustomize userCustomize  = new UserCustomize(user);
                return ResponseEntity.ok(userCustomize);
            }
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    // @PreAuthorize("authentication.principal.getId() == #id")
    @PatchMapping("/{id}")
    public ResponseEntity<AuthResponse> updateUser(
        @PathVariable String id,
        @Valid @RequestBody UserDTO username) {
    
        System.out.println("updateUser called with id: " + id + " and name: " + username.getUsername());

        if (bucket.tryConsume(1)) {
            User user = userServ.updateUsername(id, username.getUsername());
    
            String token = jwtService.generateToken(user, new UserPrincipal(user));
    
            return ResponseEntity.ok(new AuthResponse(token));
        }
    
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    // @PreAuthorize("hasAuthority('ADMIN') or authentication.principal.getId() == #id")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        userServ.deleteUser(id);

        return ResponseEntity.ok("deleted");
    }

    @GetMapping("/me")
    @PostAuthorize("returnObject.body != null and returnObject.body.name == authentication.principal.user.getName()")
    public ResponseEntity<UserCustomize> getCurrentUser(@AuthenticationPrincipal UserPrincipal user) {
        if (bucket.tryConsume(1)) {
            UserCustomize userCustomize = new UserCustomize(user.user);
            return ResponseEntity.ok(userCustomize);
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    // The user externals resources

    @GetMapping("get")
    public ResponseEntity<List<Product>> getUserProducts(@AuthenticationPrincipal UserPrincipal user) {
        List<Product> myProducts = userServ.getUserProducts(user.getId());
        return ResponseEntity.ok(myProducts);
    }

    @GetMapping("/Avatars/{filename}")
    public ResponseEntity<UrlResource> getImage(@PathVariable String filename) {
        try {
            Path file = Paths.get(System.getProperty("user.dir"), "Avatars").resolve(filename);
            UrlResource resource = new UrlResource(file.toUri());
    
            if (resource.exists() || resource.isReadable()) {
                String mimeType = Files.probeContentType(file);
                if (mimeType == null) {
                    mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }
    
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(mimeType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    

}
