package com.uniworld.uniworld_backend.controller;

import com.uniworld.uniworld_backend.Playlist;
import com.uniworld.uniworld_backend.Song;
import com.uniworld.uniworld_backend.User;
import com.uniworld.uniworld_backend.dto.PlaylistRequest;
import com.uniworld.uniworld_backend.dto.PlaylistResponse;
import com.uniworld.uniworld_backend.repository.PlaylistRepository;
import com.uniworld.uniworld_backend.repository.SongRepository;
import com.uniworld.uniworld_backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    public PlaylistController(
            PlaylistRepository playlistRepository,
            UserRepository userRepository,
            SongRepository songRepository
    ) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.songRepository = songRepository;
    }

    @GetMapping
    public List<PlaylistResponse> getAll() {
        return playlistRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/mine")
    public List<PlaylistResponse> getMine(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return playlistRepository.findByUserUserID(user.getUserID()).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public PlaylistResponse getById(@PathVariable Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
        return toResponse(playlist);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistResponse create(@RequestBody PlaylistRequest request, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Playlist playlist = new Playlist();
        applyRequestToPlaylist(playlist, request, user);
        return toResponse(playlistRepository.save(playlist));
    }

    @PutMapping("/{id}")
    public PlaylistResponse update(
            @PathVariable Long id,
            @RequestBody PlaylistRequest request,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);
        Playlist existing = playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        if (!existing.getUser().getUserID().equals(user.getUserID())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own playlists");
        }

        applyRequestToPlaylist(existing, request, user);
        return toResponse(playlistRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Playlist existing = playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        if (!existing.getUser().getUserID().equals(user.getUserID())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own playlists");
        }

        playlistRepository.delete(existing);
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void applyRequestToPlaylist(Playlist playlist, PlaylistRequest request, User owner) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playlist name is required");
        }

        playlist.setName(request.name());
        playlist.setIsPublic(request.isPublic() != null ? request.isPublic() : Boolean.TRUE);
        playlist.setCoverImage(request.coverImage());
        playlist.setUser(owner);

        List<Long> songIds = request.songIds() == null ? List.of() : request.songIds();
        List<Song> songs = songIds.isEmpty() ? new ArrayList<>() : songRepository.findAllById(songIds);
        if (songs.size() != songIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more song IDs are invalid");
        }
        playlist.setSongs(songs);
    }

    private PlaylistResponse toResponse(Playlist playlist) {
        List<Long> songIds = playlist.getSongs() == null
                ? List.of()
                : playlist.getSongs().stream().map(Song::getSongID).toList();

        return new PlaylistResponse(
                playlist.getPlaylistID(),
                playlist.getName(),
                playlist.getIsPublic(),
                playlist.getCoverImage(),
                playlist.getUser().getUserID(),
                songIds
        );
    }
}
