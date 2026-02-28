import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RightSb } from './components/right-sb/right-sb';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RightSb],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('uniworld-frontend');
}
