import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import type { Artist } from '../../interfaces/Artist';
import type { Song } from '../../interfaces/Song';
import type { Album } from '../../interfaces/Album';
import { Search } from '../../components/search/search';
import { ArtistService } from '../../services/artist.service';
import { SidebarPlayerService } from '../../services/sidebar-player.service';

@Component({
  selector: 'app-artist-profile',
  imports: [CommonModule, Search],
  templateUrl: './artist-profile.html',
  styleUrl: './artist-profile.scss',
})
export class ArtistProfile implements OnInit {
  loading = true;
  errorMessage = '';
  artist: Artist | null = null;
  songs: Song[] = [];
  albums: Album[] = [];
  selectedSongIndex = -1;
  readonly fallbackImage = 'assets/logo/colored-logo.png';
  readonly fallbackCoverImage =
    "data:image/svg+xml;utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='320' height='320' viewBox='0 0 320 320'%3E%3Crect width='320' height='320' fill='%231f1f1f'/%3E%3Ccircle cx='160' cy='160' r='88' fill='none' stroke='%23707070' stroke-width='16'/%3E%3Ccircle cx='160' cy='160' r='14' fill='%23707070'/%3E%3C/svg%3E";

  constructor(
    private readonly route: ActivatedRoute,
    private readonly artistService: ArtistService,
    private readonly sidebarPlayerService: SidebarPlayerService,
  ) {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        switchMap((params) => {
          const artistId = Number(params.get('id'));
          this.loading = true;
          this.errorMessage = '';
          this.artist = null;
          this.songs = [];
          this.albums = [];

          if (!Number.isInteger(artistId) || artistId <= 0) {
            this.errorMessage = 'Invalid artist id.';
            this.loading = false;
            return of(null);
          }

          return this.artistService.getById(artistId).pipe(
            switchMap((artist) =>
              forkJoin({
                songs: this.artistService.getSongs(artistId).pipe(catchError(() => of([]))),
                albums: this.artistService.getAlbums(artistId).pipe(catchError(() => of([]))),
              }).pipe(
                map((result) => ({
                  artist,
                  songs: result.songs,
                  albums: result.albums,
                })),
              ),
            ),
          );
        }),
      )
      .subscribe({
        next: (result) => {
          if (!result) return;
          this.artist = result.artist;
          this.songs = result.songs;
          this.albums = result.albums;
          this.loading = false;
        },
        error: () => {
          this.errorMessage = 'Artist not found.';
          this.loading = false;
        },
      });
  }

  formatDuration(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.floor(seconds % 60).toString().padStart(2, '0');
    return `${minutes}:${remainingSeconds}`;
  }

  playSong(songIndex: number): void {
    if (this.songs.length === 0) return;
    this.selectedSongIndex = songIndex;
    this.sidebarPlayerService.setSearchQueue(this.songs, songIndex);
  }

  onArtistImageError(event: Event): void {
    (event.target as HTMLImageElement).src = this.fallbackImage;
  }

  onMediaImageError(event: Event): void {
    (event.target as HTMLImageElement).src = this.fallbackCoverImage;
  }
}
