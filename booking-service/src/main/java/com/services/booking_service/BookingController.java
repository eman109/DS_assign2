package com.services.booking_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> body) {
        Long customerId = Long.parseLong(body.get("customerId").toString());
        String customerName = body.get("customerName").toString();
        Long offerId = Long.parseLong(body.get("offerId").toString());

        Booking booking = bookingService.createBooking(
                customerId, customerName, offerId);

        if (booking == null) {
            return ResponseEntity.badRequest()
                    .body("{\"error\":\"Booking failed - check notifications\"}");
        }
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Booking>> getCustomerBookings(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(
                bookingService.getCustomerBookings(customerId));
    }


    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<Booking>> getProviderBookings(
            @PathVariable Long providerId) {
        return ResponseEntity.ok(
                bookingService.getProviderBookings(providerId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/notifications/customer")
    public ResponseEntity<String> getCustomerNotifications() {
        Object msg = null;
        StringBuilder all = new StringBuilder("[");
        org.springframework.amqp.core.Message message;

        try {
            org.springframework.amqp.rabbit.core.RabbitTemplate rt = bookingService.getRabbitTemplate();

            while ((message = rt.receive(
                    RabbitMQConfig.CUSTOMER_QUEUE, 500)) != null) {
                if (all.length() > 1)
                    all.append(",");
                all.append(new String(message.getBody()));
            }
        } catch (Exception e) {
            return ResponseEntity.ok("[]");
        }

        all.append("]");
        return ResponseEntity.ok(all.toString());
    }


    @GetMapping("/notifications/provider")
    public ResponseEntity<String> getProviderNotifications() {
        StringBuilder all = new StringBuilder("[");
        org.springframework.amqp.core.Message message;

        try {
            org.springframework.amqp.rabbit.core.RabbitTemplate rt = bookingService.getRabbitTemplate();

            while ((message = rt.receive(
                    RabbitMQConfig.PROVIDER_QUEUE, 500)) != null) {
                if (all.length() > 1)
                    all.append(",");
                all.append(new String(message.getBody()));
            }
        } catch (Exception e) {
            return ResponseEntity.ok("[]");
        }

        all.append("]");
        return ResponseEntity.ok(all.toString());
    }
}