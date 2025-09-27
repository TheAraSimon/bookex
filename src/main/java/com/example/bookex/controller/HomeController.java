package com.example.bookex.controller;

import com.example.bookex.dto.listing.ListingCardDto;
import com.example.bookex.dto.listing.ListingDetailDto;
import com.example.bookex.service.ListingService;
import com.example.bookex.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ListingService listingService;
    private final RatingService ratingService;

    @GetMapping("/")
    public String root() { return "redirect:/browse"; }

    @GetMapping("/browse")
    public String browse(Model model, @RequestParam(value = "ok", required = false) String ok) {
        List<ListingCardDto> listings = listingService.browsePublic();
        model.addAttribute("listings", listings);
        model.addAttribute("ok", ok);
        return "browse";
    }

    @GetMapping("/listings/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ListingDetailDto dto = listingService.getListingDetail(id);
        model.addAttribute("listing", dto);
        model.addAttribute("bookAvg", ratingService.getAverages(dto.getBook().getId()));
        return "listing-detail";
    }
}

