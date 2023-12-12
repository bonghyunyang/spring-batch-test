package com.practice.springbatch.Configuration;

import com.practice.springbatch.Model.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class SimpleChunkJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job ChunkJob() {
        Job chunkJob = jobBuilderFactory.get("ChunkJob0")
                .start(step())
                .build();

        return chunkJob;
    }

    @Bean
    @JobScope
    public Step step() {
        return stepBuilderFactory.get("step")
                .<Member, Member>chunk(5)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Member> reader() {

        Map<String,Object> parameterValues = new HashMap<>();
        log.info("ItemReader 실행");
        return new JpaPagingItemReaderBuilder<Member>()
                .pageSize(5)
                .parameterValues(parameterValues)
                .queryString("SELECT m FROM Member m")
                .entityManagerFactory(entityManagerFactory)
                .name("JpaPagingItemReader")
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, Member> processor(){
        log.info("ItemProcessor 실행");
        return new ItemProcessor<Member, Member>() {
            @Override
            public Member process(Member member) {
                log.error(" ID : " + member.getId() + " / Name : " + member.getName() + " / amount : " + member.getAmount());
                member.setAmount(member.getAmount() + 1000);
                log.info(" ID : " + member.getId() + " / Name : " + member.getName() + " / amount : " + member.getAmount());
                return member;
            }
        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<Member> writer(){
        log.info("ItemWriter 실행");
        return new JpaItemWriterBuilder<Member>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
