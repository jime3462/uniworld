import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, throwError } from 'rxjs';

export interface AuthResponse {
  token: string | null;
  userID: number;
  name: string;
  email: string;
  role: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly baseUrl = 'http://localhost:8080/api/auth';
  private readonly tokenStorageKey = 'uniworld_token';
  private readonly roleStorageKey = 'uniworld_role';

  constructor(private readonly http: HttpClient) {}

  register(payload: {
    name: string;
    email: string;
    password: string;
  }): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/register`, payload)
      .pipe(tap((response) => this.storeAuthSession(response)));
  }

  login(payload: { identifier: string; password: string }): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, payload)
      .pipe(tap((response) => this.storeAuthSession(response)));
  }

  me(): Observable<AuthResponse> {
    if (!this.getToken()) {
      return throwError(() => new Error('No authentication token'));
    }

    return this.http.get<AuthResponse>(`${this.baseUrl}/me`);
  }

  logout(): void {
    localStorage.removeItem(this.tokenStorageKey);
    localStorage.removeItem(this.roleStorageKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenStorageKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  getRole(): string | null {
    const storedRole = localStorage.getItem(this.roleStorageKey);
    if (storedRole) {
      return storedRole;
    }

    const token = this.getToken();
    if (!token) {
      return null;
    }

    return this.extractRoleFromToken(token);
  }

  private storeAuthSession(response: AuthResponse): void {
    if (response.token) {
      localStorage.setItem(this.tokenStorageKey, response.token);
    }

    if (response.role) {
      localStorage.setItem(this.roleStorageKey, response.role.toUpperCase());
    }
  }

  private extractRoleFromToken(token: string): string | null {
    try {
      const payloadPart = token.split('.')[1];
      if (!payloadPart) {
        return null;
      }

      const normalizedPayload = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
      const decodedPayload = atob(normalizedPayload);
      const payload = JSON.parse(decodedPayload) as { role?: string };

      return payload.role ? payload.role.toUpperCase() : null;
    } catch {
      return null;
    }
  }
}
