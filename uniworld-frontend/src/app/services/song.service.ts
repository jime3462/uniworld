import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import type { Song } from '../interfaces/Song';

@Injectable({
  providedIn: 'root',
})
export class SongService {
  private readonly baseUrl = 'http://localhost:8080/api/songs';

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<Song[]> {
    return this.http.get<Song[]>(this.baseUrl);
  }

  getById(songId: number): Observable<Song> {
    return this.http.get<Song>(`${this.baseUrl}/${songId}`);
  }
}