package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.request.CustomerRegisterRequest;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.CustomerRegisterResponse;
import com.suraj.MurtiSystem.entity.Customer;
import com.suraj.MurtiSystem.entity.Customer.ContactPerson;
import com.suraj.MurtiSystem.entity.Ganpati;
import com.suraj.MurtiSystem.repository.CustomerRepository;
import com.suraj.MurtiSystem.repository.GanpatiRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private GanpatiRepository ganpatiRepository;

    // ========== PUBLIC REGISTRATION ==========
    @Transactional
    public ApiResponse<CustomerRegisterResponse> registerCustomer(CustomerRegisterRequest request) {
        try {
            logger.info("=== PUBLIC REGISTER CUSTOMER ===");
            logger.info("Name: {}, Phone: {}, Type: {}", request.getName(), request.getPhone(), request.getRegistrationType());

            if (customerRepository.existsByPhone(request.getPhone())) {
                Customer existing = customerRepository.findByPhone(request.getPhone()).get();
                logger.info("Customer already exists with ID: {}", existing.getId());
                return ApiResponse.success(mapToResponse(existing), "ग्राहक आधीच नोंदणीकृत आहे");
            }

            Customer customer = createCustomerEntity(request);
            customer.setIsPromoted(false);

            if (request.getGanpatiId() != null && !request.getGanpatiId().isEmpty()) {
                populateGanpatiDetails(customer, request.getGanpatiId());
            }

            Customer savedCustomer = customerRepository.save(customer);
            logger.info("Customer saved successfully with ID: {}", savedCustomer.getId());
            logger.info("Saved - Name: {}, Phone: {}, Type: {}",
                    savedCustomer.getName(),
                    savedCustomer.getPhone(),
                    savedCustomer.getRegistrationType());

            return ApiResponse.success(mapToResponse(savedCustomer), "ग्राहक नोंदणी यशस्वी");
        } catch (Exception e) {
            logger.error("Customer registration failed: ", e);
            return ApiResponse.error("ग्राहक नोंदणी अयशस्वी: " + e.getMessage());
        }
    }

    // ========== ADMIN CREATE ==========
    @Transactional
    public ApiResponse<CustomerRegisterResponse> createCustomer(CustomerRegisterRequest request) {
        try {
            logger.info("=== ADMIN CREATE CUSTOMER ===");
            logger.info("Name: {}, Phone: {}, Type: {}", request.getName(), request.getPhone(), request.getRegistrationType());

            if (customerRepository.existsByPhone(request.getPhone())) {
                Customer existing = customerRepository.findByPhone(request.getPhone()).get();
                logger.info("Customer already exists with ID: {}", existing.getId());
                return ApiResponse.success(mapToResponse(existing), "ग्राहक आधीच नोंदणीकृत आहे");
            }

            Customer customer = createCustomerEntity(request);
            customer.setIsPromoted(false);

            if (request.getGanpatiId() != null && !request.getGanpatiId().isEmpty()) {
                populateGanpatiDetails(customer, request.getGanpatiId());
            }

            Customer savedCustomer = customerRepository.save(customer);
            logger.info("Customer created successfully with ID: {}", savedCustomer.getId());

            return ApiResponse.success(mapToResponse(savedCustomer), "ग्राहक नोंदणी यशस्वी");
        } catch (Exception e) {
            logger.error("Customer creation failed: ", e);
            return ApiResponse.error("ग्राहक नोंदणी अयशस्वी: " + e.getMessage());
        }
    }

    // ========== GET ALL CUSTOMERS ==========
    public ApiResponse<List<CustomerRegisterResponse>> getAllCustomers() {
        try {
            logger.info("=== GET ALL CUSTOMERS ===");
            List<Customer> customers = customerRepository.findAllCustomers();
            logger.info("Total customers found: {}", customers.size());

            for (int i = 0; i < Math.min(3, customers.size()); i++) {
                Customer c = customers.get(i);
                logger.info("Customer {}: ID={}, Name={}, Phone={}, Type={}, City={}",
                        i + 1, c.getId(), c.getName(), c.getPhone(), c.getRegistrationType(), c.getCity());
            }

            return ApiResponse.success(customers.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("Failed to fetch customers: ", e);
            return ApiResponse.error("ग्राहक मिळवण्यात अयशस्वी: " + e.getMessage());
        }
    }

    // ========== GET CUSTOMER BY ID ==========
    public ApiResponse<CustomerRegisterResponse> getCustomerById(String id) {
        try {
            Customer customer = findCustomerById(id);
            return ApiResponse.success(mapToResponse(customer));
        } catch (Exception e) {
            return ApiResponse.error("ग्राहक सापडला नाही: " + e.getMessage());
        }
    }

    // ========== GET CUSTOMERS BY TYPE ==========
    public ApiResponse<List<CustomerRegisterResponse>> getCustomersByType(String type) {
        try {
            Customer.RegistrationType registrationType;
            try {
                registrationType = Customer.RegistrationType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("Invalid registration type. Use HOME or MANDAL");
            }
            List<Customer> customers = customerRepository.findCustomersByRegistrationType(registrationType);
            return ApiResponse.success(customers.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            return ApiResponse.error("ग्राहक मिळवण्यात अयशस्वी: " + e.getMessage());
        }
    }

    // ========== UPDATE CUSTOMER ==========
    @Transactional
    public ApiResponse<CustomerRegisterResponse> updateCustomer(String id, CustomerRegisterRequest request) {
        try {
            logger.info("=== UPDATE CUSTOMER: {} ===", id);
            Customer existing = findCustomerById(id);

            existing.setName(request.getName());
            existing.setPhone(request.getPhone());
            existing.setAlternatePhone(request.getAlternatePhone());

            if ("MANDAL".equals(request.getRegistrationType())) {
                existing.setRegistrationType(Customer.RegistrationType.MANDAL);
                existing.setMandalName(request.getMandalName());
                existing.setAdhyakshyaName(request.getAdhyakshyaName());
                existing.setAdhyakshyaPhone(request.getAdhyakshyaPhone());

                List<ContactPerson> contacts = new ArrayList<>();
                if (request.getContactPerson1Phone() != null && !request.getContactPerson1Phone().isEmpty()) {
                    contacts.add(new ContactPerson("Contact Person 1", request.getContactPerson1Phone(), "संपर्क व्यक्ती १"));
                }
                if (request.getContactPerson2Phone() != null && !request.getContactPerson2Phone().isEmpty()) {
                    contacts.add(new ContactPerson("Contact Person 2", request.getContactPerson2Phone(), "संपर्क व्यक्ती २"));
                }
                existing.setContactPersons(contacts);
            } else {
                existing.setRegistrationType(Customer.RegistrationType.HOME);
                existing.setMandalName(null);
                existing.setAdhyakshyaName(null);
                existing.setAdhyakshyaPhone(null);
                existing.setContactPersons(new ArrayList<>());
            }

            existing.setAddress(request.getAddress());
            existing.setCity(request.getCity());
            existing.setTaluka(request.getTaluka());
            existing.setDistrict(request.getDistrict());
            existing.setState(request.getState());
            existing.setPincode(request.getPincode());
            existing.setUpdatedAt(LocalDateTime.now());

            Customer updated = customerRepository.save(existing);
            logger.info("Customer updated successfully: {}", updated.getId());

            return ApiResponse.success(mapToResponse(updated), "ग्राहक अपडेट केला");
        } catch (Exception e) {
            logger.error("Customer update failed: ", e);
            return ApiResponse.error("ग्राहक अपडेट अयशस्वी: " + e.getMessage());
        }
    }

    // ========== DELETE CUSTOMER ==========
    @Transactional
    public ApiResponse<Void> deleteCustomer(String id) {
        try {
            if (!customerRepository.existsById(id)) {
                return ApiResponse.error("ग्राहक सापडला नाही");
            }
            customerRepository.deleteById(id);
            logger.info("Customer deleted: {}", id);
            return ApiResponse.success(null, "ग्राहक हटविला");
        } catch (Exception e) {
            logger.error("Customer delete failed: ", e);
            return ApiResponse.error("ग्राहक हटवण्यात अयशस्वी: " + e.getMessage());
        }
    }

    // ========== PROMOTE CUSTOMER ==========
    @Transactional
    public ApiResponse<CustomerRegisterResponse> promoteCustomer(String id) {
        try {
            Customer customer = findCustomerById(id);

            if (customer.getIsPromoted()) {
                return ApiResponse.error("ग्राहक आधीच बुकिंगमध्ये प्रमोट केला गेला आहे");
            }

            customer.setIsPromoted(true);
            customer.setUpdatedAt(LocalDateTime.now());
            Customer updated = customerRepository.save(customer);
            logger.info("Customer promoted: {}", id);
            return ApiResponse.success(mapToResponse(updated), "ग्राहक बुकिंगसाठी प्रमोट केला");
        } catch (Exception e) {
            logger.error("Promote failed: ", e);
            return ApiResponse.error("प्रमोट करण्यात अयशस्वी: " + e.getMessage());
        }
    }

    // ========== UNPROMOTE CUSTOMER ==========
    @Transactional
    public ApiResponse<CustomerRegisterResponse> unpromoteCustomer(String id) {
        try {
            Customer customer = findCustomerById(id);
            customer.setIsPromoted(false);
            customer.setUpdatedAt(LocalDateTime.now());
            Customer updated = customerRepository.save(customer);
            logger.info("Customer unpromoted: {}", id);
            return ApiResponse.success(mapToResponse(updated), "ग्राहक प्रमोट रद्द केला");
        } catch (Exception e) {
            logger.error("Unpromote failed: ", e);
            return ApiResponse.error("प्रमोट रद्द करण्यात अयशस्वी: " + e.getMessage());
        }
    }

    // ========== PRIVATE METHODS ==========

    private Customer findCustomerById(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ग्राहक सापडला नाही"));
    }

    private Customer createCustomerEntity(CustomerRegisterRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setAlternatePhone(request.getAlternatePhone());
        customer.setRole("CUSTOMER");
        customer.setIsActive(true);
        customer.setIsPromoted(false);

        if ("MANDAL".equals(request.getRegistrationType())) {
            customer.setRegistrationType(Customer.RegistrationType.MANDAL);
            customer.setMandalName(request.getMandalName());
            customer.setAdhyakshyaName(request.getAdhyakshyaName());
            customer.setAdhyakshyaPhone(request.getAdhyakshyaPhone());

            List<ContactPerson> contacts = new ArrayList<>();
            if (request.getContactPerson1Phone() != null && !request.getContactPerson1Phone().isEmpty()) {
                contacts.add(new ContactPerson("Contact Person 1", request.getContactPerson1Phone(), "संपर्क व्यक्ती १"));
            }
            if (request.getContactPerson2Phone() != null && !request.getContactPerson2Phone().isEmpty()) {
                contacts.add(new ContactPerson("Contact Person 2", request.getContactPerson2Phone(), "संपर्क व्यक्ती २"));
            }
            customer.setContactPersons(contacts);
        } else {
            customer.setRegistrationType(Customer.RegistrationType.HOME);
        }

        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setTaluka(request.getTaluka());
        customer.setDistrict(request.getDistrict());
        customer.setState(request.getState());
        customer.setPincode(request.getPincode());

        return customer;
    }

    private void populateGanpatiDetails(Customer customer, String ganpatiId) {
        if (ganpatiId != null && !ganpatiId.isEmpty()) {
            ganpatiRepository.findById(ganpatiId).ifPresent(ganpati -> {
                customer.setGanpatiId(ganpati.getId());
                customer.setGanpatiName(ganpati.getName());
                customer.setGanpatiHeight(ganpati.getHeight());
                customer.setGanpatiPrice(ganpati.getPrice());
                if (ganpati.getImages() != null && !ganpati.getImages().isEmpty()) {
                    customer.setGanpatiImage(ganpati.getImages().get(0));
                }
                logger.info("Ganpati details populated: {}", ganpati.getName());
            });
        }
    }

    private CustomerRegisterResponse mapToResponse(Customer customer) {
        CustomerRegisterResponse response = new CustomerRegisterResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setPhone(customer.getPhone());
        response.setAlternatePhone(customer.getAlternatePhone());
        response.setRegistrationType(customer.getRegistrationType() != null ? customer.getRegistrationType().name() : null);
        response.setMandalName(customer.getMandalName());
        response.setAdhyakshyaName(customer.getAdhyakshyaName());
        response.setAdhyakshyaPhone(customer.getAdhyakshyaPhone());
        response.setAddress(customer.getAddress());
        response.setCity(customer.getCity());
        response.setTaluka(customer.getTaluka());
        response.setDistrict(customer.getDistrict());
        response.setState(customer.getState());
        response.setPincode(customer.getPincode());
        response.setRole(customer.getRole());
        response.setIsActive(customer.getIsActive());
        response.setIsPromoted(customer.getIsPromoted());
        response.setCreatedAt(customer.getCreatedAt());

        response.setGanpatiId(customer.getGanpatiId());
        response.setGanpatiName(customer.getGanpatiName());
        response.setGanpatiHeight(customer.getGanpatiHeight());
        response.setGanpatiPrice(customer.getGanpatiPrice());
        response.setGanpatiImage(customer.getGanpatiImage());

        if (customer.getContactPersons() != null) {
            response.setContactPersons(customer.getContactPersons().stream()
                    .map(cp -> {
                        CustomerRegisterResponse.ContactPersonDto dto = new CustomerRegisterResponse.ContactPersonDto();
                        dto.setName(cp.getName());
                        dto.setPhone(cp.getPhone());
                        dto.setDesignation(cp.getDesignation());
                        return dto;
                    })
                    .collect(Collectors.toList()));
        }

        return response;
    }
}