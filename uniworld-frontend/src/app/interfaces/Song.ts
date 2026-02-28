export interface Song {
  songID: number;
  title: string;
  artist: string;
  album: string;
  genre: string;
  keyScale: string;
  tempo: number; // Tempo in BPM
  duration: number; // Duration in seconds
  audioFile: string;
}