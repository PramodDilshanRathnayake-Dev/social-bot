package com.socialbot.social_bot.service.order;

import com.socialbot.social_bot.config.WhatzziProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class PackageCatalog {

    private final WhatzziProperties props;

    public PackageCatalog(WhatzziProperties props) {
        this.props = props;
    }

    public List<WhatzziProperties.PackageTier> list() {
        return (props.getPackages() == null) ? List.of() : List.copyOf(props.getPackages());
    }

    public Optional<WhatzziProperties.PackageTier> findByCode(String code) {
        if (code == null) return Optional.empty();
        String normalized = code.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return Optional.empty();
        return list().stream()
                .filter(p -> p.getCode() != null && p.getCode().trim().toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst();
    }
}

