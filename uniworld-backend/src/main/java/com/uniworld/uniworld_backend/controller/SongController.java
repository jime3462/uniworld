package com.uniworld.uniworld_backend.controller;

import com.uniworld.uniworld_backend.Song;
import com.uniworld.uniworld_backend.repository.SongRepository;
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
@RequestMapping("/api/songs")
public class SongController {

    private final SongRepository songRepository;

    public SongController(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @GetMapping
    public List<Song> getAll() {
        return songRepository.findAll();
    }

    @GetMapping("/{id}")
    public Song getById(@PathVariable Long id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Song create(@RequestBody Song song) {
        song.setSongID(null);
        return songRepository.save(song);
    }

    @PutMapping("/{id}")
    public Song update(@PathVariable Long id, @RequestBody Song song) {
        Song existing = songRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));

        existing.setTitle(song.getTitle());
        existing.setArtists(song.getArtists());
        existing.setAlbum(song.getAlbum());
        existing.setGenre(song.getGenre());
        existing.setKeyScale(song.getKeyScale());
        existing.setTempo(song.getTempo());
        existing.setDuration(song.getDuration());
        existing.setAudioFile(song.getAudioFile());

        return songRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!songRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found");
        }
        songRepository.deleteById(id);
    }
}
