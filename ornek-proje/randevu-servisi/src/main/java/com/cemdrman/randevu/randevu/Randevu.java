package com.cemdrman.randevu.randevu;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Randevu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hastaAdi;

    private String hastaTelefon;

    private Long doktorId;

    private LocalDateTime tarih;

    @Enumerated(EnumType.STRING)
    private RandevuTipi tip;

    protected Randevu() {
    }

    public Randevu(String hastaAdi, String hastaTelefon, Long doktorId, LocalDateTime tarih, RandevuTipi tip) {
        this.hastaAdi = hastaAdi;
        this.hastaTelefon = hastaTelefon;
        this.doktorId = doktorId;
        this.tarih = tarih;
        this.tip = tip;
    }

    public Long getId() {
        return id;
    }

    public String getHastaAdi() {
        return hastaAdi;
    }

    public String getHastaTelefon() {
        return hastaTelefon;
    }

    public Long getDoktorId() {
        return doktorId;
    }

    public LocalDateTime getTarih() {
        return tarih;
    }

    public RandevuTipi getTip() {
        return tip;
    }
}
