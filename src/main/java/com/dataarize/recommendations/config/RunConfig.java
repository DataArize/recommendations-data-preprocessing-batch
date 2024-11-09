package com.dataarize.recommendations.config;

import com.dataarize.recommendations.constants.ExceptionMessages;
import com.dataarize.recommendations.constants.HelperConstants;
import com.dataarize.recommendations.exceptions.MissingFilePathException;
import com.dataarize.recommendations.listener.FlatFileWriterListener;
import com.dataarize.recommendations.listener.GCSDataReaderListener;
import com.dataarize.recommendations.listener.PreProcessorStepListener;
import com.dataarize.recommendations.model.DataSet;
import com.dataarize.recommendations.processor.DataProcessor;
import com.dataarize.recommendations.tasklet.TransferToGCSTasklet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.PlatformTransactionManager;

import java.net.MalformedURLException;
import java.util.Objects;

/**
 * Configuration class for setting up the batch processing steps for data ingestion, transformation, and output
 * in a Spring Batch environment. This class configures readers, writers, and steps for processing data read
 * from Google Cloud Storage (GCS), performing transformations, and writing the results to an output file.
 *
 * <p>This class is a part of the Spring Batch infrastructure that is responsible for orchestrating data transfer
 * tasks, such as reading, processing, and exporting large datasets in a structured and reliable manner.</p>
 *
 * <p>The class includes methods to configure:
 * <ul>
 *     <li>ItemReader for reading data from Google Cloud Storage using a FlatFileItemReader.</li>
 *     <li>ItemWriter for writing processed data into a flat file with CSV formatting.</li>
 *     <li>Steps for preprocessing and transferring data, utilizing tasklets and chunk-based processing for
 *     efficiency.</li>
 * </ul>
 * </p>
 *
 * <p>This configuration ensures that the batch processing tasks are executed with transaction management and
 * lifecycle listeners to ensure robustness, fault tolerance, and error handling.</p>
 */
@Slf4j
@Configuration
public class RunConfig {

    /**
     * Configures a {@link FlatFileItemReader} bean to read data from a specified Google Cloud Storage file.
     * This reader uses Spring Batch's {@link FlatFileItemReaderBuilder} to create a reader with a custom line mapper
     * and strict validation on file existence.
     *
     * @param resourceLoader a {@link ResourceLoader} to load resources, such as files, from the specified path.
     * @param filePathParameter the input file path as a job parameter, specifying the location of the file in GCS.
     *                          This parameter must be non-null and valid. If the path is null or empty, a
     *                          {@link MissingFilePathException} is thrown.
     * @return a configured {@link FlatFileItemReader} for reading lines from the specified file in GCS.
     * @throws MalformedURLException if the provided file path is invalid or cannot be converted into a URL.
     * @throws MissingFilePathException if the file path parameter is null or missing.
     */
    @Bean
    @StepScope
    protected FlatFileItemReader<String> gcsItemReader(ResourceLoader resourceLoader,
                                                       @Value("#{jobParameters['INPUT_FILE_PATH']}") String filePathParameter) throws MalformedURLException {

        String filePath = Objects.requireNonNull(filePathParameter, () -> {
            throw new MissingFilePathException(ExceptionMessages.MISSING_INPUT_FILE_PATH);
        });

        return new FlatFileItemReaderBuilder<String>()
                .name(HelperConstants.READER_NAME)
                .resource(resourceLoader.getResource(filePath))
                .lineMapper(createLineMapper())
                .strict(true)
                .build();
    }

    /**
     * Creates and configures a {@link DefaultLineMapper} for mapping lines in a delimited text file
     * to a {@link String} representation. This mapper utilizes a {@link DelimitedLineTokenizer} to
     * parse each line based on a specified delimiter and a token name, allowing each line of the file
     * to be interpreted as a single field.
     *
     * <p>Specifically, this line mapper is useful for processing flat files where each line should be
     * read as a distinct string value identified by a custom delimiter and field name, as specified
     * in {@link HelperConstants}.</p>
     *
     * <p><b>Configuration:</b></p>
     * <ul>
     *     <li><b>Delimiter:</b> Configured via {@code HelperConstants.END_OF_LINE_CHARACTER} to split each line.</li>
     *     <li><b>Field Name:</b> Defined by {@code HelperConstants.TOKENIZER_NAME}, ensuring each line is mapped
     *         based on a consistent field name.</li>
     * </ul>
     *
     * <p>This method returns a {@link DefaultLineMapper} instance that can be injected into a reader for file
     * processing tasks requiring line-by-line mapping.</p>
     *
     * @return a configured {@link DefaultLineMapper} that uses a {@link DelimitedLineTokenizer} with specified
     * delimiter and field name to map each line of the file to a {@link String}.
     */
    private DefaultLineMapper<String> createLineMapper() {
        DefaultLineMapper<String> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(HelperConstants.END_OF_LINE_CHARACTER);
        tokenizer.setNames(HelperConstants.TOKENIZER_NAME);
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> fieldSet.readString(HelperConstants.TOKENIZER_NAME));
        return lineMapper;
    }


    /**
     * Creates and configures a {@link FlatFileItemWriter} for writing {@link DataSet} records to a specified file.
     * The writer outputs data to a file location defined by the application configuration, with lines formatted
     * according to the specified {@link #createLineAggregator()}.
     *
     * <p>This writer is ideal for batch processing tasks that involve exporting structured data to a flat file,
     * commonly used in data transfer and reporting tasks within enterprise systems.</p>
     *
     * <p><b>Configuration:</b></p>
     * <ul>
     *     <li><b>File Path:</b> The output path is dynamically injected via {@code spring.application.output}, allowing
     *         the file location to be configurable for different environments.</li>
     *     <li><b>File Resource:</b> A {@link FileSystemResource} is created using the {@code outputPath} combined with
     *         {@code HelperConstants.FILE_NAME}, defining the full file path for the output file.</li>
     *     <li><b>Line Aggregator:</b> Configured with {@link #createLineAggregator()} to control the line formatting
     *         of each {@link DataSet} record, ensuring data consistency and readability.</li>
     * </ul>
     *
     * <p>This method is annotated with {@code @Bean}, allowing the writer to be managed within the Spring context
     * and injected where needed in the batch processing workflow.</p>
     *
     * @param outputPath the base output path specified by {@code spring.application.output}, which defines
     *                   the directory where the output file will be stored.
     * @return a configured {@link FlatFileItemWriter} for writing {@link DataSet} records to the specified file location.
     */
    @Bean
    protected FlatFileItemWriter<DataSet> flatFileItemWriter(@Value("${spring.application.output}") String outputPath) {

        return new FlatFileItemWriterBuilder<DataSet>()
                .name(HelperConstants.WRITER_NAME)
                .resource(new FileSystemResource(outputPath+HelperConstants.FILE_NAME))
                .lineAggregator(createLineAggregator())
                .build();
    }

    /**
     * Creates and configures a {@link DelimitedLineAggregator} for formatting {@link DataSet} records
     * as comma-separated values (CSV) in each output line.
     *
     * <p>This line aggregator is essential for exporting structured data where each {@link DataSet} record
     * is transformed into a single line of text, with fields separated by commas. This format is commonly
     * used in data export tasks across enterprise systems, enabling easy parsing and integration with other
     * applications and platforms.</p>
     *
     * <p><b>Configuration:</b></p>
     * <ul>
     *     <li><b>Field Extractor:</b> Uses a {@link BeanWrapperFieldExtractor} to extract specific fields
     *         from the {@link DataSet} object. The fields are defined by {@code HelperConstants.FIELD_EXTRACTOR_NAMES},
     *         ensuring consistency with domain-specific attributes.</li>
     *     <li><b>Delimiter:</b> Configured with a comma ({@code ,}) as the delimiter, aligning with standard CSV format,
     *         which facilitates integration with systems that consume CSV data.</li>
     * </ul>
     *
     * <p>This method is integral to the batch processing workflow, allowing each {@link DataSet} item to be
     * converted into a consistent text representation suitable for flat file writing.</p>
     *
     * @return a fully configured {@link DelimitedLineAggregator} for formatting {@link DataSet} records as CSV lines.
     * @throws Exception if the field extractor properties are not set correctly.
     */
    private DelimitedLineAggregator<DataSet> createLineAggregator() {
        BeanWrapperFieldExtractor<DataSet> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(HelperConstants.FIELD_EXTRACTOR_NAMES);
        fieldExtractor.afterPropertiesSet();
        DelimitedLineAggregator<DataSet> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    /**
     * Configures and returns a Spring Batch {@link Step} for transferring data to Google Cloud Storage (GCS).
     *
     * <p>This step utilizes the {@link TransferToGCSTasklet} to handle the actual transfer of data to GCS.
     * It is essential within the batch job workflow, ensuring data is reliably and securely moved to cloud storage.
     * This method leverages the {@link StepBuilder} for flexible step configuration, allowing transaction management
     * and tasklet invocation for file transfer operations in GCS.</p>
     *
     * <p><b>Configuration:</b></p>
     * <ul>
     *     <li><b>Job Repository:</b> The {@link JobRepository} maintains job execution states, ensuring
     *         transaction consistency and enabling state management for the step within the job lifecycle.</li>
     *     <li><b>Transaction Manager:</b> A {@link PlatformTransactionManager} controls transaction boundaries,
     *         ensuring data integrity during file transfers.</li>
     *     <li><b>Tasklet:</b> The {@link TransferToGCSTasklet} performs the file transfer to GCS and includes
     *         logic for secure and managed data handling.</li>
     * </ul>
     *
     * <p>This method is integral to building batch processing steps with managed, consistent transactions,
     * particularly for enterprise workloads involving data transfer to cloud storage.</p>
     *
     * @param jobRepository the {@link JobRepository} responsible for managing step states
     * @param transactionManager the {@link PlatformTransactionManager} to handle transaction boundaries
     * @param tasklet the {@link TransferToGCSTasklet} that executes the data transfer logic
     * @return a configured {@link Step} instance for data transfer to GCS
     */
    @Bean
    protected Step transferToGCSStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     TransferToGCSTasklet tasklet) {
        return new StepBuilder(HelperConstants.TRANSFER_TO_GCS_STEP, jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    /**
     * Configures and returns a Spring Batch {@link Step} for preprocessing data by reading from Google Cloud Storage (GCS),
     * processing the data, and writing the results to an output file.
     *
     * <p>This step is configured with a chunk-based processing model, enabling efficient handling of large datasets
     * by processing data in chunks of 500 records per transaction. It is designed for scalable, fault-tolerant
     * processing with integrated listeners for reader, writer, and step lifecycle management.</p>
     *
     * <p><b>Components:</b></p>
     * <ul>
     *     <li><b>Job Repository:</b> The {@link JobRepository} manages the persistence of job execution state, ensuring
     *         that the step can be restarted and its progress can be tracked as needed.</li>
     *     <li><b>Transaction Manager:</b> The {@link PlatformTransactionManager} controls transaction boundaries for
     *         chunk processing, maintaining data consistency and allowing rollback in case of errors.</li>
     *     <li><b>gcsItemReader:</b> An {@link ItemReader} implementation that reads input data from GCS, providing
     *         the raw data as {@code String} instances for further processing.</li>
     *     <li><b>readerListener:</b> A {@link GCSDataReaderListener} that monitors and manages reader events, enabling
     *         logging, validation, and error handling to meet enterprise data management standards.</li>
     *     <li><b>dataProcessor:</b> A {@link DataProcessor} that applies business logic and data transformations to
     *         convert the {@code String} input data into {@code DataSet} output records.</li>
     *     <li><b>flatFileItemWriter:</b> An {@link ItemWriter} that writes processed {@code DataSet} records to a file,
     *         outputting data in the specified format.</li>
     *     <li><b>writerListener:</b> A {@link FlatFileWriterListener} that monitors and manages writer events,
     *         logging write operations, and handling errors according to enterprise standards.</li>
     *     <li><b>stepListener:</b> A {@link PreProcessorStepListener} that handles step-level lifecycle events,
     *         logging the start, completion, and potential errors within the step execution.</li>
     * </ul>
     *
     * <p>This method establishes a highly configurable, enterprise-ready batch step that follows transactional
     * and state-management best practices while ensuring secure, traceable, and maintainable data processing.</p>
     *
     * @param jobRepository the {@link JobRepository} to track job state and manage persistence
     * @param transactionManager the {@link PlatformTransactionManager} to define transaction boundaries for chunk processing
     * @param gcsItemReader the {@link ItemReader} to read raw data from GCS
     * @param readerListener the {@link GCSDataReaderListener} to monitor and handle reader events
     * @param dataProcessor the {@link DataProcessor} to transform raw data into structured {@code DataSet} records
     * @param flatFileItemWriter the {@link ItemWriter} to write transformed data to an output file
     * @param writerListener the {@link FlatFileWriterListener} to monitor and handle writer events
     * @param stepListener the {@link PreProcessorStepListener} to handle step lifecycle events
     * @return a configured {@link Step} instance for preprocessing data from GCS and outputting transformed records
     */
    @Bean
    protected Step preProcessorStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    @Qualifier("gcsItemReader") ItemReader<String> gcsItemReader,
                                    GCSDataReaderListener readerListener,
                                    DataProcessor dataProcessor,
                                    @Qualifier("flatFileItemWriter") ItemWriter<DataSet> flatFileItemWriter,
                                    FlatFileWriterListener writerListener,
                                    PreProcessorStepListener stepListener) {
        return new StepBuilder(HelperConstants.STEP_NAME, jobRepository)
                .<String, DataSet>chunk(500, transactionManager)
                .reader(gcsItemReader)
                .listener(readerListener)
                .processor(dataProcessor)
                .writer(flatFileItemWriter)
                .listener(writerListener)
                .listener(stepListener)
                .build();
    }

    /**
     * Configures and returns a Spring Batch {@link Job} for preprocessing and transferring data to Google Cloud Storage (GCS).
     *
     * <p>This job is composed of two primary steps:</p>
     * <ul>
     *     <li><b>PreProcessor Step:</b> Reads data from GCS, processes the data, and writes transformed records to an output file.
     *     This step uses chunk processing for efficient handling of large datasets.</li>
     *     <li><b>Transfer to GCS Step:</b> Transfers the processed file to a specified GCS bucket for secure storage and
     *     further processing or analysis.</li>
     * </ul>
     *
     * <p>The job follows a sequential flow, starting with the pre-processing step, followed by the transfer step.
     * This configuration ensures that the data is preprocessed successfully before initiating the transfer, promoting
     * data integrity and process traceability.</p>
     *
     * <p><b>Enterprise Standards:</b></p>
     * <ul>
     *     <li><b>Modularity:</b> Each step is defined as a separate component, allowing for easier testing, reusability, and
     *     maintenance.</li>
     *     <li><b>Traceability:</b> The job is managed by a {@link JobRepository}, enabling job status tracking and
     *     restart capabilities in case of failure.</li>
     *     <li><b>Error Handling:</b> Each step can be configured with listeners for logging, error handling, and notifications,
     *     ensuring that issues are promptly addressed and can be analyzed.</li>
     * </ul>
     *
     * @param jobRepository the {@link JobRepository} to manage job state, persistence, and restart capabilities
     * @param preProcessorStep the {@link Step} responsible for reading, processing, and writing data locally
     * @param transferToGCSStep the {@link Step} responsible for transferring the processed data file to GCS
     * @return a configured {@link Job} instance that performs data preprocessing and secure transfer to GCS
     */
    @Bean
    protected Job preProcessorJob(JobRepository jobRepository,
                                  @Qualifier("preProcessorStep") Step preProcessorStep,
                                  @Qualifier("transferToGCSStep") Step transferToGCSStep) {
        return new JobBuilder(HelperConstants.JOB_NAME, jobRepository)
                .start(preProcessorStep)
                .next(transferToGCSStep)
                .build();
    }
}
