package com.dataarize.recommendations;

import com.dataarize.recommendations.constants.HelperConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Main entry point for the Spring Boot application that launches a batch job for data preprocessing.
 * <p>
 * This class is responsible for initializing and launching the Spring Batch job, {@link Job preProcessorJob},
 * which processes data and stores the results in Google Cloud Storage (GCS). It also validates that the correct
 * arguments (GCS bucket paths) are provided and logs any errors if the arguments are missing or incorrect.
 * </p>
 *
 * <p><b>Key Functionality:</b></p>
 * <ul>
 *     <li>Uses Spring Batch's {@link JobLauncher} to start a predefined batch job.</li>
 *     <li>Validates and processes input arguments for GCS file paths.</li>
 *     <li>Sets up and passes job parameters dynamically based on the runtime input arguments.</li>
 *     <li>Logs execution status and exits with appropriate codes based on job success or failure.</li>
 * </ul>
 *
 * <p><b>Enterprise Standards Considerations:</b></p>
 * <ul>
 *     <li><b>Logging:</b> Uses {@link Slf4j} for structured, consistent logging, providing visibility into job execution.</li>
 *     <li><b>Error Handling:</b> Logs critical errors and exits with the appropriate status code in case of missing arguments or job failure.</li>
 *     <li><b>Job Execution Monitoring:</b> Ensures that job execution status is checked, and the application exits based on the success or failure of the job.</li>
 * </ul>
 *
 * @see CommandLineRunner
 * @see Job
 * @see JobLauncher
 * @see JobExecution
 */
@Slf4j
@SpringBootApplication
public class RecommendationsApplication implements CommandLineRunner {


	private final Job preProcessorJob;
	private final JobLauncher jobLauncher;

	/**
	 * Constructor that initializes the {@link Job preProcessorJob} and {@link JobLauncher jobLauncher}.
	 *
	 * @param preProcessorJob the Spring Batch job that processes the data
	 * @param jobLauncher the job launcher that starts the job execution
	 */
	@Autowired
    public RecommendationsApplication(@Qualifier("preProcessorJob") Job preProcessorJob,
									  JobLauncher jobLauncher) {
        this.preProcessorJob = preProcessorJob;
        this.jobLauncher = jobLauncher;
    }

	/**
	 * The main entry point for the application. Starts the Spring Boot application and triggers the batch job
	 * based on the provided arguments.
	 *
	 * @param args command-line arguments provided to the application. Expects:
	 *             - args[0] = GCS input file path
	 *             - args[1] = GCS output bucket path
	 */
    public static void main(String[] args) {
		SpringApplication.run(RecommendationsApplication.class, args);
	}


	/**
	 * Runs the batch job using the provided command-line arguments. If the arguments are insufficient,
	 * an error message is logged, and the application exits with a non-zero status code.
	 *
	 * <p>This method constructs {@link JobParameters} with the required inputs and launches the job.
	 * It handles failure scenarios by logging an error and terminating with the appropriate exit code.</p>
	 *
	 * @param args command-line arguments passed during the application run
	 * @throws Exception if an error occurs during job execution
	 */
	@Override
	public void run(String... args) throws Exception {

		if(args.length < 2) {
			log.error("Please provide the GCS bucket path for the input file");
			System.exit(-1);
		}

		JobParameters jobParameters = new JobParametersBuilder()
				.addLocalDateTime(HelperConstants.RUN_DATE_TIME, LocalDateTime.now())
				.addString(HelperConstants.INPUT_FILE_PATH, args[0])
				.addString(HelperConstants.OUTPUT_BUCKET_PATH, args[1])
				.toJobParameters();

		JobExecution jobExecution = jobLauncher.run(preProcessorJob, jobParameters);
		if(jobExecution.getStatus().isUnsuccessful()) System.exit(-1);
		System.exit(0);

	}
}
