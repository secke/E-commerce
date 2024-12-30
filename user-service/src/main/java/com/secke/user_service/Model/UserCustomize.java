package com.secke.user_service.Model;

public class UserCustomize {
//    private User user;
    private String id;
    private String name;
    private String email;
    private Role role;
    private String avatar;

    public UserCustomize(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.avatar = user.getAvatar();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }

    public String getAvatar() {  
        return avatar; 
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
