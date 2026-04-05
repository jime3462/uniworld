import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-signin',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './signin.html',
  styleUrl: './signin.scss',
})
export class Signin {
  email = '';
  password = '';
  isSubmitting = false;
  errorMessage = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  onSubmit(): void {
    this.errorMessage = '';
    this.isSubmitting = true;

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.isSubmitting = false;
        void this.router.navigate(['/home']);
      },
      error: (error) => {
        this.isSubmitting = false;
        const backendMessage =
          error?.error?.message ??
          error?.error?.detail ??
          error?.error?.error;
        this.errorMessage = backendMessage ?? 'Sign in failed. Please check your credentials.';
      },
    });
  }

}
