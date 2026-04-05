import type { Album } from './Album';
import type { Artist } from './Artist';

export interface Song {
  songID: number;
  title: string;
  album: Album;
  artists: Artist[];
  genre: string;
  keyScale: string;
  tempo: number; // Tempo in BPM
  duration: number; // Duration in seconds
  audioFile: string;
}