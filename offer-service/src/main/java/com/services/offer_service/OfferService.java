package com.services.offer_service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OfferService {

    @Autowired
    private OfferRepository offerRepository;

    public Offer createOffer(Offer offer) {
        offer.setActive(true);
        return offerRepository.save(offer);
    }

    public List<Offer> getAllActiveOffers() {
        return offerRepository.findByActiveTrue();
    }

    public List<Offer> getOffersByCategory(String category) {
        return offerRepository.findByCategoryIgnoreCaseAndActiveTrue(category);
    }

    public List<Offer> getOffersByProvider(Long providerId) {
        return offerRepository.findByProviderId(providerId);
    }

    public Offer updateOffer(Long offerId, double newPrice,
            String newDate, boolean active) {
        Optional<Offer> optional = offerRepository.findById(offerId);
        if (optional.isEmpty())
            return null;

        Offer offer = optional.get();
        offer.setPrice(newPrice);
        offer.setAvailableDate(newDate);
        offer.setActive(active);
        return offerRepository.save(offer);
    }

    public Optional<Offer> getOfferById(Long offerId) {
        return offerRepository.findById(offerId);
    }

    public void deactivateOffer(Long offerId) {
        offerRepository.findById(offerId).ifPresent(offer -> {
            offer.setActive(false);
            offerRepository.save(offer);
        });
    }
}