package com.cemdrman.randevu.mesajlasma;

import com.cemdrman.randevu.randevu.RandevuOlusturulduEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RandevuEventProducer {

    private static final Logger log = LoggerFactory.getLogger(RandevuEventProducer.class);

    private final KafkaTemplate<String, RandevuOlusturulduEvent> kafkaTemplate;

    public RandevuEventProducer(KafkaTemplate<String, RandevuOlusturulduEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Veritabanı commit olmadan Kafka'ya mesaj gitmesin diye AFTER_COMMIT (varsayılan) kullanıyoruz
    @TransactionalEventListener
    public void gonder(RandevuOlusturulduEvent event) {
        // Aynı randevunun mesajları aynı partition'a gitsin diye key olarak randevu id kullanıyoruz
        kafkaTemplate.send(KafkaConfig.RANDEVU_TOPIC, event.randevuId().toString(), event)
                .whenComplete((sonuc, hata) -> {
                    if (hata != null) {
                        log.error("Randevu event'i gönderilemedi: {}", event.randevuId(), hata);
                    } else {
                        log.info("Randevu event'i gönderildi. partition: {}, offset: {}",
                                sonuc.getRecordMetadata().partition(), sonuc.getRecordMetadata().offset());
                    }
                });
    }
}
