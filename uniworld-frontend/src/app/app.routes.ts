import { Routes } from '@angular/router';
import { Signup } from './pages/signup/signup';
import { Signin } from './pages/signin/signin';
import { Home } from './pages/home/home';

export const routes: Routes = [
  { path: '', redirectTo: '/signup', pathMatch: 'full' },
  { path: 'signup', component: Signup },
  { path: 'signin', component: Signin },
  { path: 'home', component: Home }
];
