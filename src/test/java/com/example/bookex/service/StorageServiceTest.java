package com.example.bookex.service;

import com.example.bookex.dto.listing.BookImageDto;
import com.example.bookex.entity.BookImage;
import com.example.bookex.entity.BookListing;
import com.example.bookex.entity.User;
import com.example.bookex.repository.BookImageRepository;
import com.example.bookex.repository.BookListingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void addImage_saves_whenValid_andWithinLimit() throws Exception {
        BookImageRepository bookImageRepository = mock(BookImageRepository.class);
        BookListingRepository bookListingRepository = mock(BookListingRepository.class);
        StorageService service = new StorageService(bookImageRepository, bookListingRepository);

        ReflectionTestUtils.setField(service, "maxImages", 5);
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());

        User owner = User.builder().id(1L).build();
        BookListing bookListing = BookListing.builder()
                .id(11L)
                .user(owner)
                .available(true)
                .build();

        when(bookListingRepository.findById(11L)).thenReturn(Optional.of(bookListing));
        when(bookImageRepository.countByListing(bookListing)).thenReturn(0L);
        when(bookImageRepository.findByListingOrderByIdImageNoAsc(bookListing)).thenReturn(List.of());
        when(bookImageRepository.save(any(BookImage.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", new byte[]{1, 2, 3});

        BookImageDto dto = service.addImage(owner, 11L, file);

        assertThat(dto.getImageNo()).isEqualTo((short) 1);
        assertThat(dto.getPath()).startsWith("/uploads/listings/11/");
        verify(bookImageRepository).save(any(BookImage.class));
    }

    @Test
    void addImage_throws_whenLimitReached() {
        BookImageRepository bookImageRepository = mock(BookImageRepository.class);
        BookListingRepository bookListingRepository = mock(BookListingRepository.class);
        StorageService storageService = new StorageService(bookImageRepository, bookListingRepository);

        ReflectionTestUtils.setField(storageService, "maxImages", 1);
        ReflectionTestUtils.setField(storageService, "uploadDir", tempDir.toString());

        User owner = User.builder().id(1L).build();
        BookListing bookListing = BookListing.builder()
                .id(22L)
                .user(owner)
                .available(true)
                .build();

        when(bookListingRepository.findById(22L)).thenReturn(Optional.of(bookListing));
        when(bookImageRepository.countByListing(bookListing)).thenReturn(1L); // уже достигли лимита

        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[]{1});

        assertThatThrownBy(() -> storageService.addImage(owner, 22L, file))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Image limit");
    }

    @Test
    void addImage_throws_whenWrongContentType() {
        BookImageRepository bookImageRepository = mock(BookImageRepository.class);
        BookListingRepository bookListingRepository = mock(BookListingRepository.class);
        StorageService storageService = new StorageService(bookImageRepository, bookListingRepository);

        ReflectionTestUtils.setField(storageService, "maxImages", 5);
        ReflectionTestUtils.setField(storageService, "uploadDir", tempDir.toString());

        User owner = User.builder().id(1L).build();
        BookListing bookListing = BookListing.builder()
                .id(33L)
                .user(owner)
                .available(true)
                .build();

        when(bookListingRepository.findById(33L)).thenReturn(Optional.of(bookListing));
        when(bookImageRepository.countByListing(bookListing)).thenReturn(0L);

        MockMultipartFile file = new MockMultipartFile(
                "file", "x.gif", "image/gif", new byte[]{1, 2}); // не JPEG/PNG

        assertThatThrownBy(() -> storageService.addImage(owner, 33L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only JPEG/PNG");
    }
}
