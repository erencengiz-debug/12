package com.sase.app.mapper;

import com.sase.app.dto.sase.SaseEslestirmeListDto;
import com.sase.app.entity.SaseEslestirme;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SaseEslestirmeMapper {

    SaseEslestirmeListDto toListDto(SaseEslestirme eslestirme);

    List<SaseEslestirmeListDto> toListDtos(List<SaseEslestirme> eslestirmeler);
}
