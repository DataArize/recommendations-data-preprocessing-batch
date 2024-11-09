package com.dataarize.recommendations.processor;

import com.dataarize.recommendations.constants.HelperConstants;
import com.dataarize.recommendations.model.DataSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Processes each line of input data by associating it with a specific movie ID and transforming it into a {@link DataSet} object.
 * This processor is used as part of a Spring Batch job to prepare data for output by parsing movie IDs and related data fields.
 *
 * <p><b>Functionality:</b></p>
 * <ul>
 *     <li>Detects lines that contain only a movie ID (ending with a colon) and assigns the ID for subsequent data records.</li>
 *     <li>Parses each line of data associated with a movie, extracting user ID, rating, and date, then maps it to a {@link DataSet} object.</li>
 * </ul>
 *
 * <p><b>Enterprise Standards:</b></p>
 * <ul>
 *     <li><b>Modular Processing:</b> The implementation uses the {@link ItemProcessor} interface, enabling seamless integration with
 *     Spring Batch and allowing for isolated processing logic.</li>
 *     <li><b>Contextual Information:</b> Stores the output path in the {@link org.springframework.batch.item.ExecutionContext} for reference across batch components,
 *     improving traceability and maintainability.</li>
 *     <li><b>Error Handling:</b> If any processing errors occur, the system logs these using {@link Slf4j} for efficient monitoring and debugging.</li>
 * </ul>
 *
 * @see ItemProcessor
 * @see StepExecutionListener
 */
@Slf4j
@Component
public class DataProcessor implements ItemProcessor<String, DataSet>, StepExecutionListener {

    private String movieId;
    @Value("${spring.application.output}")
    private String outputPath;

    /**
     * Before the step starts, stores the temporary output path in the {@link org.springframework.batch.item.ExecutionContext}.
     *
     * @param stepExecution the context of the current batch step
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        stepExecution.getJobExecution().getExecutionContext().put(HelperConstants.TEMP_PATH, outputPath+HelperConstants.FILE_NAME);
    }

    /**
     * Processes each line of input data by determining if it is a movie ID or a data record.
     * <p>If the line ends with a colon, it is considered a movie ID, which will be associated with the subsequent records.
     * Otherwise, the line is split into user ID, rating, and date fields, which are mapped to a {@link DataSet} object.</p>
     *
     * @param item the input string representing a line of data
     * @return a {@link DataSet} object if the line represents a data record, or {@code null} if it is a movie ID
     * @throws Exception if any processing error occurs
     */
    @Override
    public DataSet process(String item) throws Exception {
        if(item.endsWith(HelperConstants.COLON)) {
            movieId = item.replace(HelperConstants.COLON, HelperConstants.EMPTY_SPACE).trim();
            return null;
        }
        String[] data = item.split(HelperConstants.COMMA);
        return new DataSet(movieId, data[0], data[1], data[2]);
    }
}
