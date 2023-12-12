package com.practice.springbatch.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StepNextConditionalJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job stepNextConditionalJob() {
        return jobBuilderFactory.get("stepNextConditionalJob")
                .start(conditionalJobStep1())
                // on() : 캐치할 "ExitStatus" 지정 (RepeatStatus가 아님)
                // 즉, 분기처리를 위해 상태값 조정 필요 시 ExitStatus를 사용한다.
                .on("FAILED") // FAILED 일 경우 (ExitStatus가 FAILED인 경우)
                .to(conditionalJobStep3()) // step3으로 이동한다.
                .on("*") // step3의 결과 관계 없이
                .end() // step3으로 이동하면 Flow가 종료한다.
                .from(conditionalJobStep1()) // step1로부터
                .on("*") // FAILED 외에 모든 경우
                .to(conditionalJobStep2()) // step2로 이동한다.
                .next(conditionalJobStep3()) // step2가 정상 종료되면 step3으로 이동한다.
                .on("*") // step3의 결과 관계 없이
                .end() // step3으로 이동하면 Flow가 종료한다.
                .end() // Job 종료
                .build();
    }

    @Bean
    public Step conditionalJobStep1() {
        return stepBuilderFactory.get("step1")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step1");

                    /**
                     ExitStatus를 FAILED로 지정한다.
                     해당 status를 보고 flow가 진행된다.
                     **/
                    contribution.setExitStatus(ExitStatus.FAILED);

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step conditionalJobStep2() {
        return stepBuilderFactory.get("conditionalJobStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step2");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step conditionalJobStep3() {
        return stepBuilderFactory.get("conditionalJobStep3")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step3");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    public class SkipCheckingListener extends StepExecutionListenerSupport {

        public ExitStatus afterStep(StepExecution stepExecution) {
            String exitCode = stepExecution.getExitStatus().getExitCode();
            if (!exitCode.equals(ExitStatus.FAILED.getExitCode()) &&
                    stepExecution.getSkipCount() > 0) {
                return new ExitStatus("COMPLETED WITH SKIPS");
            }
            else {
                return null;
            }
        }
    }
}