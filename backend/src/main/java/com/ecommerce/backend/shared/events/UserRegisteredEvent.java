package com.ecommerce.backend.shared.events;

import com.ecommerce.backend.modules.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRegisteredEvent extends BaseEvent {

    private Long userId;
    private String userEmail;
    private String firstName;

    public UserRegisteredEvent(User user) {
        super(user.getId().toString());
        this.userId = user.getId();
        this.userEmail = user.getEmail();
        this.firstName = user.getFirstName();
    }
}
