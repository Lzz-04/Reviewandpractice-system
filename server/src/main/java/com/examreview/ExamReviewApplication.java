package com.examreview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.examreview.mapper")
public class ExamReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamReviewApplication.class, args);
    }
}
