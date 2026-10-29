## Feign Client

Mikroservisler arasında senkron haberleşme gerektiğinde Spring dünyasında en çok kullanılan araçlardan biri
Feign Client. Mülakatlarda "diğer servis cevap vermezse ne olur?" sorusu mutlaka geliyor. Bu yazıda Feign Client'ı
[örnek proje](../ornek-proje) üzerinden anlatmaya çalışacağız.

#### Feign Client Nedir?

Feign **declarative** bir HTTP client'tır. `RestTemplate` ile URL oluşturup, isteği gönderip cevabı nesneye
çevirmek yerine sadece bir **interface** yazıyoruz, implementasyonu Spring bizim yerimize oluşturuyor.

Örnek projede `randevu-servisi` randevu oluşturmadan önce doktor bilgisini `doktor-servisi`'nden alıyor:

```java
@FeignClient(name = "doktor-servisi", url = "${doktor-servisi.url}", configuration = DoktorClientConfig.class)
public interface DoktorClient {

    @GetMapping("/doktorlar/{id}")
    DoktorCevap getir(@PathVariable("id") Long id);
}
```

Gördüğümüz üzere controller yazar gibi Spring MVC annotation'larını kullanıyoruz. Feign'i aktif etmek için main
class'a `@EnableFeignClients` eklememiz gerekiyor. Artık `DoktorClient`'ı herhangi bir bean'e inject edip normal bir
method gibi çağırabiliyoruz:

```java
DoktorCevap doktor = doktorClient.getir(istek.doktorId());
```

Burada `@PathVariable("id")` içinde ismi açıkça yazdık. Feign interface'lerinde parametre isimleri her zaman
derlenmiş class'tan okunamadığı için ismi yazmak daha güvenli.

#### Konfigürasyon

```java
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
```

Mülakatlarda sorulabilecek bir tuzak: Bu class'a `@Configuration` yazarsak component scan onu bulur ve ayarlar
**bütün** Feign client'lara uygulanır. Sadece bir client'a özel ayar istiyorsak `@FeignClient` içindeki
`configuration` parametresine vermeliyiz.

#### Error Decoder

Feign varsayılan olarak 2xx dışındaki her cevapta `FeignException` fırlatır. Bunu yakalayıp status koduna bakmak
yerine `ErrorDecoder` ile kendi exception'larımıza çeviriyoruz. Örnek projede `doktor-servisi` 404 döndüğünde
`DoktorBulunamadiException` fırlatılıyor ve `randevu-servisi` de kendi kullanıcısına 404 dönüyor:

```json
{"detail":"Doktor bulunamadı: http://localhost:8081/doktorlar/99","status":404,"title":"Not Found"}
```

#### Timeout

Karşı servis cevap vermezse isteğimiz ne kadar bekleyecek? Timeout vermezsek thread'lerimiz uzun süre bekleyebilir
ve karşı servisteki yavaşlık bizim servisimize de yayılır (cascading failure).

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          doktor-servisi:
            connect-timeout: 2000
            read-timeout: 3000
```

- `connect-timeout`: Bağlantı kurulana kadar beklenecek süre.
- `read-timeout`: Bağlantı kurulduktan sonra cevap için beklenecek süre.

#### Logging

`Logger.Level` dört değer alır: `NONE`, `BASIC` (method, URL, status, süre), `HEADERS` ve `FULL` (body dahil).
Logların görünmesi için client'ın paketinin `DEBUG` seviyesinde olması gerekiyor:

```yaml
logging:
  level:
    com.cemdrman.randevu.client: DEBUG
```

```
[DoktorClient#getir] ---> GET http://localhost:8081/doktorlar/1 HTTP/1.1
[DoktorClient#getir] <--- HTTP/1.1 200 (11ms)
```

`FULL` seviyesinde kişisel veriler ve token'lar loglanabileceği için production'da dikkatli olmalıyız.

#### Retry ve Circuit Breaker

Feign varsayılan olarak **retry yapmaz** (`Retryer.NEVER_RETRY`). Retry eklerken sadece idempotent istekleri
(GET gibi) tekrar denemeliyiz; bir POST isteğini tekrar denemek aynı randevunun iki kere oluşturulmasına sebep olabilir.

Karşı servis uzun süre hata veriyorsa her isteği timeout'a kadar bekletmek yerine **circuit breaker** ile istekleri
bir süreliğine hiç göndermeyebiliriz. Spring Cloud CircuitBreaker ve Resilience4j ile Feign'e fallback
tanımlanabiliyor:

```java
@FeignClient(name = "doktor-servisi", url = "${doktor-servisi.url}", fallback = DoktorClientFallback.class)
public interface DoktorClient {
    ...
}
```

```yaml
spring:
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true
```

Örnek projede circuit breaker eklemedik. Randevu oluştururken doktor bilgisi olmadan devam edemediğimiz için
fallback yerine hatayı kullanıcıya dönmek daha doğru.

#### Service Discovery

`url` parametresi vermezsek Feign `name` değerini servis adı olarak kullanır ve Eureka, Consul ya da Kubernetes
gibi bir service discovery üzerinden servisin adresini bulur. Spring Cloud LoadBalancer da istekleri servisin
instance'ları arasında dağıtır.

#### Feign'in Geleceği: HTTP Interface

Spring Cloud OpenFeign artık **feature complete** durumda, yani sadece hata düzeltmeleri yapılıyor. Spring ekibi yeni
projeler için Spring Framework'ün kendi **HTTP Interface** client'ını öneriyor. Aynı client'ı şöyle yazabiliyoruz:

```java
@HttpExchange("/doktorlar")
public interface DoktorHttpClient {

    @GetExchange("/{id}")
    DoktorCevap getir(@PathVariable Long id);
}

@Configuration
public class HttpClientConfig {

    @Bean
    public DoktorHttpClient doktorHttpClient(@Value("${doktor-servisi.url}") String url) {
        RestClient restClient = RestClient.builder().baseUrl(url).build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(DoktorHttpClient.class);
    }
}
```

Spring Framework 7 ile `@ImportHttpServices` annotation'ı geldi ve bu konfigürasyonu yazmamıza da gerek kalmadı.
Spring Cloud bağımlılığı olmadığı için yeni projelerde HTTP Interface tercih edilebilir, mevcut Feign client'ları
ise interface yapısı benzer olduğu için kolayca taşınabilir.

#### Test

Feign client'ı test etmek için karşı servisi ayağa kaldırmamıza gerek yok. Örnek projede JDK'nın içinde gelen
`HttpServer` ile sahte bir doktor servisi oluşturduk ve adresini `@DynamicPropertySource` ile verdik:

```java
@DynamicPropertySource
static void doktorServisiUrl(DynamicPropertyRegistry registry) {
    registry.add("doktor-servisi.url", () -> "http://localhost:" + SAHTE_DOKTOR_SERVISI.getAddress().getPort());
}

@Test
void doktorYoksaErrorDecoderKendiExceptionimiziFirlatir() {
    assertThatThrownBy(() -> doktorClient.getir(99L))
            .isInstanceOf(DoktorBulunamadiException.class)
            .hasMessageContaining("/doktorlar/99");
}
```

Daha kapsamlı senaryolar için WireMock da kullanılabilir.

### Mülakat Soruları

<details>
<summary>1. Feign Client ile RestTemplate arasındaki fark nedir?</summary>

RestTemplate ile her istekte URL, method ve cevap tipini kodda belirtiriz. Feign'de sadece interface yazarız,
implementasyonu Spring oluşturur. Feign ayrıca timeout, logging, error decoder ve service discovery ayarlarını
client bazında konfigürasyonla yönetmemizi sağlar. RestTemplate bakım modunda, yerine `RestClient` önerilir.
</details>

<details>
<summary>2. Karşı servis 30 saniye cevap vermezse ne olur?</summary>

Timeout tanımlanmadıysa isteği yapan thread bekler ve bu durum çok sayıda istekte bizim servisimizi de kilitler.
Bu yüzden `connect-timeout` ve `read-timeout` mutlaka verilmeli, sürekli hata veren servisler için circuit breaker
kullanılmalıdır.
</details>

<details>
<summary>3. Feign client konfigürasyon class'ına <code>@Configuration</code> yazarsak ne olur?</summary>

Component scan class'ı bulur ve içindeki bean'ler bütün Feign client'lara uygulanır. Sadece bir client'a özel
olması için `@Configuration` yazmadan `@FeignClient(configuration = ...)` ile verilmelidir.
</details>

<details>
<summary>4. Bütün isteklere Authorization header'ı nasıl ekleriz?</summary>

Bir `RequestInterceptor` bean'i tanımlarız. `apply(RequestTemplate template)` method'unda
`template.header("Authorization", "Bearer " + token)` ile her isteğe header eklenir.
</details>

<details>
<summary>5. Senkron (Feign) ve asenkron (Kafka) haberleşmeyi ne zaman seçersiniz?</summary>

Cevaba hemen ihtiyacımız varsa (randevu oluşturmadan önce doktorun operatör olup olmadığını bilmek gibi) senkron
çağrı yaparız. İşin sonucunu beklememiz gerekmiyorsa (SMS gönderimi gibi) asenkron mesajlaşma kullanırız. Asenkron
haberleşme servisleri birbirinden bağımsız hale getirir, karşı servis kapalı olsa bile mesaj kaybolmaz.
</details>

### Kaynaklar

- https://docs.spring.io/spring-cloud-openfeign/reference/
- https://docs.spring.io/spring-framework/reference/integration/rest-clients.html

Faydalı olması dileğiyle.
