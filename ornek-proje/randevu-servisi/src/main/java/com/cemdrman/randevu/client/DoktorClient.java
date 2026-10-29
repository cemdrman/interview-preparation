package com.cemdrman.randevu.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doktor-servisi", url = "${doktor-servisi.url}", configuration = DoktorClientConfig.class)
public interface DoktorClient {

    @GetMapping("/doktorlar/{id}")
    DoktorCevap getir(@PathVariable("id") Long id);
}
