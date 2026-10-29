package com.cemdrman.randevu.randevu;

import com.cemdrman.randevu.client.DoktorCevap;
import com.cemdrman.randevu.client.DoktorClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RandevuService {

    private final RandevuRepository randevuRepository;
    private final DoktorClient doktorClient;
    private final ApplicationEventPublisher eventPublisher;

    public RandevuService(RandevuRepository randevuRepository, DoktorClient doktorClient,
                          ApplicationEventPublisher eventPublisher) {
        this.randevuRepository = randevuRepository;
        this.doktorClient = doktorClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RandevuCevabi olustur(RandevuIstegi istek) {
        DoktorCevap doktor = doktorClient.getir(istek.doktorId());

        if (istek.tip() == RandevuTipi.AMELIYAT && !doktor.operator()) {
            throw new IsKuraliException("Sadece operatör doktorlar ameliyata girebilir");
        }

        Randevu randevu = randevuRepository.save(new Randevu(istek.hastaAdi(), istek.hastaTelefon(),
                doktor.id(), istek.tarih(), istek.tip()));

        // Kafka'ya transaction commit olduktan sonra gönderilir, bkz. RandevuEventProducer
        eventPublisher.publishEvent(new RandevuOlusturulduEvent(randevu.getId(), randevu.getHastaAdi(),
                randevu.getHastaTelefon(), doktor.isim(), randevu.getTarih(), randevu.getTip()));

        return new RandevuCevabi(randevu.getId(), randevu.getHastaAdi(), doktor.isim(), randevu.getTarih(),
                randevu.getTip());
    }

    @Transactional(readOnly = true)
    public Randevu getir(Long id) {
        return randevuRepository.findById(id)
                .orElseThrow(() -> new RandevuBulunamadiException(id));
    }
}
