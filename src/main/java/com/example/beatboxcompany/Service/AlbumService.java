package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.AlbumDto;
import com.example.beatboxcompany.Request.AlbumRequest;
import java.util.List;

public interface AlbumService {
// --- CHO ARTIST ---
    AlbumDto createPendingAlbum(AlbumRequest request, String currentUserId);
    List<AlbumDto> getAlbumsByUploader(String currentUserId);

    // --- CHO ADMIN ---
    List<AlbumDto> getAlbumsByStatus(String status);
    List<AlbumDto> getAllAlbums(); // <--- BẮT BUỘC PHẢI CÓ
    AlbumDto approveAlbum(String albumId);
    AlbumDto rejectAlbum(String albumId, String reason);
    AlbumDto updateAlbumStatus(String albumId, String status, String reason);
    void importAlbumFromSpotify(String spotifyId);

    // --- CHO PUBLIC ---
    AlbumDto getPublishedAlbumById(String albumId);}