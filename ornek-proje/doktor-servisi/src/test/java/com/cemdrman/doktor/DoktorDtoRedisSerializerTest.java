package com.cemdrman.doktor;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DoktorDtoRedisSerializerTest {

    private final JacksonJsonRedisSerializer<DoktorDto> serializer = new JacksonJsonRedisSerializer<>(DoktorDto.class);

    @Test
    void doktorRediseJsonOlarakYazilir() {
        DoktorDto doktor = new DoktorDto(1L, "Ayşe Yılmaz", "Genel Cerrahi", true);

        byte[] veri = serializer.serialize(doktor);

        assertThat(new String(veri, StandardCharsets.UTF_8))
                .isEqualTo("{\"id\":1,\"isim\":\"Ayşe Yılmaz\",\"brans\":\"Genel Cerrahi\",\"operator\":true}");
        assertThat(serializer.deserialize(veri)).isEqualTo(doktor);
    }
}
