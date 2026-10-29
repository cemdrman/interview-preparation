package com.cemdrman.randevu.randevu;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RandevuIstegi(
        @NotBlank String hastaAdi,
        @NotBlank String hastaTelefon,
        @NotNull Long doktorId,
        @NotNull @Future LocalDateTime tarih,
        @NotNull RandevuTipi tip) {
}
