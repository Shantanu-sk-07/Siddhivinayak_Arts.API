package com.suraj.MurtiSystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class WhatsAppService {

    @Value("${admin.whatsapp.number}")
    private String adminWhatsAppNumber;

    public String generateWhatsAppLink(String message) {
        try {
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
            return "https://wa.me/" + adminWhatsAppNumber + "?text=" + encodedMessage;
        } catch (UnsupportedEncodingException e) {
            return "https://wa.me/" + adminWhatsAppNumber;
        }
    }

    public String getBookingRequestMessage(String customerName, String customerPhone, String ganpatiName, double price, String bookingId) {
        return String.format(
                "🆕 *NEW BOOKING REQUEST* 🆕\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "📋 *Booking Details*\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "🔹 *Booking ID:* %s\n" +
                        "🔹 *Ganpati:* %s\n" +
                        "🔹 *Price:* ₹%.2f\n" +
                        "🔹 *Advance (30%%):* ₹%.2f\n" +
                        "\n👤 *Customer Details*\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "🔹 *Name:* %s\n" +
                        "🔹 *Phone:* %s\n" +
                        "\n📌 *Status:* PENDING APPROVAL\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "🔗 Approve: http://localhost:3000/admin/bookings\n" +
                        "📞 Contact: %s",
                bookingId, ganpatiName, price, price * 0.3,
                customerName, customerPhone, customerPhone
        );
    }

    public String getInterestedMessage(String customerName, String customerPhone, String ganpatiName, double price) {
        return String.format(
                "❤️ *NEW INTEREST* ❤️\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "🪔 *Ganpati Details*\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "🔹 *Name:* %s\n" +
                        "🔹 *Price:* ₹%.2f\n" +
                        "\n👤 *Customer Details*\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "🔹 *Name:* %s\n" +
                        "🔹 *Phone:* %s\n" +
                        "\n📞 Contact customer to follow up!",
                ganpatiName, price, customerName, customerPhone
        );
    }

    public String getBookingApprovedMessage(String customerName, String ganpatiName, String bookingId, double advanceAmount, String customerPhone) {
        return String.format(
                "✅ *BOOKING APPROVED* ✅\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "📋 *Booking Details*\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "🔹 *Booking ID:* %s\n" +
                        "🔹 *Ganpati:* %s\n" +
                        "🔹 *Advance to Pay:* ₹%.2f\n" +
                        "\n👤 *Customer:* %s\n" +
                        "📞 *Phone:* %s\n" +
                        "\n💳 Payment link: http://localhost:3000/customer/payments?bookingId=%s",
                bookingId, ganpatiName, advanceAmount, customerName, customerPhone, bookingId
        );
    }

    public String getPaymentReceivedMessage(String customerName, String ganpatiName, String bookingId, double amount, String paymentMethod) {
        return String.format(
                "💰 *PAYMENT RECEIVED* 💰\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "📋 *Booking ID:* %s\n" +
                        "🔹 *Ganpati:* %s\n" +
                        "🔹 *Amount:* ₹%.2f\n" +
                        "🔹 *Method:* %s\n" +
                        "\n👤 *Customer:* %s\n" +
                        "\n✅ Booking is now CONFIRMED!\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "🔗 View: http://localhost:3000/admin/bookings",
                bookingId, ganpatiName, amount, paymentMethod, customerName
        );
    }

    public String getPickupCompletedMessage(String customerName, String ganpatiName, String bookingId) {
        return String.format(
                "🎉 *PICKUP COMPLETED* 🎉\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "📋 *Booking ID:* %s\n" +
                        "🔹 *Ganpati:* %s\n" +
                        "👤 *Customer:* %s\n" +
                        "\n✅ Ganpati has been successfully delivered!\n" +
                        "━━━━━━━━━━━━━━━━━━━━",
                bookingId, ganpatiName, customerName
        );
    }
}