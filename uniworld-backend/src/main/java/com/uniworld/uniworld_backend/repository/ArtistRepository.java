package com.uniworld.uniworld_backend.repository;

import com.uniworld.uniworld_backend.Artist;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
	Optional<Artist> findByNameIgnoreCase(String name);
}
