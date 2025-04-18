package com.example.csemaster.v1.domain.account.user;

import com.example.csemaster.core.tools.QuizResultCounter;
import com.example.csemaster.v1.dto.NicknameDTO;
import com.example.csemaster.v1.dto.QuizResultDTO;
import com.example.csemaster.v1.dto.response.QuizStatsResponse;
import com.example.csemaster.v1.dto.response.UserInfoResponse;
import com.example.csemaster.core.exception.ApiException;
import com.example.csemaster.core.exception.ApiErrorType;
import com.example.csemaster.v1.mapper.ActiveUserMapper;
import com.example.csemaster.core.repository.ActiveUserRepository;
import com.example.csemaster.core.repository.QuizLogRepository;
import com.example.csemaster.core.repository.QuizSubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final ActiveUserRepository activeUserRepository;
    private final QuizLogRepository quizLogRepository;
    private final QuizSubjectRepository quizSubjectRepository;

    public UserInfoResponse getUserInfo(String userId) {
        return activeUserRepository.findById(userId)
                .map(ActiveUserMapper.INSTANCE::toUserInfo)
                .orElse(null);
    }

    public ResponseEntity<?> setUserNickname(String userId, NicknameDTO nickNameDTO) {
        try {
            return activeUserRepository.findById(userId)
                    .map(activeUser -> {
                        log.info("User nickname change '{}' to '{}' [userId: {}]", activeUser.getNickname(), nickNameDTO.getNickname(), userId);
                        activeUser.setNickname(nickNameDTO.getNickname());
                        activeUserRepository.save(activeUser);

                        return ResponseEntity.ok().build();
                    }).orElse(ResponseEntity.notFound().build());
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ApiErrorType.DUPLICATE_NICKNAME);
        }
    }

    // 카테고리별 정답률 구하는 함수
    private Map<String, Double> getQuizAccuracy(List<QuizResultDTO> quizResult) {
        // 모든 과목을 조회해서 카운터 객체 초기화
        QuizResultCounter quizResultCounter = new QuizResultCounter(quizSubjectRepository.getAllSubject());

        // 카운터 객체에 문제 풀이 결과 추가
        quizResult.forEach(q -> {
            try {
                quizResultCounter.pushLog(q.getSubject(), q.isCorrect());
            } catch (IllegalArgumentException e) {
                log.debug("Invalid subject.");
            }
        });

        // 정답률 계산 후 리턴
        return quizResultCounter.getCorrectRate();
    };

    private int getTotalCorrect(List<QuizResultDTO> quizResult) {
        int correctCnt = 0;

        for (QuizResultDTO e : quizResult) {
            correctCnt += e.isCorrect() ? 1 : 0;
        }

        return correctCnt;
    }

    public QuizStatsResponse getUserQuizResult(String userId) {
        List<QuizResultDTO> quizResult = quizLogRepository.findAllByUserIdV1(userId);
        int total = quizResult.size();
        int totalCorrect = getTotalCorrect(quizResult);
        int totalIncorrect = total - totalCorrect;
        Double totalAccuracy = ((double) totalCorrect / total) * 100;

        return new QuizStatsResponse(total, totalCorrect, totalIncorrect, totalAccuracy, getQuizAccuracy(quizResult));
    }
}
