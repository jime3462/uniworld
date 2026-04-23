import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import type { Song } from '../interfaces/Song';

interface SidebarPlayerState {
  queue: Song[];
  startIndex: number;
}

@Injectable({
  providedIn: 'root',
})
export class SidebarPlayerService {
  private readonly stateSubject = new BehaviorSubject<SidebarPlayerState>({
    queue: [],
    startIndex: 0,
  });

  readonly state$ = this.stateSubject.asObservable();

  setSearchQueue(queue: Song[], startIndex = 0): void {
    if (queue.length === 0) {
      this.clearSearchQueue();
      return;
    }

    const boundedStartIndex = Math.max(0, Math.min(startIndex, queue.length - 1));
    this.stateSubject.next({ queue, startIndex: boundedStartIndex });
  }

  setSearchIndex(startIndex: number): void {
    const currentState = this.stateSubject.value;
    if (currentState.queue.length === 0) {
      return;
    }

    const boundedStartIndex = Math.max(0, Math.min(startIndex, currentState.queue.length - 1));
    this.stateSubject.next({
      queue: currentState.queue,
      startIndex: boundedStartIndex,
    });
  }

  clearSearchQueue(): void {
    this.stateSubject.next({ queue: [], startIndex: 0 });
  }
}