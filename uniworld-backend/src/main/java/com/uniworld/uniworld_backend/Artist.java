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
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artistID;
    
    private String name;
    private String genre;
    private String image;

    @OneToMany(mappedBy = "artist")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Album> albums;

    @ManyToMany(mappedBy = "artists")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Song> songs;
}
