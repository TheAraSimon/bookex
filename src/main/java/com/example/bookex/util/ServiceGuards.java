package com.example.bookex.util;

import com.example.bookex.entity.BookListing;
import com.example.bookex.entity.SwapRequest;
import com.example.bookex.entity.User;
import com.example.bookex.entity.enums.SwapStatus;
import org.springframework.security.access.AccessDeniedException;

public final class ServiceGuards {

    private ServiceGuards() {}

    /** Require that actor is the owner of the listing. */
    public static void requireOwner(User actor, BookListing listing) {
        if (actor == null || listing == null || listing.getUser() == null) {
            throw new AccessDeniedException("Unauthorized");
        }
        Long ownerId = listing.getUser().getId();
        if (!ownerId.equals(actor.getId())) {
            throw new AccessDeniedException("You are not the owner of this listing");
        }
    }

    /** Require that actor is the owner of the listing related to the swap. */
    public static void requireOwner(User actor, SwapRequest swap) {
        if (actor == null || swap == null || swap.getListing() == null || swap.getListing().getUser() == null) {
            throw new AccessDeniedException("Unauthorized");
        }
        Long ownerId = swap.getListing().getUser().getId();
        if (!ownerId.equals(actor.getId())) {
            throw new AccessDeniedException("Only the listing owner can respond");
        }
    }

    /** Require that actor participates in the swap (owner or requester). */
    public static void requireParticipant(User actor, SwapRequest swap) {
        if (actor == null || swap == null || swap.getListing() == null || swap.getUser() == null) {
            throw new AccessDeniedException("Unauthorized");
        }
        Long ownerId = swap.getListing().getUser().getId();
        Long requesterId = swap.getUser().getId();
        Long actorId = actor.getId();
        if (!ownerId.equals(actorId) && !requesterId.equals(actorId)) {
            throw new AccessDeniedException("Only participants can modify this swap");
        }
    }

    /** Enforce expected swap status. */
    public static void requireStatus(SwapRequest swapRequest, SwapStatus expected) {
        if (swapRequest.getStatus() != expected) {
            throw new IllegalStateException("Invalid state: must be " + expected);
        }
    }

    /** Common string helper. */
    public static String trimOrNull(String string) {
        return (string == null || string.isBlank()) ? null : string.trim();
    }
}

