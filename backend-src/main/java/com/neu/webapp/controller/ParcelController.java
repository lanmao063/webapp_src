package com.neu.webapp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neu.webapp.common.Result;
import com.neu.webapp.dto.ParcelEntryRequest;
import com.neu.webapp.entity.Parcel;
import com.neu.webapp.security.UserContext;
import com.neu.webapp.service.ParcelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parcel")
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PostMapping("/entry")
    public Result<Parcel> entry(@Valid @RequestBody ParcelEntryRequest request) {
        String enteredBy = UserContext.getCurrentUsername();
        return Result.ok(parcelService.enterWarehouse(request, enteredBy));
    }

    @PutMapping("/{id}/pickup")
    public Result<?> pickup(@PathVariable String id) {
        parcelService.pickup(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<Parcel> getById(@PathVariable String id) {
        Parcel parcel = parcelService.getById(id);
        if (parcel == null) {
            return Result.fail(404, "包裹不存在");
        }
        return Result.ok(parcel);
    }

    @GetMapping("/search")
    public Result<IPage<Parcel>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(parcelService.search(keyword, status, page, size));
    }
}
