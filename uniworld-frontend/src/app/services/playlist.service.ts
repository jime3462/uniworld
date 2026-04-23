import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import type { PlaylistRequest, PlaylistResponse } from '../interfaces/Playlist';

@Injectable({
  providedIn: 'root',
})
export class PlaylistService {
  private readonly baseUrl = 'http://localhost:8080/api/playlists';

  constructor(private readonly http: HttpClient) {}

  create(request: PlaylistRequest): Observable<PlaylistResponse> {
    return this.http.post<PlaylistResponse>(this.baseUrl, request);
  }

  getAll(): Observable<PlaylistResponse[]> {
    return this.http.get<PlaylistResponse[]>(this.baseUrl);
  }
}
