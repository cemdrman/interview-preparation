package com.cemdrman.randevu.randevu;

public class RandevuBulunamadiException extends RuntimeException {

    public RandevuBulunamadiException(Long id) {
        super("Randevu bulunamadı: " + id);
    }
}
