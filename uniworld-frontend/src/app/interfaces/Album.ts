import type { Artist } from './Artist';

export interface Album {
	albumID: number;
	title: string;
	artist: Artist;
	genre: string;
	releaseYear: number;
	coverImage: string;
}
