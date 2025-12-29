package com.nihal.Music.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "user_playlists",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "name"})
        }
)
public class UserPlayList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @NotBlank(message = "Playlist name is required")
    @Column(nullable = false)
    @Size(min = 1, max = 15)
    private String name;


    @ManyToMany
    @JoinTable(name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    @JsonIgnore
    private Set<Song> songs = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;


    @ManyToMany
    @JoinTable(name = "playlist_collaborators",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> collaborators = new HashSet<>();

    private String code;

    private LocalDateTime expiryTime;


    public boolean isCodeValid(String givenCode) {
        return this.code != null
                && this.code.equals(givenCode)
                && this.expiryTime != null
                && LocalDateTime.now().isBefore(this.expiryTime);
    }


}
