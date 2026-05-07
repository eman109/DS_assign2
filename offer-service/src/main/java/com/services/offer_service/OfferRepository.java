package com.services.offer_service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findByCategoryIgnoreCaseAndActiveTrue(String category);

    List<Offer> findByActiveTrue();

    List<Offer> findByProviderId(Long providerId);
}