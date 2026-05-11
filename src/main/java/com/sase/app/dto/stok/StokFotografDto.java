package com.sase.app.dto.stok;

import java.util.UUID;

public record StokFotografDto(
        UUID id,
        String fotoUrl,
        Integer sira
) {}
