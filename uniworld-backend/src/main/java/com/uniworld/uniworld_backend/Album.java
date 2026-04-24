package com.uniworld.uniworld_backend;

import jakarta.persistence.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long albumID;
    
    private String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Artist artist;

    private String genre;
    private int releaseYear;
    private String coverImage;

    @OneToMany(mappedBy = "album")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Song> songs;

}
