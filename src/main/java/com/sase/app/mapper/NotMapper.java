package com.sase.app.mapper;

import com.sase.app.dto.not.NotDto;
import com.sase.app.entity.Not;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = TimeConverters.class)
public interface NotMapper {

    NotDto toDto(Not not);

    List<NotDto> toDtos(List<Not> notlar);
}
