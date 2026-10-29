## Yeni Java Özellikleri (Java 9 → Java 27)

Mülakatlarda hâlâ Java 8 ile gelen lambda ve Stream API soruları çok soruluyor fakat artık bunun yanında
"record nedir?", "sealed class ne işe yarar?", "virtual thread platform thread'den ne farkı var?" gibi
sorular da gelmeye başladı. Bu yazıda Java 9'dan Java 27'ye kadar gelen ve mülakatlarda en çok karşımıza çıkan
özellikleri örneklerle anlatmaya çalışacağız.

Örneklerin büyük kısmı bu klasördeki class'larda çalışır halde duruyor. Proje Java 21 ile derlendiği için
Java 22 ve sonrasına ait örnekler sadece bu dosyada yer alıyor; onları denemek için ilgili JDK sürümü gerekiyor.
Preview özellikleri denemek için `--enable-preview` parametresini vermemiz gerekiyor.

| Sürüm | LTS | Mülakatta öne çıkanlar |
|---|---|---|
| Java 9 | | `List.of`, `Set.of`, `Map.of`, module sistemi |
| Java 10 | | `var` |
| Java 11 | ✔ | `String` methodları, `HttpClient` |
| Java 14 | | switch expression, helpful NullPointerException |
| Java 15 | | text block |
| Java 16 | | record, `instanceof` pattern matching, `Stream.toList()` |
| Java 17 | ✔ | sealed class |
| Java 21 | ✔ | virtual thread, pattern matching for switch, record pattern, sequenced collection |
| Java 22 | | unnamed variable (`_`) |
| Java 24 | | stream gatherer |
| Java 25 | ✔ | scoped value, flexible constructor body, compact source file, module import |
| Java 26 | | HTTP/3, final field'ların reflection ile değiştirilmesine uyarı |
| Java 27 | | compact object headers varsayılan, her ortamda G1, lazy constant (preview), structured concurrency (preview) |

### Java 9 - 15

#### Collection Factory Methods

Java 9 ile `List.of`, `Set.of` ve `Map.of` methodları geldi. `question1` klasöründeki `CollectionsMain.java`
class'ında da `List.of` kullanmıştık. Bu methodlar ile oluşturulan collection'lar **immutable**'dır.

```java
List<String> nameList = List.of("elif", "emir", "elif");
System.out.println(nameList); //[elif, emir, elif]

nameList.add("cem"); //UnsupportedOperationException

Set.of("elif", "emir", "elif"); //IllegalArgumentException: duplicate element: elif
```

Gördüğümüz üzere `List.of` tekrar eden elemanlara izin veriyor fakat `Set.of` tekrar eden eleman gördüğünde
compile time'da değil **runtime**'da hata fırlatıyor. Ayrıca bu methodlar `null` elemana izin vermez,
`List.of("cem", null)` çağrısı `NullPointerException` fırlatır.

#### var

Java 10 ile local değişkenlerde tipi yazmak yerine `var` kullanabiliyoruz. Burada önemli olan nokta Java hâlâ
**statically typed** bir dil. Tipi compiler bizim yerimize çıkarıyor (type inference).

```java
var isimler = new ArrayList<String>();
isimler.add("cem");
isimler.add(5); //does not compile

var x; //does not compile
var y = null; //does not compile
```

`var` sadece **local değişkenlerde** kullanılabilir. Class field'larında, method parametrelerinde ve method
dönüş tiplerinde kullanılamaz. Değer atanmadan ya da `null` ile tanımlanamaz çünkü compiler tipi çıkaramaz.

#### String Methods

Java 11 ile `String` class'ına sık kullandığımız birkaç method eklendi:

```java
System.out.println("   ".isBlank()); //true
System.out.println("   ".isEmpty()); //false

String bosluklu = " cem ";
System.out.println(bosluklu.trim().length()); //5
System.out.println(bosluklu.strip().length()); //3

System.out.println("ab".repeat(3)); //ababab
System.out.println("satir1\nsatir2\nsatir3".lines().count()); //3
```

Mülakatlarda en çok `trim()` ile `strip()` farkı soruluyor. `trim()` sadece ` ` ve altındaki karakterleri
siler, `strip()` ise **Unicode** boşluk karakterlerini de tanır. Yukarıdaki örnekte ` ` bir Unicode boşluk
karakteri olduğu için `trim()` onu silmedi.

#### Switch Expression

Java 14 ile switch artık bir **değer döndürebiliyor**.

```java
int calismaSaati = switch (gun) {
    case "CUMARTESI", "PAZAR" -> 0;
    case "CUMA" -> {
        System.out.println("hafta sonu yaklaşıyor");
        yield 6;
    }
    default -> 8;
};
```

Ok (`->`) kullanımında `break` yazmamıza gerek yok çünkü **fall-through** yok. Bir case'de birden fazla satır
çalıştırmak istersek süslü parantez açıp değeri `yield` ile döndürüyoruz. Switch expression bir değer
döndürdüğü için bütün ihtimalleri karşılamak zorunda (exhaustive), bu yüzden burada `default` olmadan kod derlenmez.

#### Helpful NullPointerException

Java 14 öncesinde zincirleme method çağrılarında NPE aldığımızda hangi çağrının `null` döndüğünü bulmak
zordu. Java 14 ile gelen ve Java 15'ten itibaren varsayılan olarak açık olan bu özellik sayesinde artık hata mesajı
bunu bize söylüyor:

```java
record Adres(String sehir) {}
record Hasta(String isim, Adres adres) {}

Hasta hasta = new Hasta("Ali", new Adres(null));
hasta.adres().sehir().length();
//Cannot invoke "String.length()" because the return value of "Adres.sehir()" is null
```

#### Text Block

Java 15 ile çok satırlı String'leri `"""` ile yazabiliyoruz. JSON, SQL ya da HTML yazarken `\n` ve `+`
kalabalığından kurtuluyoruz.

```java
String json = """
        {
          "isim": "cem",
          "dil": "java"
        }
        """;
```

Girintiyi kapanış `"""` karakterlerinin konumu belirliyor. Kapanış ayrı bir satırda olduğu için String'in
sonunda bir `\n` karakteri **vardır**.

### Java 16 - 17

#### Record

Record'lar veri taşımak için kullanılan, **immutable** ve boilerplate kodu azaltan özel class'lardır.
Constructor, getter (accessor), `equals()`, `hashCode()` ve `toString()` methodlarını compiler bizim yerimize ekler.

```java
record Hemsire(String isim) {}

Hemsire hemsire = new Hemsire("Zeynep");
System.out.println(hemsire.isim()); //Zeynep
System.out.println(hemsire); //Hemsire[isim=Zeynep]
```

Record'larda setter yoktur, field'lar `final`'dır ve record'lar başka bir class'tan extend edilemez, kendileri de
extend edilemez. Fakat interface implement edebilirler. Compact constructor ve constructor overloading
detaylarını [Java Record](https://bilisim.io/2024/10/06/9126/) yazısında anlatmıştık.

#### Pattern Matching for instanceof

Java 16 öncesinde `instanceof` ile kontrol ettikten sonra bir de cast etmemiz gerekiyordu:

```java
if (obj instanceof Hemsire) {
    Hemsire hemsire = (Hemsire) obj;
    return hemsire.isim();
}
```

Artık kontrol ve cast işlemini tek satırda yapabiliyoruz:

```java
if (!(obj instanceof Hemsire hemsire)) {
    return "tanımsız";
}
return hemsire.isim();
```

Burada dikkat etmemiz gereken nokta `hemsire` değişkeninin scope'u. Değişken sadece eşleşmenin **kesin olduğu**
yerde kullanılabilir (flow scoping). Yukarıdaki örnekte eşleşmediği durumda method'dan çıktığımız için `if`
bloğundan sonra `hemsire` değişkenini kullanabiliyoruz.

Aynı sürümde `Stream.toList()` methodu da geldi. `collect(Collectors.toList())` ile farkı, dönen listenin
**unmodifiable** olması.

#### Sealed Class

Java 17 ile bir class ya da interface'i kimlerin extend/implement edebileceğini sınırlayabiliyoruz.
`question2` klasöründeki hastane örneğini düşünelim. Hastanede sadece doktor ve hemşireler var:

```java
sealed interface Personel permits Doktor, Hemsire {}

record Doktor(String isim, boolean operator) implements Personel {}
record Hemsire(String isim) implements Personel {}

record Hasta(String isim) implements Personel {} //does not compile
```

`permits` listesinde olan class'lar **final**, **sealed** ya da **non-sealed** olmak zorundadır. Record'lar zaten
final olduğu için ayrıca belirtmemize gerek kalmadı. Sealed class'ların asıl faydasını bir sonraki başlıkta
switch ile birlikte göreceğiz.

### Java 21

#### Pattern Matching for Switch ve Record Pattern

Java 21 ile switch içinde tip kontrolü yapabiliyor, record'ları parçalarına ayırabiliyoruz (deconstruction).
`when` ile de ek koşul (guard) yazabiliyoruz.

```java
static String gorev(Personel personel) {
    return switch (personel) {
        case Doktor(String isim, boolean operator) when operator -> isim + " ameliyata girebilir";
        case Doktor d -> d.isim() + " sadece muayene yapar";
        case Hemsire h -> h.isim() + " ameliyata girebilir";
    };
}
```

Gördüğümüz üzere `default` yazmadık. `Personel` sealed olduğu için compiler bütün alt tipleri biliyor ve
switch'in bütün ihtimalleri karşıladığını kontrol edebiliyor. Yarın `Personel`'e yeni bir tip eklersek bu switch
**derlenmez**, böylece unuttuğumuz yerleri compiler bize gösterir:

```
error: the switch expression does not cover all possible input values
```

Case'lerin sırası da önemli. Eğer `case Doktor d` satırını en üste yazarsak bütün doktorları o yakalayacağı için
altındaki satır hiç çalışmaz ve kod derlenmez:

```
error: this case label is dominated by a preceding case label
```

Ayrıca switch'e `null` gelirse `NullPointerException` fırlatılır. Bunu istemiyorsak `case null ->` yazabiliriz.

#### Sequenced Collections

Java 21 öncesinde bir listenin son elemanını almak için `list.get(list.size() - 1)` yazıyorduk.
Artık sırası olan collection'lar için ortak bir interface var: `SequencedCollection`.

```java
List<String> nameList = new ArrayList<>(List.of("elif", "emir", "cem"));

System.out.println(nameList.getFirst()); //elif
System.out.println(nameList.getLast()); //cem
System.out.println(nameList.reversed()); //[cem, emir, elif]

LinkedHashSet<String> nameSet = new LinkedHashSet<>(nameList);
nameSet.addFirst("nehir");
System.out.println(nameSet); //[nehir, elif, emir, cem]
```

`List`, `Deque`, `LinkedHashSet`, `SortedSet` bu interface'i kullanır. `HashSet` ise sıra garantisi vermediği
için kullanmaz. `reversed()` yeni bir liste oluşturmaz, var olan listenin ters **görünümünü** (view) döner.

#### Virtual Thread

Klasik thread'ler (platform thread) işletim sistemi thread'lerine bire bir bağlıdır ve oluşturmaları maliyetlidir.
Virtual thread'leri ise JVM yönetir ve bir milyon tane bile oluşturabiliriz.

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 10_000).forEach(i -> executor.submit(() -> {
        Thread.sleep(Duration.ofSeconds(1));
        return i;
    }));
}
//10.000 görev yaklaşık 1 saniyede tamamlanır
```

Her biri 1 saniye bekleyen 10.000 görev yaklaşık 1 saniyede bitti. Bir virtual thread `sleep` ya da I/O
beklerken bağlı olduğu platform thread'i (carrier thread) bırakır ve o thread başka bir işi çalıştırır.

Burada önemli olan nokta virtual thread'ler kodu **daha hızlı çalıştırmaz**, daha fazla işi **aynı anda bekletmemizi**
sağlar. Yani veritabanı, HTTP çağrısı gibi I/O ağırlıklı işlerde fayda sağlar, CPU ağırlıklı işlerde fark yaratmaz.
Virtual thread'leri havuza (pool) almaya gerek yoktur, ihtiyaç olduğunda oluşturulup bırakılırlar.
Java 21'de `synchronized` blok içinde bekleyen virtual thread carrier thread'i bırakamıyordu (pinning), bu sorun
Java 24 ile çözüldü.

### Java 22 - 25

Bu başlıktaki örnekler Java 25 gerektirdiği için projede değil sadece bu dosyada yer alıyor.

#### Unnamed Variables

Java 22 ile kullanmadığımız değişkenlere isim vermek yerine `_` yazabiliyoruz:

```java
String sonuc = switch (personel) {
    case Doktor(String isim, _) -> isim + " doktordur";
    case Hemsire(var isim) -> isim + " hemşiredir";
};

try {
    Integer.parseInt("abc");
} catch (NumberFormatException _) {
    System.out.println("sayı değil");
}
```

#### Stream Gatherers

Java 24 ile Stream API'ye kendi ara (intermediate) operasyonlarımızı yazabileceğimiz `gather` methodu geldi.
Hazır gelen `Gatherers` class'ı ile örneğin listeyi parçalara bölebiliyoruz:

```java
var pencereler = Stream.of(1, 2, 3, 4, 5, 6, 7)
        .gather(Gatherers.windowFixed(3))
        .toList();
System.out.println(pencereler); //[[1, 2, 3], [4, 5, 6], [7]]
```

#### Flexible Constructor Bodies

`question1` klasöründeki `Main.java` class'ında constructor zincirini görmüştük. Java 25 öncesinde `super()`
çağrısı constructor'ın **ilk satırı** olmak zorundaydı. Artık `super()`'dan önce validasyon yapabiliyoruz:

```java
class PatikaInterview extends Interview {

    PatikaInterview(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("x negatif olamaz");
        }
        super();
        System.out.println("Patika.dev ile mülakata hoş geldin x " + x);
    }
}

new PatikaInterview(-1); //IllegalArgumentException: x negatif olamaz
```

Gördüğümüz üzere hatalı değer geldiğinde üst class'ın constructor'ı hiç çalışmadı ve ekrana
"Mülakata hoş geldin" yazılmadı. `super()`'dan önce `this` ile instance methodlarını çağıramayız ve field'ları okuyamayız.

#### Compact Source Files ve Instance Main Methods

Java'ya yeni başlayan biri için `public static void main(String[] args)` satırı çok fazla kavram içeriyor.
Java 25 ile tek dosyalık programlarda class tanımı yapmadan şöyle yazabiliyoruz:

```java
void main() {
    IO.println("Merhaba Java 25");
}
```

`java Main.java` komutu ile derlemeden direkt çalıştırabiliriz. `IO` class'ı `java.lang` paketinde olduğu için
import gerekmez.

#### Module Import Declarations

Java 25 ile bir modülün export ettiği bütün paketleri tek satırda import edebiliyoruz:

```java
import module java.base;

List<String> isimler = List.of("cem", "elif"); //java.util import etmeden
```

#### Scoped Values

`ThreadLocal`'e alternatif olarak Java 25 ile `ScopedValue` geldi. Değer sadece belirlediğimiz scope içinde
geçerlidir ve **immutable**'dır.

```java
static final ScopedValue<String> KULLANICI = ScopedValue.newInstance();

ScopedValue.where(KULLANICI, "cem").run(() -> islem());

void islem() {
    System.out.println("işlemi yapan: " + KULLANICI.get()); //işlemi yapan: cem
}
```

`ThreadLocal` değişkenler `remove()` çağrılmadığında bellekte kalabiliyor ve her virtual thread için ayrı kopya
tuttukları için milyonlarca virtual thread ile pahalıya patlayabiliyor. `ScopedValue` scope bittiğinde
otomatik olarak temizlenir.

#### Compact Object Headers (Java 25)

Java 25 ile `-XX:+UseCompactObjectHeaders` parametresi verilerek her nesnenin header boyutu küçültülebiliyordu.
Java 27 ile bu özellik varsayılan olarak açık geliyor, detaylarını bir sonraki başlıkta göreceğiz.

### Java 26 - 27

Java 26 Mart 2026'da, Java 27 Eylül 2026'da çıktı. İkisi de LTS değil, bir sonraki LTS sürümü Java 29 olacak.
Fakat bu sürümlerde gelen bazı değişiklikler kod değiştirmeden uygulamamızın davranışını değiştirdiği için
mülakatlarda sorulmaya başladı.

#### Compact Object Headers by Default

Java'da her nesnenin başında JVM'in kullandığı bir header bulunur. 64-bit sistemlerde bu header 96 bit (12 byte)
yer kaplıyordu. Java 27 ile header **64 bit**'e (8 byte) indi ve bu davranış artık **varsayılan** olarak açık.

```java
record Randevu(int id, int doktorId) {}

// Java 25: 12 byte header + 4 + 4 = 20 byte -> 8 byte hizalama ile 24 byte
// Java 27:  8 byte header + 4 + 4 = 16 byte
```

Gördüğümüz üzere bu nesne %33 küçüldü. Nesneler 8 byte'ın katlarına hizalandığı (alignment) için kazanç her
nesnede bu kadar olmuyor fakat milyonlarca küçük nesne tutan bir uygulamada heap kullanımı belirgin şekilde azalıyor. JEP'te paylaşılan SPECjbb2015 testinde
heap kullanımı **%22**, CPU kullanımı **%8** azalmış. Eski davranışa dönmek istersek:

```
java -XX:-UseCompactObjectHeaders -jar uygulama.jar
```

Bu konuyu
[Your Heap Just Got Smaller in Java 27: Compact Object Headers](https://medium.com/@cemdrman/your-heap-just-got-smaller-in-java-27-compact-object-headers-9b6d8b62f63f)
yazısında detaylı anlattık.

#### G1 Default Garbage Collector in All Environments

Java 9'dan beri varsayılan garbage collector G1'di fakat bu sadece **server** sınıfı makineler için geçerliydi.
Tek CPU'su olan ya da 1792 MB'tan az belleği olan ortamlarda JVM sessizce **Serial GC** seçiyordu.

Bu durum özellikle Kubernetes'te can sıkıyordu. Pod'a `cpu: 1` ve `memory: 1Gi` limiti verdiğimizde JVM kendini
küçük bir makinede sanıyor ve Serial GC ile çalışıyordu. Java 27 ile GC belirtmediğimiz her durumda **G1**
seçiliyor. Serial GC kaldırılmadı, istersek yine seçebiliyoruz:

```
java -XX:+UseSerialGC -jar uygulama.jar
```

Hangi GC'nin seçildiğini görmek için:

```
java -Xlog:gc -version
```

#### Lazy Constants (Preview)

Bir `static final` field'ı class yüklenirken hemen oluşturmak uygulamanın açılışını yavaşlatabiliyor. Lazy
oluşturmak istediğimizde ise field'ı `final` yapamıyor, double-checked locking gibi hataya açık kodlar yazıyorduk.
`LazyConstant` değeri ilk `get()` çağrısında **bir kere** oluşturur, birden fazla thread aynı anda çağırsa bile.

```java
static final LazyConstant<String> AYAR = LazyConstant.of(() -> {
    System.out.println("ayar yükleniyor");
    return "hastane-ayarlari";
});

System.out.println("uygulama başladı");
System.out.println(AYAR.get());
System.out.println(AYAR.get());
//uygulama başladı
//ayar yükleniyor
//hastane-ayarlari
//hastane-ayarlari
```

Gördüğümüz üzere "ayar yükleniyor" sadece bir kere yazıldı. Java 27 ile `List.ofLazy`, `Map.ofLazy` ve
`Set.ofLazy` methodları da var. JVM `LazyConstant` değerini bir kere oluştuktan sonra sabit gibi optimize edebiliyor.

#### Primitive Types in Patterns (Preview)

Pattern matching artık `int`, `long`, `double`, `boolean` gibi primitive tiplerle de çalışıyor:

```java
static String durum(int kod) {
    return switch (kod) {
        case 0 -> "bekliyor";
        case 1 -> "onaylandı";
        case int i when i >= 100 -> "sistem hatası: " + i;
        case int i -> "bilinmeyen durum: " + i;
    };
}

int sayi = 100;
if (sayi instanceof byte b) {
    System.out.println("byte'a sığar: " + b); //byte'a sığar: 100
}

int buyuk = 1000;
if (!(buyuk instanceof byte)) {
    System.out.println("1000 byte'a sığmaz"); //1000 byte'a sığmaz
}
```

Burada önemli olan nokta `instanceof byte` kontrolünün değerin **kayıpsız** dönüştürülüp dönüştürülemeyeceğine
bakması. Eskiden `(byte) 1000` cast'i hata vermeden `-24` döndürüyordu.

#### Structured Concurrency (Preview)

Bir istekte birden fazla servisi aynı anda çağırıp sonuçlarını birleştirmek istediğimizi düşünelim. Bunu
`ExecutorService` ile yaptığımızda bir görev hata alırsa diğerini iptal etmek, thread'leri kapatmak bizim işimizdi.
`StructuredTaskScope` alt görevleri bir blok içine alıyor, blok bitince hepsinin bitmiş olmasını garanti ediyor.

```java
record Cevap(String doktor, int randevuSayisi) {}

Cevap getir() throws ExecutionException, InterruptedException {
    try (var scope = StructuredTaskScope.open()) {
        Subtask<String> doktor = scope.fork(() -> doktorServisi.getir());
        Subtask<Integer> randevu = scope.fork(() -> randevuServisi.say());

        scope.join(); //biri hata alırsa diğeri iptal edilir

        return new Cevap(doktor.get(), randevu.get());
    }
}
```

Her `fork` bir virtual thread'de çalışır. Java 27'de `join()` hata durumunda `ExecutionException` fırlatıyor.
Bu özellik yedinci kez preview olarak geldi, yani API hâlâ değişiyor.

#### Diğer Değişiklikler

- **HTTP/3 (Java 26):** `HttpClient` artık HTTP/3 destekliyor. `HttpClient.newBuilder().version(HttpClient.Version.HTTP_3)` ile seçilebiliyor.
- **Prepare to Make Final Mean Final (Java 26):** Reflection ile `final` field'ları değiştirmek artık uyarı veriyor. İleride tamamen engellenecek. `final` field'ları reflection ile değiştiren kütüphanelere dikkat etmek gerekiyor.
- **Applet API kaldırıldı (Java 26).**
- **Post-Quantum Hybrid Key Exchange (Java 27):** TLS 1.3 bağlantılarında klasik ECDHE ile ML-KEM'i birleştiren, kuantum bilgisayarlara dayanıklı anahtar değişimi geldi. `X25519MLKEM768` varsayılan olarak açık.
- **JFR In-Process Data Redaction (Java 27):** Java Flight Recorder kayıtlarında komut satırı parametreleri, environment variable'lar ve system property'ler içindeki `password`, `token`, `secret` gibi değerler dosyaya yazılmadan `[REDACTED]` ile maskeleniyor. Varsayılan olarak açık.
- **PEM Encodings (Preview)** ve **Vector API (Incubator)** geliştirilmeye devam ediyor.

### Mülakat Soruları

Önce kendiniz cevaplamaya çalışın, sonra cevaba bakın.

<details>
<summary>1. <code>var</code> kullanmak Java'yı dinamik tipli bir dil yapar mı?</summary>

Hayır. Tip compile time'da belirlenir ve değişmez. `var` sadece tipi bizim yerimize compiler'ın yazmasını sağlar.
Field, method parametresi ve dönüş tipi olarak kullanılamaz.
</details>

<details>
<summary>2. <code>List.of()</code> ile <code>Arrays.asList()</code> arasındaki fark nedir?</summary>

`List.of()` tamamen immutable'dır ve `null` kabul etmez. `Arrays.asList()` sabit boyutludur, eleman eklenip
silinemez fakat `set()` ile eleman değiştirilebilir, `null` kabul eder ve arkasındaki array ile bağlıdır.
</details>

<details>
<summary>3. Record ile Lombok <code>@Data</code> arasındaki fark nedir?</summary>

Record dilin bir parçasıdır ve immutable'dır, setter yoktur, extend edilemez. `@Data` ise annotation processor ile
kod üretir, setter içerir ve mutable'dır. JPA entity'leri için record uygun değildir çünkü JPA no-arg constructor
ve mutable field ister. Record'lar DTO olarak kullanılmak için daha uygundur.
</details>

<details>
<summary>4. Sealed class olmadan da final class ve package-private constructor ile benzer şey yapılamaz mı?</summary>

Kısmen yapılabilir fakat compiler alt tipleri bilemez. Sealed class sayesinde compiler switch'in bütün tipleri
karşıladığını kontrol eder (exhaustiveness), yeni tip eklendiğinde unutulan switch'ler derleme hatası verir.
</details>

<details>
<summary>5. Switch'te <code>case Doktor d</code> satırını guard'lı case'den önce yazarsak ne olur?</summary>

Kod derlenmez: "this case label is dominated by a preceding case label". Daha genel olan case alttaki özel case'i
her zaman yakalayacağı için compiler buna izin vermez.
</details>

<details>
<summary>6. Virtual thread uygulamamı hızlandırır mı?</summary>

CPU ağırlıklı işlerde hayır. Virtual thread'ler throughput'u artırır, yani I/O beklerken carrier thread'i
bırakarak daha fazla işi aynı anda bekletir. Tek bir isteğin süresi kısalmaz.
</details>

<details>
<summary>7. Virtual thread'ler için thread pool kullanmalı mıyız?</summary>

Hayır. Virtual thread oluşturmak ucuzdur, her görev için yeni bir tane oluşturulur. Eşzamanlı erişimi sınırlamak
istiyorsak (örneğin veritabanı bağlantısı) pool yerine `Semaphore` kullanılır.
</details>

<details>
<summary>8. <code>Stream.toList()</code> ile <code>Collectors.toList()</code> aynı mı?</summary>

Hayır. `Stream.toList()` unmodifiable bir liste döner. `Collectors.toList()` dönen listenin tipini garanti etmez,
pratikte `ArrayList` döner ve değiştirilebilir.
</details>

<details>
<summary>9. <code>ThreadLocal</code> yerine neden <code>ScopedValue</code> kullanalım?</summary>

`ScopedValue` immutable'dır, sadece belirlenen scope içinde geçerlidir ve scope bitince otomatik temizlenir.
`ThreadLocal` değiştirilebilir, `remove()` unutulursa memory leak'e sebep olabilir ve virtual thread'lerle
birlikte kullanıldığında maliyetlidir.
</details>

<details>
<summary>10. Preview feature nedir, production'da kullanılır mı?</summary>

Dile eklenmiş fakat henüz kesinleşmemiş özelliklerdir. Kullanmak için `--enable-preview` parametresi gerekir ve
sonraki sürümlerde değişebilir ya da kaldırılabilir (String Templates Java 23'te kaldırıldı). Bu yüzden
production'da kullanılması önerilmez.
</details>

<details>
<summary>11. Java 27'ye geçtik, kod değiştirmeden heap kullanımı düştü. Neden?</summary>

Java 27 ile compact object headers varsayılan olarak açık geldi. Her nesnenin header'ı 12 byte'tan 8 byte'a indi.
Çok sayıda küçük nesne tutan uygulamalarda heap kullanımı ve GC sayısı azalır. Kapatmak için
`-XX:-UseCompactObjectHeaders` kullanılır.
</details>

<details>
<summary>12. Kubernetes'te 1 CPU limiti olan bir pod'da Java 21 hangi GC'yi kullanır? Java 27'de ne değişti?</summary>

Java 21 tek CPU ya da 1792 MB'tan az bellek gördüğünde Serial GC seçer. Java 27 ile GC belirtilmediği her durumda
G1 seçilir. Hangi GC'nin kullanıldığından emin olmak için GC'yi parametre ile belirtmek en doğrusudur.
</details>

<details>
<summary>13. Structured concurrency ile <code>ExecutorService</code> arasındaki fark nedir?</summary>

`ExecutorService` ile başlattığımız görevler birbirinden bağımsızdır; biri hata alınca diğerlerini iptal etmek ve
kaynakları kapatmak bizim sorumluluğumuzdadır. `StructuredTaskScope` alt görevleri bir blok içinde toplar, bir
görev hata alınca diğerlerini iptal eder ve blok bittiğinde açık thread kalmamasını garanti eder.
</details>

### Kaynaklar

- OCP Oracle Certified Professional Java SE 21 Developer Study Guide – Scott Selikoff & Jeanne Boyarsky
- https://openjdk.org/projects/jdk/
- https://openjdk.org/projects/jdk/26/
- https://openjdk.org/projects/jdk/27/
- https://bilisim.io/2024/10/06/9126/

Faydalı olması dileğiyle.
