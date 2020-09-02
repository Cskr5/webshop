package hu.progmasters.gmistore.service;

import hu.progmasters.gmistore.dto.UserDto;
import hu.progmasters.gmistore.model.User;
import hu.progmasters.gmistore.repository.UserRepository;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final SessionRegistry sessionRegistry;

    public UserService(UserRepository userRepository,SessionRegistry sessionRegistry) {
        this.userRepository = userRepository;
        this.sessionRegistry = sessionRegistry;
    }

    public UserDto getUserData(String username) {
        System.out.println(sessionRegistry.getAllPrincipals());
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        UserDto userDto = new UserDto(
                user.getId(), user.getUsername(), user.getLastName(), user.getFirstName(), user.getShippingAddress(),
                user.getBillingAddress(),user.getEmail(), user.getPhoneNumber(), user.getRoles(), user.getRegistered(),
                user.isActive(), user.getOrderList());
        return userDto;
    }

    public User getUserByUsername(String username) {
        Optional<User> userByUsername = userRepository.findUserByUsername(username);
        return userByUsername.orElse(null);

    public List<UserDto> getUserList(){
        return userRepository.findAll().stream().map(user -> new UserDto(user.getId(), user.getUsername(), user.getLastName(), user.getFirstName(), user.getShippingAddress(),
                user.getBillingAddress(),user.getEmail(), user.getPhoneNumber(), user.getRoles(), user.getRegistered(),
                user.isActive(), user.getOrderList())).collect(Collectors.toList());
    }
}
