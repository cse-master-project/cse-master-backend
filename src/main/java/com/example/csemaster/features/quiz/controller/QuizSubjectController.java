package com.example.csemaster.features.quiz.controller;

import com.example.csemaster.dto.*;
import com.example.csemaster.dto.request.SubjectSortRequest;
import com.example.csemaster.dto.response.SubjectResponse;
import com.example.csemaster.features.quiz.service.QuizSubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "QuizSubject", description = "카테고리 관련 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz/subject")
public class QuizSubjectController {

    private final QuizSubjectService quizSubjectService;

    // 카테고리 조회
    @Operation(
            summary = "전체 과목 조회",
            description = "전체 과목 목록과 하위 챕터 조회"
    )
    @GetMapping()
    public List<SubjectResponse> getAllSubject() {
        return quizSubjectService.getAllSubject();
    }

    // 카테고리 추가
    @Operation(
            summary = "과목 추가",
            description = "입력된 문자열을 새로운 과목으로 추가"
    )
    @PostMapping()
    public List<String> addSubject(@RequestBody @Valid SubjectRequest subjectRequest) {
        return quizSubjectService.addSubject(subjectRequest);
    }


    // 서브 카테고리 추가
    @Operation(
            summary = "챕터 추가",
            description = "지정한 과목에 새로운 챕터 추가"
    )
    @PostMapping("/detail")
    public List<String> addDetailSubject(@RequestBody @Valid String subject, @RequestBody @Valid String chapter) {
        return quizSubjectService.addDetailSubject(subject, chapter);
    }

    // 카테고리 수정
    @Operation(
            summary = "과목 수정",
            description = "기존 과목의 이름을 변경할 수 있다. 기존의 이름과 새로운 이름을 입력 해야함."
    )
    @PatchMapping()
    public ResponseEntity<?> updateSubject(@RequestBody @Valid SubjectUpdateDTO subjectUpdateDTO) {
        return quizSubjectService.updateSubject(subjectUpdateDTO);
    }

    // 서브 카테고리 수정
    @Operation(
            summary = "챕터 수정",
            description = "기존의 챕터 이름을 새로운 챕터 이름으로 변경할 수 있다."
    )
    @PatchMapping("/detail")
    public SubjectDTO updateDetailSubject(@RequestBody @Valid ChapterUpdateDTO updateDTO) {
        return quizSubjectService.updateDetailSubject(updateDTO);
    }

    // 카테고리 삭제
    @Operation(
            summary = "과목 삭제",
            description = "과목을 삭제할 수 있으며 하위 챕터들은 모두 삭제된다. 삭제할 과목에 속한 문제가 있다면 삭제할 수 없다."
    )
    @DeleteMapping()
    public ResponseEntity<?> deleteSubject(@RequestBody @Valid SubjectRequest subjectRequest) {
        return quizSubjectService.deleteSubject(subjectRequest);
    }

    // 서브 카테고리 삭제
    @Operation(
            summary = "챕터 삭제",
            description = "챕터를 삭제할 수 있다. 과목과 마찬가지로 해당 챕터에 속한 문제가 있을 경우 삭제할 수 없다."
    )
    @DeleteMapping("/detail")
    public SubjectDTO deleteChapter(@RequestBody @Valid String subject, @RequestBody @Valid String chapter) {
        return quizSubjectService.deleteChapter(subject, chapter);
    }

    // 서브 카테고리 순서 변경
    @Operation(
            summary = "챕터 정렬 순서 재설정",
            description = "특정한 과목 내의 챕터들의 정렬 순서를 변경할 수 있다. 모든 항목의 인덱스를 지정해주어야한다."
    )
    @PostMapping("/detail/sort-order")
    public SubjectResponse adjustDetailSubject(@RequestBody @Valid SubjectSortRequest request) {
        return quizSubjectService.adjustDetailSubject(request.getSubject(), request.getChapters());
    }

}
