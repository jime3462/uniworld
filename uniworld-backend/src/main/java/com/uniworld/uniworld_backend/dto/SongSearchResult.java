package com.uniworld.uniworld_backend.dto;

public record SongSearchResult(
        Long songID,
        String title,
        String genre,
        String keyScale,
        int tempo,
        int duration,
        String audioFile,
        Long albumID,
        String albumTitle,
        String coverImage,
        String artistNames
) {
}