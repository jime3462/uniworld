package com.uniworld.uniworld_backend.dto;

import java.util.List;

public record SearchResultResponse(
        List<SongSearchResult> songs,
        List<ArtistSearchResult> artists,
        List<AlbumSearchResult> albums
) {
}