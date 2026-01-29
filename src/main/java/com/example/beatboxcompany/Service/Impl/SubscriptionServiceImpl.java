package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Dto.SubscriptionDto;
import com.example.beatboxcompany.Entity.Subscription;
import com.example.beatboxcompany.Service.SubscriptionService;
import com.example.beatboxcompany.Exception.ResourceNotFoundException; // Giả định bạn có lớp Exception này
import com.example.beatboxcompany.Repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j // Lombok: Logging
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    // Helper: Chuyển Entity sang DTO
    private SubscriptionDto mapToDto(Subscription subscription) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(subscription.getId());
        dto.setName(subscription.getName());
        dto.setDescription(subscription.getDescription());
        dto.setPrice(subscription.getPrice());
        dto.setCurrency(subscription.getCurrency());
        dto.setDuration(subscription.getDuration());
        dto.setFeatures(subscription.getFeatures());
        dto.setStatus(subscription.getStatus());
        return dto;
    }
    
    // Helper: Chuyển DTO sang Entity (Cho mục đích tạo/cập nhật)
    private Subscription mapToEntity(SubscriptionDto dto) {
        Subscription entity = new Subscription();
        entity.setId(dto.getId()); // ID chỉ được set nếu là cập nhật
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setCurrency(dto.getCurrency());
        entity.setDuration(dto.getDuration());
        entity.setFeatures(dto.getFeatures());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE"); // Mặc định là ACTIVE
        return entity;
    }

    @Override
    public List<SubscriptionDto> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionDto createSubscription(SubscriptionDto subscriptionDto) {
        Subscription subscription = mapToEntity(subscriptionDto);
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription created: {}", savedSubscription.getName());
        return mapToDto(savedSubscription);
    }

    @Override
    public SubscriptionDto updateSubscription(String id, SubscriptionDto subscriptionDto) {
        Subscription existingSubscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));

        // Cập nhật các trường
        existingSubscription.setName(subscriptionDto.getName());
        existingSubscription.setDescription(subscriptionDto.getDescription());
        existingSubscription.setPrice(subscriptionDto.getPrice());
        existingSubscription.setFeatures(subscriptionDto.getFeatures());
        existingSubscription.setStatus(subscriptionDto.getStatus());
        
        Subscription updatedSubscription = subscriptionRepository.save(existingSubscription);
        log.info("Subscription updated: {}", updatedSubscription.getName());
        return mapToDto(updatedSubscription);
    }

    @Override
    public void deleteSubscription(String id) {
        if (!subscriptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subscription", "id", id);
        }
        subscriptionRepository.deleteById(id);
        log.warn("Subscription deleted with ID: {}", id);
    }
}