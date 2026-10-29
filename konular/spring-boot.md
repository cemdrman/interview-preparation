## Spring Boot

Spring Boot mülakatlarında genelde "Spring ile Spring Boot arasındaki fark nedir?" sorusuyla başlanıyor ve
auto-configuration, bean yaşam döngüsü, `@Transactional` gibi konularla devam ediliyor. Bu yazıda bu konuları
[örnek proje](../ornek-proje) üzerinden anlatmaya çalışacağız.

#### Spring vs Spring Boot

Spring Framework bize dependency injection, transaction yönetimi, web katmanı gibi altyapıyı sağlıyor fakat bu
altyapıyı ayağa kaldırmak için bir sürü konfigürasyon yazmamız gerekiyordu. Spring Boot bu konfigürasyonları
**bizim yerimize** yapıyor:

- **Starter**: `spring-boot-starter-webmvc` eklediğimizde Spring MVC, Jackson, Tomcat gibi bağımlılıklar uyumlu
  versiyonlarıyla birlikte geliyor.
- **Auto-configuration**: Classpath'te ne olduğuna bakarak bean'leri otomatik oluşturuyor.
- **Embedded server**: Uygulamayı bir `jar` olarak `java -jar` ile çalıştırabiliyoruz, ayrıca Tomcat kurmaya gerek yok.
- **Actuator**: Health check, metrik gibi production'da ihtiyaç duyduğumuz endpoint'leri hazır sunuyor.

#### @SpringBootApplication

```java
@SpringBootApplication
public class RandevuServisiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RandevuServisiApplication.class, args);
    }
}
```

`@SpringBootApplication` aslında üç annotation'ın birleşimi:

- `@SpringBootConfiguration`: Class'ın bir konfigürasyon class'ı olduğunu belirtir.
- `@EnableAutoConfiguration`: Auto-configuration'ı açar.
- `@ComponentScan`: Bu class'ın bulunduğu paket ve **alt paketlerindeki** `@Component`, `@Service`,
  `@Repository`, `@Controller` class'larını tarar.

Bu yüzden main class'ı en üst pakete koyuyoruz. Başka bir paketteki bean'ler bulunamaz.

#### Auto-configuration Nasıl Çalışır?

Auto-configuration class'ları `@Conditional` annotation'ları ile hangi durumda çalışacaklarını belirtir:

```java
@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
public class RabbitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        ...
    }
}
```

`@ConditionalOnClass` classpath'te `RabbitTemplate` varsa çalış, `@ConditionalOnMissingBean` ise biz kendi
`RabbitTemplate` bean'imizi tanımladıysak onu kullan, tanımlamadıysak bunu oluştur demek. Örnek projede
`RabbitConfig` class'ında sadece `MessageConverter` bean'i tanımladık; Spring Boot `RabbitTemplate`'i oluştururken
bizim converter'ımızı kullandı.

Hangi auto-configuration'ın çalışıp hangisinin çalışmadığını görmek için uygulamayı `--debug` parametresi ile
başlatabiliriz.

#### Dependency Injection

Bean'leri üç şekilde inject edebiliriz: constructor, setter ve field (`@Autowired`). Örnek projede her yerde
**constructor injection** kullandık:

```java
@Service
public class RandevuService {

    private final RandevuRepository randevuRepository;
    private final DoktorClient doktorClient;

    public RandevuService(RandevuRepository randevuRepository, DoktorClient doktorClient) {
        this.randevuRepository = randevuRepository;
        this.doktorClient = doktorClient;
    }
}
```

Constructor injection ile field'lar `final` olabiliyor, bağımlılıklar açıkça görünüyor ve unit test yazarken
Spring'e ihtiyaç duymadan `new RandevuService(...)` ile nesne oluşturabiliyoruz. Tek constructor varsa
`@Autowired` yazmamıza gerek yok.

#### Bean Scope

| Scope | Açıklama |
|---|---|
| `singleton` | Varsayılan. Container'da tek bir nesne oluşturulur. |
| `prototype` | Her istendiğinde yeni nesne oluşturulur. |
| `request` | Her HTTP isteği için bir nesne. |
| `session` | Her HTTP session için bir nesne. |

Singleton bean'ler **thread-safe değildir**. Bütün istekler aynı nesneyi kullandığı için bean içinde değişken
bir state tutmamalıyız.

#### @Transactional

`@Transactional` Spring AOP ile çalışır. Spring bean'imizin etrafına bir **proxy** oluşturur, method çağrılmadan önce
transaction'ı başlatır, method bitince commit, `RuntimeException` fırlatılırsa rollback eder.

Proxy ile çalıştığı için mülakatlarda en çok sorulan tuzaklar şunlar:

```java
@Service
public class RandevuService {

    public void topluOlustur(List<RandevuIstegi> istekler) {
        istekler.forEach(this::olustur); //transaction açılmaz!
    }

    @Transactional
    public RandevuCevabi olustur(RandevuIstegi istek) {
        ...
    }
}
```

- **Self-invocation**: Aynı class içinden `this.olustur()` çağrısı proxy'den geçmediği için transaction açılmaz.
- **Checked exception**: Varsayılan olarak sadece `RuntimeException` ve `Error` rollback yapar.
  `Exception` için `@Transactional(rollbackFor = Exception.class)` yazmamız gerekir.
- **private method**: Proxy private method'ları yakalayamaz.

Aynı proxy mantığı `@Cacheable` ve `@Async` için de geçerli.

#### Transaction ve Event

Örnek projede randevuyu kaydettikten sonra Kafka'ya event gönderiyoruz. Eğer Kafka'ya method içinde gönderirsek
ve sonrasında transaction rollback olursa, veritabanında olmayan bir randevu için event göndermiş oluruz.
Bunun için `@TransactionalEventListener` kullandık:

```java
// RandevuService
eventPublisher.publishEvent(new RandevuOlusturulduEvent(...));

// RandevuEventProducer
@TransactionalEventListener
public void gonder(RandevuOlusturulduEvent event) {
    kafkaTemplate.send(KafkaConfig.RANDEVU_TOPIC, event.randevuId().toString(), event);
}
```

`@TransactionalEventListener` varsayılan olarak `AFTER_COMMIT` fazında çalışır, yani event sadece commit başarılı
olursa gönderilir. Fakat commit sonrası Kafka'ya gönderim başarısız olursa event kaybolur. Bunu garanti altına almak
için [Transactional Outbox Pattern](https://bilisim.io/2024/12/15/spring-boot-ile-adim-adim-transactional-outbox-pattern/)
kullanılmalıdır.

#### Exception Handling ve ProblemDetail

Hataları her controller'da ayrı ayrı yakalamak yerine `@RestControllerAdvice` ile tek bir yerde topluyoruz.
`ProblemDetail` ise hata cevapları için standart (RFC 9457) bir format sunuyor:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IsKuraliException.class)
    public ProblemDetail isKurali(IsKuraliException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }
}
```

```json
{"detail":"Sadece operatör doktorlar ameliyata girebilir","instance":"/randevular","status":422,"title":"Unprocessable Content"}
```

#### Validation

```java
public record RandevuIstegi(
        @NotBlank String hastaAdi,
        @NotBlank String hastaTelefon,
        @NotNull Long doktorId,
        @NotNull @Future LocalDateTime tarih,
        @NotNull RandevuTipi tip) {
}

@PostMapping
public ResponseEntity<RandevuCevabi> olustur(@Valid @RequestBody RandevuIstegi istek) {
    ...
}
```

`@Valid` yazmazsak annotation'lar **çalışmaz**. Validasyon hatasında Spring `MethodArgumentNotValidException`
fırlatır ve `400 Bad Request` döner.

#### Configuration ve Profile

Ayarları `application.yml` dosyasında tutuyoruz. Ortama göre farklı ayarlar için `application-prod.yml` gibi
profile dosyaları oluşturup `--spring.profiles.active=prod` ile seçebiliyoruz. Aynı ayar birden fazla yerde
tanımlıysa öncelik sırası kabaca şöyle (üstteki kazanır):

1. Komut satırı parametreleri (`--server.port=9090`)
2. Environment variable'lar (`SERVER_PORT=9090`)
3. Profile'a özel dosyalar (`application-prod.yml`)
4. `application.yml`

#### Virtual Thread

Java 21 ve üstünde tek bir ayar ile Tomcat'in her isteği virtual thread üzerinde işlemesini sağlayabiliyoruz:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

Örnek projeyi çalıştırdığımızda loglarda thread isimlerinin `tomcat-handler-41` gibi olduğunu görebiliriz.

#### Spring Boot 4 ile Gelenler

Spring Boot 4 (Kasım 2025) ile gelen ve mülakatta karşımıza çıkabilecek değişiklikler:

- Spring Framework 7 ve Jakarta EE 11 tabanlı. Minimum Java 17.
- Auto-configuration'lar daha küçük modüllere bölündü. `spring-boot-starter-web` yerine
  `spring-boot-starter-webmvc` kullanılıyor, test için de `spring-boot-starter-webmvc-test` gibi teknolojiye özel
  starter'lar var.
- Jackson 3 varsayılan oldu, paket adı `com.fasterxml.jackson` yerine `tools.jackson`.
- Testlerde `@MockBean` yerine Spring Framework'ün `@MockitoBean` annotation'ı kullanılıyor.
- HTTP servis client'ları (`@HttpExchange`) için `@ImportHttpServices` ile kolay konfigürasyon geldi.
  Detayı [Feign Client](feign-client.md) yazısında.

#### Test

Spring Boot'ta bütün context'i ayağa kaldıran `@SpringBootTest` dışında sadece belli bir katmanı yükleyen
**slice** testler var:

| Annotation | Yüklenenler |
|---|---|
| `@WebMvcTest` | Controller, `@RestControllerAdvice`, MockMvc |
| `@DataJpaTest` | JPA repository'ler, gömülü veritabanı |
| `@SpringBootTest` | Bütün uygulama |

```java
@WebMvcTest(DoktorController.class)
class DoktorControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private DoktorService doktorService;

    @Test
    void doktorYoksa404Doner() {
        given(doktorService.getir(99L)).willThrow(new DoktorBulunamadiException(99L));

        assertThat(mockMvc.get().uri("/doktorlar/99"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .extractingPath("$.detail").isEqualTo("Doktor bulunamadı: 99");
    }
}
```

Örnek projeyi yazarken karşılaştığımız bir tuzak: `@EnableCaching` annotation'ını main class'a koyduğumuzda
`@WebMvcTest` ayağa kalkmadı çünkü slice test `CacheManager` bean'ini yüklemiyordu fakat main class'taki annotation
onu istiyordu. Çözüm olarak `@EnableCaching`'i ayrı bir `@Configuration` class'ına taşıdık. Aynı durum
`@EnableScheduling`, `@EnableJpaAuditing` gibi annotation'lar için de geçerli.

Testcontainers ile entegrasyon testi detaylarını
[Testcontainers, TestRestTemplate ve JUnit5 ile Integration Test](https://bilisim.io/2024/03/20/testcontainers-testresttemplate-ve-junit5-ile-integration-test/)
yazısında anlatmıştık.

### Mülakat Soruları

<details>
<summary>1. Spring Boot uygulaması başlarken neler olur?</summary>

`SpringApplication.run` environment'ı hazırlar (`application.yml`, environment variable'lar), `ApplicationContext`
oluşturur, component scan ve auto-configuration ile bean tanımlarını toplar, bean'leri oluşturup bağımlılıklarını
inject eder, embedded server'ı başlatır ve son olarak `ApplicationRunner` / `CommandLineRunner` bean'lerini çalıştırır.
</details>

<details>
<summary>2. <code>@Component</code>, <code>@Service</code>, <code>@Repository</code> arasındaki fark nedir?</summary>

Üçü de bean tanımlar. `@Service` ve `@Repository` aslında `@Component`'tir, anlam katmak için kullanılır.
`@Repository` ayrıca veritabanı exception'larını Spring'in `DataAccessException` hiyerarşisine çevirir.
</details>

<details>
<summary>3. Aynı tipte iki bean varsa ne olur?</summary>

Spring hangisini inject edeceğini bilemez ve `NoUniqueBeanDefinitionException` fırlatır. `@Primary` ile varsayılan
bean'i belirtebilir ya da `@Qualifier("isim")` ile hangisini istediğimizi söyleyebiliriz.
</details>

<details>
<summary>4. Aynı class içinden <code>@Transactional</code> method çağırırsak ne olur?</summary>

Transaction açılmaz. `@Transactional` proxy ile çalışır, `this.method()` çağrısı proxy'den geçmez.
Method'u başka bir bean'e taşımak en temiz çözümdür.
</details>

<details>
<summary>5. <code>@Transactional(readOnly = true)</code> ne işe yarar?</summary>

Hibernate dirty checking yapmaz, snapshot tutmaz, bu yüzden okuma işlemlerinde performans kazandırır. Bazı
veritabanı ve connection pool ayarlarında sorguyu read replica'ya yönlendirmek için de kullanılır.
</details>

<details>
<summary>6. <code>spring.jpa.open-in-view</code> nedir, neden kapatırız?</summary>

Varsayılan olarak açıktır ve veritabanı bağlantısını HTTP isteği bitene kadar açık tutar. Bu sayede controller'da
lazy alanlara erişebiliriz fakat bağlantı uzun süre meşgul kalır ve view katmanında fark etmeden sorgu atılır.
Örnek projede `false` yaptık.
</details>

<details>
<summary>7. <code>@SpringBootTest</code> ile <code>@WebMvcTest</code> arasındaki fark nedir?</summary>

`@SpringBootTest` bütün uygulamayı yükler, `@WebMvcTest` sadece web katmanını yükler ve service'leri
`@MockitoBean` ile mock'lamamızı bekler. Slice testler daha hızlıdır.
</details>

<details>
<summary>8. Actuator ile hangi bilgilere ulaşabiliriz?</summary>

`/actuator/health` ile uygulamanın ve bağlı olduğu veritabanı, Redis, RabbitMQ gibi sistemlerin durumunu,
`/actuator/metrics` ile metrikleri, `/actuator/caches` ile cache'leri görebiliriz. Production'da sadece gerekli
endpoint'ler açılmalı ve güvenlik altına alınmalıdır.
</details>

### Kaynaklar

- https://docs.spring.io/spring-boot/reference/
- https://bilisim.io/2024/03/20/testcontainers-testresttemplate-ve-junit5-ile-integration-test/
- https://bilisim.io/2024/12/15/spring-boot-ile-adim-adim-transactional-outbox-pattern/

Faydalı olması dileğiyle.
