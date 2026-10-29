## Apache Kafka

Mikroservis mimarisi konuşulan her mülakatta "servisler birbiriyle nasıl haberleşiyor?" sorusu geliyor ve cevap
çoğunlukla Kafka oluyor. Arkasından da "sıralama nasıl garanti ediliyor?", "mesaj iki kere işlenirse ne olur?" gibi
sorular geliyor. Bu yazıda Kafka'nın temel kavramlarını [örnek proje](../ornek-proje) üzerinden anlatmaya çalışacağız.

#### Kafka Nedir?

Kafka dağıtık bir **event log**'udur. Mesajlar diske sırayla eklenir (append-only) ve okunduktan sonra
**silinmez**, belirlenen süre (retention) boyunca saklanır. Bu yüzden aynı mesajı birden fazla servis, farklı
zamanlarda okuyabilir.

Örnek projede randevu oluşturulduğunda `randevu-olusturuldu` topic'ine bir event gönderiyoruz. Bugün bu event'i
sadece bildirim gönderen consumer okuyor fakat yarın faturalama ya da istatistik servisi eklendiğinde
`randevu-servisi`'nde hiçbir değişiklik yapmadan aynı event'i okuyabilirler.

#### Temel Kavramlar

| Kavram | Açıklama |
|---|---|
| Broker | Kafka sunucusu. Birden fazla broker bir cluster oluşturur. |
| Topic | Mesajların yazıldığı mantıksal kategori. Örn. `randevu-olusturuldu` |
| Partition | Topic'in parçaları. Paralellik partition sayısı kadardır. |
| Offset | Mesajın partition içindeki sıra numarası. |
| Producer | Mesajı gönderen uygulama. |
| Consumer Group | Aynı topic'i birlikte okuyan consumer'lar. Her partition grupta tek bir consumer'a atanır. |
| Replication | Her partition'ın başka broker'larda kopyası tutulur. Biri lider, diğerleri follower'dır. |

Kafka 4.0 ile ZooKeeper tamamen kaldırıldı. Cluster bilgileri artık Kafka'nın kendi içindeki **KRaft** protokolü
ile yönetiliyor.

#### Topic ve Partition

```java
@Bean
public NewTopic randevuTopic() {
    return TopicBuilder.name(RANDEVU_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
}
```

Topic'i 3 partition ile oluşturduk. Bu durumda `bildirim-grubu` consumer group'unda en fazla 3 consumer
**aynı anda** çalışabilir. 4. consumer'ı eklersek boşta bekler. Yani partition sayısı paralelliğin üst sınırıdır.

#### Sıralama Garantisi

Kafka sıralamayı sadece **partition içinde** garanti eder. Aynı randevuya ait mesajların sırası bozulmasın
istiyorsak aynı **key** ile göndermeliyiz, çünkü aynı key her zaman aynı partition'a gider:

```java
kafkaTemplate.send(KafkaConfig.RANDEVU_TOPIC, event.randevuId().toString(), event)
        .whenComplete((sonuc, hata) -> {
            if (hata != null) {
                log.error("Randevu event'i gönderilemedi: {}", event.randevuId(), hata);
            } else {
                log.info("Randevu event'i gönderildi. partition: {}, offset: {}",
                        sonuc.getRecordMetadata().partition(), sonuc.getRecordMetadata().offset());
            }
        });
```

`send` methodu `CompletableFuture` döner, yani mesaj **asenkron** gönderilir. Sonucu `whenComplete` ile
kontrol etmezsek gönderim hatalarından haberimiz olmaz.

#### Producer Ayarları

```yaml
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JacksonJsonSerializer
      acks: all
      properties:
        enable.idempotence: true
```

- `acks=0`: Broker'dan cevap beklenmez. Hızlı ama mesaj kaybolabilir.
- `acks=1`: Sadece lider partition yazınca cevap döner. Lider çökerse mesaj kaybolabilir.
- `acks=all`: Bütün senkron replica'lar (ISR) yazınca cevap döner. En güvenlisi.

`enable.idempotence=true` ile producer retry yaptığında aynı mesajın iki kere yazılması engellenir.

#### Consumer

```java
@KafkaListener(topics = KafkaConfig.RANDEVU_TOPIC, groupId = "bildirim-grubu")
public void dinle(RandevuOlusturulduEvent event) {
    ...
}
```

Consumer mesajı işledikten sonra **offset commit** eder, yani "buraya kadar okudum" der. Uygulama yeniden
başladığında son commit edilen offset'ten devam eder. `auto-offset-reset: earliest` ayarı, consumer group ilk
defa çalıştığında topic'in en başından okumasını sağlar.

Consumer group'a yeni bir consumer katıldığında ya da biri çıktığında partition'lar consumer'lar arasında yeniden
dağıtılır. Buna **rebalance** denir ve bu sırada okuma kısa süreliğine durur.

#### Teslim Garantileri

| Garanti | Açıklama |
|---|---|
| At-most-once | Offset önce commit edilir, sonra işlenir. Hata olursa mesaj kaybolur. |
| At-least-once | Önce işlenir, sonra offset commit edilir. Hata olursa mesaj tekrar işlenir. Spring Kafka varsayılanı. |
| Exactly-once | Kafka transaction'ları ile sağlanır. Fakat sadece Kafka'dan okuyup Kafka'ya yazan akışlarda geçerlidir. |

Pratikte en çok **at-least-once** kullanılır. Bu durumda aynı mesaj iki kere gelebileceği için consumer'ın
**idempotent** olması gerekir. Örneğin işlenen mesajların id'si bir tabloda tutulur ve tekrar gelen mesaj atlanır.

#### Hata Yönetimi ve Dead Letter Topic

Consumer'da hata alınan mesajı sonsuza kadar denemek o partition'daki diğer mesajları da bekletir.
Örnek projede mesajı 2 kez daha deneyip yine hata alırsak ayrı bir topic'e gönderiyoruz:

```java
@Bean
public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
    return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), new FixedBackOff(1000L, 2));
}
```

`DeadLetterPublishingRecoverer` mesajı varsayılan olarak `randevu-olusturuldu-dlt` topic'ine gönderir. Bu topic'i
izleyip mesajları elle ya da bir job ile tekrar işleyebiliriz. Spring Boot `DefaultErrorHandler` bean'ini
`@KafkaListener` container'larına otomatik olarak bağlar.

#### Veritabanı ve Kafka Tutarlılığı

Randevuyu veritabanına kaydedip Kafka'ya event gönderiyoruz. Bu iki işlem tek bir transaction içinde değil
(dual write problemi). Kayıt başarılı olup Kafka'ya gönderim başarısız olursa event kaybolur. Örnek projede event'i
commit sonrasında gönderiyoruz fakat bu da kaybı engellemiyor. Bunun çözümü **Transactional Outbox Pattern**:
event aynı transaction içinde bir outbox tablosuna yazılır, ayrı bir süreç (Debezium gibi) bu tablodan okuyup
Kafka'ya gönderir. Detaylarını
[Veri Tutarlılığı: Outbox Pattern ve Debezium](https://bilisim.io/2024/12/15/spring-boot-ile-adim-adim-transactional-outbox-pattern/)
yazısında anlatmıştık.

#### Test

Kafka'yı test etmek için Docker'a ihtiyaç duymadan `spring-kafka-test` ile gömülü bir broker ayağa kaldırabiliyoruz:

```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = KafkaConfig.RANDEVU_TOPIC, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class RandevuAkisTest {
    ...
}
```

Gerçek Kafka ile test etmek için örnek projedeki `RandevuEntegrasyonTest` class'ında Testcontainers kullandık.

### Mülakat Soruları

<details>
<summary>1. Kafka'da sıralama nasıl garanti edilir?</summary>

Sadece partition içinde garanti edilir. Sırası önemli mesajlar aynı key ile gönderilir, böylece aynı partition'a
düşerler. Producer tarafında retry sırasında sıranın bozulmaması için idempotence açık olmalıdır.
</details>

<details>
<summary>2. 3 partition'lı bir topic'i 5 consumer okursa ne olur?</summary>

Aynı consumer group'taysalar 3'ü birer partition okur, 2'si boşta bekler. Farklı consumer group'lardaysalar her
grup bütün mesajları ayrı ayrı okur.
</details>

<details>
<summary>3. Kafka ile RabbitMQ arasındaki fark nedir?</summary>

Kafka mesajları okunduktan sonra silmeyen bir log'dur, birden fazla consumer group aynı mesajları okuyabilir ve
geçmişe dönüp tekrar okunabilir. RabbitMQ ise mesajı consumer'a iletip onaylandıktan sonra silen bir message
broker'dır, karmaşık yönlendirme (exchange) yapabilir. Detaylı karşılaştırma [RabbitMQ](rabbitmq.md) yazısında.
</details>

<details>
<summary>4. Consumer mesajı işledi fakat offset commit edemeden çöktü. Ne olur?</summary>

Consumer yeniden başladığında son commit edilen offset'ten okumaya başlar ve aynı mesajı tekrar işler. Bu yüzden
consumer'lar idempotent yazılmalıdır.
</details>

<details>
<summary>5. <code>acks=all</code> tek başına mesaj kaybını engeller mi?</summary>

Tek başına yetmez. `min.insync.replicas` değeri de en az 2 olmalıdır, yoksa ISR'da tek replica kaldığında
`acks=all` aslında `acks=1` gibi davranır. Replication factor genelde 3 seçilir.
</details>

<details>
<summary>6. Consumer lag nedir?</summary>

Partition'a yazılan son mesajın offset'i ile consumer'ın commit ettiği offset arasındaki farktır. Lag sürekli
artıyorsa consumer'lar yetişemiyordur; consumer sayısı (partition sayısı kadar) artırılabilir ya da işleme süresi
kısaltılabilir.
</details>

<details>
<summary>7. Partition sayısını sonradan artırabilir miyiz?</summary>

Artırabiliriz fakat azaltamayız. Artırdığımızda key'lerin partition dağılımı değişir, yani aynı key artık başka
bir partition'a gidebilir ve geçiş sırasında sıralama bozulabilir.
</details>

### Kaynaklar

- https://kafka.apache.org/documentation/
- https://docs.spring.io/spring-kafka/reference/
- https://bilisim.io/2024/08/06/spring-boot-ile-apache-kafka-kullanimi-yuksek-performansli-veri-isleme/
- https://bilisim.io/2024/12/15/spring-boot-ile-adim-adim-transactional-outbox-pattern/

Faydalı olması dileğiyle.
