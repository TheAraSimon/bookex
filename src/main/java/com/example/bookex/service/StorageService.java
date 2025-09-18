package com.example.bookex.service;

import com.example.bookex.dto.listing.BookImageDto;
import com.example.bookex.entity.BookImage;
import com.example.bookex.entity.BookImageId;
import com.example.bookex.entity.BookListing;
import com.example.bookex.entity.User;
import com.example.bookex.repository.BookImageRepository;
import com.example.bookex.repository.BookListingRepository;
import com.example.bookex.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageService {

    private final BookImageRepository images;
    private final BookListingRepository listings;

    @Value("${app.max-images-per-listing:5}")
    private int maxImages;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public BookImageDto addImage(User owner, Long listingId, MultipartFile file) throws IOException {
        BookListing listing = listings.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        ensureOwner(owner, listing);

        long currentCount = images.countByListing(listing);
        if (currentCount >= maxImages) {
            throw new IllegalStateException("Image limit reached (" + maxImages + ")");
        }

        validateImageFile(file);

        short nextNo = nextAvailableImageNo(listing);


        String ext = safeExt(file.getOriginalFilename());
        String filename = nextNo + "-" + UUID.randomUUID() + "." + ext;
        Path dir = Paths.get(uploadDir, "listings", listing.getId().toString());
        Files.createDirectories(dir);
        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(dir)) throw new SecurityException("Invalid path");

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        BookImageId id = new BookImageId(listing.getId(), nextNo);
        String publicPath = "/uploads/listings/" + listing.getId() + "/" + filename;
        BookImage img = BookImage.builder()
                .id(id)
                .listing(listing)
                .path(publicPath)
                .build();
        images.save(img);
        return DtoMapper.toImageDto(img);
    }

    @Transactional
    public void deleteImage(User owner, Long listingId, short imageNo) throws IOException {
        BookListing listing = listings.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        ensureOwner(owner, listing);

        BookImageId id = new BookImageId(listingId, imageNo);
        BookImage img = images.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        removeFileIfExists(img.getPath());

        images.delete(img);
    }

    public List<BookImageDto> listImages(Long listingId) {
        BookListing listing = listings.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        return DtoMapper.toImageDtoList(images.findByListingOrderByIdImageNoAsc(listing));
    }

    // --- helpers
    private static void ensureOwner(User owner, BookListing l) {
        if (!l.getUser().getId().equals(owner.getId())) {
            throw new SecurityException("You are not the owner of this listing");
        }
    }

    private static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File is empty");
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException("File too large (max 5MB)");
        }
        String ct = file.getContentType();
        if (ct == null || !(ct.equalsIgnoreCase("image/jpeg") || ct.equalsIgnoreCase("image/png"))) {
            throw new IllegalArgumentException("Only JPEG/PNG allowed");
        }
        String name = file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(name);
        if (ext == null) throw new IllegalArgumentException("Invalid file extension");
        String lower = ext.toLowerCase();
        if (!(lower.equals("jpg") || lower.equals("jpeg") || lower.equals("png"))) {
            throw new IllegalArgumentException("Only JPG/PNG extensions allowed");
        }
    }

    private short nextAvailableImageNo(BookListing listing) {
        var existing = images.findByListingOrderByIdImageNoAsc(listing)
                .stream().map(i -> i.getId().getImageNo()).sorted().toList();
        for (short i = 1; i <= (short) maxImages; i++) {
            if (!existing.contains(i)) return i;
        }
        throw new IllegalStateException("No available image slot");
    }

    private String safeExt(String filename) {
        String ext = FilenameUtils.getExtension(filename);
        if (!StringUtils.hasText(ext)) throw new IllegalArgumentException("File must have extension");
        return ext.toLowerCase();
    }

    private void removeFileIfExists(String publicPath) throws IOException {
        // publicPath like /uploads/...
        String relative = publicPath.startsWith("/") ? publicPath.substring(1) : publicPath;
        Path target = Paths.get(relative).normalize();
        if (Files.exists(target)) Files.delete(target);
    }
}
