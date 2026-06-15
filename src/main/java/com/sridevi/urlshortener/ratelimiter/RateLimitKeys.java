package com.sridevi.urlshortener.ratelimiter;

public final class RateLimitKeys {
    public static String forIp(String ipAddress) { return "rate:ip:" + ipAddress; }
    public static String forUser(long userId) { return "rate:user:" + userId; }
    private RateLimitKeys() {}
}
