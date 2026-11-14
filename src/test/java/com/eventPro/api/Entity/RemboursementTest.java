package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.StatutRemboursement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemboursementTest {

    @Test
    void approveChangesPendingRefundToApproved() {
        Remboursement refund = Remboursement.builder().build();

        refund.approuver();

        assertEquals(StatutRemboursement.APPROVED, refund.getStatut());
    }

    @Test
    void rejectChangesPendingRefundToRejected() {
        Remboursement refund = Remboursement.builder().build();

        refund.refuser();

        assertEquals(StatutRemboursement.REJECTED, refund.getStatut());
    }

    @Test
    void approveOnlyWorksForPendingRefunds() {
        Remboursement approved = Remboursement.builder().statut(StatutRemboursement.APPROVED).build();
        Remboursement rejected = Remboursement.builder().statut(StatutRemboursement.REJECTED).build();

        assertThrows(IllegalStateException.class, approved::approuver);
        assertThrows(IllegalStateException.class, rejected::approuver);
    }

    @Test
    void rejectOnlyWorksForPendingRefunds() {
        Remboursement approved = Remboursement.builder().statut(StatutRemboursement.APPROVED).build();
        Remboursement rejected = Remboursement.builder().statut(StatutRemboursement.REJECTED).build();

        assertThrows(IllegalStateException.class, approved::refuser);
        assertThrows(IllegalStateException.class, rejected::refuser);
    }
}
