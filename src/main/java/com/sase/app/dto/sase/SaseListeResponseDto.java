package com.sase.app.dto.sase;

import java.util.List;

public record SaseListeResponseDto(
        List<SaseEslestirmeResponseDto> data,
        long totalCount,
        int page,
        int pageSize,
        int totalPages
) {}
