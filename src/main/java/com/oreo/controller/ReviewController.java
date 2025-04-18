package com.oreo.controller;

import com.oreo.entity.Restaurant;
import com.oreo.entity.User;
import com.oreo.service.RestaurantService;
import com.oreo.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final RestaurantService restaurantService;

    @PostMapping("/reviews/add")
    public String addReview(@RequestParam(value = "restaurantId") Integer restaurantId,
            @RequestParam(value = "content") String content,
            @RequestParam(value = "rating") int rating,
            HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null)
            return "redirect:/login";

        // RestaurantService의 findById는 Integer를 받음
        Restaurant restaurant = restaurantService.findById(restaurantId);
        reviewService.addReview(user, restaurant, content, rating);

        return "redirect:/restaurants/" + restaurantId;
    }

    @PostMapping("/reviews/delete")
    public String deleteReview(@RequestParam(value = "reviewId") Long reviewId,
            @RequestParam(value = "restaurantId") Integer restaurantId,
            HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null)
            return "redirect:/login";

        try {
            // reviewId는 Long 타입 유지
            reviewService.deleteReview(reviewId, user);
        } catch(SecurityException e) {
             return "redirect:/restaurants/" + restaurantId + "?error=delete_forbidden";
        } catch(IllegalArgumentException e) {
             return "redirect:/restaurants/" + restaurantId + "?error=review_not_found";
        }
        return "redirect:/restaurants/" + restaurantId;
    }
}