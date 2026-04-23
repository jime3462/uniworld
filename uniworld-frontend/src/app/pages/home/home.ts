import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Search } from '../../components/search/search';

@Component({
  selector: 'app-home',
  imports: [CommonModule, Search],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  readonly genres = [
    'Pop',
    'Hip-Hop/Rap',
    'R&B',
    'Reggaeton',
    'Country',
    'EDM',
    'Rock',
    'K-Pop',
    'Afrobeats',
    'Regional Mexican'
  ];

  constructor(private readonly router: Router) {}

  searchByGenre(genre: string): void {
    void this.router.navigate(['/search-result'], {
      queryParams: { keyword: genre }
    });
  }
}
