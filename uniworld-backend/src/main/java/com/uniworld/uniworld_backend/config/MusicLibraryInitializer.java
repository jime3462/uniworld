package com.uniworld.uniworld_backend.config;

import com.uniworld.uniworld_backend.Album;
import com.uniworld.uniworld_backend.Artist;
import com.uniworld.uniworld_backend.Song;
import com.uniworld.uniworld_backend.repository.AlbumRepository;
import com.uniworld.uniworld_backend.repository.ArtistRepository;
import com.uniworld.uniworld_backend.repository.SongRepository;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MusicLibraryInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MusicLibraryInitializer.class);
    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".mp3", ".wav", ".ogg", ".m4a");
    private static final String DEFAULT_ARTIST_NAME = "Imported Artist";
    private static final String DEFAULT_ALBUM_TITLE = "Imported Album";
    private static final String DEFAULT_GENRE = "Unknown";
    private static final String DEFAULT_KEY = "Unknown";

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;

    @Value("${app.music.import-folder:../uniworld-frontend/src/app/assets/music}")
    private String importFolder;

    @Value("${app.music.cover-folder:../uniworld-frontend/src/app/assets/covers}")
    private String coverFolder;

    public MusicLibraryInitializer(
            SongRepository songRepository,
            ArtistRepository artistRepository,
            AlbumRepository albumRepository
    ) {
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
    }

    @Override
    public void run(String... args) {
        Path folderPath = resolveImportFolderPath();
        if (!Files.isDirectory(folderPath)) {
            logger.info("Music import skipped. Folder not found: {}", folderPath);
            return;
        }

        Path coverFolderPath = resolveCoverFolderPath();
        createDirectoryIfMissing(coverFolderPath);

        try (Stream<Path> files = Files.list(folderPath)) {
            files
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedAudioFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .forEach(path -> importOrUpdateSong(path, coverFolderPath));
        } catch (IOException exception) {
            logger.error("Failed importing songs from folder: {}", folderPath, exception);
        }
    }

    private void importOrUpdateSong(Path songPath, Path coverFolderPath) {
        String fileName = songPath.getFileName().toString();
        String audioFilePath = "/assets/music/" + fileName;

        AudioMetadata metadata = readAudioMetadata(songPath, coverFolderPath);
        Artist artist = findOrCreateArtist(metadata.artist());
        Album album = findOrCreateAlbum(metadata.album(), artist, metadata.genre(), metadata.coverImagePath());

        Song song = songRepository.findByAudioFileIgnoreCase(audioFilePath).orElseGet(Song::new);
        song.setAudioFile(audioFilePath);
        song.setTitle(metadata.title());
        song.setAlbum(album);
        song.setArtists(List.of(artist));
        song.setGenre(metadata.genre());
        song.setKeyScale(metadata.keyScale());
        song.setTempo(metadata.tempo());
        song.setDuration(metadata.duration());

        songRepository.save(song);
        logger.info("Imported/updated song metadata: {}", audioFilePath);
    }

    private AudioMetadata readAudioMetadata(Path songPath, Path coverFolderPath) {
        String fileName = songPath.getFileName().toString();
        String defaultTitle = buildSongTitle(fileName);

        try {
            String lowerCaseName = fileName.toLowerCase(Locale.ROOT);
            if (!lowerCaseName.endsWith(".mp3")) {
                return new AudioMetadata(defaultTitle, DEFAULT_ARTIST_NAME, DEFAULT_ALBUM_TITLE, DEFAULT_GENRE, DEFAULT_KEY, 0, 0, null);
            }

            Mp3File mp3File = new Mp3File(songPath.toString());
            String title = valueOrDefault(readTitle(mp3File), defaultTitle);
            String artist = valueOrDefault(readArtist(mp3File), DEFAULT_ARTIST_NAME);
            String album = valueOrDefault(readAlbum(mp3File), DEFAULT_ALBUM_TITLE);
            String genre = valueOrDefault(readGenre(mp3File), DEFAULT_GENRE);
            String keyScale = valueOrDefault(readKeyScale(mp3File), DEFAULT_KEY);
            int tempo = parsePositiveInt(readBpm(mp3File), 0);
            int duration = Math.max(0, (int) Math.round(mp3File.getLengthInSeconds()));
            String coverImagePath = extractCoverImage(songPath, mp3File, coverFolderPath);

            return new AudioMetadata(title, artist, album, genre, keyScale, tempo, duration, coverImagePath);
        } catch (IOException | UnsupportedTagException | InvalidDataException exception) {
            logger.warn("Could not parse metadata for {}. Using defaults.", fileName, exception);
            return new AudioMetadata(defaultTitle, DEFAULT_ARTIST_NAME, DEFAULT_ALBUM_TITLE, DEFAULT_GENRE, DEFAULT_KEY, 0, 0, null);
        }
    }

    private String extractCoverImage(Path songPath, Mp3File mp3File, Path coverFolderPath) {
        ID3v2 id3v2Tag = mp3File.getId3v2Tag();
        if (id3v2Tag == null) {
            return null;
        }

        byte[] albumImage = id3v2Tag.getAlbumImage();
        if (albumImage == null || albumImage.length == 0) {
            return null;
        }

        String extension = imageExtensionFromMimeType(id3v2Tag.getAlbumImageMimeType());
        String hashedName = sha1Hex(songPath.toAbsolutePath().toString());
        String fileName = hashedName + extension;
        Path outputPath = coverFolderPath.resolve(fileName);

        try {
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, albumImage);
            return "/assets/covers/" + fileName;
        } catch (IOException exception) {
            logger.warn("Could not extract artwork for {}", songPath.getFileName(), exception);
            return null;
        }
    }

    private Artist findOrCreateArtist(String artistName) {
        return artistRepository.findByNameIgnoreCase(artistName)
                .orElseGet(() -> {
                    Artist artist = new Artist();
                    artist.setName(artistName);
                    artist.setGenre(DEFAULT_GENRE);
                    artist.setImage(null);
                    return artistRepository.save(artist);
                });
    }

    private Album findOrCreateAlbum(String albumTitle, Artist artist, String genre, String coverImagePath) {
        Optional<Album> existingAlbum = albumRepository.findByTitleIgnoreCaseAndArtist(albumTitle, artist);
        if (existingAlbum.isPresent()) {
            Album album = existingAlbum.get();
            if ((album.getCoverImage() == null || album.getCoverImage().isBlank()) && coverImagePath != null) {
                album.setCoverImage(coverImagePath);
                return albumRepository.save(album);
            }
            return album;
        }

        Album album = new Album();
        album.setTitle(albumTitle);
        album.setArtist(artist);
        album.setGenre(genre);
        album.setReleaseYear(0);
        album.setCoverImage(coverImagePath);
        return albumRepository.save(album);
    }

    private String readTitle(Mp3File mp3File) {
        ID3v2 id3v2Tag = mp3File.getId3v2Tag();
        if (id3v2Tag != null && id3v2Tag.getTitle() != null && !id3v2Tag.getTitle().isBlank()) {
            return id3v2Tag.getTitle();
        }

        ID3v1 id3v1Tag = mp3File.getId3v1Tag();
        return id3v1Tag != null ? id3v1Tag.getTitle() : null;
    }

    private String readArtist(Mp3File mp3File) {
        ID3v2 id3v2Tag = mp3File.getId3v2Tag();
        if (id3v2Tag != null && id3v2Tag.getArtist() != null && !id3v2Tag.getArtist().isBlank()) {
            return id3v2Tag.getArtist();
        }

        ID3v1 id3v1Tag = mp3File.getId3v1Tag();
        return id3v1Tag != null ? id3v1Tag.getArtist() : null;
    }

    private String readAlbum(Mp3File mp3File) {
        ID3v2 id3v2Tag = mp3File.getId3v2Tag();
        if (id3v2Tag != null && id3v2Tag.getAlbum() != null && !id3v2Tag.getAlbum().isBlank()) {
            return id3v2Tag.getAlbum();
        }

        ID3v1 id3v1Tag = mp3File.getId3v1Tag();
        return id3v1Tag != null ? id3v1Tag.getAlbum() : null;
    }

    private String readGenre(Mp3File mp3File) {
        ID3v2 id3v2Tag = mp3File.getId3v2Tag();
        if (id3v2Tag != null && id3v2Tag.getGenreDescription() != null && !id3v2Tag.getGenreDescription().isBlank()) {
            return id3v2Tag.getGenreDescription();
        }

        ID3v1 id3v1Tag = mp3File.getId3v1Tag();
        return id3v1Tag != null ? id3v1Tag.getGenreDescription() : null;
    }

    private String readKeyScale(Mp3File mp3File) {
        ID3v2 id3v2Tag = mp3File.getId3v2Tag();
        if (id3v2Tag == null) {
            return null;
        }

        String key = id3v2Tag.getKey();
        return key != null && !key.isBlank() ? key : null;
    }

    private String readBpm(Mp3File mp3File) {
        ID3v2 id3v2Tag = mp3File.getId3v2Tag();
        if (id3v2Tag == null) {
            return null;
        }

        int bpm = id3v2Tag.getBPM();
        return bpm > 0 ? String.valueOf(bpm) : null;
    }

    private int parsePositiveInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        try {
            int parsed = Integer.parseInt(value.trim());
            return Math.max(0, parsed);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    private void createDirectoryIfMissing(Path directoryPath) {
        try {
            Files.createDirectories(directoryPath);
        } catch (IOException exception) {
            logger.warn("Could not create directory: {}", directoryPath, exception);
        }
    }

    private Path resolveCoverFolderPath() {
        Path configuredPath = Paths.get(coverFolder);
        if (configuredPath.isAbsolute()) {
            return configuredPath.normalize();
        }

        Path workingDirectory = Paths.get(System.getProperty("user.dir"));
        return workingDirectory.resolve(configuredPath).normalize();
    }

    private String imageExtensionFromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return ".jpg";
        }

        String normalized = mimeType.toLowerCase(Locale.ROOT);
        if (normalized.contains("png")) {
            return ".png";
        }

        if (normalized.contains("gif")) {
            return ".gif";
        }

        if (normalized.contains("webp")) {
            return ".webp";
        }

        return ".jpg";
    }

    private String sha1Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte hashByte : hashBytes) {
                builder.append(String.format("%02x", hashByte));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private Path resolveImportFolderPath() {
        Path configuredPath = Paths.get(importFolder);
        if (configuredPath.isAbsolute()) {
            return configuredPath.normalize();
        }

        Path workingDirectory = Paths.get(System.getProperty("user.dir"));
        return workingDirectory.resolve(configuredPath).normalize();
    }

    private boolean isSupportedAudioFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    private String buildSongTitle(String fileName) {
        int extensionDot = fileName.lastIndexOf('.');
        String nameWithoutExtension = extensionDot > 0 ? fileName.substring(0, extensionDot) : fileName;
        return nameWithoutExtension.replace('_', ' ').trim();
    }

    private record AudioMetadata(
            String title,
            String artist,
            String album,
            String genre,
            String keyScale,
            int tempo,
            int duration,
            String coverImagePath
    ) {
    }
}