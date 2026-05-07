package com.uniworld.uniworld_backend.dto;

import java.util.List;

public record PlaylistSongDTO(
        Long songID,
        String title,
        PlaylistSongAlbumDTO album,
        List<PlaylistSongArtistDTO> artists,
        String genre,
        String keyScale,
        int tempo,
        int duration,
        String audioFile
) {}
