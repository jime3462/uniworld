package com.uniworld.uniworld_backend.dto;

public record PlaylistSongAlbumDTO(
        Long albumID,
        String title,
        PlaylistSongArtistDTO artist,
        String genre,
        int releaseYear,
        String coverImage
) {}
