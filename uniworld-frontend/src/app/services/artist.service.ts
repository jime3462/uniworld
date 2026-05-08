import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import type { Artist } from '../interfaces/Artist';
import type { Song } from '../interfaces/Song';
import type { Album } from '../interfaces/Album';

@Injectable({
  providedIn: 'root',
})
export class ArtistService {
  private readonly baseUrl = 'http://localhost:8080/api/artists';

  constructor(private readonly http: HttpClient) {}

  getById(artistId: number): Observable<Artist> {
    return this.http.get<Artist>(`${this.baseUrl}/${artistId}`);
  }

  getSongs(artistId: number): Observable<Song[]> {
    return this.http.get<Song[]>(`${this.baseUrl}/${artistId}/songs`);
  }

  getAlbums(artistId: number): Observable<Album[]> {
    return this.http.get<Album[]>(`${this.baseUrl}/${artistId}/albums`);
  }
}
