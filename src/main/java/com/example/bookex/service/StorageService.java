package com.example.bookex.service;

import com.example.bookex.dto.listing.BookImageDto;
import com.example.bookex.entity.BookImage;
import com.example.bookex.entity.BookImageId;
import com.example.bookex.entity.BookListing;
import com.example.bookex.entity.User;
import com.example.bookex.exceptions.NotFoundException;
import com.example.bookex.repository.BookImageRepository;
import com.example.bookex.repository.BookListingRepository;
import com.example.bookex.util.DtoMapper;
import com.example.bookex.util.ServiceGuards;
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

    private final BookImageRepository bookImageRepository;
    private final BookListingRepository bookListingRepository;

    @Value("${app.max-images-per-listing:5}")
    private int maxImages;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public BookImageDto addImage(User owner, Long listingId, MultipartFile file) throws IOException {
        BookListing bookListing = bookListingRepository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        ServiceGuards.requireOwner(owner, bookListing);

        long currentCount = bookImageRepository.countByListing(bookListing);
        if (currentCount >= maxImages) {
            throw new IllegalStateException("Image limit reached (" + maxImages + ")");
        }

        validateImageFile(file);

        short nextNo = nextAvailableImageNo(bookListing);


        String ext = safeExt(file.getOriginalFilename());
        String filename = nextNo + "-" + UUID.randomUUID() + "." + ext;
        Path dir = Paths.get(uploadDir, "listings", bookListing.getId().toString());
        Files.createDirectories(dir);
        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(dir)) throw new SecurityException("Invalid path");

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        BookImageId id = new BookImageId(bookListing.getId(), nextNo);
        String publicPath = "/uploads/listings/" + bookListing.getId() + "/" + filename;
        BookImage img = BookImage.builder()
                .id(id)
                .listing(bookListing)
                .path(publicPath)
                .build();
        bookImageRepository.save(img);
        return DtoMapper.toImageDto(img);
    }

    @Transactional
    public void deleteImage(User owner, Long listingId, short imageNo) throws IOException {
        BookListing bookListing = bookListingRepository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        ServiceGuards.requireOwner(owner, bookListing);

        BookImageId id = new BookImageId(listingId, imageNo);
        BookImage img = bookImageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        removeFileIfExists(img.getPath());

        bookImageRepository.delete(img);
    }

    public List<BookImageDto> listImages(Long listingId) {
        BookListing bookListing = bookListingRepository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        return DtoMapper.toImageDtoList(bookImageRepository.findByListingOrderByIdImageNoAsc(bookListing));
    }

    // --- helpers

    private static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File is empty");
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException("File too large (max 5MB)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/png"))) {
            throw new IllegalArgumentException("Only JPEG/PNG allowed");
        }
        String name = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(name);
        if (extension == null) throw new IllegalArgumentException("Invalid file extension");
        String lower = extension.toLowerCase();
        if (!(lower.equals("jpg") || lower.equals("jpeg") || lower.equals("png"))) {
            throw new IllegalArgumentException("Only JPG/PNG extensions allowed");
        }
    }

    private short nextAvailableImageNo(BookListing listing) {
        List<Short> existing = bookImageRepository.findByListingOrderByIdImageNoAsc(listing)
                .stream().map(i -> i.getId().getImageNo()).sorted().toList();
        for (short i = 1; i <= (short) maxImages; i++) {
            if (!existing.contains(i)) return i;
        }
        throw new IllegalStateException("No available image slot");
    }

    private String safeExt(String filename) {
        String extension = FilenameUtils.getExtension(filename);
        if (!StringUtils.hasText(extension)) throw new IllegalArgumentException("File must have extension");
        return extension.toLowerCase();
    }

    private void removeFileIfExists(String publicPath) throws IOException {
        // publicPath like /uploads/...
        String relative = publicPath.startsWith("/") ? publicPath.substring(1) : publicPath;
        Path target = Paths.get(relative).normalize();
        if (Files.exists(target)) Files.delete(target);
    }
}
