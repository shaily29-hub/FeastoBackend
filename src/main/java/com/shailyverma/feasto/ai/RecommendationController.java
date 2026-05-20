package com.shailyverma.feasto.ai;

import com.shailyverma.feasto.auth_users.entity.User;
import com.shailyverma.feasto.menu.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RecommendationController {

    private final RecommendationService
            recommendationService;

    @PostMapping
    public List<Menu> getRecommendations(
            @RequestBody User user
    ) {

        return recommendationService
                .getRecommendations(user);
    }
}