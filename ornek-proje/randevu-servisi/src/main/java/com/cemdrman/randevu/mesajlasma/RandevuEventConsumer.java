package com.cemdrman.randevu.mesajlasma;

import com.cemdrman.randevu.randevu.RandevuOlusturulduEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RandevuEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(RandevuEventConsumer.class);

    private final RabbitTemplate rabbitTemplate;

    public RandevuEventConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @KafkaListener(topics = KafkaConfig.RANDEVU_TOPIC, groupId = "bildirim-grubu")
    public void dinle(RandevuOlusturulduEvent event) {
        log.info("Randevu event'i alındı: {}", event.randevuId());

        BildirimMesaji mesaj = new BildirimMesaji(event.hastaTelefon(),
                "Sayın %s, %s tarihli randevunuz Dr. %s ile oluşturuldu."
                        .formatted(event.hastaAdi(), event.tarih(), event.doktorAdi()));

        rabbitTemplate.convertAndSend(RabbitConfig.BILDIRIM_EXCHANGE, RabbitConfig.SMS_ROUTING_KEY, mesaj);
    }
}
