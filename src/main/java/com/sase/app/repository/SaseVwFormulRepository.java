package com.sase.app.repository;

import com.sase.app.entity.SaseVwFormul;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SaseVwFormulRepository extends JpaRepository<SaseVwFormul, Integer> {

    @Query("SELECT DISTINCT f.ekleyenKullanici FROM SaseVwFormul f WHERE f.ekleyenKullanici IS NOT NULL ORDER BY f.ekleyenKullanici")
    List<String> listDistinctEkleyenKullanici();

    List<SaseVwFormul> findAllByOrderByIdDesc();
}
