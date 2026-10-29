package com.cemdrman.randevu.randevu;

import java.time.LocalDateTime;

public record RandevuCevabi(Long id, String hastaAdi, String doktorAdi, LocalDateTime tarih, RandevuTipi tip) {
}
