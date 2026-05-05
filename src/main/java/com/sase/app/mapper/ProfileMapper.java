package com.sase.app.mapper;

import com.sase.app.dto.profile.ProfileDto;
import com.sase.app.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {

    ProfileDto toDto(Profile profile);
}
