package com.uniworld.uniworld_backend.dto;

import java.util.List;

public record PlaylistResponse(
        Long playlistID,
        String name,
        Boolean isPublic,
        String coverImage,
        Long userID,
        List<Long> songIds,
        List<PlaylistSongDTO> songs
) {
}
