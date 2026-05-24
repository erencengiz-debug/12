package com.sase.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sase_vw_formuls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaseVwFormul {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** PostgreSQL {@code SERIAL} → {@code integer}; {@code Long} Hibernate'de {@code bigint} bekler. */
    private Integer id;

    @Column(name = "ekleyen_kullanici", length = 100)
    private String ekleyenKullanici;

    @Column(name = "baslik", length = 255)
    private String baslik;

    @Column(name = "stok_liste", columnDefinition = "TEXT")
    private String stokListe;

    @Column(name = "sase_kod_1", length = 100) private String saseKod1;
    @Column(name = "sase_kod_2", length = 100) private String saseKod2;
    @Column(name = "sase_kod_3", length = 100) private String saseKod3;
    @Column(name = "sase_kod_4", length = 100) private String saseKod4;
    @Column(name = "sase_kod_5", length = 100) private String saseKod5;
    @Column(name = "sase_kod_6", length = 100) private String saseKod6;
    @Column(name = "sase_kod_7", length = 100) private String saseKod7;
    @Column(name = "sase_kod_8", length = 100) private String saseKod8;
    @Column(name = "sase_kod_9", length = 100) private String saseKod9;

    @Column(name = "sase_no", length = 100)
    private String saseNo;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "uretim_tarihi_bas")
    private LocalDate uretimTarihiBas;

    @Column(name = "uretim_tarihi_bit")
    private LocalDate uretimTarihiBit;

    @Column(name = "model_yili", length = 20)
    private String modelYili;

    @Column(name = "satis_tipi", length = 50)
    private String satisTipi;

    @Column(name = "motor_kodu", length = 100)
    private String motorKodu;

    @Column(name = "sanzuman_kodu", length = 100)
    private String sanzimanKodu;

    @Column(name = "aks_tahrigi_tanimi", length = 100)
    private String aksTahrigiTanimi;

    @Column(name = "donanim", length = 100)
    private String donanim;

    @Column(name = "executed")
    private Boolean executed;

    @Column(name = "execute_date")
    private LocalDate executeDate;

    @Column(name = "eslenik_sase_adedi")
    private Integer eslenikSaseAdedi;

    @Column(name = "degerli_aciklama_stok_kods", columnDefinition = "TEXT")
    private String degerliAciklamaStokKods;
}
