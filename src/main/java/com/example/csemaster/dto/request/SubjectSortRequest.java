package com.example.csemaster.dto.request;

import com.example.csemaster.dto.ChapterDTO;
import lombok.Data;

import java.util.List;

@Data
public class SubjectSortRequest {
    private String subject;
    private List<ChapterDTO> chapters;
}