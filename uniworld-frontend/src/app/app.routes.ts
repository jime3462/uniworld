import { Routes } from '@angular/router';
import { Signup } from './pages/signup/signup';
import { Signin } from './pages/signin/signin';
import { Home } from './pages/home/home';
import { SearchResult } from './pages/search-result/search-result';

export const routes: Routes = [
  { path: '', redirectTo: '/signup', pathMatch: 'full' },
  { path: 'signup', component: Signup },
  { path: 'signin', component: Signin },
  { path: 'home', component: Home },
  { path: 'search-result', component: SearchResult }
];
