package com.cemdrman.doktor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(DoktorController.class)
class DoktorControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private DoktorService doktorService;

    @Test
    void doktorBilgisiDoner() {
        given(doktorService.getir(1L)).willReturn(new DoktorDto(1L, "Ayşe Yılmaz", "Genel Cerrahi", true));

        assertThat(mockMvc.get().uri("/doktorlar/1"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.operator").isEqualTo(true);
    }

    @Test
    void doktorYoksa404Doner() {
        given(doktorService.getir(99L)).willThrow(new DoktorBulunamadiException(99L));

        assertThat(mockMvc.get().uri("/doktorlar/99"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .extractingPath("$.detail").isEqualTo("Doktor bulunamadı: 99");
    }
}
