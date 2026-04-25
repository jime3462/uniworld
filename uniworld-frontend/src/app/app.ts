import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { LeftSb } from './components/left-sb/left-sb';
import { RightSb } from './components/right-sb/right-sb';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, LeftSb, RightSb],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  showLeftSidebar = true;

  constructor(private readonly router: Router) {
    this.updateLayoutVisibility(this.router.url);
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event) => {
        const navigationEnd = event as NavigationEnd;
        this.updateLayoutVisibility(navigationEnd.urlAfterRedirects);
      });
  }

  private updateLayoutVisibility(url: string): void {
    this.showLeftSidebar = !(url.startsWith('/signin') || url.startsWith('/signup'));
  }
}
