## Redis

"Uygulamanız yavaşladı, ne yaparsınız?" sorusunun cevaplarından biri genelde cache oluyor ve arkasından
"cache'teki veri güncel değilse ne olacak?" sorusu geliyor. Bu yazıda Redis'i ve Spring Cache kullanımını
[örnek proje](../ornek-proje) üzerinden anlatmaya çalışacağız.

#### Redis Nedir?

Redis verileri **bellekte** (in-memory) tutan bir key-value veritabanıdır. Veriler bellekte olduğu için okuma ve
yazma işlemleri milisaniyenin altında sürer. Komutları tek bir thread üzerinde sırayla çalıştırdığı için tek bir
komut **atomik**tir.

En çok kullanıldığı yerler:

- Cache
- Session saklama
- Rate limiting (`INCR` + `EXPIRE`)
- Distributed lock (`SET key value NX PX 30000`)
- Sıralama tabloları (Sorted Set)

#### Veri Tipleri

| Tip | Örnek Kullanım |
|---|---|
| String | Cache, sayaç |
| Hash | Bir nesnenin alanları (`HSET doktor:1 isim "Ayşe"`) |
| List | Kuyruk, son 10 işlem |
| Set | Tekil elemanlar, etiketler |
| Sorted Set | Puan tablosu, öncelikli kuyruk |
| Stream | Kafka'ya benzer event log |

#### Spring Cache

Spring Cache bir **abstraction**'dır. Kod tarafında `@Cacheable` gibi annotation'lar kullanırız, arkada Redis,
Caffeine ya da basit bir `ConcurrentHashMap` olması kodumuzu değiştirmez.

```java
@Cacheable(cacheNames = "doktorlar", key = "#id")
public DoktorDto getir(Long id) {
    log.info("Doktor veritabanından okunuyor: {}", id);
    return doktorRepository.findById(id)
            .map(DoktorDto::from)
            .orElseThrow(() -> new DoktorBulunamadiException(id));
}

@Transactional
@CacheEvict(cacheNames = "doktorlar", key = "#id")
public DoktorDto operatorDurumunuGuncelle(Long id, boolean operator) {
    ...
}
```

Gördüğümüz üzere `getir` method'u ilk çağrıldığında veritabanına gidiyor ve sonucu Redis'te `doktorlar::1` key'i ile
saklıyor. İkinci çağrıda method **hiç çalışmıyor**, sonuç Redis'ten dönüyor. Doktor güncellendiğinde
`@CacheEvict` ile cache'teki kaydı siliyoruz, bir sonraki istekte güncel veri veritabanından okunuyor.

| Annotation | Ne yapar? |
|---|---|
| `@Cacheable` | Cache'te varsa method'u çalıştırmadan döner, yoksa çalıştırıp sonucu cache'e yazar. |
| `@CachePut` | Method'u her zaman çalıştırır ve sonucu cache'e yazar. |
| `@CacheEvict` | Cache'ten siler. `allEntries = true` ile bütün cache temizlenir. |

`@Cacheable` da `@Transactional` gibi proxy ile çalışır. Aynı class içinden çağrılan method'larda cache **çalışmaz**.

#### Redis Cache Ayarları

```java
@EnableCaching
@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer doktorCacheCustomizer() {
        RedisCacheConfiguration doktorCache = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(new JacksonJsonRedisSerializer<>(DoktorDto.class)));

        return builder -> builder.withCacheConfiguration("doktorlar", doktorCache);
    }
}
```

- **TTL**: Her kayıt 10 dakika sonra silinir. TTL vermezsek, `@CacheEvict` unutulan bir yerde cache sonsuza kadar
  eski veriyi döner.
- **Serializer**: Varsayılan olarak Java serialization kullanılır. Bu durumda class'ın `Serializable` olması
  gerekir ve Redis'teki veri okunamaz. JSON serializer ile veri şöyle saklanır:

```
redis-cli --raw GET doktorlar::1
{"id":1,"isim":"Ayşe Yılmaz","brans":"Genel Cerrahi","operator":true}
```

Burada önemli olan bir nokta da cache'e JPA **entity** yerine **DTO** koymamız. Entity'ler lazy alanlar ve
Hibernate proxy'leri içerdiği için serialize edilirken sorun çıkarabilir.

#### Cache Stratejileri

| Strateji | Açıklama |
|---|---|
| Cache-aside | Uygulama önce cache'e bakar, yoksa veritabanından okuyup cache'e yazar. `@Cacheable` budur. |
| Write-through | Yazma işlemi hem cache'e hem veritabanına aynı anda yapılır. |
| Write-behind | Önce cache'e yazılır, veritabanına sonra toplu yazılır. Hızlıdır ama veri kaybı riski vardır. |

#### Cache Problemleri

- **Cache stampede**: Çok okunan bir kaydın TTL'i dolduğunda yüzlerce istek aynı anda veritabanına gider.
  `@Cacheable(sync = true)` ile aynı instance içinde tek bir isteğin veritabanına gitmesi sağlanabilir.
- **Cache penetration**: Veritabanında olmayan bir id ile sürekli istek gelirse her istek veritabanına gider.
  Boş sonuç da kısa bir TTL ile cache'lenebilir ya da Bloom filter kullanılabilir.
- **Cache avalanche**: Çok sayıda kaydın TTL'i aynı anda dolar. TTL'lere rastgele birkaç saniye eklenerek
  dağıtılabilir.

#### Eviction Policy

Redis'e verdiğimiz bellek (`maxmemory`) dolduğunda hangi kayıtların silineceğini `maxmemory-policy` belirler.
Cache olarak kullanıyorsak genelde `allkeys-lru` (en uzun süredir kullanılmayanı sil) seçilir. Varsayılan değer
`noeviction`'dır, yani bellek dolunca yazma işlemleri hata verir.

#### Kalıcılık (Persistence)

- **RDB**: Belirli aralıklarla bellekteki verinin anlık görüntüsü (snapshot) diske yazılır.
- **AOF**: Her yazma komutu bir dosyaya eklenir. Daha az veri kaybı olur fakat dosya büyür.

Sadece cache olarak kullanıyorsak kalıcılık kapatılabilir.

#### Distributed Lock

Birden fazla instance'ı olan bir uygulamada `@Scheduled` job'ların her instance'ta çalışmasını engellemek için
Redis ile distributed lock kullanabiliriz. Bu konuyu
[Your @Scheduled Job Runs Three Times. ShedLock Fixes That.](https://medium.com/@cemdrman/your-scheduled-job-runs-three-times-shedlock-fixes-that-0c63c741e606)
yazısında anlatmıştık.

### Mülakat Soruları

<details>
<summary>1. Redis tek thread ile nasıl bu kadar hızlı?</summary>

Veriler bellekte olduğu için disk beklemesi yoktur. Komutlar çok kısa sürdüğü için tek thread yeterlidir ve
lock maliyeti olmaz. Ağ trafiği için Redis 6 ile I/O thread'leri eklendi fakat komutlar hâlâ tek thread'de çalışır.
</details>

<details>
<summary>2. Cache'teki veri veritabanıyla tutarsız olursa ne yaparsınız?</summary>

Veri güncellendiğinde `@CacheEvict` ile cache temizlenir ve TTL verilerek eski verinin en fazla ne kadar
yaşayacağı sınırlanır. Birden fazla servis aynı veriyi güncelliyorsa event ile cache temizlenebilir.
Tam tutarlılık gereken verilerde (bakiye gibi) cache kullanmamak daha doğrudur.
</details>

<details>
<summary>3. Neden entity yerine DTO cache'liyoruz?</summary>

Entity'ler Hibernate proxy'leri ve lazy alanlar içerebilir, serialize edilirken `LazyInitializationException`
alınabilir. Ayrıca cache'ten dönen entity managed değildir, değiştirilirse fark edilmez.
</details>

<details>
<summary>4. Local cache (Caffeine) ile Redis arasında nasıl seçim yaparsınız?</summary>

Caffeine uygulamanın belleğinde çalışır, ağ maliyeti yoktur ve çok hızlıdır fakat her instance'ın kendi cache'i
olur ve instance'lar arasında tutarsızlık oluşabilir. Redis ortak bir cache sunar fakat her erişim bir ağ
çağrısıdır. Az değişen veriler için ikisi birlikte de kullanılabilir.
</details>

<details>
<summary>5. <code>KEYS *</code> komutunu production'da neden kullanmamalıyız?</summary>

Bütün key'leri tarar ve Redis tek thread çalıştığı için bu sırada diğer bütün komutlar bekler. Yerine `SCAN`
kullanılmalıdır.
</details>

<details>
<summary>6. Redis ile distributed lock alırken nelere dikkat edilmeli?</summary>

Lock `SET key value NX PX` ile tek komutta alınmalı ve mutlaka bir süre verilmelidir, yoksa lock'u alan instance
çökerse lock hiç bırakılmaz. Lock bırakılırken değerin bizim yazdığımız değer olduğu kontrol edilmelidir. Redisson
ya da ShedLock gibi kütüphaneler bunları hazır sunar.
</details>

### Kaynaklar

- https://redis.io/docs/latest/
- https://docs.spring.io/spring-boot/reference/io/caching.html
- https://medium.com/@cemdrman/your-scheduled-job-runs-three-times-shedlock-fixes-that-0c63c741e606

Faydalı olması dileğiyle.
