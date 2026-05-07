package com.services.offer_service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    @Autowired
    private OfferService offerService;

    @PostMapping
    public ResponseEntity<Offer> createOffer(@RequestBody Offer offer) {
        return ResponseEntity.ok(offerService.createOffer(offer));
    }

    @GetMapping
    public ResponseEntity<List<Offer>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllActiveOffers());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Offer>> getByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(offerService.getOffersByCategory(category));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<Offer>> getByProvider(
            @PathVariable Long providerId) {
        return ResponseEntity.ok(offerService.getOffersByProvider(providerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<Offer> offer = offerService.getOfferById(id);
        if (offer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(offer.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOffer(@PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        double price = Double.parseDouble(body.get("price").toString());
        String date = body.get("availableDate").toString();
        boolean active = Boolean.parseBoolean(body.get("active").toString());

        Offer updated = offerService.updateOffer(id, price, date, active);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivate(@PathVariable Long id) {
        offerService.deactivateOffer(id);
        return ResponseEntity.ok("Offer deactivated");
    }
}