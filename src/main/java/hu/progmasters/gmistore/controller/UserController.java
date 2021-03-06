package hu.progmasters.gmistore.controller;

import hu.progmasters.gmistore.dto.product.ProductDto;
import hu.progmasters.gmistore.dto.user.UserDto;
import hu.progmasters.gmistore.dto.user.UserEditableDetailsDto;
import hu.progmasters.gmistore.service.UserService;
import hu.progmasters.gmistore.validator.UserEditValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserEditValidator userEditValidator;

    @Autowired
    public UserController(UserEditValidator userEditValidator, UserService userService) {
        this.userService = userService;
        this.userEditValidator = userEditValidator;
    }

    @InitBinder("userEditableDetailsDto")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(userEditValidator);
    }

    @GetMapping("/my-account")
    public ResponseEntity<UserDto> getUserData() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDto userDto = userService.getUserData(username);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PutMapping("/edit")
    public ResponseEntity<Void> updateUserDetails(@RequestBody @Valid UserEditableDetailsDto user) {
        userService.updateUserByName(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/favorite-products")
    public ResponseEntity<Set<ProductDto>> getFavoriteProducts(Principal principal) {
        Set<ProductDto> favoriteProducts = userService.getFavoriteProducts(principal);
        return new ResponseEntity<>(favoriteProducts, HttpStatus.OK);
    }

    @GetMapping("/count-of-favorite-products")
    public ResponseEntity<Integer> getCountOfFavoriteProducts(Principal principal) {
        int result = userService.getCountOfFavoriteProducts(principal);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/favorite-products/{id}")
    public ResponseEntity<Boolean> addProductToFavorites(@PathVariable("id") Long id, Principal principal) {
        boolean result = userService.addProductToFavorites(id, principal);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/favorite-products/{id}")
    public ResponseEntity<Boolean> removeProductFromFavorites(@PathVariable("id") Long id, Principal principal) {
        boolean result = userService.removeProductFromFavorites(id, principal);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
