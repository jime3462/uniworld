package com.uniworld.uniworld_backend.repository;

import com.uniworld.uniworld_backend.Song;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {
	@Override
	@EntityGraph(attributePaths = {"album", "album.artist", "artists"})
	List<Song> findAll();

	@Override
	@EntityGraph(attributePaths = {"album", "album.artist", "artists"})
	Optional<Song> findById(Long id);

	boolean existsByAudioFileIgnoreCase(String audioFile);

	@EntityGraph(attributePaths = {"album", "album.artist", "artists"})
	Optional<Song> findByAudioFileIgnoreCase(String audioFile);

	@EntityGraph(attributePaths = {"album", "album.artist", "artists"})
	List<Song> findByArtists_ArtistID(Long artistId);
}
