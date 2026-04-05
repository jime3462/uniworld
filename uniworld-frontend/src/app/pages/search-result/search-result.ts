import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Player } from '../../components/player/player';
import type { Album } from '../../interfaces/Album';
import type { Artist } from '../../interfaces/Artist';
import type { Song } from '../../interfaces/Song';
import { SearchService } from '../../services/search.service';
import type { SearchResultResponse } from '../../interfaces/search-result';

@Component({
  selector: 'app-search-result',
  imports: [CommonModule, Player],
  templateUrl: './search-result.html',
  styleUrl: './search-result.scss',
})
export class SearchResult implements OnInit {
  keyword = '';
  loading = false;
  errorMessage = '';
  selectedSongIndex = 0;
  playerQueue: Song[] = [];
  readonly fallbackCoverImage =
    "data:image/svg+xml;utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='320' height='320' viewBox='0 0 320 320'%3E%3Crect width='320' height='320' fill='%231f1f1f'/%3E%3Ccircle cx='160' cy='160' r='88' fill='none' stroke='%23707070' stroke-width='16'/%3E%3Ccircle cx='160' cy='160' r='14' fill='%23707070'/%3E%3C/svg%3E";
  results: SearchResultResponse = {
    songs: [],
    artists: [],
    albums: [],
  };

  constructor(
    private readonly route: ActivatedRoute,
    private readonly searchService: SearchService,
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const keyword = params.get('keyword')?.trim() ?? '';
      this.keyword = keyword;
      this.selectedSongIndex = 0;
      if (!keyword) {
        this.results = { songs: [], artists: [], albums: [] };
        this.playerQueue = [];
        this.errorMessage = 'Enter a keyword to search.';
        return;
      }

      this.search(keyword);
    });
  }

  private search(keyword: string): void {
    this.loading = true;
    this.errorMessage = '';

    this.searchService.search(keyword).subscribe({
      next: (results) => {
        this.results = results;
        this.playerQueue = results.songs.map((song) => this.toPlayerSong(song));
        this.selectedSongIndex = 0;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Search failed. Please try again.';
        this.loading = false;
      },
    });
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

}
