package com.uniworld.uniworld_backend.controller;

import com.uniworld.uniworld_backend.Album;
import com.uniworld.uniworld_backend.Artist;
import com.uniworld.uniworld_backend.Song;
import com.uniworld.uniworld_backend.dto.AlbumSearchResult;
import com.uniworld.uniworld_backend.dto.ArtistSearchResult;
import com.uniworld.uniworld_backend.dto.SearchResultResponse;
import com.uniworld.uniworld_backend.dto.SongSearchResult;
import com.uniworld.uniworld_backend.repository.AlbumRepository;
import com.uniworld.uniworld_backend.repository.ArtistRepository;
import com.uniworld.uniworld_backend.repository.SongRepository;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;

    public SearchController(
            SongRepository songRepository,
            ArtistRepository artistRepository,
            AlbumRepository albumRepository
    ) {
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
    }

    @GetMapping
    public SearchResultResponse search(@RequestParam String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Keyword is required");
        }

        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT).trim();

        List<SongSearchResult> songs = songRepository.findAll().stream()
                .filter(song -> matchesSong(song, normalizedKeyword))
                .map(this::toSongResult)
                .toList();

        List<ArtistSearchResult> artists = artistRepository.findAll().stream()
                .filter(artist -> matchesArtist(artist, normalizedKeyword))
                .map(this::toArtistResult)
                .toList();

        List<AlbumSearchResult> albums = albumRepository.findAll().stream()
                .filter(album -> matchesAlbum(album, normalizedKeyword))
                .map(this::toAlbumResult)
                .toList();

        return new SearchResultResponse(songs, artists, albums);
    }

    private boolean matchesSong(Song song, String keyword) {
        String albumTitle = song.getAlbum() != null ? song.getAlbum().getTitle() : "";
        String artistNames = song.getArtists() == null
                ? ""
                : song.getArtists().stream().map(Artist::getName).collect(Collectors.joining(" "));

        return contains(song.getTitle(), keyword)
                || contains(song.getGenre(), keyword)
                || contains(song.getKeyScale(), keyword)
                || contains(albumTitle, keyword)
                || contains(artistNames, keyword);
    }

    private boolean matchesArtist(Artist artist, String keyword) {
        return contains(artist.getName(), keyword)
                || contains(artist.getGenre(), keyword);
    }

    private boolean matchesAlbum(Album album, String keyword) {
        String artistName = album.getArtist() != null ? album.getArtist().getName() : "";
        return contains(album.getTitle(), keyword)
                || contains(album.getGenre(), keyword)
                || contains(artistName, keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private SongSearchResult toSongResult(Song song) {
        String artistNames = song.getArtists() == null
                ? ""
                : song.getArtists().stream().map(Artist::getName).filter(Objects::nonNull).collect(Collectors.joining(", "));

        return new SongSearchResult(
                song.getSongID(),
                song.getTitle(),
                song.getGenre(),
                song.getKeyScale(),
                song.getTempo(),
                song.getDuration(),
                song.getAudioFile(),
                song.getAlbum() != null ? song.getAlbum().getAlbumID() : null,
                song.getAlbum() != null ? song.getAlbum().getTitle() : null,
                song.getAlbum() != null ? song.getAlbum().getCoverImage() : null,
                artistNames
        );
    }

    private ArtistSearchResult toArtistResult(Artist artist) {
        return new ArtistSearchResult(
                artist.getArtistID(),
                artist.getName(),
                artist.getGenre(),
                artist.getImage()
        );
    }

    private AlbumSearchResult toAlbumResult(Album album) {
        return new AlbumSearchResult(
                album.getAlbumID(),
                album.getTitle(),
                album.getGenre(),
                album.getReleaseYear(),
                album.getCoverImage(),
                album.getArtist() != null ? album.getArtist().getArtistID() : null,
                album.getArtist() != null ? album.getArtist().getName() : null
        );
    }
}