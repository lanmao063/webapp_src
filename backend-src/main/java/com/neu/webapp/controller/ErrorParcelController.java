package com.neu.webapp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neu.webapp.common.Result;
import com.neu.webapp.entity.ErrorParcel;
import com.neu.webapp.security.UserContext;
import com.neu.webapp.service.ErrorParcelService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/error-parcel")
public class ErrorParcelController {

    private final ErrorParcelService errorParcelService;

    public ErrorParcelController(ErrorParcelService errorParcelService) {
        this.errorParcelService = errorParcelService;
    }

    @PostMapping
    public Result<ErrorParcel> register(@RequestBody Map<String, String> body) {
        String username = UserContext.getCurrentUsername();
        return Result.ok(errorParcelService.registerError(
                body.get("parcelId"),
                body.get("errorType"),
                body.get("description"),
                username
        ));
    }

    @GetMapping("/list")
    public Result<IPage<ErrorParcel>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(errorParcelService.search(status, page, size));
    }

    @PutMapping("/{id}/handle")
    public Result<?> handle(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String username = UserContext.getCurrentUsername();
        errorParcelService.handleError(id, username, body.get("handleResult"));
        return Result.ok();
    }
}
