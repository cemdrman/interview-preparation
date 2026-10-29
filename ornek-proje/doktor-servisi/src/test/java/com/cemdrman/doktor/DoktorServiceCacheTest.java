package com.cemdrman.doktor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// Redis yerine bellekteki cache ile @Cacheable davranışını test ediyoruz
@SpringBootTest(properties = "spring.cache.type=simple")
class DoktorServiceCacheTest {

    @Autowired
    private DoktorService doktorService;

    @MockitoSpyBean
    private DoktorRepository doktorRepository;

    @Test
    void ayniDoktorIkinciKezIstenirseVeritabaninaGidilmez() {
        DoktorDto ilk = doktorService.getir(1L);
        DoktorDto ikinci = doktorService.getir(1L);

        assertThat(ikinci).isEqualTo(ilk);
        verify(doktorRepository, times(1)).findById(1L);
    }

    @Test
    void guncellemeSonrasiCacheTemizlenir() {
        doktorService.getir(2L);

        doktorService.operatorDurumunuGuncelle(2L, true);
        DoktorDto guncel = doktorService.getir(2L);

        assertThat(guncel.operator()).isTrue();
        verify(doktorRepository, times(3)).findById(2L);
    }
}
