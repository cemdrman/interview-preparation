## RabbitMQ

Kafka'dan sonra mülakatlarda en çok karşımıza çıkan message broker RabbitMQ. Genelde "Kafka varken neden RabbitMQ
kullanalım?" sorusu soruluyor. Bu yazıda RabbitMQ'nun temel kavramlarını ve Kafka'dan farkını
[örnek proje](../ornek-proje) üzerinden anlatmaya çalışacağız.

#### RabbitMQ Nedir?

RabbitMQ AMQP protokolünü kullanan bir **message broker**'dır. Producer mesajı doğrudan kuyruğa göndermez, bir
**exchange**'e gönderir. Exchange de mesajı **binding** kurallarına göre bir ya da birden fazla kuyruğa yönlendirir.
Consumer mesajı işleyip onayladığında (ack) mesaj kuyruktan **silinir**.

```
Producer -> Exchange -> (binding, routing key) -> Queue -> Consumer
```

Örnek projede Kafka'dan gelen randevu event'ini aldıktan sonra RabbitMQ'ya "bu SMS'i gönder" görevini bırakıyoruz:

```java
rabbitTemplate.convertAndSend(RabbitConfig.BILDIRIM_EXCHANGE, RabbitConfig.SMS_ROUTING_KEY, mesaj);
```

#### Exchange Tipleri

| Tip | Yönlendirme |
|---|---|
| Direct | Routing key binding key ile **birebir** eşleşen kuyruğa gider. |
| Topic | Routing key desen ile eşleşir. `bildirim.*` ya da `bildirim.#` gibi. |
| Fanout | Routing key'e bakmadan bağlı **bütün** kuyruklara gider. |
| Headers | Routing key yerine mesaj header'larına göre yönlendirir. |

Örnek projede `direct` exchange kullandık. İleride e-posta bildirimi eklemek istersek `bildirim.eposta` routing
key'i ile yeni bir kuyruk bağlamamız yeterli.

#### Queue, Exchange ve Binding Tanımları

```java
@Bean
public DirectExchange bildirimExchange() {
    return new DirectExchange(BILDIRIM_EXCHANGE);
}

@Bean
public Queue smsKuyrugu() {
    return QueueBuilder.durable(SMS_KUYRUGU)
            .deadLetterExchange(DLX)
            .deadLetterRoutingKey(SMS_ROUTING_KEY)
            .build();
}

@Bean
public Binding smsBinding() {
    return BindingBuilder.bind(smsKuyrugu()).to(bildirimExchange()).with(SMS_ROUTING_KEY);
}
```

Spring Boot bu bean'leri gördüğünde RabbitMQ'ya bağlandığı anda exchange, queue ve binding'leri **otomatik**
oluşturur. `durable` kuyruklar RabbitMQ yeniden başladığında silinmez.

#### Consumer ve Acknowledgement

```java
@RabbitListener(queues = RabbitConfig.SMS_KUYRUGU)
public void dinle(BildirimMesaji mesaj) {
    if (mesaj.telefon() == null || mesaj.telefon().isBlank()) {
        throw new IllegalArgumentException("Telefon numarası boş olamaz");
    }
    log.info("SMS gönderildi -> {}: {}", mesaj.telefon(), mesaj.metin());
}
```

Spring AMQP varsayılan olarak method hatasız biterse mesajı **ack**'ler, exception fırlatılırsa **nack**'ler.
Nack'lenen mesaj varsayılan olarak tekrar kuyruğa konur (requeue). Mesaj hiç işlenemiyorsa bu durum sonsuz bir
döngüye dönüşür. Buna **poison message** denir.

#### Retry ve Dead Letter Queue

Sonsuz döngüyü engellemek için mesajı birkaç kez deneyip sonra dead letter kuyruğuna gönderiyoruz:

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        default-requeue-rejected: false
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1s
```

Mesaj 3 kez denenir. Yine hata alırsa `default-requeue-rejected: false` olduğu için kuyruğa geri konmaz,
kuyruğa tanımladığımız **dead letter exchange** (`bildirim-dlx`) üzerinden `sms-kuyrugu.dlq` kuyruğuna gider.
Örnek projedeki `RandevuEntegrasyonTest` testinde telefonu boş bir mesajın DLQ'ya düştüğünü test ettik.

#### Prefetch

Consumer'ın ack beklemeden en fazla kaç mesaj alabileceğini **prefetch** belirler
(`spring.rabbitmq.listener.simple.prefetch`, varsayılan 250). Uzun süren işlerde bu değer düşürülmezse bir consumer
mesajları alıp bekletirken diğer consumer'lar boşta kalabilir.

#### Mesaj Formatı

Varsayılan olarak Spring AMQP nesneleri Java serialization ile gönderir. Bu durumda başka dillerde yazılmış
servisler mesajı okuyamaz ve güvenlik riski oluşur. JSON kullanmak için bir `MessageConverter` bean'i tanımlıyoruz,
Spring Boot bunu hem `RabbitTemplate`'e hem listener'lara otomatik bağlıyor:

```java
@Bean
public MessageConverter jsonMessageConverter() {
    return new JacksonJsonMessageConverter();
}
```

#### Kafka vs RabbitMQ

| | Kafka | RabbitMQ |
|---|---|---|
| Model | Dağıtık log | Message broker (kuyruk) |
| Okunan mesaj | Silinmez, retention süresince saklanır | Ack sonrası silinir |
| Tekrar okuma | Offset geri alınarak yapılabilir | Yapılamaz |
| Birden fazla okuyucu | Her consumer group bütün mesajları okur | Her mesajı tek bir consumer alır (fanout ile çoğaltılabilir) |
| Yönlendirme | Topic ve partition | Exchange tipleri ile esnek yönlendirme |
| Sıralama | Partition içinde | Tek consumer olan kuyrukta |
| Kullanım | Event streaming, log toplama, yüksek hacim | Görev kuyruğu, iş dağıtımı, RPC |

Örnek projede ikisini birlikte kullanmamızın sebebi de bu. "Randevu oluşturuldu" bir **event**, bunu birden fazla
servis okuyabilir, bu yüzden Kafka'da. "Bu SMS'i gönder" ise bir **görev**, bir kere yapılıp silinmeli, bu yüzden
RabbitMQ'da.

### Mülakat Soruları

<details>
<summary>1. Producer mesajı neden doğrudan kuyruğa göndermiyor?</summary>

Exchange sayesinde producer hangi kuyrukların mesajı alacağını bilmek zorunda kalmaz. Yeni bir kuyruk eklemek için
producer'da değişiklik yapmadan sadece binding eklemek yeterlidir.
</details>

<details>
<summary>2. RabbitMQ yeniden başlarsa mesajlar kaybolur mu?</summary>

Kuyruk `durable` ve mesajlar `persistent` ise kaybolmaz. Spring AMQP mesajları varsayılan olarak persistent
gönderir. Daha yüksek dayanıklılık için birden fazla node'a kopyalanan **quorum queue** kullanılabilir.
</details>

<details>
<summary>3. Producer mesajın broker'a ulaştığından nasıl emin olur?</summary>

**Publisher confirm** açılır (`spring.rabbitmq.publisher-confirm-type: correlated`). Broker mesajı aldığında
producer'a onay gönderir. Mesaj hiçbir kuyruğa yönlendirilemediyse `publisher-returns` ile geri döner.
</details>

<details>
<summary>4. Aynı mesaj iki kez işlenebilir mi?</summary>

Evet. Consumer mesajı işleyip ack göndermeden çökerse mesaj başka bir consumer'a tekrar gönderilir. Bu yüzden
consumer'lar idempotent olmalıdır.
</details>

<details>
<summary>5. Dead letter queue'ya düşen mesajlarla ne yapılır?</summary>

Neden hata aldığı incelenir (header'larda `x-death` bilgisi bulunur), hata düzeltildikten sonra mesajlar elle
ya da bir job ile asıl kuyruğa tekrar gönderilir. DLQ'daki mesaj sayısı için alarm kurulmalıdır.
</details>

<details>
<summary>6. Bir SMS servisi için Kafka mı RabbitMQ mu seçersiniz?</summary>

Her SMS bir kere gönderilip silinmesi gereken bir görev olduğu için RabbitMQ daha uygundur. Retry, DLQ ve
consumer sayısını artırarak iş dağıtımı hazır gelir. Mesajların sonradan tekrar okunması ya da birden fazla servisin
aynı mesajı okuması gerekiyorsa Kafka seçilir.
</details>

### Kaynaklar

- https://www.rabbitmq.com/tutorials
- https://docs.spring.io/spring-amqp/reference/

Faydalı olması dileğiyle.
