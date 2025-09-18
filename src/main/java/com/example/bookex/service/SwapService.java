package com.example.bookex.service;

import com.example.bookex.dto.swap.SwapViewDto;
import com.example.bookex.entity.BookListing;
import com.example.bookex.entity.SwapRequest;
import com.example.bookex.entity.User;
import com.example.bookex.entity.enums.SwapStatus;
import com.example.bookex.repository.BookListingRepository;
import com.example.bookex.repository.SwapRequestRepository;
import com.example.bookex.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SwapService {

    private final SwapRequestRepository swaps;
    private final BookListingRepository listings;

    @Transactional
    public SwapViewDto createRequest(User requester, Long listingId, String message) {
        BookListing listing = listings.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        if (!listing.isAvailable()) throw new IllegalStateException("Listing not available");
        if (listing.getUser().getId().equals(requester.getId()))
            throw new IllegalArgumentException("Cannot request your own listing");

        SwapRequest s = SwapRequest.builder()
                .listing(listing)
                .user(requester)
                .message(trimOrNull(message))
                .status(SwapStatus.PENDING)
                .build();
        s = swaps.save(s);
        return DtoMapper.toSwapDto(s);
    }

    // FR-5.2 owner accept/decline
    @Transactional
    public SwapViewDto ownerRespond(User owner, Long swapId, boolean accept) {
        SwapRequest s = swaps.findById(swapId)
                .orElseThrow(() -> new IllegalArgumentException("Swap not found"));

        ensureOwner(owner, s);
        ensureStatus(s, SwapStatus.PENDING);

        s.setStatus(accept ? SwapStatus.ACCEPTED : SwapStatus.DECLINED);
        swaps.save(s);
        return DtoMapper.toSwapDto(s); // contact auto-revealed when ACCEPTED via mapper
    }

    @Transactional
    public SwapViewDto markCompleted(User actor, Long swapId) {
        SwapRequest s = swaps.findById(swapId)
                .orElseThrow(() -> new IllegalArgumentException("Swap not found"));
        ensureParticipant(actor, s);
        ensureStatus(s, SwapStatus.ACCEPTED);

        s.setStatus(SwapStatus.COMPLETED);
        swaps.save(s);
        return DtoMapper.toSwapDto(s);
    }

    @Transactional
    public SwapViewDto markCancelled(User actor, Long swapId) {
        SwapRequest s = swaps.findById(swapId)
                .orElseThrow(() -> new IllegalArgumentException("Swap not found"));
        ensureParticipant(actor, s);
        ensureStatus(s, SwapStatus.ACCEPTED);

        s.setStatus(SwapStatus.CANCELLED);
        swaps.save(s);
        return DtoMapper.toSwapDto(s);
    }

    // FR-5.4 inbox/outbox
    public List<SwapViewDto> inbox(User owner) {
        List<BookListing> owned = listings.findByUser(owner);
        return swaps.findByListingInOrderByCreatedAtDesc(owned)
                .stream().map(DtoMapper::toSwapDto).toList();
    }

    public List<SwapViewDto> outbox(User requester) {
        return swaps.findByUserOrderByCreatedAtDesc(requester)
                .stream().map(DtoMapper::toSwapDto).toList();
    }

    // --- helpers
    private static void ensureOwner(User owner, SwapRequest s) {
        Long realOwnerId = s.getListing().getUser().getId();
        if (!realOwnerId.equals(owner.getId())) {
            throw new SecurityException("Only the listing owner can respond");
        }
    }

    private static void ensureParticipant(User actor, SwapRequest s) {
        Long ownerId = s.getListing().getUser().getId();
        Long requesterId = s.getUser().getId();
        if (!ownerId.equals(actor.getId()) && !requesterId.equals(actor.getId())) {
            throw new SecurityException("Only participants can modify this swap");
        }
    }

    private static void ensureStatus(SwapRequest s, SwapStatus expected) {
        if (s.getStatus() != expected) {
            throw new IllegalStateException("Invalid state: must be " + expected);
        }
    }

    private static String trimOrNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}

