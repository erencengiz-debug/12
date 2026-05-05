package com.sase.app.mapper;

import com.sase.app.dto.stok.StokDetailDto;
import com.sase.app.dto.stok.StokListDto;
import com.sase.app.entity.Stok;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StokMapper {

    StokListDto toListDto(Stok stok);

    List<StokListDto> toListDtos(List<Stok> stoklar);

    StokDetailDto toDetailDto(Stok stok);
}
