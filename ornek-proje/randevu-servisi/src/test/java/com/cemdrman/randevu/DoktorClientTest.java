package com.cemdrman.randevu;

import com.cemdrman.randevu.client.DoktorBulunamadiException;
import com.cemdrman.randevu.client.DoktorCevap;
import com.cemdrman.randevu.client.DoktorClient;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// doktor-servisi yerine JDK'nın HttpServer'ı ile sahte bir servis ayağa kaldırıyoruz
// Kafka ve RabbitMQ bu testte kullanılmadığı için bağlantı kurulmasın
@SpringBootTest(properties = {
        "spring.kafka.admin.auto-create=false",
        "spring.kafka.listener.auto-startup=false",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class DoktorClientTest {

    private static final HttpServer SAHTE_DOKTOR_SERVISI = sahteServis();

    @Autowired
    private DoktorClient doktorClient;

    @DynamicPropertySource
    static void doktorServisiUrl(DynamicPropertyRegistry registry) {
        registry.add("doktor-servisi.url", () -> "http://localhost:" + SAHTE_DOKTOR_SERVISI.getAddress().getPort());
    }

    @AfterAll
    static void kapat() {
        SAHTE_DOKTOR_SERVISI.stop(0);
    }

    @Test
    void doktorBilgisiJsondanRecordaCevrilir() {
        DoktorCevap doktor = doktorClient.getir(1L);

        assertThat(doktor).isEqualTo(new DoktorCevap(1L, "Ayşe Yılmaz", "Genel Cerrahi", true));
    }

    @Test
    void doktorYoksaErrorDecoderKendiExceptionimiziFirlatir() {
        assertThatThrownBy(() -> doktorClient.getir(99L))
                .isInstanceOf(DoktorBulunamadiException.class)
                .hasMessageContaining("/doktorlar/99");
    }

    private static HttpServer sahteServis() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/doktorlar/1", exchange -> cevapVer(exchange, 200,
                    "{\"id\":1,\"isim\":\"Ayşe Yılmaz\",\"brans\":\"Genel Cerrahi\",\"operator\":true}"));
            server.createContext("/doktorlar/99", exchange -> cevapVer(exchange, 404,
                    "{\"status\":404,\"detail\":\"Doktor bulunamadı: 99\"}"));
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void cevapVer(com.sun.net.httpserver.HttpExchange exchange, int status, String govde)
            throws IOException {
        byte[] veri = govde.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, veri.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(veri);
        }
    }
}
