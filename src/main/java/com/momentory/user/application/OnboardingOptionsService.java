package com.momentory.user.application;

import com.momentory.user.domain.Gender;
import com.momentory.user.domain.InterestArea;
import com.momentory.user.domain.UserProfile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class OnboardingOptionsService {

    public OnboardingOptionsResult getOptions() {
        return new OnboardingOptionsResult(
                new NicknamePolicyResult(UserProfile.NICKNAME_MAX_LENGTH, true),
                toOptions(Gender.values()),
                toOptions(InterestArea.values()),
                "HH:mm",
                UserProfile.DEFAULT_TIME_ZONE
        );
    }

    private List<OnboardingOptionResult> toOptions(Gender[] genders) {
        return Arrays.stream(genders)
                .map(gender -> new OnboardingOptionResult(gender.name(), gender.getLabel()))
                .toList();
    }

    private List<OnboardingOptionResult> toOptions(InterestArea[] interestAreas) {
        return Arrays.stream(interestAreas)
                .map(interestArea -> new OnboardingOptionResult(interestArea.name(), interestArea.getLabel()))
                .toList();
    }
}
