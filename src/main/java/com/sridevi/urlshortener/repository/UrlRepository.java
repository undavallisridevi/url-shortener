package com.sridevi.urlshortener.repository;

import com.sridevi.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCodeAndDeletedFalse(String shortCode);
    boolean existsByShortCode(String shortCode);
    @Query(value = "SELECT nextval('url_id_seq')", nativeQuery = true)
    long nextSequenceValue();
    @Query("select u from Url u join fetch u.user where u.shortCode = :shortCode and u.deleted = false")
    Optional<Url> findOwnedActiveUrl(@Param("shortCode") String shortCode);
}
