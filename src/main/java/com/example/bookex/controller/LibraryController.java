package com.example.bookex.controller;


import com.example.bookex.dto.listing.ListingDetailDto;
import com.example.bookex.dto.listing.ListingFormDto;
import com.example.bookex.entity.User;
import com.example.bookex.service.ListingService;
import com.example.bookex.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/library")
public class LibraryController {

    private final ListingService listingService;
    private final StorageService storageService;

    @GetMapping
    public String myLibrary(@AuthenticationPrincipal User me, Model model,
                            @RequestParam(value="ok", required = false) String ok) {
        model.addAttribute("cards", listingService.myLibrary(me));
        model.addAttribute("ok", ok);
        return "library";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ListingFormDto());
        return "listing-form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal User me,
                         @Valid @ModelAttribute("form") ListingFormDto form,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "listing-form";
        ListingDetailDto listingDetailDto = listingService.createListing(me, form);
        return "redirect:/library?ok=Listing+created";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ListingDetailDto listingDetailDto = listingService.getListingDetail(id);
        ListingFormDto listingFormDto = ListingFormDto.builder()
                .id(listingDetailDto.getId())
                .title(listingDetailDto.getBook().getTitle())
                .author(listingDetailDto.getBook().getAuthor())
                .isbn(listingDetailDto.getBook().getIsbn())
                .condition(com.example.bookex.entity.enums.Condition.valueOf(listingDetailDto.getCondition()))
                .available(listingDetailDto.isAvailable())
                .notes(listingDetailDto.getNotes())
                .build();
        model.addAttribute("form", listingFormDto);
        model.addAttribute("images", listingDetailDto.getImages());
        return "listing-form";
    }

    @PostMapping("/{id}")
    public String update(@AuthenticationPrincipal User me,
                         @PathVariable Long id,
                         @Valid @ModelAttribute("form") ListingFormDto form,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "listing-form";
        listingService.updateListing(me, id, form);
        return "redirect:/library?ok=Listing+updated";
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal User me, @PathVariable Long id) {
        listingService.deleteListing(me, id);
        return "redirect:/library?ok=Listing+deleted";
    }

    @PostMapping("/{id}/images")
    public String uploadImage(@AuthenticationPrincipal User me,
                              @PathVariable Long id,
                              @RequestParam("file") MultipartFile file) throws Exception {
        storageService.addImage(me, id, file);
        return "redirect:/library/" + id + "/edit?ok=Image+uploaded";
    }

    @PostMapping("/{id}/images/{no}/delete")
    public String deleteImage(@AuthenticationPrincipal User me,
                              @PathVariable Long id,
                              @PathVariable("no") short imageNo) throws Exception {
        storageService.deleteImage(me, id, imageNo);
        return "redirect:/library/" + id + "/edit?ok=Image+deleted";
    }
}

