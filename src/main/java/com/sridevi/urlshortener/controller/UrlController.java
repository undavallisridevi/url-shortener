package com.sridevi.urlshortener.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sridevi.urlshortener.dto.AnalyticsResponse;
import com.sridevi.urlshortener.dto.CreateUrlRequest;
import com.sridevi.urlshortener.dto.TopUrlResponse;
import com.sridevi.urlshortener.dto.UrlResponse;
import com.sridevi.urlshortener.dto.UrlStatsResponse;
import com.sridevi.urlshortener.dto.UserUrlResponse;
import com.sridevi.urlshortener.service.UrlService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {
	private final UrlService service;

	public UrlController(UrlService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<UrlResponse> create(@Valid @RequestBody CreateUrlRequest request, Authentication auth) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, auth.getName()));
	}

	@GetMapping("/{shortCode}/stats")
	UrlStatsResponse stats(@PathVariable String shortCode, Authentication auth) {
		return service.stats(shortCode, auth.getName());
	}

	@DeleteMapping("/{shortCode}")
	ResponseEntity<Void> delete(@PathVariable String shortCode, Authentication auth) {
		service.delete(shortCode, auth.getName());
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<List<UserUrlResponse>> getMyUrls(Authentication authentication) {
		return ResponseEntity.ok(service.getMyUrls(authentication.getName()));
	}
	@GetMapping("/{shortCode}/analytics")
	public AnalyticsResponse analytics(
	        @PathVariable String shortCode,
	        Authentication auth
	) {
	    return service.analytics(
	            shortCode,
	            auth.getName()
	    );
	}
	@GetMapping("/top")
	public List<TopUrlResponse> topUrls() {
	    return service.topUrls();
	}
}
