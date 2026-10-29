package com.cemdrman.randevu.mesajlasma;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    public static final String RANDEVU_TOPIC = "randevu-olusturuldu";

    @Bean
    public NewTopic randevuTopic() {
        return TopicBuilder.name(RANDEVU_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // Mesaj 1 saniye arayla 2 kez daha denenir, yine hata alırsa randevu-olusturuldu-dlt topic'ine gönderilir
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), new FixedBackOff(1000L, 2));
    }
}
