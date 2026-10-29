package com.cemdrman.doktor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Doktor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String isim;

    private String brans;

    private boolean operator;

    protected Doktor() {
    }

    public Doktor(String isim, String brans, boolean operator) {
        this.isim = isim;
        this.brans = brans;
        this.operator = operator;
    }

    public Long getId() {
        return id;
    }

    public String getIsim() {
        return isim;
    }

    public String getBrans() {
        return brans;
    }

    public boolean isOperator() {
        return operator;
    }

    public void setOperator(boolean operator) {
        this.operator = operator;
    }
}
