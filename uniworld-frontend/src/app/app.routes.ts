import { Routes } from '@angular/router';
import { Signup } from './pages/signup/signup';
import { Signin } from './pages/signin/signin';
import { Home } from './pages/home/home';
import { PlaylistPage } from './pages/playlist/playlist';
import { SearchResult } from './pages/search-result/search-result';
import { Profile } from './pages/profile/profile';

export const routes: Routes = [
  { path: '', redirectTo: '/signin', pathMatch: 'full' },
  { path: 'signup', component: Signup },
  { path: 'signin', component: Signin },
  { path: 'home', component: Home },
  { path: 'playlist/:id', component: PlaylistPage },
  { path: 'search-result', component: SearchResult },
  { path: 'profile', component: Profile }
];
