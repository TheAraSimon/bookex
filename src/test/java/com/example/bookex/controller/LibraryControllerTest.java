package com.example.bookex.controller;

import com.example.bookex.dto.listing.ListingDetailDto;
import com.example.bookex.dto.listing.ListingFormDto;
import com.example.bookex.entity.User;
import com.example.bookex.entity.enums.Condition;
import com.example.bookex.service.ListingService;
import com.example.bookex.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LibraryControllerTest {

    private User me() {
        return User.builder().id(100L).email("u@e.com").username("user").password("x").build();
    }

    @Test
    void myLibrary_addsCardsAndOk_returnsLibrary() {
        ListingService listingService = mock(ListingService.class);
        StorageService storageService = mock(StorageService.class);
        LibraryController libraryController = new LibraryController(listingService, storageService);

        when(listingService.myLibrary(me())).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = libraryController.myLibrary(me(), model, "OK");

        assertThat(view).isEqualTo("library");
        assertThat(model.containsAttribute("cards")).isTrue();
        assertThat(model.getAttribute("ok")).isEqualTo("OK");
    }

    @Test
    void createForm_returnsListingForm_withEmptyForm() {
        LibraryController libraryController = new LibraryController(mock(ListingService.class), mock(StorageService.class));
        Model model = new ExtendedModelMap();

        String view = libraryController.createForm(model);

        assertThat(view).isEqualTo("listing-form");
        assertThat(model.containsAttribute("form")).isTrue();
        assertThat(model.getAttribute("form")).isInstanceOf(ListingFormDto.class);
    }

    @Test
    void create_valid_returnsRedirect() {
        ListingService listingService = mock(ListingService.class);
        LibraryController libraryController = new LibraryController(listingService, mock(StorageService.class));

        ListingFormDto form = ListingFormDto.builder()
                .title("Dune").author("Frank").isbn("")
                .condition(Condition.GOOD).available(true).notes("N").build();

        when(listingService.createListing(any(), any()))
                .thenReturn(ListingDetailDto.builder().id(1L).build());

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        String view = libraryController.create(me(), form, bindingResult);

        assertThat(view).isEqualTo("redirect:/library?ok=Listing+created");
        verify(listingService).createListing(any(), any());
    }

    @Test
    void editForm_buildsFormAndImages_returnsListingForm() {
        ListingService listingService = mock(ListingService.class);
        StorageService storageService = mock(StorageService.class);
        LibraryController libraryController = new LibraryController(listingService, storageService);

        ListingDetailDto listingDetailDto = ListingDetailDto.builder()
                .id(5L)
                .book(com.example.bookex.dto.book.BookDto.builder()
                        .title("T").author("A").isbn("I").build())
                .condition(Condition.GOOD.name())
                .available(true)
                .notes("N")
                .images(List.of())
                .build();

        when(listingService.getListingDetail(5L)).thenReturn(listingDetailDto);

        Model model = new ExtendedModelMap();
        String view = libraryController.editForm(5L, model);

        assertThat(view).isEqualTo("listing-form");
        assertThat(model.containsAttribute("form")).isTrue();
        assertThat(model.containsAttribute("images")).isTrue();
    }

    @Test
    void update_valid_returnsRedirect() {
        ListingService listingService = mock(ListingService.class);
        LibraryController libraryController = new LibraryController(listingService, mock(StorageService.class));

        ListingFormDto form = ListingFormDto.builder()
                .title("T").author("A").isbn("I")
                .condition(Condition.USED).available(false).notes("N").build();

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        String view = libraryController.update(me(), 7L, form, bindingResult);

        assertThat(view).isEqualTo("redirect:/library?ok=Listing+updated");
        verify(listingService).updateListing(any(), eq(7L), any());
    }

    @Test
    void delete_returnsRedirect() {
        ListingService listingService = mock(ListingService.class);
        LibraryController libraryController = new LibraryController(listingService, mock(StorageService.class));

        String view = libraryController.delete(me(), 9L);

        assertThat(view).isEqualTo("redirect:/library?ok=Listing+deleted");
        verify(listingService).deleteListing(any(), eq(9L));
    }

    @Test
    void uploadImage_callsStorage_returnsRedirect() throws Exception {
        StorageService storageService = mock(StorageService.class);
        LibraryController libraryController = new LibraryController(mock(ListingService.class), storageService);

        MockMultipartFile file = new MockMultipartFile("file", "c.jpg", "image/jpeg", new byte[]{1,2});
        String view = libraryController.uploadImage(me(), 11L, file);

        assertThat(view).isEqualTo("redirect:/library/11/edit?ok=Image+uploaded");
        verify(storageService).addImage(any(), eq(11L), any());
    }

    @Test
    void deleteImage_callsStorage_returnsRedirect() throws Exception {
        StorageService storageService = mock(StorageService.class);
        LibraryController libraryController = new LibraryController(mock(ListingService.class), storageService);

        String view = libraryController.deleteImage(me(), 12L, (short)3);

        assertThat(view).isEqualTo("redirect:/library/12/edit?ok=Image+deleted");
        verify(storageService).deleteImage(any(), eq(12L), eq((short)3));
    }
}
