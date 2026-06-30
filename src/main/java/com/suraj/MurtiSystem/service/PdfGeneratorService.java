package com.suraj.MurtiSystem.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.suraj.MurtiSystem.entity.ConfirmedBooking;
import com.suraj.MurtiSystem.entity.ConfirmedBooking.PaymentRecord;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfGeneratorService {

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(183, 28, 28);
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(211, 47, 47);
    private static final DeviceRgb SUCCESS_COLOR = new DeviceRgb(46, 125, 50);
    private static final DeviceRgb WARNING_COLOR = new DeviceRgb(237, 108, 2);
    private static final DeviceRgb TEXT_DARK = new DeviceRgb(26, 26, 26);
    private static final DeviceRgb TEXT_LIGHT = new DeviceRgb(102, 102, 102);
    private static final DeviceRgb BG_LIGHT = new DeviceRgb(248, 244, 240);

    public byte[] generateReceiptPdf(ConfirmedBooking booking) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            try {
                String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Lord_Ganesha.jpg/320px-Lord_Ganesha.jpg";
                Image ganeshImage = new Image(ImageDataFactory.create(imageUrl));
                ganeshImage.setWidth(50).setHeight(50).setFixedPosition(40, 720);
                document.add(ganeshImage);
            } catch (Exception e) {
            }

            Paragraph header = new Paragraph("सिद्धिविनायक आर्ट्स")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(22)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR);
            document.add(header);

            Paragraph subHeader = new Paragraph("गणपती मूर्ती बुकिंग पावती")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setFontColor(TEXT_LIGHT)
                    .setMarginBottom(10);
            document.add(subHeader);

            Paragraph bappaLine = new Paragraph("🌺 गणपती बाप्पा मोरया 🌺")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(SECONDARY_COLOR)
                    .setMarginBottom(15);
            document.add(bappaLine);

            Paragraph receiptNo = new Paragraph("पावती क्रमांक: " + booking.getReceiptNumber())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setBackgroundColor(BG_LIGHT)
                    .setPadding(6)
                    .setMarginBottom(15);
            document.add(receiptNo);

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
            infoTable.setMarginBottom(15);

            infoTable.addCell(createSectionHeaderCell("📋 ग्राहक माहिती", 2));

            String customerName = booking.getCustomer() != null ? booking.getCustomer().getName() : booking.getCustomerName();
            infoTable.addCell(createLabelCell("नाव"));
            infoTable.addCell(createValueCell(customerName));

            String customerPhone = booking.getCustomer() != null ? booking.getCustomer().getPhone() : booking.getCustomerPhone();
            infoTable.addCell(createLabelCell("मोबाईल"));
            infoTable.addCell(createValueCell(customerPhone));

            if (booking.getMandalName() != null && !booking.getMandalName().isEmpty()) {
                infoTable.addCell(createLabelCell("मंडळ"));
                infoTable.addCell(createValueCell(booking.getMandalName()));
            }

            String customerAddress = booking.getCustomer() != null ? booking.getCustomer().getAddress() : booking.getCustomerAddress();
            if (customerAddress != null && !customerAddress.isEmpty()) {
                infoTable.addCell(createLabelCell("पत्ता"));
                infoTable.addCell(createValueCell(customerAddress));
            }

            infoTable.addCell(createSectionHeaderCell("🗿 गणपती माहिती", 2));

            infoTable.addCell(createLabelCell("नाव"));
            infoTable.addCell(createValueCell(booking.getGanpati().getName()));

            infoTable.addCell(createLabelCell("उंची"));
            infoTable.addCell(createValueCell(booking.getGanpati().getHeight()));

            infoTable.addCell(createLabelCell("किंमत"));
            infoTable.addCell(createValueCell("₹" + String.format("%,.0f", booking.getGanpati().getPrice()), PRIMARY_COLOR));

            document.add(infoTable);

            Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{33, 33, 33})).useAllAvailableWidth();
            paymentTable.setMarginTop(10).setMarginBottom(15);

            Cell totalPriceCell = new Cell().setBackgroundColor(new DeviceRgb(255, 245, 240))
                    .setPadding(8).setTextAlignment(TextAlignment.CENTER);
            totalPriceCell.add(new Paragraph("एकूण किंमत").setFontSize(10).setFontColor(TEXT_LIGHT).setBold());
            totalPriceCell.add(new Paragraph("₹" + String.format("%,.0f", booking.getTotalPrice()))
                    .setFontSize(16).setBold().setFontColor(PRIMARY_COLOR));
            paymentTable.addCell(totalPriceCell);

            Cell paidCell = new Cell().setBackgroundColor(new DeviceRgb(232, 245, 233))
                    .setPadding(8).setTextAlignment(TextAlignment.CENTER);
            paidCell.add(new Paragraph("आतापर्यंत भरले").setFontSize(10).setFontColor(TEXT_LIGHT).setBold());
            paidCell.add(new Paragraph("₹" + String.format("%,.0f", booking.getTotalPaidSoFar()))
                    .setFontSize(16).setBold().setFontColor(SUCCESS_COLOR));
            paymentTable.addCell(paidCell);

            Cell remainingCell = new Cell().setBackgroundColor(new DeviceRgb(255, 243, 224))
                    .setPadding(8).setTextAlignment(TextAlignment.CENTER);
            remainingCell.add(new Paragraph("बाकी रक्कम").setFontSize(10).setFontColor(TEXT_LIGHT).setBold());
            remainingCell.add(new Paragraph("₹" + String.format("%,.0f", booking.getRemainingPayment()))
                    .setFontSize(16).setBold().setFontColor(WARNING_COLOR));
            paymentTable.addCell(remainingCell);

            document.add(paymentTable);

            if (booking.getPaymentHistory() != null && !booking.getPaymentHistory().isEmpty()) {
                Paragraph historyTitle = new Paragraph("📜 पेमेंट हिस्ट्री")
                        .setBold().setFontSize(12).setFontColor(PRIMARY_COLOR).setMarginBottom(5);
                document.add(historyTitle);

                Table historyTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                        .useAllAvailableWidth().setMarginBottom(15);

                historyTable.addCell(createHeaderCell("तारीख"));
                historyTable.addCell(createHeaderCell("रक्कम"));
                historyTable.addCell(createHeaderCell("प्रकार"));
                historyTable.addCell(createHeaderCell("बाकी"));

                for (PaymentRecord record : booking.getPaymentHistory()) {
                    historyTable.addCell(createCell(record.getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
                    historyTable.addCell(createCell("₹" + String.format("%,.0f", record.getAmount()), SUCCESS_COLOR));
                    historyTable.addCell(createCell(record.getPaymentType()));
                    historyTable.addCell(createCell("₹" + String.format("%,.0f", record.getRemainingAfterPayment()), WARNING_COLOR));
                }
                document.add(historyTable);
            }

            Table bookingTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            bookingTable.setMarginBottom(15);

            bookingTable.addCell(createSectionHeaderCell("📅 बुकिंग तपशील", 2));

            bookingTable.addCell(createLabelCell("बुकिंग तारीख"));
            bookingTable.addCell(createValueCell(booking.getBookingDate() != null ? booking.getBookingDate() : "N/A"));

            String statusText = "⏳ प्रलंबित";
            DeviceRgb statusColor = WARNING_COLOR;
            if ("COMPLETED".equals(booking.getStatus())) {
                statusText = "✅ पूर्ण";
                statusColor = SUCCESS_COLOR;
            } else if ("CANCELLED".equals(booking.getStatus())) {
                statusText = "❌ रद्द";
                statusColor = new DeviceRgb(211, 47, 47);
            }
            bookingTable.addCell(createLabelCell("स्थिती"));
            bookingTable.addCell(createValueCell(statusText, statusColor));

            document.add(bookingTable);

            List<String> phoneNumbers = getPhoneNumbers(booking);
            if (!phoneNumbers.isEmpty()) {
                Table contactTable = new Table(UnitValue.createPercentArray(new float[]{20, 80})).useAllAvailableWidth();
                contactTable.setMarginBottom(15);

                contactTable.addCell(createSectionHeaderCell("📞 संपर्क क्रमांक", 2));

                for (int i = 0; i < phoneNumbers.size(); i++) {
                    contactTable.addCell(createCell(String.valueOf(i + 1), TEXT_LIGHT));
                    contactTable.addCell(createCell(phoneNumbers.get(i)));
                }
                document.add(contactTable);
            }

            Paragraph footerBappa = new Paragraph("🌺 गणपती बाप्पा मोरया 🌺")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16)
                    .setFontColor(PRIMARY_COLOR)
                    .setMarginTop(20);
            document.add(footerBappa);

            Paragraph footerThanks = new Paragraph("🙏 धन्यवाद! 🙏")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setFontColor(TEXT_LIGHT)
                    .setMarginTop(5);
            document.add(footerThanks);

            Paragraph footerMsg = new Paragraph("आपल्या विश्वासाबद्दल हार्दिक आभार")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(TEXT_LIGHT)
                    .setMarginTop(2);
            document.add(footerMsg);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    private Cell createSectionHeaderCell(String text, int colSpan) {
        Cell cell = new Cell(colSpan, 1)
                .add(new Paragraph(text).setBold().setFontSize(12).setFontColor(PRIMARY_COLOR))
                .setBorderBottom(new SolidBorder(PRIMARY_COLOR, 2))
                .setPaddingBottom(4)
                .setMarginBottom(4);
        return cell;
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(PRIMARY_COLOR)
                .setPadding(4)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Cell createLabelCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(10).setFontColor(TEXT_LIGHT).setBold())
                .setBorder(new SolidBorder(1))
                .setPadding(4);
    }

    private Cell createValueCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(10).setFontColor(TEXT_DARK))
                .setBorder(new SolidBorder(1))
                .setPadding(4);
    }

    private Cell createValueCell(String text, DeviceRgb color) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(10).setFontColor(color).setBold())
                .setBorder(new SolidBorder(1))
                .setPadding(4);
    }

    private Cell createCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(9).setFontColor(TEXT_DARK))
                .setBorder(new SolidBorder(1))
                .setPadding(4);
    }

    private Cell createCell(String text, DeviceRgb color) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(9).setFontColor(color).setBold())
                .setBorder(new SolidBorder(1))
                .setPadding(4);
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
}