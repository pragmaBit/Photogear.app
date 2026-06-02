package app.photogear.model;

import java.time.LocalDateTime;

public class User {

    private String id;
    private String googleId;
    private String email;
    private String name;
    private String picture;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public String getId()                       { return id; }
    public void   setId(String id)             { this.id = id; }

    public String getGoogleId()                { return googleId; }
    public void   setGoogleId(String googleId) { this.googleId = googleId; }

    public String getEmail()                   { return email; }
    public void   setEmail(String email)       { this.email = email; }

    public String getName()                    { return name; }
    public void   setName(String name)         { this.name = name; }

    public String getPicture()                 { return picture; }
    public void   setPicture(String picture)   { this.picture = picture; }

    public LocalDateTime getCreatedAt()                        { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin()                        { return lastLogin; }
    public void          setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}
