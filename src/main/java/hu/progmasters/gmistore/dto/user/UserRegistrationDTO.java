package hu.progmasters.gmistore.dto.user;

import java.time.LocalDateTime;

public class UserRegistrationDTO {

    private long id;
    private LocalDateTime registered;

    public UserRegistrationDTO(long id, LocalDateTime registered) {
        this.id = id;
        this.registered = registered;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getRegistered() {
        return registered;
    }

    public void setRegistered(LocalDateTime registered) {
        this.registered = registered;
    }
}
