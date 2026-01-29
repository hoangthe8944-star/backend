package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Entity.Artist;
import com.example.beatboxcompany.Entity.Song; // Thêm import
import com.example.beatboxcompany.Entity.Album; // Thêm import
import com.example.beatboxcompany.Dto.ArtistDto;
import com.example.beatboxcompany.Dto.SongDto; // Thêm import
import com.example.beatboxcompany.Dto.AlbumDto; // Thêm import
import com.example.beatboxcompany.Mapper.ArtistMapper;
import com.example.beatboxcompany.Mapper.SongMapper; // Giả định bạn có Mapper này
import com.example.beatboxcompany.Mapper.AlbumMapper; // Giả định bạn có Mapper này
import com.example.beatboxcompany.Repository.ArtistRepository;
import com.example.beatboxcompany.Repository.SongRepository; // Thêm repository
import com.example.beatboxcompany.Repository.AlbumRepository; // Thêm repository
import com.example.beatboxcompany.Service.ArtistService;
import com.example.beatboxcompany.Service.SpotifyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final SpotifyService spotifyService;
    private final SongRepository songRepository; // Thêm mới
    private final AlbumRepository albumRepository;
    private final SongMapper songMapper;
    private final AlbumMapper albumMapper;

    public ArtistServiceImpl(ArtistRepository artistRepository, SpotifyService spotifyService,
            SongRepository songRepository, AlbumRepository albumRepository) {
        this.artistRepository = artistRepository;
        this.spotifyService = spotifyService;
        this.songRepository = songRepository; // Khởi tạo
        this.albumRepository = albumRepository; // Khởi tạo
        this.songMapper = new SongMapper(); // Khởi tạo
        this.albumMapper = new AlbumMapper(artistRepository, songRepository); // Khởi tạo
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ArtistDto createArtistByAdmin(String userId, String artistName) {
        if (artistRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Người dùng này đã có hồ sơ nghệ sĩ!");
        }

        Artist artist = new Artist();
        artist.setUserId(userId);
        artist.setName(artistName);
        artist.setCreatedAt(LocalDateTime.now());
        artist.setVerified(true);

        // Khởi tạo các mảng để tránh lỗi Null
        artist.setGenres(new ArrayList<>());
        artist.setImages(new ArrayList<>());

        Artist saved = artistRepository.save(artist);
        return ArtistMapper.toDto(saved);
    }

    // --- LOGIC ĐỒNG BỘ TOÀN DIỆN TỪ SPOTIFY (Ảnh, Thể loại, Follower) ---
    @Override
    public ArtistDto syncArtistFromSpotify(String artistId, String spotifyArtistId) {
        // 1. Tìm Artist trong DB của bạn (ví dụ cái bản ghi Ariana Grande đang trống
        // kia)
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Artist"));

        // 2. Gọi API lấy FULL profile từ Spotify
        // Lưu ý: spotifyArtistId của Ariana Grande là "66CVBvUOV8p6S0qqR20S6t"
        Map<String, Object> details = (Map<String, Object>) spotifyService.getArtistDetails(spotifyArtistId);

        if (details != null) {
            System.out.println("===> Đang đồng bộ dữ liệu cho: " + details.get("name"));

            // 3. Cập nhật Thể loại
            if (details.containsKey("genres")) {
                artist.setGenres((List<String>) details.get("genres"));
            }

            // 4. Cập nhật Hình ảnh (Đây là phần bạn đang bị empty)
            if (details.containsKey("images")) {
                List<Map<String, Object>> spotifyImages = (List<Map<String, Object>>) details.get("images");
                if (!spotifyImages.isEmpty()) {
                    List<String> imageUrls = spotifyImages.stream()
                            .map(img -> (String) img.get("url"))
                            .collect(Collectors.toList());

                    artist.setImages(imageUrls);
                    artist.setAvatarUrl(imageUrls.get(0)); // Lấy tấm to nhất làm đại diện
                    artist.setCoverImageUrl(imageUrls.get(0));
                }
            }

            // 5. Cập nhật Follower
            if (details.containsKey("followers")) {
                Map<String, Object> followers = (Map<String, Object>) details.get("followers");
                artist.setFollowerCount(Long.valueOf(followers.get("total").toString()));
            }

            artist.setUpdatedAt(LocalDateTime.now());
        }

        return ArtistMapper.toDto(artistRepository.save(artist));
    }

    @Override
    public List<ArtistDto> getArtistsByGenre(String genreSlug) {
        return artistRepository.findByGenresContaining(genreSlug.toLowerCase()).stream()
                .map(ArtistMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ArtistDto getPublicArtistProfile(String artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ nghệ sĩ!"));
        return ArtistMapper.toDto(artist);
    }

    @Override
    public ArtistDto getCurrentArtistProfile(String userId) {
        Artist artist = artistRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Bạn chưa tạo hồ sơ nghệ sĩ!"));
        return ArtistMapper.toDto(artist);
    }

    @Override
    public ArtistDto updateArtistProfile(String userId, ArtistDto updateDto) {
        Artist existingArtist = artistRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không có quyền chỉnh sửa hồ sơ này!"));

        Artist updatedArtist = ArtistMapper.updateEntity(existingArtist, updateDto);

        // Cập nhật thủ công các trường ảnh và thể loại nếu có gửi lên
        if (updateDto.getGenres() != null)
            updatedArtist.setGenres(updateDto.getGenres());
        if (updateDto.getAvatarUrl() != null)
            updatedArtist.setAvatarUrl(updateDto.getAvatarUrl());
        if (updateDto.getCoverImageUrl() != null)
            updatedArtist.setCoverImageUrl(updateDto.getCoverImageUrl());

        updatedArtist.setUpdatedAt(LocalDateTime.now());
        return ArtistMapper.toDto(artistRepository.save(updatedArtist));
    }

    @Override
    public List<ArtistDto> searchArtists(String query) {
        return artistRepository.findByNameContainingIgnoreCase(query).stream()
                .map(ArtistMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArtistDto> getAllArtists() {
        return artistRepository.findAll().stream()
                .map(ArtistMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ArtistDto adminUpdateName(String artistId, String newName) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Artist"));
        artist.setName(newName);
        return ArtistMapper.toDto(artistRepository.save(artist));
    }

@Override
public List<SongDto> getSongsByArtistId(String artistId) {
    return songRepository.findByArtistId(artistId).stream()
        .filter(song -> "PUBLISHED".equals(song.getStatus()))
        .map(song -> {
            SongDto dto = songMapper.toDto(song);

            // map artistName
            if (song.getArtistId() != null) {
                artistRepository.findById(song.getArtistId())
                    .ifPresent(artist -> dto.setArtistName(artist.getName()));
            }

            // map albumName
            if (song.getAlbumId() != null) {
                albumRepository.findById(song.getAlbumId())
                    .ifPresent(album -> dto.setAlbumName(album.getTitle()));
            }

            return dto;
        })
        .collect(Collectors.toList());
}

    // --- LOGIC MỚI: LẤY TẤT CẢ ALBUM CỦA NGHỆ SĨ ---
    @Override
    public List<AlbumDto> getAlbumsByArtistId(String artistId) {
        // Tìm các album của nghệ sĩ
        return albumRepository.findByArtistId(artistId).stream()
                .map(albumMapper::toFullDto)
                .collect(Collectors.toList());
    }
}