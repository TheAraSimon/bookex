package com.example.bookex.controller;

import com.example.bookex.dto.book.BookDto;
import com.example.bookex.dto.listing.ListingCardDto;
import com.example.bookex.dto.listing.ListingDetailDto;
import com.example.bookex.dto.rating.RatingAverageDto;
import com.example.bookex.service.ListingService;
import com.example.bookex.service.RatingService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HomeControllerTest {

    @Test
    void root_redirectsToBrowse() {
        HomeController homeController = new HomeController(mock(ListingService.class), mock(RatingService.class));
        assertThat(homeController.root()).isEqualTo("redirect:/browse");
    }

    @Test
    void browse_addsListingsAndOk_returnsBrowse() {
        ListingService listingService = mock(ListingService.class);
        RatingService ratingService = mock(RatingService.class);
        HomeController homeController = new HomeController(listingService, ratingService);

        when(listingService.browsePublic()).thenReturn(List.of(
                ListingCardDto.builder().id(1L).build(),
                ListingCardDto.builder().id(2L).build()
        ));

        Model model = new ExtendedModelMap();
        String view = homeController.browse(model, "DONE");

        assertThat(view).isEqualTo("browse");
        assertThat(model.getAttribute("ok")).isEqualTo("DONE");
        assertThat((List<?>) model.getAttribute("listings")).hasSize(2);
    }

    @Test
    void detail_addsListingAndAverages_returnsListingDetail() {
        ListingService listingService = mock(ListingService.class);
        RatingService ratingService = mock(RatingService.class);
        HomeController homeController = new HomeController(listingService, ratingService);

        ListingDetailDto listingDetailDto = ListingDetailDto.builder()
                .id(9L)
                .book(BookDto.builder().id(5L).build())
                .build();
        when(listingService.getListingDetail(9L)).thenReturn(listingDetailDto);
        when(ratingService.getAverages(5L))
                .thenReturn(RatingAverageDto.builder().count(0).build());

        Model model = new ExtendedModelMap();
        String view = homeController.detail(9L, model);

        assertThat(view).isEqualTo("listing-detail");
        assertThat(model.getAttribute("listing")).isEqualTo(listingDetailDto);
        assertThat(model.getAttribute("bookAvg")).isInstanceOf(RatingAverageDto.class);
    }
}
