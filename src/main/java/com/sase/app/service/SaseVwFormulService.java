package com.sase.app.service;

import com.sase.app.dto.sase.SaseVwFormulDto;
import com.sase.app.entity.SaseVwFormul;
import com.sase.app.repository.SaseVwFormulRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaseVwFormulService {

    private final SaseVwFormulRepository repository;

    public List<SaseVwFormulDto> listAllDescending() {
        return repository.findAllByOrderByIdDesc().stream().map(SaseVwFormulService::toDto).toList();
    }

    public List<String> distinctEkleyenKullanici() {
        return repository.listDistinctEkleyenKullanici();
    }

    public SaseVwFormulDto getDto(Integer id) {
        return repository.findById(id).map(SaseVwFormulService::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Kayıt bulunamadı: " + id));
    }

    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Kayıt bulunamadı: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Tek satırı temizler: tüm alanlar null olur ({@code executed} dahil).
     */
    @Transactional
    public void clearRow(Integer id) {
        SaseVwFormul e = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Kayıt bulunamadı: " + id));
        e.setEkleyenKullanici(null);
        e.setBaslik(null);
        e.setStokListe(null);
        e.setSaseKod1(null);
        e.setSaseKod2(null);
        e.setSaseKod3(null);
        e.setSaseKod4(null);
        e.setSaseKod5(null);
        e.setSaseKod6(null);
        e.setSaseKod7(null);
        e.setSaseKod8(null);
        e.setSaseKod9(null);
        e.setSaseNo(null);
        e.setModel(null);
        e.setUretimTarihiBas(null);
        e.setUretimTarihiBit(null);
        e.setModelYili(null);
        e.setSatisTipi(null);
        e.setMotorKodu(null);
        e.setSanzimanKodu(null);
        e.setAksTahrigiTanimi(null);
        e.setDonanim(null);
        e.setExecuted(null);
        e.setExecuteDate(null);
        e.setEslenikSaseAdedi(null);
        e.setDegerliAciklamaStokKods(null);
    }

    private static SaseVwFormulDto toDto(SaseVwFormul e) {
        return new SaseVwFormulDto(
                e.getId(),
                e.getEkleyenKullanici(),
                e.getBaslik(),
                e.getStokListe(),
                e.getSaseKod1(),
                e.getSaseKod2(),
                e.getSaseKod3(),
                e.getSaseKod4(),
                e.getSaseKod5(),
                e.getSaseKod6(),
                e.getSaseKod7(),
                e.getSaseKod8(),
                e.getSaseKod9(),
                e.getSaseNo(),
                e.getModel(),
                e.getUretimTarihiBas(),
                e.getUretimTarihiBit(),
                e.getModelYili(),
                e.getSatisTipi(),
                e.getMotorKodu(),
                e.getSanzimanKodu(),
                e.getAksTahrigiTanimi(),
                e.getDonanim(),
                e.getExecuted(),
                e.getExecuteDate(),
                e.getEslenikSaseAdedi(),
                e.getDegerliAciklamaStokKods()
        );
    }
}
