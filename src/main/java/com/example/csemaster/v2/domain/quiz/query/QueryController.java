package com.example.csemaster.v2.domain.quiz.query;

import com.example.csemaster.core.dao.quiz.core.ActiveQuizEntity;
import com.example.csemaster.v2.dto.response.QuizRejectResponse;
import com.example.csemaster.v2.dto.response.QuizResponse;
import com.example.csemaster.v2.dto.response.UserQuizResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Quiz Query", description = "문제 조회 기능 (웹)")
@RestController(value = "V2QueryForWebController")
@RequiredArgsConstructor
@RequestMapping("/api/v2/quiz")
public class QueryController {
    private final QueryService queryService;

    // 모든 활성화된 문제 조회
    @Operation(
            summary = "문제 전체 조회",
            description = "활성화 상태인 문제 전체를 조회 (활성화 기준: 승인된 유저 문제나 삭제되지 않은 문제) [페이징 적용]"
    )
    @GetMapping("")
    public Page<ActiveQuizEntity> getAllQuiz(Pageable pageable) {
        return queryService.getQuiz(pageable);
    }

    // 현재 활성화된 유저 문제만 조회
    @Operation(
            summary = "사용자 문제 전체 조회",
            description = "활성화 상태인 사용자 문제 전체를 조회 [페이징 적용]"
    )
    @GetMapping("/user")
    public Page<ActiveQuizEntity> getUserQuiz(Pageable pageable) {
        return queryService.getUserQuiz(pageable);
    }

    // 현재 활성화된 기본 문제만 조회
    @Operation(
            summary = "기본 문제 전체 조회",
            description = "활성화 상태인 기본 문제 전체를 조회 [페이징 적용]"
    )
    @GetMapping("/default")
    public Page<ActiveQuizEntity> getDefaultQuiz(Pageable pageable) {
        return queryService.getDefaultQuiz(pageable);
    }

    // 문제 아이디로 문제 조회
    @Operation(
            summary = "문제 ID로 원하는 문제 하나를 조회한다. (예: /quiz/2 -> 2번 문제 조회)"
    )
    @GetMapping("/{quizId}")
    public QuizResponse getQuizById(@PathVariable Long quizId) { return queryService.getQuizById(quizId); }

    // 해당 유저가 만든 퀴즈 조회
    @Operation(
            summary = "자신이 추가한 문제만 조회 [사용자 전용]",
            description = "로그인된 유저가 만든 문제를 조회한다. [페이징 적용]"
    )
    @GetMapping("/my")
    public Page<UserQuizResponse> getMyQuiz(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        return queryService.getMyQuiz(userId, pageable);
    }

    // 문제의 승인 여부 확인
    // FIXME : 리스트를 반환할 필요가 없음. 반려된 문제인지 검증 필요
    @Operation(
            summary = "사용자 문제 반려 사유 조회 [사용자 전용]",
            description = "자신이 만든 문제의 승인 여부를 quiz id를 통해 확인할 수 있다."
    )
    @GetMapping("/my/reject")
    public List<QuizRejectResponse> getQuizReject(@RequestParam Long quizId) {
        return queryService.getQuizReject(quizId);
    }
}
