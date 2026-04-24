package com.uniworld.uniworld_backend.dto;

import java.util.List;

public record PlaylistRequest(
        String name,
        Boolean isPublic,
        String coverImage,
        List<Long> songIds
) {
}
