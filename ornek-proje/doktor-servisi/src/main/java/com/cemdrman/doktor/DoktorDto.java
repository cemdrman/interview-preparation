package com.cemdrman.doktor;

public record DoktorDto(Long id, String isim, String brans, boolean operator) {

    static DoktorDto from(Doktor doktor) {
        return new DoktorDto(doktor.getId(), doktor.getIsim(), doktor.getBrans(), doktor.isOperator());
    }
}
