import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import type { Playlist } from '../../interfaces/Playlist';
import type { Song } from '../../interfaces/Song';
import { PlaylistService } from '../../services/playlist.service';
import { SidebarPlayerService } from '../../services/sidebar-player.service';
import { SongService } from '../../services/song.service';

@Component({
  selector: 'app-playlist-page',
  imports: [CommonModule],
  templateUrl: './playlist.html',
  styleUrl: './playlist.scss',
})
export class PlaylistPage implements OnInit {
  loading = true;
  errorMessage = '';
  playlist: Playlist | null = null;
  songs: Song[] = [];
  readonly fallbackCoverImage =
    "data:image/svg+xml;utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='320' height='320' viewBox='0 0 320 320'%3E%3Crect width='320' height='320' fill='%231f1f1f'/%3E%3Ccircle cx='160' cy='160' r='88' fill='none' stroke='%23707070' stroke-width='16'/%3E%3Ccircle cx='160' cy='160' r='14' fill='%23707070'/%3E%3C/svg%3E";

  constructor(
    private readonly route: ActivatedRoute,
    private readonly playlistService: PlaylistService,
    private readonly songService: SongService,
    private readonly sidebarPlayerService: SidebarPlayerService,
  ) {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        switchMap((params) => {
          const playlistId = Number(params.get('id'));
          this.loading = true;
          this.errorMessage = '';
          this.playlist = null;
          this.songs = [];

          if (!Number.isInteger(playlistId) || playlistId <= 0) {
            this.errorMessage = 'Invalid playlist id.';
            this.loading = false;
            return of(null);
          }

          return this.playlistService.getById(playlistId);
        }),
      )
      .subscribe({
        next: (playlist) => {
          if (!playlist) {
            return;
          }

          this.playlist = playlist;
          const songIds = playlist.songIds ?? [];
          if (songIds.length === 0) {
            this.loading = false;
            return;
          }

          this.songService.getAll().subscribe({
            next: (allSongs) => {
              const songsById = new Map(allSongs.map((song) => [song.songID, song]));
              this.songs = songIds
                .map((songId) => songsById.get(songId))
                .filter((song): song is Song => song !== undefined);
              this.loading = false;
            },
            error: () => {
              this.errorMessage = 'Failed to load songs for this playlist.';
              this.loading = false;
            },
          });
        },
        error: () => {
          this.errorMessage = 'Failed to load playlist.';
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

  onMediaImageError(event: Event): void {
    const image = event.target as HTMLImageElement;
    image.src = this.fallbackCoverImage;
  }

  playSong(songIndex: number): void {
    if (this.songs.length === 0) {
      return;
    }

    this.sidebarPlayerService.setSearchQueue(this.songs, songIndex);
  }
}
