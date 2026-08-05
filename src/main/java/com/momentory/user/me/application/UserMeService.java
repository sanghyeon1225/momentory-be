package com.momentory.user.me.application;

import com.momentory.user.domain.User;
import com.momentory.user.application.AuthenticatedUserNotFoundException;
import com.momentory.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserMeService {

    private final UserRepository userRepository;

    public UserMeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserMeResult getUserMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(AuthenticatedUserNotFoundException::new);
        return new UserMeResult(user.getId(), user.getRole(), user.requiresOnboarding());
    }
}
