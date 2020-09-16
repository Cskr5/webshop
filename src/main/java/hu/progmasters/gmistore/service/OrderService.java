package hu.progmasters.gmistore.service;

import hu.progmasters.gmistore.dto.AddressDetails;
import hu.progmasters.gmistore.dto.CustomerDetails;
import hu.progmasters.gmistore.dto.ProductDto;
import hu.progmasters.gmistore.dto.order.OrderListDto;
import hu.progmasters.gmistore.dto.order.OrderRequest;
import hu.progmasters.gmistore.dto.product.ProductListDetailDto;
import hu.progmasters.gmistore.enums.EnglishAlphabet;
import hu.progmasters.gmistore.model.*;
import hu.progmasters.gmistore.repository.OrderRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
public class OrderService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final CartService cartService;
    private final LookupService lookupService;
    private final InventoryService inventoryService;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserService userService, CartService cartService,
                        LookupService lookupService, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.cartService = cartService;
        this.lookupService = lookupService;
        this.inventoryService = inventoryService;
    }

    /**
     * Attempts to fetch a registered user's details
     *
     * @return A CustomerDetails DTO
     */
    public CustomerDetails getCustomerDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(authentication.getName());
        if (user != null) {
            LOGGER.debug("Customer details found! username: {}", authentication.getName());
            return setValuesForCustomerDetails(user);
        }
        LOGGER.info("Customer details not found! username: {}", authentication.getName());
        return null;
    }

    private CustomerDetails setValuesForCustomerDetails(User user) {
        CustomerDetails customerDetails = new CustomerDetails();
        if (user.getShippingAddress() != null) {
            AddressDetails shippingAddress = new AddressDetails(user.getShippingAddress());
            customerDetails.setShippingAddress(shippingAddress);
        } else {
            customerDetails.setShippingAddress(null);
        }

        if (user.getBillingAddress() != null) {
            AddressDetails billingAddress = new AddressDetails(user.getBillingAddress());
            customerDetails.setBillingAddress(billingAddress);
        } else {
            customerDetails.setBillingAddress(null);
        }

        if (user.getPhoneNumber() != null) {
            customerDetails.setPhoneNumber(user.getPhoneNumber());
        } else {
            customerDetails.setPhoneNumber(null);
        }

        customerDetails.setFirstName(user.getFirstName());
        customerDetails.setLastName(user.getLastName());
        customerDetails.setEmail(user.getEmail());
        return customerDetails;
    }

    /**
     * Attempts to create a new Order
     *
     * @param orderRequest A DTO containing the required details
     * @param session      The actual session object
     * @return A boolean, true if successful, false otherwise
     */
    public boolean createOrder(OrderRequest orderRequest, HttpSession session) {
        Cart actualCart = cartService.getActualCart(session);
        if (actualCart.getItems().isEmpty()) {
            return false;
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User userByUsername = userService.getUserByUsername(username);

        userByUsername.setPhoneNumber(orderRequest.getPhoneNumber());
        updateCustomerAddresses(orderRequest, userByUsername);
        Order order = setOrderDetails(actualCart, userByUsername);
        order.setPaymentMethod(lookupService.getPaymentMethodByKey(orderRequest.getPaymentMethod()));
        order.setStatus(lookupService.getOrderStatusByKey("CONFIRMED"));
        saveOrderItems(actualCart, order);
        orderRepository.save(order);
        inventoryService.updateAvailableAndSoldQuantities(actualCart.getItems());
        cartService.deleteCart(actualCart.getId());
        LOGGER.info("New Order, unique id: {}, username: {}", order.getUniqueId(), userByUsername.getUsername());
        return true;
    }

    private void updateCustomerAddresses(OrderRequest orderRequest, User user) {
        Address shippingAddress = mapAddressDetailsToAddress(orderRequest.getShippingAddress());
        if (user.getShippingAddress() != null) {
            Address currentShippingAddress = user.getShippingAddress();
            updateAddress(currentShippingAddress, shippingAddress);
        } else {
            user.setShippingAddress(shippingAddress);
        }

        Address billingAddress = mapAddressDetailsToAddress(orderRequest.getBillingAddress());
        if (user.getBillingAddress() != null) {
            Address currentBillingAddress = user.getBillingAddress();
            updateAddress(currentBillingAddress, billingAddress);
        } else {
            user.setBillingAddress(billingAddress);
        }
    }

    private void updateAddress(Address currentAddress, Address newAddress) {
        currentAddress.setCity(newAddress.getCity());
        currentAddress.setStreet(newAddress.getStreet());
        currentAddress.setNumber(newAddress.getNumber());
        if (newAddress.getDoor() != null) {
            currentAddress.setDoor(newAddress.getDoor());
        }
        if (newAddress.getFloor() != null) {
            currentAddress.setFloor(newAddress.getFloor());
        }
        currentAddress.setCountry(newAddress.getCountry());
        currentAddress.setPostcode(newAddress.getPostcode());
    }

    private Address mapAddressDetailsToAddress(AddressDetails addressDetails) {
        Address address = new Address();
        address.setCity(addressDetails.getCity());
        address.setStreet(addressDetails.getStreet());
        address.setNumber(addressDetails.getNumber());
        if (addressDetails.getDoor() != null) {
            address.setDoor(addressDetails.getDoor());
        }
        if (addressDetails.getFloor() != null) {
            address.setFloor(addressDetails.getFloor());
        }
        address.setCountry(addressDetails.getCountry());
        address.setPostcode(addressDetails.getPostcode());
        return address;
    }

    private Order setOrderDetails(Cart actualCart, User userByUsername) {
        Order order = new Order();
        order.setDeliveryCost(actualCart.getShippingMethod().getCost());
        order.setExpectedDeliveryDate(actualCart.getExpectedDeliveryDate());
        order.setOrderedAt(LocalDateTime.now());
        order.setTotalPrice(actualCart.getTotalPrice());
        order.setUser(userByUsername);
        order.setUniqueId(generateUniqueId());
        return order;
    }

    private void saveOrderItems(Cart actualCart, Order order) {
        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem item : actualCart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getCount());
            orderItem.setPrice(item.getProduct().getPrice());
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);
    }

    private String generateUniqueId() {
        EnglishAlphabet[] alphabet = EnglishAlphabet.values();
        String generatedId = IntStream.range(0, 6)
                .mapToObj(i -> String.valueOf(alphabet[generateRandomNumberForLetters()]))
                .collect(Collectors.joining("", "GMI", String.valueOf(generateRandomDigits())));
        Optional<Order> order = orderRepository.findOrderByUniqueId(generatedId);
        return order.isPresent() ? generateUniqueId() : generatedId;
    }

    public List<OrderListDto> getAllOrders() {
        return orderRepository.findAllByOrderListDetails();
    }

    public Set<ProductListDetailDto> getAllProductsOrderedByUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<OrderItem> allOrderedProductsByUser = orderRepository.findAllOrderedProductsByUser(authentication.getName());

        if (allOrderedProductsByUser != null) {
            LOGGER.debug("User order details found! username: {}", authentication.getName());
            Set<ProductListDetailDto> products = allOrderedProductsByUser.stream()
                    .map(this::mapOrderItemToProduct)
                    .collect(Collectors.toSet());
            return products;
        }
        LOGGER.info("User order details not found! username: {}", authentication.getName());
        return null;
    }

    private ProductListDetailDto mapOrderItemToProduct(OrderItem orderItem) {
        ProductListDetailDto product = new ProductListDetailDto();
        product.setId(orderItem.getId());
        product.setName(orderItem.getProduct().getName());
        product.setProductCode(orderItem.getProduct().getProductCode());
        product.setFeatures(orderItem.getProduct().getFeatures());
        product.setPictureUrl(orderItem.getProduct().getPictureUrl());
        product.setPictures(orderItem.getProduct().getPictures());
        product.setPrice(orderItem.getProduct().getPrice());
        product.setDiscount(orderItem.getProduct().getDiscount());
        product.setAverageRating(orderItem.getProduct().getAverageRating());
        product.setOrderItemId(orderItem.getId());
        return product;
    }

    private int generateRandomNumberForLetters() {
        return new Random().nextInt(EnglishAlphabet.values().length);
    }

    private int generateRandomDigits() {
        return ThreadLocalRandom.current().nextInt(100000, 1000000);
    }
}
