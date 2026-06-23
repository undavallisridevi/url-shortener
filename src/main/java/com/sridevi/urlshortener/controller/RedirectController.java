package com.sridevi.urlshortener.controller;

import com.sridevi.urlshortener.exception.TooManyRequestsException;
import com.sridevi.urlshortener.ratelimiter.RateLimitDecision;
import com.sridevi.urlshortener.ratelimiter.RateLimitKeys;
import com.sridevi.urlshortener.ratelimiter.RateLimiter;
import com.sridevi.urlshortener.ratelimiter.RateLimiterProperties;
import com.sridevi.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class RedirectController {

	private final UrlService service;
	private final RateLimiter rateLimiter;
	private final RateLimiterProperties properties;

	public RedirectController(UrlService service, RateLimiter rateLimiter, RateLimiterProperties properties) {
		this.service = service;
		this.rateLimiter = rateLimiter;
		this.properties = properties;
	}

	@GetMapping("/{shortCode:[A-Za-z0-9_-]+}")
	public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {

		String ipAddress = request.getRemoteAddr();

		RateLimitDecision decision = rateLimiter.tryAcquire(RateLimitKeys.forIp(ipAddress), properties.perIpLimit(),
				properties.window());

		if (!decision.allowed()) {
			throw new TooManyRequestsException("Rate limit exceeded. Please try again later.");
		}

		String originalUrl = service.resolve(shortCode);

		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(originalUrl)).build();
	}
}