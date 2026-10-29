package com.cemdrman.randevu.client;

import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

// @Configuration eklemiyoruz; eklersek bu ayarlar bütün Feign client'lara uygulanır
public class DoktorClientConfig {

    @Bean
    public ErrorDecoder doktorErrorDecoder() {
        ErrorDecoder varsayilan = new ErrorDecoder.Default();
        return (methodKey, response) -> {
            if (response.status() == 404) {
                return new DoktorBulunamadiException("Doktor bulunamadı: " + response.request().url());
            }
            return varsayilan.decode(methodKey, response);
        };
    }

    @Bean
    public Logger.Level doktorLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
