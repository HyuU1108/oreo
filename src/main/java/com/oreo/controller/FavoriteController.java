package com.oreo.controller;

import com.oreo.dto.RestaurantResponseDto;
import com.oreo.entity.User;
import com.oreo.entity.Restaurant;
import com.oreo.service.FavoriteService;
import com.oreo.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final ReviewService reviewService;

    @GetMapping("/favorites")
    public String favoriteList(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        List<Restaurant> favoriteRestaurants = favoriteService.getFavoritesByUser(loginUser);

        List<RestaurantResponseDto> favoriteDtos = favoriteRestaurants.stream()
            .map(restaurant -> new RestaurantResponseDto(
                restaurant,
                reviewService.getAverageRating(restaurant),
                reviewService.getReviewCount(restaurant)
            ))
            .collect(Collectors.toList());

        model.addAttribute("favorites", favoriteDtos);
        return "favorite/list";
    }

    @PostMapping("/favorites/remove")
    public String removeFavorite(@RequestParam(value = "restaurantId") Integer restaurantId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null)
            return "redirect:/login";

        try {
            favoriteService.removeFavorite(loginUser, restaurantId);
            redirectAttributes.addFlashAttribute("message", "찜 목록에서 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "찜 취소 중 오류가 발생했습니다.");
            System.err.println("Error removing favorite: " + e.getMessage());
        }
        return "redirect:/favorites";
    }

    @PostMapping("/favorites/add")
    public String addFavorite(@RequestParam(value = "restaurantId") Integer restaurantId,
             HttpSession session, RedirectAttributes redirectAttributes) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null)
            return "redirect:/login";

        try {
             favoriteService.addFavorite(loginUser, restaurantId);
             redirectAttributes.addFlashAttribute("message", "찜 목록에 추가되었습니다.");
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("error", "찜 추가 중 오류가 발생했습니다.");
             System.err.println("Error adding favorite: " + e.getMessage());
        }
        return "redirect:/restaurants/" + restaurantId;
    }
}