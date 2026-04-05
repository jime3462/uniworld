import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import type { SearchResultResponse } from '../interfaces/search-result';

@Injectable({
  providedIn: 'root',
})
export class SearchService {
  private readonly baseUrl = 'http://localhost:8080/api/search';

  constructor(private readonly http: HttpClient) {}

  search(keyword: string): Observable<SearchResultResponse> {
    const params = new HttpParams().set('keyword', keyword);
    return this.http.get<SearchResultResponse>(this.baseUrl, { params });
  }
}