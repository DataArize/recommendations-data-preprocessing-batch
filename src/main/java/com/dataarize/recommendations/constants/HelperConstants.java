package com.dataarize.recommendations.constants;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public final class HelperConstants {

    public static final String COLON = ":";
    public static final String EMPTY_SPACE = "";
    public static final String COMMA = ",";
    public static final String RUN_DATE_TIME = "RUN_DATE_TIME";
    public static final String INPUT_FILE_PATH = "INPUT_FILE_PATH";
    public static final String JOB_NAME = "PRE PROCESSOR BATCH - " + LocalDateTime.now();
    public static final String STEP_NAME = "PRE PROCESSOR STEP";
    public static final String READER_NAME = "GCS Item Reader";
    public static final String END_OF_LINE_CHARACTER = "\r\n";
    public static final String TOKENIZER_NAME = "line";
    public static final String WRITER_NAME = "GCS Item Writer";
    public static final String FILE_NAME = "Dataset_"+LocalDateTime.now()+".txt";
    public static final String[] FIELD_EXTRACTOR_NAMES = new String[] {"movieId", "customerId", "ratings", "date"};
    public static final String OUTPUT_BUCKET_PATH = "OUTPUT_BUCKET_PATH";
    public static final String TEMP_PATH = "TEMP_PATH";
    public static final String TRANSFER_TO_GCS_STEP = "Transfer to GCS Step";
    public static final String TARGET_DIRECTORY = "output/";
}
