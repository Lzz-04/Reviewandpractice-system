package com.examreview.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResultDTO {
    private int total;
    private int success;
    private int failed;
    private List<String> errors = new ArrayList<>();
}
