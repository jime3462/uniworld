export interface SongSearchResult {
  songID: number;
  title: string;
  genre: string;
  keyScale: string;
  tempo: number;
  duration: number;
  audioFile: string;
  albumID: number | null;
  albumTitle: string | null;
  coverImage: string | null;
  artistNames: string;
}

export interface ArtistSearchResult {
  artistID: number;
  name: string;
  genre: string;
  image: string | null;
}

export interface AlbumSearchResult {
  albumID: number;
  title: string;
  genre: string;
  releaseYear: number;
  coverImage: string | null;
  artistID: number | null;
  artistName: string | null;
}

export interface SearchResultResponse {
  songs: SongSearchResult[];
  artists: ArtistSearchResult[];
  albums: AlbumSearchResult[];
}