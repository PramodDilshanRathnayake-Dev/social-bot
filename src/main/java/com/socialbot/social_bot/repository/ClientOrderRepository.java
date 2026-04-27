package com.socialbot.social_bot.repository;

import com.socialbot.social_bot.model.ClientOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ClientOrderRepository extends JpaRepository<ClientOrder, Long> {
    Optional<ClientOrder> findTopByProspectPhoneAndStatusInOrderByUpdatedAtDesc(
            String prospectPhone,
            Collection<ClientOrder.Status> statuses
    );
}

