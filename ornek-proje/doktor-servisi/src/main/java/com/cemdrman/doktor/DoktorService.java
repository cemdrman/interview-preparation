package com.cemdrman.doktor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoktorService {

    private static final Logger log = LoggerFactory.getLogger(DoktorService.class);

    private final DoktorRepository doktorRepository;

    public DoktorService(DoktorRepository doktorRepository) {
        this.doktorRepository = doktorRepository;
    }

    @Cacheable(cacheNames = "doktorlar", key = "#id")
    public DoktorDto getir(Long id) {
        log.info("Doktor veritabanından okunuyor: {}", id);
        return doktorRepository.findById(id)
                .map(DoktorDto::from)
                .orElseThrow(() -> new DoktorBulunamadiException(id));
    }

    public List<DoktorDto> listele() {
        return doktorRepository.findAll().stream()
                .map(DoktorDto::from)
                .toList();
    }

    @Transactional
    @CacheEvict(cacheNames = "doktorlar", key = "#id")
    public DoktorDto operatorDurumunuGuncelle(Long id, boolean operator) {
        Doktor doktor = doktorRepository.findById(id)
                .orElseThrow(() -> new DoktorBulunamadiException(id));
        doktor.setOperator(operator);
        return DoktorDto.from(doktor);
    }
}
