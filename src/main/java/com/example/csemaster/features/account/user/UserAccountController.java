package com.example.csemaster.features.account.user;

import com.example.csemaster.dto.request.SignUpRequest;
import com.example.csemaster.features.account.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "UserAccount", description = "사용자 계정 관련 기능")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserAccountController {
    private final UserAccountService userAccountService;

    // 사용자 회원 가입
    @Operation(
            summary = "사용자 회원 가입",
            description = "구글 액세스 토큰과 닉네임을 받아서 회원 가입 후 토큰 발급"
    )
    @PostMapping("/auth/google/sign-up")
    @Transactional
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest request) {
        String googleId = userAccountService.isGoogleAuth(request.getAccessToken());

        if (googleId != null) {
            String userId = userAccountService.createUser(googleId, request.getNickname());

            return ResponseEntity.ok(userAccountService.getTokens(userId));
        } else {
            // 구글 액세스토큰이 유효하지 않을 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Google login failed");
        }
    }

    // 사용자 로그인
    @Operation(
            summary = "사용자 로그인",
            description = "액세스 토큰을 받아서 토큰 발급"
    )
    @PostMapping("/auth/google/login")
    public ResponseEntity<?> googleLogin(@RequestBody String accessToken) {
        // 넘겨받은 액세스 토큰으로 구글 api에 검증 요청
        String googleId = userAccountService.isGoogleAuth(accessToken);

        if (googleId != null) {
            // 구글 id 로 DB 검색 후, 등록된 유저인 경우 토큰 발급
            String userId = userAccountService.getUserId(googleId);

            if (userId != null) {
                return ResponseEntity.ok(userAccountService.getTokens(userId));
            } else {
                // 최초 등록을 하지 않은 유저
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Unauthorized: Non Sign-up user");
            }
        } else {
            // 구글 액세스토큰이 유효하지 않을 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Google login failed");
        }
    }

    @Operation(
            summary = "사용자 토큰 재발급",
            description = "사용자의 액세스 토큰과 리프레시 토큰 재발급"
    )
    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        return userAccountService.refreshToken(request.getHeader("Refresh-Token"));
    }

    // 사용자 로그아웃
    @Operation(
            summary = "사용자 로그아웃",
            description = "사용자 아이디, 액세스 토큰을 블랙리스트에 추가"
    )
    @PostMapping("/auth/google/logout")
    public ResponseEntity<?> googleUserLogout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        String accessToken = TokenUtils.extractAccessTokenFromHeader(request);

        return userAccountService.logout(userId, accessToken);
    }

    // 사용자 회원 탈퇴
    @Operation(
            summary = "사용자 회원 탈퇴",
            description = "로그아웃 처리 후 계정 비활성화"
    )
    @PostMapping("/deactivate")
    @Transactional
    public ResponseEntity<?> deactivateUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        String accessToken = TokenUtils.extractAccessTokenFromHeader(request);

        // 로그아웃으로 토큰 만료 후 비활성화
        userAccountService.logout(userId, accessToken);
        return userAccountService.deactivateUser(userId);
    }


}
