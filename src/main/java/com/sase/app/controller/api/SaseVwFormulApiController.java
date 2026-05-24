package com.sase.app.controller.api;

import com.sase.app.dto.common.ApiResponse;
import com.sase.app.dto.sase.SaseVwFormulDto;
import com.sase.app.service.SaseVwFormulService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sase/vw-formuls")
@RequiredArgsConstructor
public class SaseVwFormulApiController {

    private final SaseVwFormulService service;

    @GetMapping("/{id}")
    public ApiResponse<SaseVwFormulDto> get(@PathVariable Integer id) {
        return ApiResponse.ok(service.getDto(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.ok(null, "Silindi.");
    }

    @PostMapping("/{id}/clear")
    public ApiResponse<Void> clear(@PathVariable Integer id) {
        service.clearRow(id);
        return ApiResponse.ok(null, "Satır temizlendi.");
    }
}
