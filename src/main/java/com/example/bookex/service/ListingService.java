package com.example.bookex.service;

import com.example.bookex.dto.listing.ListingCardDto;
import com.example.bookex.dto.listing.ListingDetailDto;
import com.example.bookex.dto.listing.ListingFormDto;
import com.example.bookex.entity.Book;
import com.example.bookex.entity.BookImage;
import com.example.bookex.entity.BookListing;
import com.example.bookex.entity.User;
import com.example.bookex.exceptions.NotFoundException;
import com.example.bookex.repository.BookImageRepository;
import com.example.bookex.repository.BookListingRepository;
import com.example.bookex.util.DtoMapper;
import com.example.bookex.util.ServiceGuards;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListingService {

    private final BookService bookService;
    private final BookListingRepository bookListingRepository;
    private final BookImageRepository bookImageRepository;

    @Transactional
    public ListingDetailDto createListing(User owner, ListingFormDto form) {
        Book book = bookService.findOrCreate(form.getTitle(), form.getAuthor(), form.getIsbn());
        BookListing bookListing = BookListing.builder()
                .user(owner)
                .book(book)
                .condition(form.getCondition())
                .available(form.isAvailable())
                .notes(form.getNotes())
                .build();
        bookListing = bookListingRepository.save(bookListing);
        return toDetail(bookListing);
    }

    @Transactional
    public ListingDetailDto updateListing(User owner, Long listingId, ListingFormDto form) {
        BookListing bookListing = bookListingRepository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        ServiceGuards.requireOwner(owner, bookListing);
        // Update (book can be changed if title/author changed)
        Book book = bookService.findOrCreate(form.getTitle(), form.getAuthor(), form.getIsbn());
        bookListing.setBook(book);
        bookListing.setCondition(form.getCondition());
        bookListing.setAvailable(form.isAvailable());
        bookListing.setNotes(form.getNotes());
        bookListingRepository.save(bookListing);
        return toDetail(bookListing);
    }

    @Transactional
    public void deleteListing(User owner, Long listingId) {
        BookListing bookListing = bookListingRepository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        ServiceGuards.requireOwner(owner, bookListing);
        bookListingRepository.delete(bookListing);
    }

    public ListingDetailDto getListingDetail(Long listingId) {
        BookListing bookListing = bookListingRepository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        return toDetail(bookListing);
    }

    public List<ListingCardDto> browsePublic() {
        return DtoMapper.toCardList(bookListingRepository.findByAvailableTrue());
    }

    public List<ListingCardDto> myLibrary(User owner) {
        return DtoMapper.toCardList(bookListingRepository.findByUser(owner));
    }

    private ListingDetailDto toDetail(BookListing bookListing) {
        List<BookImage> images = bookImageRepository.findByListingOrderByIdImageNoAsc(bookListing);
        return DtoMapper.toDetailDto(bookListing, images);
    }
}

