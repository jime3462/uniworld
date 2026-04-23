import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  imports: [CommonModule],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile implements OnInit {
  readonly profileImageUrl = 'assets/profile-placeholder.png';
  userName = 'User';
  userEmail = '';
  userRole = '';
  loading = true;
  errorMessage = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.authService.me().subscribe({
      next: (user) => {
        this.userName = user.name?.trim() || 'User';
        this.userEmail = user.email ?? '';
        this.userRole = user.role ?? '';
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load profile details.';
        this.loading = false;
      },
    });
  }

  goHome(): void {
    void this.router.navigate(['/home']);
  }
}
