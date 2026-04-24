package com.uniworld.uniworld_backend.controller;

import com.uniworld.uniworld_backend.Album;
import com.uniworld.uniworld_backend.repository.AlbumRepository;
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
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumRepository albumRepository;

    public AlbumController(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    @GetMapping
    public List<Album> getAll() {
        return albumRepository.findAll();
    }

    @GetMapping("/{id}")
    public Album getById(@PathVariable Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Album create(@RequestBody Album album) {
        album.setAlbumID(null);
        return albumRepository.save(album);
    }

    @PutMapping("/{id}")
    public Album update(@PathVariable Long id, @RequestBody Album album) {
        Album existing = albumRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));

        existing.setTitle(album.getTitle());
        existing.setArtist(album.getArtist());
        existing.setGenre(album.getGenre());
        existing.setReleaseYear(album.getReleaseYear());
        existing.setCoverImage(album.getCoverImage());

        return albumRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!albumRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found");
        }
        albumRepository.deleteById(id);
    }
}
