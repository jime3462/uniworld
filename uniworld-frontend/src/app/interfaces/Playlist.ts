import type { Song } from './Song';
import type { User } from './User';

export interface Playlist {
	playlistID: number;
	name: string;
	isPublic: boolean;
	coverImage: string | null;
	user: User;
	songs: Song[];
}

export interface PlaylistRequest {
	name: string;
	isPublic?: boolean;
	coverImage?: string | null;
	songIds?: number[];
}

export interface PlaylistResponse {
	playlistID: number;
	name: string;
	isPublic: boolean;
	coverImage: string | null;
	user: User;
	songs: Song[];
}
