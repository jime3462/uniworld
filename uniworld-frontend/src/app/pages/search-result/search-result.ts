import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Search } from '../../components/search/search';
import { Subscription } from 'rxjs';
import type { Album } from '../../interfaces/Album';
import type { Artist } from '../../interfaces/Artist';
import type { Playlist } from '../../interfaces/Playlist';
import type { Song } from '../../interfaces/Song';
import { SearchService } from '../../services/search.service';
import { PlaylistService } from '../../services/playlist.service';
import { SongService } from '../../services/song.service';
import { SidebarPlayerService } from '../../services/sidebar-player.service';
import type { SearchResultResponse } from '../../interfaces/search-result';

@Component({
  selector: 'app-search-result',
  imports: [CommonModule, Search],
  templateUrl: './search-result.html',
  styleUrl: './search-result.scss',
})
export class SearchResult implements OnInit, OnDestroy {
  keyword = '';
  loading = false;
  errorMessage = '';
  playlistActionMessage = '';
  selectedSongIndex = 0;
  selectedPlaylist: Playlist | null = null;
  addingSongIds = new Set<number>();
  private playlistSelectionSubscription?: Subscription;
  readonly fallbackCoverImage =
    "data:image/svg+xml;utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='320' height='320' viewBox='0 0 320 320'%3E%3Crect width='320' height='320' fill='%231f1f1f'/%3E%3Ccircle cx='160' cy='160' r='88' fill='none' stroke='%23707070' stroke-width='16'/%3E%3Ccircle cx='160' cy='160' r='14' fill='%23707070'/%3E%3C/svg%3E";
  results: SearchResultResponse = {
    songs: [],
    artists: [],
    albums: [],
  };

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly searchService: SearchService,
    private readonly playlistService: PlaylistService,
    private readonly songService: SongService,
    private readonly sidebarPlayerService: SidebarPlayerService,
  ) {}

  ngOnInit(): void {
    this.playlistSelectionSubscription = this.playlistService.selectedPlaylist$.subscribe((playlist) => {
      this.selectedPlaylist = playlist;
    });

    this.route.queryParamMap.subscribe((params) => {
      const keyword = params.get('keyword')?.trim() ?? '';
      const genre = params.get('genre')?.trim() ?? '';
      this.keyword = keyword;
      this.selectedSongIndex = 0;
      this.playlistActionMessage = '';
      if (!keyword) {
        this.results = { songs: [], artists: [], albums: [] };
        this.errorMessage = 'Enter a keyword to search.';
        return;
      }

      if (genre) {
        this.searchByGenre(genre);
        return;
      }

      this.search(keyword);
    });
  }

  ngOnDestroy(): void {
    this.playlistSelectionSubscription?.unsubscribe();
  }

  private search(keyword: string): void {
    this.loading = true;
    this.errorMessage = '';

    this.searchService.search(keyword).subscribe({
      next: (results) => {
        this.results = results;
        const playerQueue = results.songs.map((song) => this.toPlayerSong(song));
        this.sidebarPlayerService.setSearchQueue(playerQueue, 0);
        this.selectedSongIndex = 0;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Search failed. Please try again.';
        this.loading = false;
      },
    });
  }

  private searchByGenre(genre: string): void {
    this.loading = true;
    this.errorMessage = '';

    this.songService.getAll().subscribe({
      next: (songs) => {
        const filteredSongs = songs.filter((song) => this.genreMatches(song.genre, genre));
        const songResults = filteredSongs.map((song) => this.toSongSearchResult(song));

        this.results = {
          songs: songResults,
          artists: [],
          albums: [],
        };

        this.sidebarPlayerService.setSearchQueue(filteredSongs, 0);
        this.selectedSongIndex = 0;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Search failed. Please try again.';
        this.loading = false;
      },
    });
  }

  private genreMatches(songGenre: string, selectedGenre: string): boolean {
    const normalizedSongGenre = this.normalizeGenreToken(songGenre);
    const normalizedSelectedGenre = this.normalizeGenreToken(selectedGenre);

    if (!normalizedSongGenre || !normalizedSelectedGenre) {
      return false;
    }

    const genreAliases: Record<string, string[]> = {
      pop: ['pop'],
      'hip-hop/rap': ['hip hop', 'hip-hop', 'hiphop', 'rap'],
      'r&b': ['r&b', 'rnb', 'rhythm and blues'],
      reggaeton: ['reggaeton', 'reggaeton latino'],
      country: ['country'],
      edm: ['edm', 'electronic', 'electro', 'house', 'techno', 'trance', 'dubstep', 'dance'],
      rock: ['rock', 'alternative rock', 'indie rock', 'punk rock', 'metal'],
      'k-pop': ['k-pop', 'k pop', 'kpop'],
      afrobeats: ['afrobeats', 'afrobeat', 'afro beats'],
      'regional mexican': ['regional mexican', 'mexican regional', 'corridos', 'norteno', 'banda', 'mariachi'],
    };

    const aliases = genreAliases[normalizedSelectedGenre] ?? [normalizedSelectedGenre];
    return aliases.some((alias) => normalizedSongGenre.includes(this.normalizeGenreToken(alias)));
  }

  private normalizeGenreToken(value: string): string {
    return (value || '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }

  formatDuration(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.floor(seconds % 60)
      .toString()
      .padStart(2, '0');
    return `${minutes}:${remainingSeconds}`;
  }

  selectSong(index: number): void {
    this.selectedSongIndex = index;
    this.sidebarPlayerService.setSearchIndex(index);
  }

  addSongToSelectedPlaylist(song: SearchResultResponse['songs'][number], event: Event): void {
    event.stopPropagation();

    if (!this.selectedPlaylist) {
      this.playlistActionMessage = 'Select a playlist in the left sidebar first.';
      return;
    }

    if (this.addingSongIds.has(song.songID)) {
      return;
    }

    this.addingSongIds.add(song.songID);
    this.playlistActionMessage = '';

    this.playlistService.addSongToPlaylist(this.selectedPlaylist, song.songID).subscribe({
      next: (updatedPlaylist) => {
        this.addingSongIds.delete(song.songID);
        this.selectedPlaylist = updatedPlaylist;
        this.playlistActionMessage = `Added "${song.title}" to ${updatedPlaylist.name}.`;
      },
      error: () => {
        this.addingSongIds.delete(song.songID);
        this.playlistActionMessage = 'Failed to add song to playlist. Please try again.';
      },
    });
  }

  isAddingSong(songId: number): boolean {
    return this.addingSongIds.has(songId);
  }

  navigateToArtist(artistId: number): void {
    void this.router.navigate(['/artist', artistId]);
  }

  onMediaImageError(event: Event): void {
    const image = event.target as HTMLImageElement;
    image.src = this.fallbackCoverImage;
  }

  private toPlayerSong(song: SearchResultResponse['songs'][number]): Song {
    const primaryArtistName = song.artistNames.split(',')[0]?.trim() || 'Unknown artist';
    const artist: Artist = {
      artistID: song.albumID ?? song.songID,
      name: primaryArtistName,
      genre: song.genre,
      image: song.coverImage || this.fallbackCoverImage,
    };

    const album: Album = {
      albumID: song.albumID ?? song.songID,
      title: song.albumTitle ?? song.title,
      artist,
      genre: song.genre,
      releaseYear: 0,
      coverImage: song.coverImage || this.fallbackCoverImage,
    };

    return {
      songID: song.songID,
      title: song.title,
      album,
      artists: [artist],
      genre: song.genre,
      keyScale: song.keyScale,
      tempo: song.tempo,
      duration: song.duration,
      audioFile: song.audioFile,
    };
  }

  private toSongSearchResult(song: Song): SearchResultResponse['songs'][number] {
    return {
      songID: song.songID,
      title: song.title,
      genre: song.genre,
      keyScale: song.keyScale,
      tempo: song.tempo,
      duration: song.duration,
      audioFile: song.audioFile,
      albumID: song.album?.albumID ?? null,
      albumTitle: song.album?.title ?? null,
      coverImage: song.album?.coverImage ?? null,
      artistNames: song.artists?.map((artist) => artist.name).join(', ') || 'Unknown artist',
    };
  }

}
