package com.services.booking_service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private RestTemplate restTemplate = new RestTemplate();

    private static final String USER_SERVICE = "http://localhost:8080/user-service/api/users";
    private static final String OFFER_SERVICE = "http://localhost:8081/api/offers";

    public Booking createBooking(Long customerId, String customerName,
            Long offerId) {

        ResponseEntity<Map> offerResponse = restTemplate
                .getForEntity(OFFER_SERVICE + "/" + offerId, Map.class);

        if (!offerResponse.getStatusCode().is2xxSuccessful()
                || offerResponse.getBody() == null) {
            sendNotification(customerName, "REJECTED",
                    "Offer not found", RabbitMQConfig.CUSTOMER_KEY);
            return null;
        }

        Map offer = offerResponse.getBody();
        boolean active = (Boolean) offer.get("active");

        if (!active) {
            sendNotification(customerName, "REJECTED",
                    "Offer is no longer available", RabbitMQConfig.CUSTOMER_KEY);
            return null;
        }

        double amount = Double.parseDouble(offer.get("price").toString());
        String providerName = offer.get("providerName").toString();
        Long providerId = Long.parseLong(offer.get("providerId").toString());
        String category = offer.get("category").toString();
        String serviceDate = offer.get("availableDate").toString();

        Map<String, Object> deductBody = Map.of("amount", amount);

        ResponseEntity<Map> deductResponse;
        try {
            deductResponse = restTemplate.postForEntity(
                    USER_SERVICE + "/" + customerId + "/deduct",
                    deductBody, Map.class);
        } catch (Exception e) {
            sendNotification(customerName, "REJECTED",
                    "Payment failed: " + e.getMessage(),
                    RabbitMQConfig.CUSTOMER_KEY);
            return null;
        }

        Map deductResult = deductResponse.getBody();
        if (deductResult == null || deductResult.containsKey("error")) {
            String reason = deductResult != null ? deductResult.get("error").toString() : "Payment failed";

            sendNotification(customerName, "REJECTED",
                    reason, RabbitMQConfig.CUSTOMER_KEY);
            return null;
        }

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setCustomerName(customerName);
        booking.setProviderId(providerId);
        booking.setProviderName(providerName);
        booking.setOfferId(offerId);
        booking.setCategory(category);
        booking.setAmount(amount);
        booking.setStatus("CONFIRMED");
        booking.setServiceDate(serviceDate);
        booking.setBookedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        restTemplate.postForEntity(
                OFFER_SERVICE + "/" + offerId + "/deactivate",
                null, String.class);

        sendNotification(customerName, "CONFIRMED",
                "Your booking for " + category + " on " + serviceDate +
                        " is confirmed! Amount paid: $" + amount,
                RabbitMQConfig.CUSTOMER_KEY);

        sendNotification(providerName, "CONFIRMED",
                "New booking from " + customerName + " for " +
                        category + " on " + serviceDate,
                RabbitMQConfig.PROVIDER_KEY);

        return booking;
    }

    public List<Booking> getCustomerBookings(Long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    public List<Booking> getProviderBookings(Long providerId) {
        return bookingRepository.findByProviderId(providerId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    private void sendNotification(String recipient, String status,
                    String message, String routingKey) {
            String notification = String.format(
                            "{\"recipient\":\"%s\",\"status\":\"%s\",\"message\":\"%s\"}",
                            recipient, status, message);

            rabbitTemplate.convertAndSend(
                            RabbitMQConfig.BOOKING_EXCHANGE,
                            routingKey,
                            notification);

            System.out.println("📨 Notification sent to " + recipient + ": " + message);

    }

    public RabbitTemplate getRabbitTemplate() {
            return rabbitTemplate;
    }
}