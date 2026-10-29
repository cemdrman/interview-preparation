package com.cemdrman.randevu;

import com.cemdrman.randevu.client.DoktorCevap;
import com.cemdrman.randevu.client.DoktorClient;
import com.cemdrman.randevu.mesajlasma.BildirimMesaji;
import com.cemdrman.randevu.mesajlasma.KafkaConfig;
import com.cemdrman.randevu.mesajlasma.RabbitConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

// Kafka için gömülü broker kullanıyoruz, Feign client ve RabbitMQ mock'lanıyor
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = KafkaConfig.RANDEVU_TOPIC, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class RandevuAkisTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private DoktorClient doktorClient;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void randevuOlusunca_kafkaUzerindenRabbitMqyaBildirimGider() {
        given(doktorClient.getir(1L)).willReturn(new DoktorCevap(1L, "Ayşe Yılmaz", "Genel Cerrahi", true));

        assertThat(mockMvc.post().uri("/randevular")
                .contentType(MediaType.APPLICATION_JSON)
                .content(istek(1L, "AMELIYAT")))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .extractingPath("$.doktorAdi").isEqualTo("Ayşe Yılmaz");

        ArgumentCaptor<BildirimMesaji> mesaj = ArgumentCaptor.forClass(BildirimMesaji.class);
        verify(rabbitTemplate, timeout(10_000))
                .convertAndSend(eq(RabbitConfig.BILDIRIM_EXCHANGE), eq(RabbitConfig.SMS_ROUTING_KEY), mesaj.capture());

        assertThat(mesaj.getValue().telefon()).isEqualTo("5550000000");
        assertThat(mesaj.getValue().metin()).contains("Ali Veli", "Ayşe Yılmaz");
    }

    @Test
    void operatorOlmayanDoktoraAmeliyatRandevusuVerilmez() {
        given(doktorClient.getir(2L)).willReturn(new DoktorCevap(2L, "Mehmet Kaya", "Dahiliye", false));

        assertThat(mockMvc.post().uri("/randevular")
                .contentType(MediaType.APPLICATION_JSON)
                .content(istek(2L, "AMELIYAT")))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson()
                .extractingPath("$.detail").isEqualTo("Sadece operatör doktorlar ameliyata girebilir");

        verify(rabbitTemplate, never()).convertAndSend(eq(RabbitConfig.BILDIRIM_EXCHANGE),
                eq(RabbitConfig.SMS_ROUTING_KEY), org.mockito.ArgumentMatchers.<Object>any());
    }

    @Test
    void eksikAlanVarsa400Doner() {
        assertThat(mockMvc.post().uri("/randevular")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hastaAdi\": \"\", \"doktorId\": 1}"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    private static String istek(Long doktorId, String tip) {
        return """
                {
                  "hastaAdi": "Ali Veli",
                  "hastaTelefon": "5550000000",
                  "doktorId": %d,
                  "tarih": "%s",
                  "tip": "%s"
                }
                """.formatted(doktorId, LocalDateTime.now().plusDays(1).withNano(0), tip);
    }
}
