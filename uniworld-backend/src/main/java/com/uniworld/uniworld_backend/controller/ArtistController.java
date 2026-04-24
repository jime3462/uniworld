package com.uniworld.uniworld_backend.controller;

import com.uniworld.uniworld_backend.Artist;
import com.uniworld.uniworld_backend.repository.ArtistRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistRepository artistRepository;

    public ArtistController(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @GetMapping
    public List<Artist> getAll() {
        return artistRepository.findAll();
    }

    @GetMapping("/{id}")
    public Artist getById(@PathVariable Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Artist create(@RequestBody Artist artist) {
        artist.setArtistID(null);
        return artistRepository.save(artist);
    }

    @PutMapping("/{id}")
    public Artist update(@PathVariable Long id, @RequestBody Artist artist) {
        Artist existing = artistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found"));

        existing.setName(artist.getName());
        existing.setGenre(artist.getGenre());
        existing.setImage(artist.getImage());

        return artistRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!artistRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found");
        }
        artistRepository.deleteById(id);
    }
}
