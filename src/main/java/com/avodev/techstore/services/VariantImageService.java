package com.avodev.techstore.services;


import com.avodev.techstore.entities.VariantImage;
import com.avodev.techstore.repositories.VariantImageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VariantImageService {
    VariantImageRepository variantImageRepository;

    public VariantImage findPrimaryByVariantId(Long variantId) {
        return variantImageRepository.findPrimaryByVariantId(variantId);
    }
}
