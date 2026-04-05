import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import type { Song } from '../../interfaces/Song';
import { SongService } from '../../services/song.service';
import { Player } from '../player/player';

@Component({
  selector: 'app-right-sb',
  imports: [CommonModule, Player],
  templateUrl: './right-sb.html',
  styleUrl: './right-sb.scss',
})
export class RightSb implements OnInit {
  songs: Song[] = [];
  songsLoading = false;
  songsError = '';

  constructor(private readonly songService: SongService) {}

  ngOnInit(): void {
    this.loadSongs();
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
