import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import type { Song } from '../../interfaces/Song';

@Component({
  selector: 'app-player',
  imports: [CommonModule],
  templateUrl: './player.html',
  styleUrl: './player.scss',
})
export class Player implements OnChanges, AfterViewInit {
  @Input() queue: Song[] = [];
  @Input() startIndex = 0;

  @ViewChild('audioPlayer') private audioPlayerRef?: ElementRef<HTMLAudioElement>;

  currentIndex = 0;
  isPlaying = false;
  isShuffleEnabled = false;
  repeatMode: 'off' | 'all' | 'one' = 'off';
  currentTimeSeconds = 0;
  durationSeconds = 0;
  readonly fallbackCoverImage =
    "data:image/svg+xml;utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='320' height='320' viewBox='0 0 320 320'%3E%3Crect width='320' height='320' fill='%231f1f1f'/%3E%3Ccircle cx='160' cy='160' r='88' fill='none' stroke='%23707070' stroke-width='16'/%3E%3Ccircle cx='160' cy='160' r='14' fill='%23707070'/%3E%3C/svg%3E";

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['queue'] || changes['startIndex']) {
      this.initializeIndex();
      this.loadCurrentTrack();
    }
  }

  ngAfterViewInit(): void {
    this.initializeIndex();
    this.loadCurrentTrack();
  }

  get currentSong(): Song | null {
    return this.queue.length > 0 ? this.queue[this.currentIndex] : null;
  }

  get currentArtists(): string {
    const song = this.currentSong;
    if (!song || !song.artists || song.artists.length === 0) {
      return 'Unknown artist';
    }

    return song.artists.map((artist) => artist.name).join(', ');
  }

  previousSong(): void {
    if (this.queue.length === 0) {
      return;
    }

    if (this.isShuffleEnabled && this.queue.length > 1) {
      this.currentIndex = this.getRandomIndexExcluding(this.currentIndex);
    } else {
      this.currentIndex =
        this.currentIndex === 0 ? this.queue.length - 1 : this.currentIndex - 1;
    }
    this.loadCurrentTrack(true);
  }

  nextSong(): void {
    if (this.queue.length === 0) {
      return;
    }

    if (this.isShuffleEnabled && this.queue.length > 1) {
      this.currentIndex = this.getRandomIndexExcluding(this.currentIndex);
    } else {
      this.currentIndex = (this.currentIndex + 1) % this.queue.length;
    }
    this.loadCurrentTrack(true);
  }

  togglePlayPause(): void {
    const audio = this.audioPlayerRef?.nativeElement;
    if (!audio || !this.currentSong) {
      return;
    }

    if (this.isPlaying) {
      audio.pause();
      this.isPlaying = false;
      return;
    }

    audio
      .play()
      .then(() => {
        this.isPlaying = true;
      })
      .catch(() => {
        this.isPlaying = false;
      });
  }

  onAudioEnded(): void {
    const audio = this.audioPlayerRef?.nativeElement;
    if (!audio || this.queue.length === 0) {
      this.isPlaying = false;
      return;
    }

    if (this.repeatMode === 'one') {
      audio.currentTime = 0;
      audio
        .play()
        .then(() => {
          this.isPlaying = true;
        })
        .catch(() => {
          this.isPlaying = false;
        });
      return;
    }

    const isLastSong = this.currentIndex === this.queue.length - 1;
    if (!this.isShuffleEnabled && this.repeatMode === 'off' && isLastSong) {
      this.isPlaying = false;
      this.currentTimeSeconds = this.durationSeconds;
      return;
    }

    this.nextSong();
  }

  onTimeUpdate(): void {
    const audio = this.audioPlayerRef?.nativeElement;
    if (!audio) {
      return;
    }

    this.currentTimeSeconds = audio.currentTime;
  }

  onLoadedMetadata(): void {
    const audio = this.audioPlayerRef?.nativeElement;
    if (!audio) {
      return;
    }

    this.durationSeconds = Number.isFinite(audio.duration) ? audio.duration : 0;
    this.currentTimeSeconds = Number.isFinite(audio.currentTime) ? audio.currentTime : 0;
  }

  onSeek(event: Event): void {
    const audio = this.audioPlayerRef?.nativeElement;
    if (!audio) {
      return;
    }

    const target = event.target as HTMLInputElement;
    const newTime = Number(target.value);
    if (!Number.isFinite(newTime)) {
      return;
    }

    audio.currentTime = newTime;
    this.currentTimeSeconds = newTime;
  }

  toggleShuffle(): void {
    this.isShuffleEnabled = !this.isShuffleEnabled;
  }

  cycleRepeatMode(): void {
    if (this.repeatMode === 'off') {
      this.repeatMode = 'all';
      return;
    }

    if (this.repeatMode === 'all') {
      this.repeatMode = 'one';
      return;
    }

    this.repeatMode = 'off';
  }

  get repeatButtonLabel(): string {
    if (this.repeatMode === 'all') {
      return 'Repeat: All';
    }

    if (this.repeatMode === 'one') {
      return 'Repeat: One';
    }

    return 'Repeat: Off';
  }

  get formattedCurrentTime(): string {
    return this.formatTime(this.currentTimeSeconds);
  }

  get formattedDuration(): string {
    return this.formatTime(this.durationSeconds);
  }

  get albumCoverImage(): string {
    const coverImage = this.currentSong?.album?.coverImage?.trim();
    return coverImage ? coverImage : this.fallbackCoverImage;
  }

  get shuffleButtonTitle(): string {
    return this.isShuffleEnabled ? 'Shuffle is on' : 'Shuffle is off';
  }

  get repeatButtonTitle(): string {
    if (this.repeatMode === 'all') {
      return 'Repeating the full queue';
    }

    if (this.repeatMode === 'one') {
      return 'Repeating the current song';
    }

    return 'Repeat is off';
  }

  onCoverImageError(event: Event): void {
    const image = event.target as HTMLImageElement;
    image.src = this.fallbackCoverImage;
  }

  private initializeIndex(): void {
    if (this.queue.length === 0) {
      this.currentIndex = 0;
      this.isPlaying = false;
      this.currentTimeSeconds = 0;
      this.durationSeconds = 0;
      return;
    }

    const boundedIndex = Math.max(0, Math.min(this.startIndex, this.queue.length - 1));
    this.currentIndex = boundedIndex;
  }

  private loadCurrentTrack(shouldAutoplay = false): void {
    const audio = this.audioPlayerRef?.nativeElement;
    const song = this.currentSong;

    if (!audio || !song) {
      this.isPlaying = false;
      this.currentTimeSeconds = 0;
      this.durationSeconds = 0;
      return;
    }

    audio.load();
    this.isPlaying = false;
    this.currentTimeSeconds = 0;
    this.durationSeconds = 0;

    if (shouldAutoplay) {
      audio
        .play()
        .then(() => {
          this.isPlaying = true;
        })
        .catch(() => {
          this.isPlaying = false;
        });
    }
  }

  private getRandomIndexExcluding(indexToAvoid: number): number {
    let nextIndex = indexToAvoid;
    while (nextIndex === indexToAvoid) {
      nextIndex = Math.floor(Math.random() * this.queue.length);
    }
    return nextIndex;
  }

  private formatTime(seconds: number): string {
    if (!Number.isFinite(seconds) || seconds < 0) {
      return '0:00';
    }

    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.floor(seconds % 60);
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  }

}
