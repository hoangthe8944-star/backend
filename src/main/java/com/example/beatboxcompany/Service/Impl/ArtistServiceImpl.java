package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Entity.Artist;
import com.example.beatboxcompany.Dto.ArtistDto;
import com.example.beatboxcompany.Mapper.ArtistMapper;
import com.example.beatboxcompany.Repository.ArtistRepository;
import com.example.beatboxcompany.Service.ArtistService;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;

    public ArtistServiceImpl(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')") // CHỈ ADMIN MỚI ĐƯỢC GỌI
    public ArtistDto createArtistByAdmin(String userId, String artistName) {
        // Kiểm tra user có tồn tại không (tùy chọn)
        // if (!userRepository.existsById(userId)) { ... }

        // Kiểm tra đã có artist chưa
        if (artistRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Người dùng này đã có hồ sơ nghệ sĩ!");
        }

        Artist artist = new Artist();
        artist.setUserId(userId);
        artist.setName(artistName);
        artist.setCreatedAt(LocalDateTime.now());
        artist.setVerified(true); // Admin tạo → tự động verified luôn (tùy bạn)

        Artist saved = artistRepository.save(artist);
        return ArtistMapper.toDto(saved);
    }

    @Override
    public ArtistDto getPublicArtistProfile(String artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ nghệ sĩ!"));
        return ArtistMapper.toDto(artist);
    }

    @Override
    public ArtistDto getCurrentArtistProfile(String userId) {
        // Tìm Artist bằng userId để đảm bảo chỉ chủ tài khoản mới được truy cập
        Artist artist = artistRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Bạn chưa tạo hồ sơ nghệ sĩ!"));
        return ArtistMapper.toDto(artist);
    }

    @Override
    public ArtistDto updateArtistProfile(String userId, ArtistDto updateDto) {
        // 1. Tìm hồ sơ bằng userId (để xác thực quyền sở hữu)
        Artist existingArtist = artistRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không có quyền chỉnh sửa hồ sơ này!"));

        // 2. Cập nhật thông tin bằng Mapper
        Artist updatedArtist = ArtistMapper.updateEntity(existingArtist, updateDto);

        // 3. Lưu
        Artist savedArtist = artistRepository.save(updatedArtist);
        return ArtistMapper.toDto(savedArtist);
    }

    @Override
    public List<ArtistDto> searchArtists(String query) {
        return null;
    }

    @Override
    public List<ArtistDto> getAllArtists() {
        // Lấy tất cả Artist trong DB
        return artistRepository.findAll().stream()
                .map(ArtistMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ArtistDto adminUpdateName(String artistId, String newName) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Artist có ID: " + artistId));

        // Cập nhật tên
        artist.setName(newName);

        // Lưu và trả về DTO
        Artist updatedArtist = artistRepository.save(artist);
        return ArtistMapper.toDto(updatedArtist);
    }
}