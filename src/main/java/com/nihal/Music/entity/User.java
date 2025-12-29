package com.nihal.Music.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nihal.Music.entity.role.AuthProviderType;
import com.nihal.Music.entity.role.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @NotBlank(message = "Name must be required")
    @Column(nullable = false)
    private String name;


    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Column(length = 255, nullable = true)
    private String password;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<UserPlayList> myPlaylists = new HashSet<>();

    @ManyToMany(mappedBy = "collaborators")
    @JsonIgnore
    private Set<UserPlayList> sharedPlaylist = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles role;

    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProviderType providerType;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private RefreshToken refreshToken;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
