import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-search',
  imports: [CommonModule, FormsModule],
  templateUrl: './search.html',
  styleUrl: './search.scss',
})
export class Search {
  keyword = '';

  constructor(private readonly router: Router) {}

  async submitSearch(): Promise<void> {
    const trimmedKeyword = this.keyword.trim();
    if (!trimmedKeyword) {
      return;
    }

    await this.router.navigate(['/search-result'], {
      queryParams: { keyword: trimmedKeyword },
    });
  }

}
