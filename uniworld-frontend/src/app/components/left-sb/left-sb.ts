import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { PlaylistService } from '../../services/playlist.service';
import type { Playlist } from '../../interfaces/Playlist';

@Component({
  selector: 'app-left-sb',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './left-sb.html',
  styleUrl: './left-sb.scss',
})
export class LeftSb implements OnInit {
  showModal = false;
  playlistName = '';
  isSubmitting = false;
  errorMessage = '';
  playlists: Playlist[] = [];
  selectedPlaylist: Playlist | null = null;

  constructor(
    public readonly authService: AuthService,
    private readonly playlistService: PlaylistService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.loadPlaylists();
  }

  loadPlaylists(): void {
    this.playlistService.getAll().subscribe({
      next: (playlists) => {
        this.playlists = playlists;
        const selectedPlaylist = this.playlistService.getSelectedPlaylistSnapshot();
        if (selectedPlaylist) {
          this.selectedPlaylist = playlists.find((playlist) => playlist.playlistID === selectedPlaylist.playlistID) ?? null;
        }

        if (playlists.length > 0 && !this.selectedPlaylist) {
          this.selectedPlaylist = playlists[0];
        }

        if (this.selectedPlaylist) {
          this.playlistService.selectPlaylist(this.selectedPlaylist);
        }
      },
      error: () => {
        console.error('Failed to load playlists');
        this.playlists = [];
      },
    });
  }

  selectPlaylist(playlist: Playlist): void {
    this.selectedPlaylist = playlist;
    this.playlistService.selectPlaylist(playlist);
    void this.router.navigate(['/playlist', playlist.playlistID]);
  }

  openCreatePlaylistModal(): void {
    this.showModal = true;
    this.playlistName = '';
    this.errorMessage = '';
  }

  closeModal(): void {
    this.showModal = false;
    this.playlistName = '';
    this.errorMessage = '';
  }

  createPlaylist(): void {
    const trimmedName = this.playlistName.trim();
    if (!trimmedName) {
      this.errorMessage = 'Playlist name cannot be empty';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    this.playlistService.create({ name: trimmedName, isPublic: false }).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.closeModal();
        this.loadPlaylists();
      },
      error: () => {
        this.isSubmitting = false;
        this.errorMessage = 'Failed to create playlist. Please try again.';
      },
    });
  }
}
