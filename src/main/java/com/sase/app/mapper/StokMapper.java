package com.sase.app.mapper;

import com.sase.app.dto.stok.MuadilKodDto;
import com.sase.app.dto.stok.StokAlisDto;
import com.sase.app.dto.stok.StokCikisDto;
import com.sase.app.dto.stok.StokDetailDto;
import com.sase.app.dto.stok.StokFotografDto;
import com.sase.app.dto.stok.StokListDto;
import com.sase.app.entity.MuadilKod;
import com.sase.app.entity.Stok;
import com.sase.app.entity.StokAlis;
import com.sase.app.entity.StokCikis;
import com.sase.app.entity.StokFotograf;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StokMapper {

    StokListDto toListDto(Stok stok);

    List<StokListDto> toListDtos(List<Stok> stoklar);

    StokDetailDto toDetailDto(Stok stok);

    MuadilKodDto toDto(MuadilKod muadilKod);

    StokAlisDto toDto(StokAlis stokAlis);

    StokCikisDto toDto(StokCikis stokCikis);

    StokFotografDto toDto(StokFotograf stokFotograf);
}
