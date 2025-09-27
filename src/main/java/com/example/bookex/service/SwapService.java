package com.example.bookex.service;

import com.example.bookex.dto.swap.SwapViewDto;
import com.example.bookex.entity.BookListing;
import com.example.bookex.entity.SwapRequest;
import com.example.bookex.entity.User;
import com.example.bookex.entity.enums.SwapStatus;
import com.example.bookex.exceptions.NotFoundException;
import com.example.bookex.repository.BookListingRepository;
import com.example.bookex.repository.SwapRequestRepository;
import com.example.bookex.util.DtoMapper;
import com.example.bookex.util.ServiceGuards;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SwapService {

    private final SwapRequestRepository swapRequestRepository;
    private final BookListingRepository bookListingRepository;

    @Transactional
    public SwapViewDto createRequest(User requester, Long listingId, String message) {
        BookListing listing = bookListingRepository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        if (!listing.isAvailable()) throw new IllegalStateException("Listing not available");
        if (listing.getUser().getId().equals(requester.getId()))
            throw new IllegalArgumentException("Cannot request your own listing");

        SwapRequest swapRequest = SwapRequest.builder()
                .listing(listing)
                .user(requester)
                .message(ServiceGuards.trimOrNull(message))
                .status(SwapStatus.PENDING)
                .build();
        swapRequest = swapRequestRepository.save(swapRequest);
        return DtoMapper.toSwapDto(swapRequest);
    }

    @Transactional
    public SwapViewDto ownerRespond(User owner, Long swapId, boolean accept) {
        SwapRequest swapRequest = swapRequestRepository.findById(swapId)
                .orElseThrow(() -> new NotFoundException("Swap not found"));

        ServiceGuards.requireOwner(owner, swapRequest);
        ServiceGuards.requireStatus(swapRequest, SwapStatus.PENDING);

        swapRequest.setStatus(accept ? SwapStatus.ACCEPTED : SwapStatus.DECLINED);
        swapRequestRepository.save(swapRequest);
        return DtoMapper.toSwapDto(swapRequest);
    }

    @Transactional
    public SwapViewDto markCompleted(User actor, Long swapId) {
        SwapRequest swapRequest = swapRequestRepository.findById(swapId)
                .orElseThrow(() -> new NotFoundException("Swap not found"));
        ServiceGuards.requireOwner(actor, swapRequest);
        ServiceGuards.requireStatus(swapRequest, SwapStatus.ACCEPTED);

        swapRequest.setStatus(SwapStatus.COMPLETED);
        swapRequestRepository.save(swapRequest);
        return DtoMapper.toSwapDto(swapRequest);
    }

    @Transactional
    public SwapViewDto markCancelled(User actor, Long swapId) {
        SwapRequest swapRequest = swapRequestRepository.findById(swapId)
                .orElseThrow(() -> new NotFoundException("Swap not found"));
        ServiceGuards.requireOwner(actor, swapRequest);
        ServiceGuards.requireStatus(swapRequest, SwapStatus.ACCEPTED);

        swapRequest.setStatus(SwapStatus.CANCELLED);
        swapRequestRepository.save(swapRequest);
        return DtoMapper.toSwapDto(swapRequest);
    }

    public List<SwapViewDto> inbox(User owner) {
        List<BookListing> ownerBookListing = bookListingRepository.findByUser(owner);
        return swapRequestRepository.findByListingInOrderByCreatedAtDesc(ownerBookListing)
                .stream().map(DtoMapper::toSwapDto).toList();
    }

    public List<SwapViewDto> outbox(User requester) {
        return swapRequestRepository.findByUserOrderByCreatedAtDesc(requester)
                .stream().map(DtoMapper::toSwapDto).toList();
    }
}

