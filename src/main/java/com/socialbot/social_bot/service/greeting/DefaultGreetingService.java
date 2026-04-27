package com.socialbot.social_bot.service.greeting;

import com.socialbot.social_bot.config.WhatzziProperties;
import org.springframework.stereotype.Service;

@Service
public class DefaultGreetingService implements GreetingService {

    private final WhatzziProperties whatzziProperties;

    public DefaultGreetingService(WhatzziProperties whatzziProperties) {
        this.whatzziProperties = whatzziProperties;
    }

    @Override
    public boolean isHi(String normalizedLowercaseMessage) {
        return "hi".equals(normalizedLowercaseMessage);
    }

    @Override
    public String reply() {
        return whatzziProperties.getHiReply();
    }
}

