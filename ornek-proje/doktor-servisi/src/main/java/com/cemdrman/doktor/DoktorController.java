package com.cemdrman.doktor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/doktorlar")
public class DoktorController {

    private final DoktorService doktorService;

    public DoktorController(DoktorService doktorService) {
        this.doktorService = doktorService;
    }

    @GetMapping
    public List<DoktorDto> listele() {
        return doktorService.listele();
    }

    @GetMapping("/{id}")
    public DoktorDto getir(@PathVariable Long id) {
        return doktorService.getir(id);
    }

    @PatchMapping("/{id}/operator")
    public DoktorDto operatorDurumunuGuncelle(@PathVariable Long id, @RequestParam boolean deger) {
        return doktorService.operatorDurumunuGuncelle(id, deger);
    }
}
