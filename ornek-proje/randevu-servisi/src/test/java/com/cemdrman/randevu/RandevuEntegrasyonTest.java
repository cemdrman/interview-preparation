package com.cemdrman.randevu;

import com.cemdrman.randevu.client.DoktorCevap;
import com.cemdrman.randevu.client.DoktorClient;
import com.cemdrman.randevu.mesajlasma.BildirimMesaji;
import com.cemdrman.randevu.mesajlasma.RabbitConfig;
import com.cemdrman.randevu.mesajlasma.SmsListener;
import com.cemdrman.randevu.randevu.RandevuIstegi;
import com.cemdrman.randevu.randevu.RandevuService;
import com.cemdrman.randevu.randevu.RandevuTipi;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

// Gerçek Kafka ve RabbitMQ ile uçtan uca test. Docker yoksa test atlanır.
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class RandevuEntegrasyonTest {

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:4.0.0");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4-management");

    @Autowired
    private RandevuService randevuService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private DoktorClient doktorClient;

    @MockitoSpyBean
    private SmsListener smsListener;

    @Test
    void randevuOlusunca_smsListenerMesajiAlir() {
        given(doktorClient.getir(1L)).willReturn(new DoktorCevap(1L, "Ayşe Yılmaz", "Genel Cerrahi", true));

        randevuService.olustur(new RandevuIstegi("Ali Veli", "5550000000", 1L,
                LocalDateTime.now().plusDays(1), RandevuTipi.MUAYENE));

        verify(smsListener, timeout(15_000)).dinle(any(BildirimMesaji.class));
    }

    @Test
    void islenemeyenMesajDeadLetterKuyruğunaGider() {
        rabbitTemplate.convertAndSend(RabbitConfig.BILDIRIM_EXCHANGE, RabbitConfig.SMS_ROUTING_KEY,
                new BildirimMesaji("", "telefonsuz mesaj"));

        Object dlqMesaji = rabbitTemplate.receiveAndConvert(RabbitConfig.SMS_DLQ, 15_000);

        assertThat(dlqMesaji).isEqualTo(new BildirimMesaji("", "telefonsuz mesaj"));
    }
}
