## Yapılacaklar ve Öneriler

Bu liste, repodaki mevcut kodlar (`question1`, `question2`), PDF'ler ve commit geçmişi incelenerek hazırlandı.
Maddeler öncelik sırasına göre gruplanmıştır. Tamamlanan maddeler işaretlenerek takip edilebilir.

### Mevcut Durum

- `question1`: `final`, `static`, constructor zinciri ve `Set` davranışı üzerine kod okuma soruları var, cevapları yok.
- `question2`: Hastane otomasyonu OOP tasarım sorusu var, çözüm kodu yok.
- `Senior_Java_Developer_550_Soru_Cevapsiz.pdf` ve `temel_algoritma_sorulari.pdf` repoda duruyor ama içerikleri aranabilir değil ve cevapsız.
- README'de birçok konu başlığı listelenmiş fakat başlıkların altında içerik ya da bağlantı yok.
- Projede test, CI ve Maven Wrapper bulunmuyor.

### 1. Öncelikli (Kısa Vadede)

- [ ] `question1` için cevap dosyası ekle. Cevaplar `<details>` etiketi ile gizlenirse kişi önce kendini deneyebilir.
  - `Main.java` çıktısının sırası (static blok → üst sınıf constructor → alt sınıf constructor) açıklanmalı.
  - `Hospital.java` içinde `this.doctors` ile static alana erişim ve `final static` alanın neden atanamadığı açıklanmalı.
  - `CollectionsMain.java` için `List`, `TreeSet`, `HashSet` farkı (tekrar, sıralama) açıklanmalı.
- [ ] `question2` (Hastane Otomasyonu) için örnek bir çözüm ve JUnit testleri ekle.
- [ ] `pom.xml` dosyasına JUnit 5 (ve isteğe bağlı AssertJ) bağımlılığı ekle, `src/test/java` klasörünü oluştur.
- [ ] Maven Wrapper (`mvnw`) ekle; Maven kurulu olmayan biri de projeyi çalıştırabilsin.
- [ ] `pom.xml` içindeki `groupId` (`com.food-order`) başka bir projeden kalmış görünüyor, `com.cemdrman` gibi bir değere çevir.
- [ ] GitHub Actions ile her push/PR'da `mvn test` çalıştıran basit bir CI ekle.

### 2. İçerik (Core Java)

README'deki her konu için `soru + örnek kod + cevap` üçlüsü ile bir klasör oluşturulabilir:

- [ ] **Exceptions**: checked/unchecked farkı, `try-with-resources`, `finally` içinde `return`, custom exception.
- [ ] **Collection API**: `equals`/`hashCode` sözleşmesi, `HashMap` iç yapısı, `ConcurrentModificationException`, `ArrayList` vs `LinkedList`.
- [ ] **Stream API**: `map`/`flatMap`, `groupingBy`, `reduce`, lazy evaluation, paralel stream tuzakları.
- [ ] **Thread / Concurrency**: `synchronized`, `volatile`, `ExecutorService`, `CompletableFuture`, deadlock örneği.
- [ ] **Generics**: wildcard (`? extends` / `? super`, PECS), type erasure.
- [x] **Modern Java (9–27)**: `var`, text block, `record`, `sealed` sınıflar, pattern matching, virtual thread, sequenced collection, scoped value ve diğerleri. Bkz. [YeniJavaOzellikleri.md](src/main/java/modernjava/YeniJavaOzellikleri.md)
- [ ] **JVM**: bellek alanları (heap, stack, metaspace), garbage collector türleri, `String` pool.

### 3. Algoritma ve Veri Yapıları

- [ ] `temel_algoritma_sorulari.pdf` içindeki soruları Markdown'a taşı ve her biri için Java çözümü + test ekle.
- [ ] Her çözüme zaman ve alan karmaşıklığı (Big-O) notu ekle.
- [ ] Sık sorulan kalıplar için ayrı bölüm: two pointers, sliding window, binary search, BFS/DFS, dinamik programlama.

### 4. Senior Seviye

- [ ] `Senior_Java_Developer_550_Soru_Cevapsiz.pdf` içindeki soruları konu başlıklarına göre Markdown dosyalarına böl; cevaplar zamanla eklenebilir.
- [ ] **System Design** vaka çalışmaları: URL kısaltıcı, rate limiter, bildirim servisi, e-ticaret sipariş akışı.
- [x] **Spring Boot** örnek proje: REST API + JPA + validation + global exception handling + Testcontainers ile entegrasyon testi. Bkz. [ornek-proje](ornek-proje)
- [ ] **Mikroservis** konuları için kısa notlar: Saga, Outbox, Circuit Breaker, idempotency, distributed tracing.
- [x] **Kafka / RabbitMQ** için küçük bir producer/consumer örneği (Docker Compose ile). Bkz. [ornek-proje](ornek-proje)

### 5. Mülakat Süreci

- [ ] Davranışsal (behavioral) sorular ve STAR tekniği ile cevaplama örnekleri.
- [ ] Canlı kodlama (live coding) sırasında dikkat edilecekler ve kontrol listesi.
- [ ] CV ve LinkedIn profili için kısa öneriler.
- [ ] Mülakatçıya sorulabilecek sorular listesi.

### 6. Repo Düzeni

- [ ] Paketleri `question1`, `question2` yerine konu bazlı adlandır (ör. `corejava.oop`, `corejava.collections`, `ood.hastane`).
- [ ] README'ye içindekiler tablosu ve repodaki soru dosyalarına bağlantılar ekle.
- [ ] README'deki kitap ve eğitimler için bağlantı ekle (Özcan ACAR'ın ücretsiz kitapları, Akın KALDIROĞLU eğitimleri).
- [ ] `CONTRIBUTING.md` ve yeni soru eklemek için bir şablon ekle; katkı yapmak isteyenler aynı formatı kullansın.
- [ ] Lisans dosyası (`LICENSE`) ekle.
