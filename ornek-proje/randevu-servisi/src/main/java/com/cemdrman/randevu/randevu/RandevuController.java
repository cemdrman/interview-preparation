package com.cemdrman.randevu.randevu;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/randevular")
public class RandevuController {

    private final RandevuService randevuService;

    public RandevuController(RandevuService randevuService) {
        this.randevuService = randevuService;
    }

    @PostMapping
    public ResponseEntity<RandevuCevabi> olustur(@Valid @RequestBody RandevuIstegi istek) {
        RandevuCevabi cevap = randevuService.olustur(istek);
        return ResponseEntity.created(URI.create("/randevular/" + cevap.id())).body(cevap);
    }

    @GetMapping("/{id}")
    public Randevu getir(@PathVariable Long id) {
        return randevuService.getir(id);
    }
}
