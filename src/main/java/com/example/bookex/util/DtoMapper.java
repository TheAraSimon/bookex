package com.example.bookex.util;

import com.example.bookex.dto.book.BookDto;
import com.example.bookex.dto.listing.*;
import com.example.bookex.dto.rating.RatingAverageDto;
import com.example.bookex.dto.swap.SwapViewDto;
import com.example.bookex.dto.user.UserProfileDto;
import com.example.bookex.dto.user.UserPublicDto;
import com.example.bookex.entity.*;
import com.example.bookex.entity.enums.ContactMethod;
import com.example.bookex.entity.enums.SwapStatus;

import java.util.List;
import java.util.stream.Collectors;

public final class DtoMapper {
    private DtoMapper() {
    }

    // --- USER ---
    public static UserProfileDto toProfileDto(User u) {
        UserProfileDto d = new UserProfileDto();
        d.setDisplayName(u.getUsername());
        d.setPublicContact(u.isPublicContact());
        d.setPreferredMethod(u.getPreferredMethod());
        d.setContactEmail(u.getContactEmail());
        d.setContactPhone(u.getContactPhone());
        return d;
    }

    public static void updateUserFromProfile(UserProfileDto d, User u) {
        u.setUsername(nullSafeTrim(d.getDisplayName()));
        u.setPublicContact(d.isPublicContact());
        u.setPreferredMethod(d.getPreferredMethod());
        u.setContactEmail(nullIfBlank(d.getContactEmail()));
        u.setContactPhone(nullIfBlank(d.getContactPhone()));
    }

    // Reveal if public=true or swap status is ACCEPTED
    public static UserPublicDto toUserPublic(User u, boolean revealContact) {
        UserPublicDto d = new UserPublicDto();
        d.setId(u.getId());
        d.setDisplayName(u.getUsername());
        if (revealContact || u.isPublicContact()) {
            if (u.getPreferredMethod() == ContactMethod.EMAIL) d.setContact(u.getContactEmail());
            else if (u.getPreferredMethod() == ContactMethod.PHONE) d.setContact(u.getContactPhone());
            else d.setContact(null);
        } else {
            d.setContact(null);
        }
        return d;
    }

    // --- BOOK ---
    public static BookDto toBookDto(Book b) {
        BookDto d = new BookDto();
        d.setId(b.getId());
        d.setTitle(b.getTitle());
        d.setAuthor(b.getAuthor());
        d.setIsbn(b.getIsbn());
        return d;
    }

    // --- IMAGES ---
    public static BookImageDto toImageDto(BookImage img) {
        BookImageDto d = new BookImageDto();
        d.setImageNo(img.getId().getImageNo());
        d.setPath(img.getPath());
        return d;
    }

    public static List<BookImageDto> toImageDtoList(List<BookImage> list) {
        return list.stream().map(DtoMapper::toImageDto).collect(Collectors.toList());
    }

    // --- LISTING CARD (for browse/swaps) ---
    public static ListingCardDto toCardDto(BookListing l) {
        ListingCardDto d = new ListingCardDto();
        d.setId(l.getId());
        d.setBook(toBookDto(l.getBook()));
        d.setOwner(toUserPublic(l.getUser(), false)); //
        d.setCondition(l.getCondition().name());
        d.setAvailable(l.isAvailable());
        return d;
    }

    public static List<ListingCardDto> toCardList(List<BookListing> list) {
        return list.stream().map(DtoMapper::toCardDto).collect(Collectors.toList());
    }

    // --- LISTING DETAIL ---
    public static ListingDetailDto toDetailDto(BookListing l, List<BookImage> images) {
        var d = new ListingDetailDto();
        d.setId(l.getId());
        d.setBook(toBookDto(l.getBook()));
        d.setOwner(toUserPublic(l.getUser(), false));
        d.setCondition(l.getCondition().name());
        d.setAvailable(l.isAvailable());
        d.setNotes(l.getNotes());
        d.setImages(toImageDtoList(images));
        return d;
    }

    // --- RATINGS ---
    public static RatingAverageDto toAvgDto(double d, double e, double j, int c) {
        return new RatingAverageDto(d, e, j, c);
    }

    // --- SWAP VIEW DTO ---
    public static SwapViewDto toSwapDto(SwapRequest s) {
        var dto = new SwapViewDto();
        dto.setId(s.getId());
        dto.setStatus(s.getStatus().name());
        dto.setListing(toCardDto(s.getListing()));
        boolean reveal = s.getStatus() == SwapStatus.ACCEPTED;
        dto.setOwner(toUserPublic(s.getListing().getUser(), reveal));
        dto.setRequester(toUserPublic(s.getUser(), reveal));
        return dto;
    }

    // helpers
    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static String nullSafeTrim(String s) {
        return s == null ? null : s.trim();
    }
}

