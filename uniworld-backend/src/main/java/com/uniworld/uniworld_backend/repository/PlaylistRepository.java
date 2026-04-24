package com.uniworld.uniworld_backend.repository;

import com.uniworld.uniworld_backend.Playlist;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUserUserID(Long userID);
}
