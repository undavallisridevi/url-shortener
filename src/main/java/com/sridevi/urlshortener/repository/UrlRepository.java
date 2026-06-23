package com.sridevi.urlshortener.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sridevi.urlshortener.entity.Url;

public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCodeAndDeletedFalse(String shortCode);
    boolean existsByShortCode(String shortCode);
    @Query(value = "SELECT nextval('url_id_seq')", nativeQuery = true)
    long nextSequenceValue();
    @Query("select u from Url u join fetch u.user where u.shortCode = :shortCode and u.deleted = false")
    Optional<Url> findOwnedActiveUrl(@Param("shortCode") String shortCode);
    
    List<Url> findByUserUsernameAndDeletedFalse(String username);
}
