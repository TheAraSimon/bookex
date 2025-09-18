package com.example.bookex.service;

import com.example.bookex.dto.listing.ListingCardDto;
import com.example.bookex.dto.listing.ListingDetailDto;
import com.example.bookex.dto.listing.ListingFormDto;
import com.example.bookex.entity.Book;
import com.example.bookex.entity.BookImage;
import com.example.bookex.entity.BookListing;
import com.example.bookex.entity.User;
import com.example.bookex.repository.BookImageRepository;
import com.example.bookex.repository.BookListingRepository;
import com.example.bookex.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListingService {

    private final BookService bookService;
    private final BookListingRepository listings;
    private final BookImageRepository images;

    @Transactional
    public ListingDetailDto createListing(User owner, ListingFormDto form) {
        Book book = bookService.findOrCreate(form.getTitle(), form.getAuthor(), form.getIsbn());
        BookListing l = BookListing.builder()
                .user(owner)
                .book(book)
                .condition(form.getCondition())
                .available(form.isAvailable())
                .notes(form.getNotes())
                .build();
        l = listings.save(l);
        return toDetail(l);
    }

    @Transactional
    public ListingDetailDto updateListing(User owner, Long listingId, ListingFormDto form) {
        BookListing l = listings.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        ensureOwner(owner, l);
        // Update (book can be changed if title/author changed)
        Book book = bookService.findOrCreate(form.getTitle(), form.getAuthor(), form.getIsbn());
        l.setBook(book);
        l.setCondition(form.getCondition());
        l.setAvailable(form.isAvailable());
        l.setNotes(form.getNotes());
        listings.save(l);
        return toDetail(l);
    }

    @Transactional
    public void deleteListing(User owner, Long listingId) {
        BookListing l = listings.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        ensureOwner(owner, l);
        listings.delete(l);
    }

    public ListingDetailDto getListingDetail(Long listingId) {
        BookListing l = listings.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        return toDetail(l);
    }

    public List<ListingCardDto> browsePublic() {
        return DtoMapper.toCardList(listings.findByAvailableTrue());
    }

    public List<ListingCardDto> myLibrary(User owner) {
        return DtoMapper.toCardList(listings.findByUser(owner));
    }

    private ListingDetailDto toDetail(BookListing l) {
        List<BookImage> imgs = images.findByListingOrderByIdImageNoAsc(l);
        return DtoMapper.toDetailDto(l, imgs);
    }

    private static void ensureOwner(User owner, BookListing l) {
        if (!l.getUser().getId().equals(owner.getId())) {
            throw new SecurityException("You are not the owner of this listing");
        }
    }
}

