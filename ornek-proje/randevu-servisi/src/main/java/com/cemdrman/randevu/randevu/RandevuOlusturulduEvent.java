package com.cemdrman.randevu.randevu;

import java.time.LocalDateTime;

public record RandevuOlusturulduEvent(Long randevuId, String hastaAdi, String hastaTelefon, String doktorAdi,
                                      LocalDateTime tarih, RandevuTipi tip) {
}
