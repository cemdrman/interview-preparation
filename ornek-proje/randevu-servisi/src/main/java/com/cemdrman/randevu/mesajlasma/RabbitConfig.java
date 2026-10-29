package com.cemdrman.randevu.mesajlasma;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String BILDIRIM_EXCHANGE = "bildirim-exchange";
    public static final String SMS_ROUTING_KEY = "bildirim.sms";
    public static final String SMS_KUYRUGU = "sms-kuyrugu";
    public static final String SMS_DLQ = "sms-kuyrugu.dlq";
    public static final String DLX = "bildirim-dlx";

    @Bean
    public DirectExchange bildirimExchange() {
        return new DirectExchange(BILDIRIM_EXCHANGE);
    }

    @Bean
    public DirectExchange bildirimDeadLetterExchange() {
        return new DirectExchange(DLX);
    }

    // İşlenemeyen mesajlar silinmek yerine DLX üzerinden sms-kuyrugu.dlq kuyruğuna gider
    @Bean
    public Queue smsKuyrugu() {
        return QueueBuilder.durable(SMS_KUYRUGU)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(SMS_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue smsDeadLetterKuyrugu() {
        return QueueBuilder.durable(SMS_DLQ).build();
    }

    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsKuyrugu()).to(bildirimExchange()).with(SMS_ROUTING_KEY);
    }

    @Bean
    public Binding smsDeadLetterBinding() {
        return BindingBuilder.bind(smsDeadLetterKuyrugu()).to(bildirimDeadLetterExchange()).with(SMS_ROUTING_KEY);
    }

    // Mesajları Java serialization yerine JSON olarak gönderip almak için
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
