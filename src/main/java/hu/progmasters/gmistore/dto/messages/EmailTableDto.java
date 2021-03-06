package hu.progmasters.gmistore.dto.messages;

import hu.progmasters.gmistore.model.EmailFromUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EmailTableDto {
    private Long id;
    private String email;
    private String subject;
    private String message;
    private LocalDate messageCreateTime;
    private boolean active;

    public EmailTableDto(EmailFromUser emailFromUser) {
        this.id = emailFromUser.getId();
        this.email = emailFromUser.getEmail();
        this.subject = emailFromUser.getSubject();
        this.message = emailFromUser.getMessage();
        this.messageCreateTime=emailFromUser.getMessageCreateTime();
        this.active=emailFromUser.isActive();
    }
}
