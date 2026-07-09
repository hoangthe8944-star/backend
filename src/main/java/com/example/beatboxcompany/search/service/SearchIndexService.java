package com.example.beatboxcompany.search.service;

import com.example.beatboxcompany.Entity.Album;
import com.example.beatboxcompany.Entity.Artist;
import com.example.beatboxcompany.Entity.Playlist;
import com.example.beatboxcompany.Entity.Song;
import com.example.beatboxcompany.Entity.User;

public interface SearchIndexService {

    /**
     * Index một bài hát
     */
    void indexSong(Song song);

    /**
     * Index một nghệ sĩ
     */
    void indexArtist(Artist artist);

    /**
     * Index một album
     */
    void indexAlbum(Album album);

    /**
     * Index một playlist
     */
    void indexPlaylist(Playlist playlist);

    /**
     * Index một user
     */
    void indexUser(User user);

    /**
     * Xóa index theo referenceId
     */
    void remove(String referenceId);

    /**
     * Rebuild toàn bộ Search Index
     */
    void rebuildIndex();
}