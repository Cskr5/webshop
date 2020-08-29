package hu.progmasters.gmistore.service;

import hu.progmasters.gmistore.dto.CartDto;
import hu.progmasters.gmistore.dto.CartItemDto;
import hu.progmasters.gmistore.dto.ShippingMethodItem;
import hu.progmasters.gmistore.model.*;
import hu.progmasters.gmistore.repository.CartRepository;
import hu.progmasters.gmistore.repository.ProductRepository;
import hu.progmasters.gmistore.repository.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class CartService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ShippingService shippingService;

    @Autowired
    public CartService(CartRepository cartRepository, ProductRepository productRepository,
                       UserRepository userRepository, ShippingService shippingService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.shippingService = shippingService;
    }

    public CartDto getCart(HttpSession session) {
        Cart actualCart = getActualCart(session);
        CartDto cartDto = new CartDto();
        cartDto.setId(actualCart.getId());
        cartDto.setCartItems(actualCart.getItems().stream()
                .map(CartItemDto::new)
                .collect(java.util.stream.Collectors.toSet()));
        cartDto.setShippingMethod(new ShippingMethodItem(actualCart.getShippingMethod()));
        cartDto.setItemsTotalPrice(actualCart.getItemsTotalPrice());
        cartDto.setTotalPrice(actualCart.getTotalPrice());
        cartDto.setExpectedDeliveryDate(actualCart.getExpectedDeliveryDate());
        return cartDto;
    }

    public boolean addProduct(Long id, int count, HttpSession session) {
        Cart actualCart = getActualCart(session);
        Optional<Product> productById = productRepository.findProductById(id);
        if (productById.isPresent()) {
            Set<CartItem> items = actualCart.getItems();
            Product actualProduct = productById.get();
            for (CartItem item : items) {
                if (item.getProduct().equals(actualProduct) &&
                        actualProduct.getInventory().getQuantityAvailable() >= count) {
                    item.setCount(item.getCount() + count);
                    setItemsTotalPrice(actualCart);
                    setCartsTotalPrice(actualCart);
                    LOGGER.debug("Product count incremented!");
                    return true;
                }
            }
            if (actualProduct.getInventory().getQuantityAvailable() >= count) {
                items.add(createNewCartItem(count, actualProduct));
                setInitialShippingMethod(actualCart);
                setItemsTotalPrice(actualCart);
                setCartsTotalPrice(actualCart);
                cartRepository.save(actualCart);
                LOGGER.debug("Product added to cart!");
                return true;
            }
        }
        LOGGER.info("Product not found! id: {}", id);
        return false;
    }

    private CartItem createNewCartItem(int count, Product actualProduct) {
        CartItem cartItem = new CartItem();
        cartItem.setProduct(actualProduct);
        cartItem.setCount(count);
        return cartItem;
    }

    private void setItemsTotalPrice(Cart cart) {
        cart.setItemsTotalPrice(cart.getItems().stream().mapToDouble(item -> (
                (item.getProduct().getPrice() / 100) * (100 - item.getProduct().getDiscount()))
                * item.getCount())
                .sum());
    }

    private void setCartsTotalPrice(Cart cart) {
        cart.setTotalPrice(cart.getItemsTotalPrice() + cart.getShippingMethod().getCost());
    }

    private Cart getActualCart(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Optional<User> userByUsername = userRepository.findUserByUsername(authentication.getName());
            if (userByUsername.isPresent()) {
                User user = userByUsername.get();
                Optional<Cart> cartByUser = cartRepository.findByUser(user);
                if (cartByUser.isPresent()) {
                    Long cartId = (Long) session.getAttribute("cart");
                    if (cartId != null) {
                        mergeCarts(cartId, cartByUser.get());
                        session.removeAttribute("cart");
                    }
                    Cart actualCart = cartByUser.get();
                    setItemsTotalPrice(actualCart);
                    setCartsTotalPrice(actualCart);
                    LOGGER.debug("Cart found, session id: {} cart id: {}", session.getId(), actualCart.getId());
                    return actualCart;
                }
                return createCart(user);
            }
        }
        if (session.getAttribute("cart") != null) {
            long cartId = (Long) session.getAttribute("cart");
            Optional<Cart> cartById = cartRepository.findById(cartId);
            if (cartById.isPresent()) {
                Cart cart = cartById.get();
                setItemsTotalPrice(cart);
                setCartsTotalPrice(cart);
                return cart;
            }
        }
        Cart actualCart = createCart(null);
        session.setAttribute("cart", actualCart.getId());
        return actualCart;
    }

    private void mergeCarts(Long cartIdFromSession, Cart usersCart) {
        Optional<Cart> cartById = cartRepository.findById(cartIdFromSession);
        if (cartById.isPresent()) {
            Set<CartItem> itemsFromTempCart = cartById.get().getItems();
            Set<CartItem> itemsToMerge = new HashSet<>();
            Set<CartItem> items = usersCart.getItems();
            itemsFromTempCart.forEach(cartItem -> {
                Product product = cartItem.getProduct();
                items.forEach(item -> {
                    if (item.getProduct().equals(product)) {
                        item.setCount(item.getCount() + cartItem.getCount());
                    } else {
                        itemsToMerge.add(cartItem);
                    }
                });
            });
            items.addAll(itemsToMerge);
            cartRepository.deleteById(cartIdFromSession);
            LOGGER.debug("Carts merged! cart id: {}", usersCart.getId());
        }
    }

    private Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setItems(new HashSet<>());
        cart.setUser(user);
        setInitialShippingMethod(cart);
        cart.setItemsTotalPrice(0.0);
        cart.setTotalPrice(0.0);
        cart = cartRepository.save(cart);
        LOGGER.debug("Cart created!");
        return cart;
    }

    private void setInitialShippingMethod(Cart actualCart) {
        ShippingMethod shippingMethod = shippingService.getInitialShippingMethod();
        actualCart.setShippingMethod(shippingMethod);
    }

    public Cart updateProductCount(Long id, int count, HttpSession session) {
        Cart actualCart = getActualCart(session);
        Set<CartItem> items = actualCart.getItems();
        if (count == 0) {
            actualCart.getItems().removeIf((cartItem -> cartItem.getId().equals(id)));
            return actualCart;
        }
        items.stream().filter(cartItem -> cartItem.getProduct().getId().equals(id) &&
                cartItem.getProduct().getInventory().getQuantityAvailable() >= count)
                .forEach(cartItem -> cartItem.setCount(count));

        setItemsTotalPrice(actualCart);
        setCartsTotalPrice(actualCart);
        cartRepository.save(actualCart);
        LOGGER.debug("Product count updated in cart, id: {}", actualCart.getId());
        return actualCart;
    }

    public void removeCartItem(Long id, HttpSession session) {
        Cart actualCart = getActualCart(session);
        actualCart.getItems().removeIf((cartItem -> cartItem.getId().equals(id)));
        setItemsTotalPrice(actualCart);
        setCartsTotalPrice(actualCart);
        LOGGER.debug("Cart item removed from cart, id: {}", actualCart.getId());
    }

    public void deleteCart(Long id) {
        cartRepository.findById(id).ifPresent(cartRepository::delete);
    }

    public void updateShippingMethod(String method, HttpSession session) {
        Cart actualCart = getActualCart(session);
        ShippingMethod shippingMethod = shippingService.fetchShippingMethod(method);
        if (shippingMethod != null) {
            actualCart.setShippingMethod(shippingMethod);
            setCartsTotalPrice(actualCart);
            actualCart.setExpectedDeliveryDate(shippingService.calculateExpectedShippingDate(shippingMethod));
        }
    }

    public int getNumberOfItemsInCart(HttpSession session) {
        Cart actualCart = getActualCart(session);
        return actualCart.getItems().size();
    }

    public boolean canCheckout(HttpSession session) {
        Cart actualCart = getActualCart(session);
        Set<CartItem> cartItems = actualCart.getItems();
        if (cartItems.isEmpty()) {
            return false;
        }
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getInventory().getQuantityAvailable() < cartItem.getCount()) {
                return false;
            }
        }
        return true;
    }
}
