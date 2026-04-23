import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import type { Song } from '../../interfaces/Song';
import { AuthService } from '../../services/auth.service';
import { SongService } from '../../services/song.service';
import { SidebarPlayerService } from '../../services/sidebar-player.service';
import { Player } from '../player/player';

@Component({
  selector: 'app-right-sb',
  imports: [CommonModule, RouterLink, Player],
  templateUrl: './right-sb.html',
  styleUrl: './right-sb.scss',
})
export class RightSb implements OnInit, OnDestroy {
  songs: Song[] = [];
  searchQueue: Song[] = [];
  searchStartIndex = 0;
  songsLoading = false;
  songsError = '';
  userName = 'User';
  readonly profileImageUrl = 'assets/profile-placeholder.png';
  private playerStateSubscription?: Subscription;

  constructor(
    private readonly songService: SongService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly sidebarPlayerService: SidebarPlayerService,
  ) {}

  ngOnInit(): void {
    this.playerStateSubscription = this.sidebarPlayerService.state$.subscribe((state) => {
      this.searchQueue = state.queue;
      this.searchStartIndex = state.startIndex;
    });

    this.loadCurrentUser();
    this.loadSongs();
  }

  ngOnDestroy(): void {
    this.playerStateSubscription?.unsubscribe();
  }

  get hasSearchQueue(): boolean {
    return this.searchQueue.length > 0;
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/signin']);
  }

  private loadCurrentUser(): void {
    this.authService.me().subscribe({
      next: (user) => {
        this.userName = user.name?.trim() || 'User';
      },
      error: () => {
        this.userName = 'User';
      },
    });
  }

  private loadSongs(): void {
    this.songsLoading = true;
    this.songsError = '';

    this.songService.getAll().subscribe({
      next: (songs) => {
        this.songs = songs;
        this.songsLoading = false;
      },
      error: () => {
        this.songsError = 'Unable to load songs for the player.';
        this.songsLoading = false;
      },
    });
  }

}
