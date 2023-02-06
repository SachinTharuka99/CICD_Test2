/**
 * Author :
 * Date : 2/1/2023
 * Time : 4:45 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.config;

import com.epic.cms.config.listener.FileReadStepExecutionListener;
import com.epic.cms.config.listener.StepSkipListener;
import com.epic.cms.config.mapper.FileRowMapper;
import com.epic.cms.config.policy.ExceptionSkipPolicy;
import com.epic.cms.config.processor.FileIdAppender;
import com.epic.cms.model.bean.RecInputRowDataBean;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class FileReadJobConfig extends DefaultBatchConfigurer {

    @Override
    public void setDataSource(DataSource ds1) {
    }
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private DataSource bkDataSource;

    @Bean
    @StepScope
    public FlatFileItemReader<RecInputRowDataBean> reader(@Value("#{jobParameters['filePath']}") String filePath) {
        FlatFileItemReader<RecInputRowDataBean> itemReader = new FlatFileItemReader<>();
        itemReader.setName("file_reader");// reader name
        itemReader.setResource(new PathResource(filePath));//file path
        itemReader.setLinesToSkip(1);//Skip the first line as it contains headers.
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    public LineMapper lineMapper() {
        return new FileRowMapper();
    }

    @Bean
    @StepScope
    public ItemProcessor<RecInputRowDataBean, RecInputRowDataBean> processor() {
        return new FileIdAppender();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<RecInputRowDataBean> writer(@Value("#{jobParameters['insertQuery']}") String insertQuery) {
        JdbcBatchItemWriter<RecInputRowDataBean> itemWriter = new JdbcBatchItemWriter<>();
        itemWriter.setDataSource(bkDataSource);
        itemWriter.setSql(insertQuery);
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("file_read_step")
                .listener(stepExecutionListener())
                .<RecInputRowDataBean, RecInputRowDataBean>chunk(1000)
                .reader(reader(null))
                .processor(processor())
                .writer(writer(null))
                .faultTolerant()
                .listener(stepSkipListener())
                .skipPolicy(stepSkipPolicy())
                .build();
    }

    @Bean(name = "file_read_job")
    public Job job() {
        return jobBuilderFactory.get("file_read_job")
                .flow(step())
                .end()
                .build();
    }

    public SkipPolicy stepSkipPolicy() {
        return new ExceptionSkipPolicy();
    }

    public SkipListener stepSkipListener() {
        return new StepSkipListener();
    }

    @Bean
    @StepScope
    public StepExecutionListener stepExecutionListener() {
        return new FileReadStepExecutionListener();
    }

}
