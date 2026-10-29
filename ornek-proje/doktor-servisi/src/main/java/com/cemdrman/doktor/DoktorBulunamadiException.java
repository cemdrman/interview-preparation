package com.cemdrman.doktor;

public class DoktorBulunamadiException extends RuntimeException {

    public DoktorBulunamadiException(Long id) {
        super("Doktor bulunamadı: " + id);
    }
}
