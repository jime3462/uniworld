package com.uniworld.uniworld_backend.dto;

public record ArtistSearchResult(
        Long artistID,
        String name,
        String genre,
        String image
) {
}