package com.uniworld.uniworld_backend.dto;

public record AlbumSearchResult(
        Long albumID,
        String title,
        String genre,
        int releaseYear,
        String coverImage,
        Long artistID,
        String artistName
) {
}