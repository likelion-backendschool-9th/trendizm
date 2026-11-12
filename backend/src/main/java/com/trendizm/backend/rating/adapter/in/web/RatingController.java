package com.trendizm.backend.rating.adapter.in.web;

import com.trendizm.backend.rating.adapter.in.web.dto.RatingRequest;
import com.trendizm.backend.rating.application.dto.RatingResponse;
import com.trendizm.backend.rating.application.port.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 없이 모든 요청 허용 (auth/adapter/out/security/SecurityConfig) */
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @GetMapping
    public Page<RatingResponse> getRatings(Pageable pageable) {
        return ratingService.getRatings(pageable);
    }

    @GetMapping("/{ratingId}")
    public RatingResponse getRating(@PathVariable Long ratingId) {
        return ratingService.getRating(ratingId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createRating(@Valid @RequestBody RatingRequest request) {
        return ratingService.createRating();
    }

    @PutMapping("/{ratingId}")
    public void updateRating(@PathVariable Long ratingId, @Valid @RequestBody RatingRequest request) {
        ratingService.updateRating(ratingId);
    }

    @DeleteMapping("/{ratingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRating(@PathVariable Long ratingId) {
        ratingService.deleteRating(ratingId);
    }
}
