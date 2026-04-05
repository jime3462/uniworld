import type { Playlist } from './Playlist';
import type { Song } from './Song';

export interface User {
  userID: number;
  name: string;
  email: string;
  role: string;
  currentSong?: Song | null;
  playlists?: Playlist[];
}