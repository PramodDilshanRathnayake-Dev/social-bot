package com.socialbot.social_bot.service.greeting;

public interface GreetingService {
    boolean isHi(String normalizedLowercaseMessage);
    String reply();
}

