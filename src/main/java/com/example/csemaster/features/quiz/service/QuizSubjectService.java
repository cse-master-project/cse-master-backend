package com.example.csemaster.features.quiz.service;

import com.example.csemaster.dto.*;
import com.example.csemaster.dto.response.SubjectResponse;
import com.example.csemaster.exception.CustomException;
import com.example.csemaster.exception.ExceptionEnum;
import com.example.csemaster.entity.SubjectEntity;
import com.example.csemaster.entity.ChapterEntity;
import com.example.csemaster.features.quiz.QuizValidator;
import com.example.csemaster.mapper.SubjectMapper;
import com.example.csemaster.repository.ChapterRepository;
import com.example.csemaster.repository.QuizSubjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuizSubjectService {

    private final QuizSubjectRepository quizSubjectRepository;
    private final ChapterRepository chapterRepository;

    public List<SubjectResponse> getAllSubject() {
        List<SubjectEntity> subjects = quizSubjectRepository.findAll();
        return subjects.stream().map(SubjectMapper.INSTANCE::toResponse).collect(Collectors.toList());
    }

    public List<String> addSubject(SubjectRequest subjectRequest) {
        Optional<SubjectEntity> subjectEntity = quizSubjectRepository.findBySubject(subjectRequest.getSubject());

        if (subjectEntity.isPresent()) {
            throw new CustomException(ExceptionEnum.DUPLICATE_SUBJECT);
        }

        SubjectEntity newSubjectEntity = new SubjectEntity();
        newSubjectEntity.setSubjectId(null);
        newSubjectEntity.setSubject(subjectRequest.getSubject());
        quizSubjectRepository.save(newSubjectEntity);

        log.info("Create new subject( {} )]", subjectRequest.getSubject());

        return quizSubjectRepository.getAllSubject();
    }

    public List<String> addDetailSubject(String subject, String chapter) {
        Long subjectId = quizSubjectRepository.findBySubject(subject).map(SubjectEntity::getSubjectId).orElse(null);

        if (subjectId == null) {
            throw new CustomException(ExceptionEnum.NOT_FOUND_SUBJECT);
        }

        Optional<ChapterEntity> existingChapter = chapterRepository.findBySubjectIdAndChapter(subjectId, chapter);

        // 이미 있는지 중복 체크
        if (existingChapter.isPresent()) {
            throw new CustomException(ExceptionEnum.DUPLICATE_DETAIL_SUBJECT);
        }

        Integer maxIdx = chapterRepository.getMaxIndex(subject).orElse(-1);

        // 새 detailSubject 생성
        ChapterEntity newChapter = new ChapterEntity(subjectId, chapter, maxIdx + 1);

        // 새 DetailSubjectEntity 저장
        chapterRepository.save(newChapter);

        log.info("Create new detail Subject( {} - {} )]", subject, chapter);

        return chapterRepository.findChapterBySubject(subject);
    }

    public ResponseEntity<?> updateSubject(SubjectUpdateDTO subjectUpdateDTO) {
        Optional<SubjectEntity> subject = quizSubjectRepository.findBySubject(subjectUpdateDTO.getSubject());

        // subject가 존재하는지 확인
        if (subject.isEmpty()) {
            throw new CustomException(ExceptionEnum.NOT_FOUND_SUBJECT);
        }

        // 수정 전후가 같은지 확인
        String oldSubject = subject.get().getSubject();
        String newSubject = subjectUpdateDTO.getNewSubject();
        if (newSubject.equals(oldSubject)) {
            throw new CustomException(ExceptionEnum.NO_CHANGE);
        }

        subject.get().setSubject(newSubject);
        quizSubjectRepository.save(subject.get());

        return ResponseEntity.ok().build();
    }

   public SubjectDTO updateDetailSubject(ChapterUpdateDTO updateDTO) {
       Long subjectId = quizSubjectRepository.findBySubject(updateDTO.getSubject()).map(SubjectEntity::getSubjectId).orElse(null);

       if (subjectId == null) {
           throw new CustomException(ExceptionEnum.NOT_FOUND_SUBJECT);
       }

       // 기존의 텍스트와 새로운 텍스트 비교시 변경점이 있는지 확인
       if (updateDTO.getDetailSubject().equals(updateDTO.getNewDetailSubject())) {
           throw new CustomException(ExceptionEnum.NO_CHANGE);
       }

       ChapterEntity chapter = chapterRepository.findBySubjectIdAndChapter(subjectId, updateDTO.getDetailSubject()).orElse(null);
       if (chapter == null) {
           throw new CustomException(ExceptionEnum.NOT_FOUND_DETAIL_SUBJECT);
       }

       // 챕터명 변경
       chapter.setChapter(updateDTO.getNewDetailSubject());
       chapterRepository.save(chapter);

       // 변경된 챕터 목록 반환
       return new SubjectDTO(updateDTO.getSubject(), chapterRepository.findChapterBySubject(updateDTO.getSubject()));
    }

    public ResponseEntity<?> deleteSubject(SubjectRequest subjectRequest) {
        Optional<SubjectEntity> subjectEntity = quizSubjectRepository.findBySubject(subjectRequest.getSubject());
        if (subjectEntity.isEmpty()) {
            throw new CustomException(ExceptionEnum.NOT_FOUND_SUBJECT);
        }

        quizSubjectRepository.delete(subjectEntity.get());

        return ResponseEntity.ok().build();
    }

    public SubjectDTO deleteChapter(String subject, String chapter) {
        Optional<SubjectEntity> subjectEntity = quizSubjectRepository.findBySubject(subject);
        if (subjectEntity.isEmpty()) {
            throw new CustomException(ExceptionEnum.NOT_FOUND_SUBJECT);
        } else {
            List<ChapterEntity> chapters = subjectEntity.get().getChapters();
            // 검색한 전체 목록에서 삭제할 레코드 검색
            ChapterEntity delChapter = chapters.stream().filter(e -> e.getChapter().equals(chapter)).findAny().orElse(null);
            // 검색 결과 없으면 유효하지 않은 요청
            if (delChapter == null) throw new CustomException(ExceptionEnum.NOT_FOUND_DETAIL_SUBJECT);
            // 삭제한 챕터 뒤부터 index 1만큼 당기기
            for (int i = delChapter.getSortIndex() + 1; i < chapters.size(); i++) {
                chapters.get(i).setSortIndex(i - 1);
            }

            // 삭제 후 바꾼 index 저장
            chapterRepository.delete(delChapter);
            chapterRepository.saveAll(chapters);
            chapterRepository.flush();
        }

        return new SubjectDTO(subject, chapterRepository.findChapterBySubject(subject));
    }

    @Transactional
    public SubjectResponse adjustDetailSubject(String subject, List<ChapterDTO> detailSubject) {
        List<ChapterEntity> bass = chapterRepository.findBySubject(subject);
        // 검색 결과가 없으면 해당 subject 가 유효하지 않는다는 의미
        if (bass.isEmpty()) throw new CustomException(ExceptionEnum.NOT_FOUND_SUBJECT);
        // 기존과 변경 사항의 개수가 다를 경우 잘못된 요청 값임
        else if (bass.size() != detailSubject.size()) throw new CustomException(ExceptionEnum.NOT_FOUND_DETAIL_SUBJECT);
        else {
            // 편한 비교를 위해 Detail subject 순으로 정렬
            bass.sort(Comparator.comparing(ChapterEntity::getChapter));
            detailSubject.sort(Comparator.comparing(ChapterDTO::getDetailSubject));

            for (int i = 0; i < bass.size(); i++) {
                // 정렬이 됐기 때문에 같은 index 의 detail subject 가 서로 다르면 잘못된 값임
                if (bass.get(i).getChapter().equals(detailSubject.get(i).getDetailSubject())) {
                    // 일치할 경우 새로운 정렬 기준으로 변경
                    bass.get(i).setSortIndex(detailSubject.get(i).getSortIndex());
                } else {
                    throw new CustomException(ExceptionEnum.NOT_FOUND_DETAIL_SUBJECT);
                }
            }

            // 변경된 index 을 기준으로 다시 정렬
            bass.sort(Comparator.comparingInt(ChapterEntity::getSortIndex));

            // 검사: sortIndex 의 순차적 증가 여부 확인
            for (int i = 0; i < bass.size(); i++) {
                if (i != bass.get(i).getSortIndex()) {
                    log.debug("sort Index 가 잘못됨. index: {}, sortIndex: {}", i, bass.get(i).getSortIndex());
                    throw new CustomException(ExceptionEnum.ARGS_NOT_VALID);
                }
            }

            // 변경사항 저장
            chapterRepository.saveAll(bass);

            return new SubjectResponse(subject, bass.stream().map(ChapterEntity::getChapter).toList());
        }
    }
}
