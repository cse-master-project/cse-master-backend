package com.example.csemaster.v2.domain.quiz.category;

import com.example.csemaster.core.dao.quiz.category.ChapterEntity;
import com.example.csemaster.core.dao.quiz.category.SubjectEntity;
import com.example.csemaster.core.exception.ApiErrorType;
import com.example.csemaster.core.exception.ApiException;
import com.example.csemaster.core.repository.ChapterRepository;
import com.example.csemaster.core.repository.QuizSubjectRepository;
import com.example.csemaster.v2.dto.*;
import com.example.csemaster.v2.dto.response.SubjectResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service(value = "V2QuizSubjectService")
@Slf4j
@RequiredArgsConstructor
public class CategoryService {
    private final QuizSubjectRepository quizSubjectRepository;
    private final ChapterRepository chapterRepository;

    // 모든 과목과 챕터 조회
    public List<SubjectResponse> getAllSubject() {
        List<SubjectEntity> subjects = quizSubjectRepository.findAll();
        return subjects.stream().map(e -> new SubjectResponse(e.getSubject(), e.getChapters().stream().map(ChapterEntity::getChapter).toList())).collect(Collectors.toList());
    }

    // TODO : 테스트 필요
    // 모든 과목 목록 조회
    public List<SubjectRequest> getSubjects() {
        return quizSubjectRepository.findAll().stream().map(e -> new SubjectRequest(e.getSubject())).toList();
    }

    // 과목 추가
    public List<String> addSubject(SubjectRequest subjectRequest) {
        Optional<SubjectEntity> subjectEntity = quizSubjectRepository.findBySubject(subjectRequest.getSubject());

        if (subjectEntity.isPresent()) {
            throw new ApiException(ApiErrorType.DUPLICATE_SUBJECT);
        }

        SubjectEntity newSubjectEntity = new SubjectEntity();
        newSubjectEntity.setSubjectId(null);
        newSubjectEntity.setSubject(subjectRequest.getSubject());
        quizSubjectRepository.save(newSubjectEntity);

        log.info("Create new subject( {} )]", subjectRequest.getSubject());

        return quizSubjectRepository.getAllSubject();
    }

    // TODO : 테스트 필요
    // 챕터 조회
    public SubjectDTO getChapter(String subject) {
        Optional<SubjectEntity> subjectEntity = quizSubjectRepository.findBySubject(subject);
        if (subjectEntity.isPresent()) {
            return new SubjectDTO(subjectEntity.get().getSubject(), subjectEntity.get().getChapters().stream().map(ChapterEntity::getChapter).toList());
        } else {
            throw new ApiException(ApiErrorType.NOT_FOUND_SUBJECT);
        }
    }

    // 챕터 추가
    public List<String> addChapter(String subject, String chapter) {
        Long subjectId = quizSubjectRepository.findBySubject(subject).map(SubjectEntity::getSubjectId).orElse(null);

        if (subjectId == null) {
            throw new ApiException(ApiErrorType.NOT_FOUND_SUBJECT);
        }

        Optional<ChapterEntity> existingChapter = chapterRepository.findBySubjectIdAndChapter(subjectId, chapter);

        // 이미 있는지 중복 체크
        if (existingChapter.isPresent()) {
            throw new ApiException(ApiErrorType.DUPLICATE_DETAIL_SUBJECT);
        }

        Integer maxIdx = chapterRepository.getMaxIndex(subject).orElse(-1);

        // 새 Chapter 생성
        ChapterEntity newChapter = new ChapterEntity(subjectId, chapter, maxIdx + 1);

        // 새 ChapterEntity 저장
        chapterRepository.save(newChapter);

        log.info("Create new detail Subject( {} - {} )]", subject, chapter);

        return chapterRepository.findChapterBySubject(subject);
    }

    // 과목 이름 변경
    public ResponseEntity<?> updateSubject(SubjectUpdateDTO subjectUpdateDTO) {
        Optional<SubjectEntity> subject = quizSubjectRepository.findBySubject(subjectUpdateDTO.getSubject());

        // subject가 존재하는지 확인
        if (subject.isEmpty()) {
            throw new ApiException(ApiErrorType.NOT_FOUND_SUBJECT);
        }

        // 수정 전후가 같은지 확인
        String oldSubject = subject.get().getSubject();
        String newSubject = subjectUpdateDTO.getNewSubject();
        if (newSubject.equals(oldSubject)) {
            throw new ApiException(ApiErrorType.NO_CHANGE);
        }

        subject.get().setSubject(newSubject);
        quizSubjectRepository.save(subject.get());

        return ResponseEntity.ok().build();
    }

    // 챕터 이름 변경
   public SubjectDTO updateChapter(ChapterUpdateDTO updateDTO) {
       // 기존의 텍스트와 새로운 텍스트 비교시 변경점이 있는지 확인
       if (updateDTO.getChapter().equals(updateDTO.getNewChapter())) {
           throw new ApiException(ApiErrorType.NO_CHANGE);
       }

       Long subjectId = quizSubjectRepository.findBySubject(updateDTO.getSubject()).map(SubjectEntity::getSubjectId).orElse(null);

       if (subjectId == null) {
           throw new ApiException(ApiErrorType.NOT_FOUND_SUBJECT);
       }

       ChapterEntity chapter = chapterRepository.findBySubjectIdAndChapter(subjectId, updateDTO.getChapter()).orElse(null);
       if (chapter == null) {
           throw new ApiException(ApiErrorType.NOT_FOUND_CHAPTER);
       }

       // 챕터명과 과목ID로 기본키가 지정되어 있어, (기본키를 변경하면 안되는지 몰랐음)
       // 기본키 변경을 허용하지 않는 JPA 특성상 삭제 후 추가해야함

       // 챕터명 변경
       Integer cacheIdx = chapter.getSortIndex();
       chapterRepository.delete(chapter);

       ChapterEntity newChapter = new ChapterEntity();
       newChapter.setSubjectId(subjectId);
       newChapter.setSortIndex(cacheIdx);
       newChapter.setChapter(updateDTO.getNewChapter());
       chapterRepository.save(newChapter);
       chapterRepository.flush();

       // 변경된 챕터 목록 반환
       return new SubjectDTO(updateDTO.getSubject(), chapterRepository.findChapterBySubject(updateDTO.getSubject()));
    }

    // 과목 삭제
    public List<String> deleteSubject(SubjectRequest subjectRequest) {
        Optional<SubjectEntity> subjectEntity = quizSubjectRepository.findBySubject(subjectRequest.getSubject());
        if (subjectEntity.isEmpty()) {
            throw new ApiException(ApiErrorType.NOT_FOUND_SUBJECT);
        }

        quizSubjectRepository.delete(subjectEntity.get());

        return quizSubjectRepository.getAllSubject();
    }

    // 챕터 삭제
    public SubjectDTO deleteChapter(String subject, String chapter) {
        Optional<SubjectEntity> subjectEntity = quizSubjectRepository.findBySubject(subject);
        if (subjectEntity.isEmpty()) {
            throw new ApiException(ApiErrorType.NOT_FOUND_SUBJECT);
        } else {
            ChapterEntity chapterEntity = chapterRepository.findBySubjectIdAndChapter(subjectEntity.get().getSubjectId(), chapter).orElse(null);
            if (chapterEntity == null) throw new ApiException(ApiErrorType.NOT_FOUND_CHAPTER);

            chapterRepository.delete(chapterEntity);
            int delIndex = chapterEntity.getSortIndex();

            List<ChapterEntity> chapters = chapterRepository.findChapterEntityBySubject(subject);
            for(int i = delIndex; i < chapters.size(); i++) {
                chapters.get(i).setSortIndex(i);
            }
            chapterRepository.saveAll(chapters);
        }

        return new SubjectDTO(subject, chapterRepository.findChapterBySubject(subject));
    }

    // 챕터 정렬
    @Transactional
    public SubjectResponse adjustChapter(String subject, List<ChapterDTO> chapters) {
        List<ChapterEntity> bass = chapterRepository.findBySubject(subject);
        // 검색 결과가 없으면 해당 subject 가 유효하지 않는다는 의미
        if (bass.isEmpty()) throw new ApiException(ApiErrorType.NOT_FOUND_SUBJECT);

        // 기존과 변경 사항의 개수가 다를 경우 잘못된 요청 값임
        else if (bass.size() != chapters.size()) throw new ApiException(ApiErrorType.NOT_FOUND_CHAPTER);
        else {
            // 편한 비교를 위해 Detail subject 순으로 정렬
            bass.sort(Comparator.comparing(ChapterEntity::getChapter));
            chapters.sort(Comparator.comparing(ChapterDTO::getChapter));

            for (int i = 0; i < bass.size(); i++) {
                // 정렬이 됐기 때문에 같은 index 의 detail subject 가 서로 다르면 잘못된 값임
                if (bass.get(i).getChapter().equals(chapters.get(i).getChapter())) {
                    // 일치할 경우 새로운 정렬 기준으로 변경
                    bass.get(i).setSortIndex(chapters.get(i).getSortIndex());
                } else {
                    throw new ApiException(ApiErrorType.NOT_FOUND_CHAPTER);
                }
            }

            // 변경된 index 을 기준으로 다시 정렬
            bass.sort(Comparator.comparingInt(ChapterEntity::getSortIndex));

            // 검사: sortIndex 의 순차적 증가 여부 확인
            for (int i = 0; i < bass.size(); i++) {
                if (i != bass.get(i).getSortIndex()) {
                    log.debug("sort Index 가 잘못됨. index: {}, sortIndex: {}", i, bass.get(i).getSortIndex());
                    throw new ApiException(ApiErrorType.ARGS_NOT_VALID);
                }
            }

            // 변경 사항 저장
            chapterRepository.saveAll(bass);

            return new SubjectResponse(subject, bass.stream().map(ChapterEntity::getChapter).toList());
        }
    }
}
