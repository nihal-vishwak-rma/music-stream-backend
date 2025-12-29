package com.nihal.Music.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(nullable = false , unique = true)
    @NotBlank(message = "Song title is required")
    private String title;

    @NotBlank(message = "Artist name is required")
    @Column(nullable = false)
    private String artist;

    @Column(nullable = false, unique = true)
    private String videoId;

    @Column(nullable = false)
    private String duration;

    @Column
    private String thumbnailUrl;

    @ManyToMany(mappedBy = "songs")
    private Set<UserPlayList> playLists = new HashSet<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
