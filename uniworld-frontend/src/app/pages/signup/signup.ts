import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-signup',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './signup.html',
  styleUrl: './signup.scss',
})
export class Signup {
  name = '';
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

    this.authService
      .register({ name: this.name, email: this.email, password: this.password })
      .subscribe({
        next: () => {
          this.isSubmitting = false;
          void this.router.navigate(['/home']);
        },
        error: (error) => {
          this.isSubmitting = false;
          const status = error?.status as number | undefined;
          const backendMessage =
            error?.error?.message ??
            error?.error?.detail ??
            error?.error?.error;

          if (status === 409) {
            this.errorMessage = 'Email is already in use. Try signing in or use another email.';
            return;
          }

          if (status === 400) {
            this.errorMessage = backendMessage ?? 'Please fill in all required fields.';
            return;
          }

          this.errorMessage = backendMessage ?? 'Sign up failed. Please try again.';
        },
      });
  }

}
