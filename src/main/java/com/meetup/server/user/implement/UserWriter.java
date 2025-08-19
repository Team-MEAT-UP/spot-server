package com.meetup.server.user.implement;

import com.meetup.server.startpoint.implement.StartPointProcessor;
import com.meetup.server.user.domain.User;
import com.meetup.server.user.infrastructure.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserWriter {

    private final UserRepository userRepository;
    private final StartPointProcessor startPointProcessor;

    public User save(User user) {
        return userRepository.save(user);
    }

    public void withdraw(User user) {
        startPointProcessor.deleteAllByUser(user);
        user.withdraw();
        save(user);
    }
}
