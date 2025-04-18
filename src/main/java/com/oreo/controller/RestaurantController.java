package com.oreo.controller;

import com.oreo.dto.RestaurantRequestDto;
import com.oreo.dto.RestaurantResponseDto;
import com.oreo.entity.Restaurant;
import com.oreo.entity.User;
import com.oreo.service.FavoriteService;
import com.oreo.service.FileStorageService;
import com.oreo.service.RestaurantService;
import com.oreo.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final FileStorageService fileStorageService;
    private final FavoriteService favoriteService;
    private final ReviewService reviewService;

    private static final List<String> MAIN_FEATURED_CATEGORIES = List.of(
            "라멘", "이자카야", "야키니쿠", "스시", "일식"
    );

    @GetMapping
    public String listRestaurants(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "searchField", required = false, defaultValue = "all") String searchField,
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            Model model) {

        Page<RestaurantResponseDto> restaurantPage;
        if (keyword != null && !keyword.isBlank()) {
            restaurantPage = restaurantService.searchRestaurants(keyword, searchField, pageable);
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("selectedSearchField", searchField);
            model.addAttribute("selectedCategory", null);
        } else if (category != null && !category.isEmpty()) {
            restaurantPage = restaurantService.getRestaurantsByCategory(category, pageable);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("searchKeyword", null);
            model.addAttribute("selectedSearchField", "all");
        } else {
            restaurantPage = restaurantService.getAllRestaurants(pageable);
            model.addAttribute("selectedCategory", null);
            model.addAttribute("searchKeyword", null);
            model.addAttribute("selectedSearchField", "all");
        }

        List<String> allDbCategories = restaurantService.getAllCategories();
        List<String> directCategories = new ArrayList<>();
        List<String> dropdownCategories = new ArrayList<>();
        Set<String> addedToDirect = new HashSet<>();

        for (String mainCat : MAIN_FEATURED_CATEGORIES) {
            if (allDbCategories.contains(mainCat)) {
                directCategories.add(mainCat);
                addedToDirect.add(mainCat);
            }
        }
        for (String dbCat : allDbCategories) {
            if (!addedToDirect.contains(dbCat)) {
                dropdownCategories.add(dbCat);
            }
        }

        model.addAttribute("directCategories", directCategories);
        model.addAttribute("dropdownCategories", dropdownCategories);
        model.addAttribute("restaurantPage", restaurantPage);

        String activeFilterInfo = "전체";
        if (model.containsAttribute("searchKeyword") && keyword != null) {
             activeFilterInfo = getSearchFieldDisplayName(searchField) + " 검색: " + keyword;
        } else if (model.containsAttribute("selectedCategory") && category != null) {
             activeFilterInfo = "카테고리: " + category;
        }
        model.addAttribute("activeFilterInfo", activeFilterInfo);

        int nowPage = restaurantPage.getNumber();
        int totalPages = restaurantPage.getTotalPages();
        int startPage = Math.max(0, nowPage - 4);
        int endPage = Math.min(totalPages > 0 ? totalPages - 1 : 0, nowPage + 4);
        startPage = Math.min(startPage, endPage);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);

        return "restaurants/list";
    }

    private String getSearchFieldDisplayName(String searchField) {
        if (searchField == null) return "전체";
        switch (searchField.toLowerCase()) {
            case "name": return "이름";
            case "description": return "설명";
            case "all":
            default: return "전체";
        }
    }

    @GetMapping("/{id}")
    public String detailRestaurant(@PathVariable("id") Integer id,
                                    HttpSession session,
                                    Model model) {
        try {
            RestaurantResponseDto restaurantDto = restaurantService.getRestaurantById(id);
            model.addAttribute("restaurant", restaurantDto);
            Restaurant restaurantEntity = restaurantService.findRestaurantEntityByIdOrThrow(id);
            model.addAttribute("reviews", reviewService.getReviewsByRestaurant(restaurantEntity));
            double averageRating = reviewService.getAverageRating(restaurantEntity);
            int reviewCount = reviewService.getReviewCount(restaurantEntity);
            model.addAttribute("averageRating", averageRating);
            model.addAttribute("reviewCount", reviewCount);
            User loginUser = (User) session.getAttribute("loginUser");
            model.addAttribute("loginUser", loginUser);
            if (loginUser != null) {
                boolean isFavorite = favoriteService.isFavorite(loginUser, restaurantEntity);
                model.addAttribute("isFavorite", isFavorite);
            } else {
                model.addAttribute("isFavorite", false);
            }
            return "restaurants/detail";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    @GetMapping("/new")
    public String newRestaurantForm(Model model) {
        List<String> allCategories = restaurantService.getAllCategories();
        model.addAttribute("allCategories", allCategories);
        if (!model.containsAttribute("restaurantRequestDto")) {
            model.addAttribute("restaurantRequestDto", new RestaurantRequestDto());
        }
        model.addAttribute("isEditMode", false);
        model.addAttribute("pageTitle", "새 음식점 등록");
        model.addAttribute("formAction", "/restaurants/new");
        return "restaurants/form";
    }

    @PostMapping("/new")
    public String createRestaurant(@Valid @ModelAttribute RestaurantRequestDto restaurantRequestDto,
                                   BindingResult bindingResult,
                                   // *** @RequestParam에 value 속성 추가 ***
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        String storedFileName = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String contentType = imageFile.getContentType();
                if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/gif"))) {
                    bindingResult.rejectValue("imageUrl", "file.typeError", "이미지 파일(JPG, PNG, GIF)만 업로드 가능합니다.");
                } else {
                    storedFileName = fileStorageService.storeFile(imageFile);
                }
            } catch (Exception e) {
                bindingResult.rejectValue("imageUrl", "file.uploadError", "파일 처리 중 오류 발생: " + e.getMessage());
            }
        }

        if (bindingResult.hasErrors()) {
            List<String> allCategories = restaurantService.getAllCategories();
            model.addAttribute("allCategories", allCategories);
            model.addAttribute("isEditMode", false);
            model.addAttribute("pageTitle", "새 음식점 등록 (오류)");
            model.addAttribute("formAction", "/restaurants/new");
             if (storedFileName != null) {
                 try { fileStorageService.deleteFile(storedFileName); } catch (Exception ignored) {}
             }
            return "restaurants/form";
        }

        restaurantRequestDto.setImageUrl(storedFileName);

        try {
            RestaurantResponseDto created = restaurantService.createRestaurant(restaurantRequestDto);
            redirectAttributes.addFlashAttribute("message", "음식점 '" + created.getName() + "'이(가) 성공적으로 등록되었습니다.");
            return "redirect:/restaurants/" + created.getId();
        } catch (Exception e) {
             if (storedFileName != null) {
                 try { fileStorageService.deleteFile(storedFileName); } catch (Exception ignored) {}
             }
            model.addAttribute("globalErrorMessage", "음식점 등록 중 오류가 발생했습니다. 다시 시도해주세요.");
            List<String> allCategories = restaurantService.getAllCategories();
            model.addAttribute("allCategories", allCategories);
            model.addAttribute("isEditMode", false);
            model.addAttribute("pageTitle", "새 음식점 등록 (오류)");
            model.addAttribute("formAction", "/restaurants/new");
            return "restaurants/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editRestaurantForm(@PathVariable("id") Integer id,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            RestaurantResponseDto existing = restaurantService.getRestaurantById(id);
            RestaurantRequestDto requestDto = (RestaurantRequestDto) model.getAttribute("restaurantRequestDto");
            if (requestDto == null) {
                requestDto = new RestaurantRequestDto();
                requestDto.setName(existing.getName());
                requestDto.setAddress(existing.getAddress());
                requestDto.setPhone(existing.getPhone());
                requestDto.setDescription(existing.getDescription());
                requestDto.setCategory(existing.getCategory());
            }
            List<String> allCategories = restaurantService.getAllCategories();
            model.addAttribute("allCategories", allCategories);
            model.addAttribute("restaurantRequestDto", requestDto);
            model.addAttribute("currentImageUrl", existing.getImageUrl());
            model.addAttribute("isEditMode", true);
            model.addAttribute("pageTitle", "음식점 정보 수정");
            model.addAttribute("formAction", "/restaurants/" + id + "/edit");
            model.addAttribute("restaurantId", id);
            return "restaurants/form";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/restaurants";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateRestaurant(@PathVariable("id") Integer id,
                                   @Valid @ModelAttribute RestaurantRequestDto restaurantRequestDto,
                                   BindingResult bindingResult,
                                   // *** @RequestParam에 value 속성 추가 ***
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        String oldFileName = null;
        try {
            RestaurantResponseDto existingRestaurant = restaurantService.getRestaurantById(id);
            oldFileName = existingRestaurant.getImageUrl();
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "수정할 음식점(ID: " + id + ")을 찾을 수 없습니다.");
            return "redirect:/restaurants";
        }

        String storedFileName = null;
        boolean newFileUploaded = false;
        if (imageFile != null && !imageFile.isEmpty()) {
             newFileUploaded = true;
             try {
                  String contentType = imageFile.getContentType();
                  if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/gif"))) {
                      bindingResult.rejectValue("imageUrl", "file.typeError", "이미지 파일(JPG, PNG, GIF)만 업로드 가능합니다.");
                  } else {
                     storedFileName = fileStorageService.storeFile(imageFile);
                  }
             } catch (Exception e) {
                  bindingResult.rejectValue("imageUrl", "file.uploadError", "파일 처리 중 오류 발생: " + e.getMessage());
                  storedFileName = null;
             }
        }

        String finalFileName = oldFileName;
        boolean shouldDeleteOldFile = false;
        if (newFileUploaded && storedFileName != null && !bindingResult.hasFieldErrors("imageUrl")) {
            finalFileName = storedFileName;
            if (oldFileName != null && !FileStorageService.isExternalUrl(oldFileName)) {
                shouldDeleteOldFile = true;
            }
        }

        restaurantRequestDto.setImageUrl(finalFileName);

        if (bindingResult.hasErrors()) {
             List<String> allCategories = restaurantService.getAllCategories();
             model.addAttribute("allCategories", allCategories);
             model.addAttribute("currentImageUrl", oldFileName);
             model.addAttribute("isEditMode", true);
             model.addAttribute("pageTitle", "음식점 정보 수정 (오류)");
             model.addAttribute("formAction", "/restaurants/" + id + "/edit");
             model.addAttribute("restaurantId", id);
             if (storedFileName != null && shouldDeleteOldFile) {
                 try { fileStorageService.deleteFile(storedFileName); } catch (Exception ignored) {}
             }
            return "restaurants/form";
        }

        try {
            restaurantService.updateRestaurant(id, restaurantRequestDto);
            if (shouldDeleteOldFile) {
                try {
                    fileStorageService.deleteFile(oldFileName);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("warningMessage", "정보는 수정되었으나 이전 이미지 파일(" + oldFileName + ") 삭제 중 오류 발생: " + e.getMessage());
                }
            }
            redirectAttributes.addFlashAttribute("message", "음식점 정보가 성공적으로 수정되었습니다.");
            return "redirect:/restaurants/" + id;
        } catch (Exception e) {
              if (storedFileName != null && shouldDeleteOldFile) {
                  try { fileStorageService.deleteFile(storedFileName); } catch (Exception ignored) {}
              }
              model.addAttribute("globalErrorMessage", "음식점 수정 중 오류가 발생했습니다. 다시 시도해주세요.");
              List<String> allCategories = restaurantService.getAllCategories();
              model.addAttribute("allCategories", allCategories);
              model.addAttribute("currentImageUrl", oldFileName);
              model.addAttribute("isEditMode", true);
              model.addAttribute("pageTitle", "음식점 정보 수정 (오류)");
              model.addAttribute("formAction", "/restaurants/" + id + "/edit");
              model.addAttribute("restaurantId", id);
            return "restaurants/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteRestaurant(@PathVariable("id") Integer id,
                                   RedirectAttributes redirectAttributes) {
        try {
            restaurantService.deleteRestaurant(id);
            redirectAttributes.addFlashAttribute("message", "음식점(ID: " + id + ")이(가) 성공적으로 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "삭제할 음식점(ID: " + id + ")을 찾을 수 없습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "삭제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/restaurants";
    }
}