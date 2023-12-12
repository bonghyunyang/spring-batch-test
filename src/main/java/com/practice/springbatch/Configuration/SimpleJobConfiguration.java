package com.practice.springbatch.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j // log 사용을 위한 lombok 어노테이션이다.
@RequiredArgsConstructor // 생성자 DI를 위한 lombok 어노테이션이다.
@Configuration // Spring Batch의 모든 Job은 @Configuration으로 등록해서 사용한다.
public class SimpleJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // Spring Batch에서 Job은 하나의 배치 작업 단위를 의미한다.
    // Job 안에는 여러 Step이 존재하고, Step 안에 Tasklet 혹은 Reader & Processor & Writer 묶음이 존재한다.
    // 즉, Step은 배치 처리 과정 중 하나의 단계라고 보면 되며, 해당 단계에서 실제 처리하고자 하는 기능과 설정을 포함한다.

    @Bean
    public Job simpleJob(){
        return jobBuilderFactory.get("simpleJob")
                .start(simpleStep1(null))
                .next(simpleStep2(null))
                .build();
    }

//    @Bean
//    @JobScope
//    public Step simpleStep1(@Value("#{jobParameters[requestDate]}") String requestDate) {
//            return stepBuilderFactory.get("simpleStep1")
//                    .tasklet((contribution, chunkContext) -> {
//                        log.info(">>>>> This is Step1");
//                        log.info(">>>>> requestDate = {}", requestDate);
//                        return RepeatStatus.FINISHED;
//                    })
//                    .build();
//        }

    @Bean
    @JobScope
    public Step simpleStep1(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return stepBuilderFactory.get("simpleStep1")
                .tasklet((contribution, chunkContext) -> {
                    throw new IllegalArgumentException("step1에서 실패합니다.");
                })
                .build();
    }

    @Bean
    @JobScope
    public Step simpleStep2(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return stepBuilderFactory.get("simpleStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is Step2");
                    log.info(">>>>> requestDate = {}", requestDate);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }


}
