package com.example.csemaster.v2.domain.account.user;

import com.example.csemaster.v2.dto.NicknameDTO;
import com.example.csemaster.v2.dto.response.QuizStatsResponse;
import com.example.csemaster.v2.dto.response.UserInfoResponse;
import com.example.csemaster.core.exception.ApiException;
import com.example.csemaster.core.exception.ApiErrorType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Profile", description = "사용자 정보 관련 기능")
@Slf4j
@RestController(value = "V2UserProfileController")
@RequiredArgsConstructor
@RequestMapping("/api/v2/user")
public class UserProfileController {
    private final UserProfileService userProfileService;

    // 사용자 정보 조회
    @Operation(
            summary = "사용자 정보 조회",
            description = "토큰에서 사용자 아이디를 추출하여 사용자 정보 조회"
    )
    @GetMapping("/info")
    private UserInfoResponse getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        UserInfoResponse response = userProfileService.getUserInfo(userId);
        if (response != null) return response;
        else throw new ApiException(ApiErrorType.INTERNAL_SERVER_ERROR);
    }

    // 사용자 닉네임 설정
    @Operation(
            summary = "사용자 닉네임 설정",
            description = "사용자의 (새) 닉네임을 받아서 설정"
    )
    @PutMapping("/info/nickname")
    private ResponseEntity<?> setUserNickname(@RequestBody @Valid NicknameDTO nickNameDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        return userProfileService.setUserNickname(userId, nickNameDTO);
    }

    // 사용자 문제 풀이 통계
    @Operation(
            summary = "사용자 문제 풀이 통계",
            description = "전체 정답률과 카테고리별 정답률 조회"
    )
    @GetMapping("/quiz-results")
    public ResponseEntity<QuizStatsResponse> getStatistics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return ResponseEntity.ok().body(userProfileService.getUserQuizResult(userId));
    }
}
