package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.request.ConfirmedBookingRequestDto;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.ConfirmedBookingResponseDto;
import com.suraj.MurtiSystem.dto.response.GanpatiResponseDto;
import com.suraj.MurtiSystem.entity.ConfirmedBooking;
import com.suraj.MurtiSystem.entity.ConfirmedBooking.BookingContact;
import com.suraj.MurtiSystem.entity.ConfirmedBooking.PaymentRecord;
import com.suraj.MurtiSystem.entity.Customer;
import com.suraj.MurtiSystem.entity.Ganpati;
import com.suraj.MurtiSystem.repository.ConfirmedBookingRepository;
import com.suraj.MurtiSystem.repository.CustomerRepository;
import com.suraj.MurtiSystem.repository.GanpatiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private ConfirmedBookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private GanpatiRepository ganpatiRepository;

    @Value("${admin.whatsapp.number}")
    private String adminWhatsAppNumber;

    public ApiResponse<List<ConfirmedBookingResponseDto>> getAllBookings() {
        List<ConfirmedBooking> bookings = bookingRepository.findAllOrderByDateDesc();
        return ApiResponse.success(bookings.stream().map(this::mapToBookingResponse).collect(Collectors.toList()));
    }

    public ApiResponse<ConfirmedBookingResponseDto> getBookingById(String id) {
        ConfirmedBooking booking = findBookingById(id);
        return ApiResponse.success(mapToBookingResponse(booking));
    }

    @Transactional
    public ApiResponse<ConfirmedBookingResponseDto> createBooking(ConfirmedBookingRequestDto request) {
        try {
            ConfirmedBooking booking = new ConfirmedBooking();

            setCustomerInfo(booking, request);

            if (request.getAdditionalContacts() != null) {
                booking.setAdditionalContacts(request.getAdditionalContacts().stream()
                        .map(c -> new BookingContact(c.getName(), c.getPhone(), c.getDesignation()))
                        .collect(Collectors.toList()));
            }

            Ganpati ganpati = getGanpati(request.getGanpatiId());
            booking.setGanpati(ganpati);

            ganpati.setAvailableSlots(ganpati.getAvailableSlots() - 1);
            ganpatiRepository.save(ganpati);

            setPaymentDetails(booking, request);

            booking.setBookingDate(request.getBookingDate());
            booking.setNotes(request.getNotes());

            booking.setStatus(request.getRemainingPayment() == 0 ? "COMPLETED" : "PENDING");

            booking.setReceiptNumber(generateReceiptNumber());
            booking.setUpdatedAt(LocalDateTime.now());

            ConfirmedBooking saved = bookingRepository.save(booking);
            return ApiResponse.success(mapToBookingResponse(saved), "बुकिंग यशस्वी!");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("बुकिंग तयार करण्यात अयशस्वी: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<ConfirmedBookingResponseDto> updateBooking(String id, ConfirmedBookingRequestDto request) {
        try {
            ConfirmedBooking booking = findBookingById(id);

            booking.setAdvancePayment(request.getAdvancePayment());
            booking.setRemainingPayment(request.getRemainingPayment());
            booking.setTotalPrice(request.getTotalPrice());
            booking.setTotalPaidSoFar(request.getAdvancePayment());
            booking.setNotes(request.getNotes());

            booking.setStatus(request.getRemainingPayment() == 0 ? "COMPLETED" : "PENDING");

            booking.setUpdatedAt(LocalDateTime.now());

            if (request.getInstallments() != null) {
                processInstallments(booking, request.getInstallments());
            }

            ConfirmedBooking saved = bookingRepository.save(booking);
            return ApiResponse.success(mapToBookingResponse(saved), "बुकिंग अपडेट केले");
        } catch (Exception e) {
            return ApiResponse.error("बुकिंग अपडेट अयशस्वी: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<Void> deleteBooking(String id) {
        try {
            ConfirmedBooking booking = findBookingById(id);

            Ganpati ganpati = booking.getGanpati();
            ganpati.setAvailableSlots(ganpati.getAvailableSlots() + 1);
            ganpatiRepository.save(ganpati);

            if (booking.getCustomer() != null) {
                Customer customer = booking.getCustomer();
                customer.setIsPromoted(false);
                customerRepository.save(customer);
            }

            bookingRepository.deleteById(id);
            return ApiResponse.success(null, "बुकिंग हटविले");
        } catch (Exception e) {
            return ApiResponse.error("बुकिंग हटवण्यात अयशस्वी: " + e.getMessage());
        }
    }

    public ApiResponse<String> sendReceiptToWhatsApp(String bookingId) {
        try {
            ConfirmedBooking booking = findBookingById(bookingId);

            List<String> phoneNumbers = getPhoneNumbers(booking);

            if (phoneNumbers.isEmpty()) {
                return ApiResponse.error("कोणताही फोन नंबर उपलब्ध नाही");
            }

            String message = generateReceiptMessage(booking);
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());

            booking.setReceiptSent(true);
            booking.setReceiptSentAt(LocalDateTime.now());
            bookingRepository.save(booking);

            return ApiResponse.success(encodedMessage, "पावती " + phoneNumbers.size() + " नंबरवर पाठवली गेली");
        } catch (Exception e) {
            return ApiResponse.error("व्हॉट्सअॅप लिंक तयार करण्यात अयशस्वी: " + e.getMessage());
        }
    }

    private ConfirmedBooking findBookingById(String id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("बुकिंग सापडले नाही"));
    }

    private Ganpati getGanpati(String ganpatiId) {
        return ganpatiRepository.findById(ganpatiId)
                .orElseThrow(() -> new RuntimeException("गणपती सापडली नाही"));
    }

    private void setCustomerInfo(ConfirmedBooking booking, ConfirmedBookingRequestDto request) {
        if (request.getCustomerId() != null && !request.getCustomerId().isEmpty()) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("ग्राहक सापडला नाही"));
            booking.setCustomer(customer);
            booking.setCustomerName(customer.getName());
            booking.setCustomerPhone(customer.getPhone());
            booking.setCustomerAddress(customer.getAddress());
            booking.setCustomerTaluka(customer.getTaluka());
            booking.setCustomerDistrict(customer.getDistrict());
            booking.setMandalName(customer.getMandalName());
        } else if (request.getCreateNewCustomer() != null && request.getCreateNewCustomer()) {
            Customer newCustomer = createNewCustomer(request);
            booking.setCustomer(newCustomer);
            booking.setCustomerName(newCustomer.getName());
            booking.setCustomerPhone(newCustomer.getPhone());
            booking.setCustomerAddress(newCustomer.getAddress());
            booking.setCustomerTaluka(newCustomer.getTaluka());
            booking.setCustomerDistrict(newCustomer.getDistrict());
            booking.setMandalName(newCustomer.getMandalName());
        } else {
            booking.setCustomerName(request.getCustomerName());
            booking.setCustomerEmail(request.getCustomerEmail());
            booking.setCustomerPhone(request.getCustomerPhone());
            booking.setCustomerAddress(request.getCustomerAddress());
            booking.setCustomerTaluka(request.getCustomerTaluka());
            booking.setCustomerDistrict(request.getCustomerDistrict());
            booking.setMandalName(request.getMandalName());
        }
    }

    private Customer createNewCustomer(ConfirmedBookingRequestDto request) {
        Customer customer = new Customer();
        customer.setName(request.getCustomerName());
        customer.setPhone(request.getCustomerPhone());
        customer.setRole("CUSTOMER");
        customer.setIsActive(true);
        customer.setIsPromoted(false);

        if ("MANDAL".equals(request.getCustomerRegistrationType())) {
            customer.setRegistrationType(Customer.RegistrationType.MANDAL);
            customer.setMandalName(request.getMandalName());
        } else {
            customer.setRegistrationType(Customer.RegistrationType.HOME);
        }

        customer.setAddress(request.getCustomerAddress());
        customer.setTaluka(request.getCustomerTaluka());
        customer.setDistrict(request.getCustomerDistrict());

        if (request.getCustomerContactPersons() != null) {
            customer.setContactPersons(request.getCustomerContactPersons().stream()
                    .map(cp -> new Customer.ContactPerson(cp.getName(), cp.getPhone(), cp.getDesignation()))
                    .collect(Collectors.toList()));
        }

        return customerRepository.save(customer);
    }

    private void setPaymentDetails(ConfirmedBooking booking, ConfirmedBookingRequestDto request) {
        booking.setTotalPrice(request.getTotalPrice());
        booking.setAdvancePayment(request.getAdvancePayment());
        booking.setRemainingPayment(request.getRemainingPayment());

        Double totalPaid = request.getAdvancePayment();
        List<PaymentRecord> paymentHistory = new ArrayList<>();

        if (request.getAdvancePayment() > 0) {
            PaymentRecord initialPayment = new PaymentRecord();
            initialPayment.setAmount(request.getAdvancePayment());
            initialPayment.setPaymentDate(LocalDateTime.now());
            initialPayment.setPaymentType("ADVANCE");
            initialPayment.setNotes("Initial advance payment");
            initialPayment.setRemainingAfterPayment(request.getRemainingPayment());
            paymentHistory.add(initialPayment);
        }

        if (request.getInstallments() != null) {
            for (ConfirmedBookingRequestDto.InstallmentDto inst : request.getInstallments()) {
                if (inst.getPaidAmount() != null && inst.getPaidAmount() > 0) {
                    totalPaid += inst.getPaidAmount();
                    PaymentRecord installmentPayment = new PaymentRecord();
                    installmentPayment.setAmount(inst.getPaidAmount());
                    installmentPayment.setPaymentDate(LocalDateTime.now());
                    installmentPayment.setPaymentType("INSTALLMENT");
                    installmentPayment.setNotes("Installment #" + inst.getId());
                    installmentPayment.setRemainingAfterPayment(inst.getNewRemaining());
                    paymentHistory.add(installmentPayment);
                }
            }
        }

        booking.setTotalPaidSoFar(totalPaid);
        booking.setPaymentHistory(paymentHistory);
    }

    private void processInstallments(ConfirmedBooking booking, List<ConfirmedBookingRequestDto.InstallmentDto> installments) {
        List<PaymentRecord> paymentHistory = booking.getPaymentHistory() != null
                ? booking.getPaymentHistory()
                : new ArrayList<>();

        for (ConfirmedBookingRequestDto.InstallmentDto inst : installments) {
            if (inst.getPaidAmount() != null && inst.getPaidAmount() > 0) {
                boolean exists = paymentHistory.stream()
                        .anyMatch(p -> p.getNotes() != null &&
                                p.getNotes().equals("Installment #" + inst.getId()));

                if (!exists) {
                    PaymentRecord installmentPayment = new PaymentRecord();
                    installmentPayment.setAmount(inst.getPaidAmount());
                    installmentPayment.setPaymentDate(LocalDateTime.now());
                    installmentPayment.setPaymentType("INSTALLMENT");
                    installmentPayment.setNotes("Installment #" + inst.getId());
                    installmentPayment.setRemainingAfterPayment(inst.getNewRemaining());
                    paymentHistory.add(installmentPayment);

                    booking.setTotalPaidSoFar(booking.getTotalPaidSoFar() + inst.getPaidAmount());
                }
            }
        }
        booking.setPaymentHistory(paymentHistory);
    }

    private List<String> getPhoneNumbers(ConfirmedBooking booking) {
        List<String> phoneNumbers = new ArrayList<>();

        if (booking.getCustomer() != null && booking.getCustomer().getPhone() != null) {
            phoneNumbers.add(booking.getCustomer().getPhone());
        }
        if (booking.getCustomerPhone() != null) {
            phoneNumbers.add(booking.getCustomerPhone());
        }

        if (booking.getAdditionalContacts() != null) {
            booking.getAdditionalContacts().forEach(contact -> {
                if (contact.getPhone() != null) {
                    phoneNumbers.add(contact.getPhone());
                }
            });
        }

        if (booking.getCustomer() != null && booking.getCustomer().getContactPersons() != null) {
            booking.getCustomer().getContactPersons().forEach(contact -> {
                if (contact.getPhone() != null) {
                    phoneNumbers.add(contact.getPhone());
                }
            });
        }

        return phoneNumbers.stream().distinct().collect(Collectors.toList());
    }

    private String generateReceiptNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String date = LocalDateTime.now().format(formatter);
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return "REC-" + date + "-" + random;
    }

    private String generateReceiptMessage(ConfirmedBooking booking) {
        String customerName = booking.getCustomer() != null ?
                booking.getCustomer().getName() : booking.getCustomerName();
        String customerPhone = booking.getCustomer() != null ?
                booking.getCustomer().getPhone() : booking.getCustomerPhone();
        String ganpatiName = booking.getGanpati().getName();
        String ganpatiHeight = booking.getGanpati().getHeight();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String date = LocalDateTime.now().format(formatter);

        StringBuilder paymentHistory = new StringBuilder();
        if (booking.getPaymentHistory() != null && !booking.getPaymentHistory().isEmpty()) {
            paymentHistory.append("\n\n📜 *Payment History:*\n");
            for (int i = 0; i < booking.getPaymentHistory().size(); i++) {
                PaymentRecord record = booking.getPaymentHistory().get(i);
                paymentHistory.append(String.format("%d. %s: ₹%.0f (Remaining: ₹%.0f)\n",
                        i + 1,
                        record.getPaymentType(),
                        record.getAmount(),
                        record.getRemainingAfterPayment()));
            }
        }

        return String.format(
                "सिद्धिविनायक आर्ट्स\nबुकिंग पावती\n\nबुकिंग क्रमांक: %s\nतारीख: %s\n\n*ग्राहक माहिती:*\nनाव: %s\nमोबाईल: %s\n\n*गणपती माहिती:*\nनाव: %s\nउंची: %s\nकिंमत: ₹%.0f\n\n*पेमेंट तपशील:*\nएकूण पेमेंट: ₹%.0f\nआतापर्यंत भरले: ₹%.0f\nबाकी रक्कम: ₹%.0f%s\n\nबुकिंग तारीख: %s",
                booking.getReceiptNumber(),
                date,
                customerName,
                customerPhone,
                ganpatiName,
                ganpatiHeight,
                booking.getTotalPrice(),
                booking.getTotalPrice(),
                booking.getTotalPaidSoFar() != null ? booking.getTotalPaidSoFar() : booking.getAdvancePayment(),
                booking.getRemainingPayment(),
                paymentHistory.toString(),
                booking.getBookingDate() != null ? booking.getBookingDate() : date
        );
    }

    private ConfirmedBookingResponseDto mapToBookingResponse(ConfirmedBooking booking) {
        ConfirmedBookingResponseDto dto = new ConfirmedBookingResponseDto();

        dto.setId(booking.getId());
        dto.setCustomerId(booking.getCustomer() != null ? booking.getCustomer().getId() : null);
        dto.setCustomerName(booking.getCustomerName());
        dto.setCustomerEmail(booking.getCustomerEmail());
        dto.setCustomerPhone(booking.getCustomerPhone());
        dto.setCustomerAddress(booking.getCustomerAddress());
        dto.setCustomerTaluka(booking.getCustomerTaluka());
        dto.setCustomerDistrict(booking.getCustomerDistrict());
        dto.setMandalName(booking.getMandalName());
        dto.setGanpatiId(booking.getGanpati() != null ? booking.getGanpati().getId() : null);
        dto.setAdvancePayment(booking.getAdvancePayment());
        dto.setRemainingPayment(booking.getRemainingPayment());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setTotalPaidSoFar(booking.getTotalPaidSoFar());
        dto.setBookingDate(booking.getBookingDate());
        dto.setActualPickupDate(booking.getActualPickupDate());
        dto.setNotes(booking.getNotes());
        dto.setStatus(booking.getStatus());
        dto.setReceiptNumber(booking.getReceiptNumber());
        dto.setReceiptSent(booking.getReceiptSent());
        dto.setReceiptSentAt(booking.getReceiptSentAt());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());

        if (booking.getPaymentHistory() != null) {
            dto.setPaymentHistory(booking.getPaymentHistory().stream()
                    .map(record -> {
                        ConfirmedBookingResponseDto.PaymentRecordDto recordDto =
                                new ConfirmedBookingResponseDto.PaymentRecordDto();
                        recordDto.setAmount(record.getAmount());
                        recordDto.setPaymentDate(record.getPaymentDate());
                        recordDto.setPaymentType(record.getPaymentType());
                        recordDto.setNotes(record.getNotes());
                        recordDto.setRemainingAfterPayment(record.getRemainingAfterPayment());
                        return recordDto;
                    })
                    .collect(Collectors.toList()));
        }

        if (booking.getCustomer() != null) {
            dto.setCustomer(mapToCustomerResponse(booking.getCustomer()));
        }

        if (booking.getGanpati() != null) {
            dto.setGanpati(mapToGanpatiResponse(booking.getGanpati()));
        }

        if (booking.getAdditionalContacts() != null) {
            dto.setAdditionalContacts(booking.getAdditionalContacts().stream()
                    .map(c -> {
                        ConfirmedBookingResponseDto.BookingContactDto contactDto =
                                new ConfirmedBookingResponseDto.BookingContactDto();
                        contactDto.setName(c.getName());
                        contactDto.setPhone(c.getPhone());
                        contactDto.setDesignation(c.getDesignation());
                        return contactDto;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private ConfirmedBookingResponseDto.CustomerResponseDto mapToCustomerResponse(Customer customer) {
        ConfirmedBookingResponseDto.CustomerResponseDto dto = new ConfirmedBookingResponseDto.CustomerResponseDto();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setPhone(customer.getPhone());
        dto.setRegistrationType(customer.getRegistrationType() != null ? customer.getRegistrationType().name() : null);
        dto.setMandalName(customer.getMandalName());
        dto.setAddress(customer.getAddress());
        dto.setCity(customer.getCity());
        dto.setTaluka(customer.getTaluka());
        dto.setDistrict(customer.getDistrict());
        dto.setState(customer.getState());
        dto.setPincode(customer.getPincode());

        if (customer.getContactPersons() != null) {
            dto.setContactPersons(customer.getContactPersons().stream()
                    .map(cp -> {
                        ConfirmedBookingResponseDto.ContactPersonDto cpDto =
                                new ConfirmedBookingResponseDto.ContactPersonDto();
                        cpDto.setName(cp.getName());
                        cpDto.setPhone(cp.getPhone());
                        cpDto.setDesignation(cp.getDesignation());
                        return cpDto;
                    })
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private GanpatiResponseDto mapToGanpatiResponse(Ganpati ganpati) {
        GanpatiResponseDto dto = new GanpatiResponseDto();
        dto.setId(ganpati.getId());
        dto.setName(ganpati.getName());
        dto.setHeight(ganpati.getHeight());
        dto.setPrice(ganpati.getPrice());
        dto.setMaterial(ganpati.getMaterial());
        dto.setColorTheme(ganpati.getColorTheme());
        dto.setImages(ganpati.getImages());
        dto.setTotalSlots(ganpati.getTotalSlots());
        dto.setAvailableSlots(ganpati.getAvailableSlots());
        dto.setRating(ganpati.getRating());
        dto.setIsActive(ganpati.getIsActive());
        dto.setCreatedAt(ganpati.getCreatedAt().toString());
        dto.setLikes(ganpati.getLikes());
        dto.setLikedBy(ganpati.getLikedBy());
        return dto;
    }
}