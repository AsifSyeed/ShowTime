package com.example.showtime.referral.repository;

import com.example.showtime.referral.model.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    // get all referrals by event id
    List<Referral> findByEventId(String eventId);

    Referral findByEventIdAndReferralType(String eventId, int value);
}
