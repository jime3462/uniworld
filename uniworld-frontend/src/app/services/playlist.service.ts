import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import type { Playlist, PlaylistRequest, PlaylistResponse } from '../interfaces/Playlist';

@Injectable({
  providedIn: 'root',
})
export class PlaylistService {
  private readonly baseUrl = 'http://localhost:8080/api/playlists';
  private readonly selectedPlaylistSubject = new BehaviorSubject<Playlist | null>(null);

  readonly selectedPlaylist$ = this.selectedPlaylistSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  create(request: PlaylistRequest): Observable<PlaylistResponse> {
    return this.http.post<PlaylistResponse>(this.baseUrl, request);
  }

  getAll(): Observable<Playlist[]> {
    return this.http.get<PlaylistResponse[]>(this.baseUrl).pipe(
      map((playlists) => playlists.map((playlist) => this.toPlaylistModel(playlist))),
    );
  }

  update(playlistId: number, request: PlaylistRequest): Observable<Playlist> {
    return this.http.put<PlaylistResponse>(`${this.baseUrl}/${playlistId}`, request).pipe(
      map((playlist) => this.toPlaylistModel(playlist)),
      tap((playlist) => this.syncSelectedPlaylist(playlist)),
    );
  }

  selectPlaylist(playlist: Playlist | null): void {
    this.selectedPlaylistSubject.next(playlist);
  }

  getSelectedPlaylistSnapshot(): Playlist | null {
    return this.selectedPlaylistSubject.value;
  }

  addSongToPlaylist(playlist: Playlist, songId: number): Observable<Playlist> {
    const existingSongIds = this.extractSongIds(playlist);
    if (existingSongIds.includes(songId)) {
      return of(playlist);
    }

    return this.update(playlist.playlistID, {
      name: playlist.name,
      isPublic: playlist.isPublic,
      coverImage: playlist.coverImage,
      songIds: [...existingSongIds, songId],
    });
  }

  private syncSelectedPlaylist(updated: Playlist): void {
    const current = this.selectedPlaylistSubject.value;
    if (!current || current.playlistID === updated.playlistID) {
      this.selectedPlaylistSubject.next(updated);
    }
  }

  private extractSongIds(playlist: Playlist): number[] {
    if (playlist.songIds && playlist.songIds.length > 0) {
      return [...playlist.songIds];
    }

    if (playlist.songs && playlist.songs.length > 0) {
      return playlist.songs.map((song) => song.songID);
    }

    return [];
  }

  private toPlaylistModel(playlist: PlaylistResponse): Playlist {
    return {
      playlistID: playlist.playlistID,
      name: playlist.name,
      isPublic: playlist.isPublic,
      coverImage: playlist.coverImage,
      userID: playlist.userID,
      songIds: playlist.songIds ?? [],
      user: playlist.user,
      songs: playlist.songs,
    };
  }
}
