package com.uniworld.uniworld_backend.repository;

import com.uniworld.uniworld_backend.Album;
import com.uniworld.uniworld_backend.Artist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
	@Override
	@EntityGraph(attributePaths = {"artist"})
	List<Album> findAll();

	@Override
	@EntityGraph(attributePaths = {"artist"})
	Optional<Album> findById(Long id);

	Optional<Album> findByTitleIgnoreCaseAndArtist(String title, Artist artist);
}
