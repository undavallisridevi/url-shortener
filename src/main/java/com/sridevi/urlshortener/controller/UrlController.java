package com.sridevi.urlshortener.controller;

import com.sridevi.urlshortener.dto.*;
import com.sridevi.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/urls")
public class UrlController {
    private final UrlService service;
    public UrlController(UrlService service) { this.service = service; }
    @PostMapping ResponseEntity<UrlResponse> create(@Valid @RequestBody CreateUrlRequest request, Authentication auth) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, auth.getName())); }
    @GetMapping("/{shortCode}/stats") UrlStatsResponse stats(@PathVariable String shortCode, Authentication auth) { return service.stats(shortCode, auth.getName()); }
    @DeleteMapping("/{shortCode}") ResponseEntity<Void> delete(@PathVariable String shortCode, Authentication auth) { service.delete(shortCode, auth.getName()); return ResponseEntity.noContent().build(); }
}
