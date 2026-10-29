package com.cemdrman.randevu.mesajlasma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SmsListener {

    private static final Logger log = LoggerFactory.getLogger(SmsListener.class);

    @RabbitListener(queues = RabbitConfig.SMS_KUYRUGU)
    public void dinle(BildirimMesaji mesaj) {
        if (mesaj.telefon() == null || mesaj.telefon().isBlank()) {
            // Retry sonrası mesaj DLQ'ya gider
            throw new IllegalArgumentException("Telefon numarası boş olamaz");
        }
        // Gerçek projede burada SMS sağlayıcısı çağrılır
        log.info("SMS gönderildi -> {}: {}", mesaj.telefon(), mesaj.metin());
    }
}
