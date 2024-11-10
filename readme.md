# Recommendations Data Preprocessing Batch

This repository contains a Spring Batch application responsible for reading raw data from Google Cloud Storage (GCS), preprocessing it, and generating a flat file that is pushed back to GCS. This preprocessed file will later be consumed by a separate Dataflow pipeline for transformation and loading into BigQuery. The ultimate goal is to build a Netflix-like recommendation engine using Vertex AI and other GCP tools. This preprocessing step is essential for cleaning and structuring the data before further transformation.

## Table of Contents

1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Setup Instructions](#setup-instructions)
4. [Configuration](#configuration)
5. [Usage](#usage)
6. [Contributing](#contributing)
7. [Licenses](#licenses)
8. [Contact Information](#contact-information)

## Project Overview

This project is the first step in building a robust recommendation engine using Google Cloud technologies. The Spring Batch application reads raw data from GCS, processes it by cleaning and formatting the data, and then outputs a preprocessed flat file to GCS. This preprocessed data will be ingested by a Dataflow pipeline for further transformations and loading into BigQuery for analysis.

### Key Components:

- **Spring Batch**: Framework used for managing the batch job that performs the preprocessing.
- **Google Cloud Storage (GCS)**: Input and output data is stored in GCS buckets.
- **Flat File Output**: The application generates a flat file for downstream processing.
- **Vertex AI**: The end goal of this project is to build a recommendation engine using machine learning models in Vertex AI.

## Features

- **Data Preprocessing**: Cleans and structures raw data for downstream processing.
- **Spring Batch**: Efficient batch processing of large datasets.
- **GCS Integration**: Reads and writes data to and from Google Cloud Storage.
- **Flat File Generation**: Creates a flat file for downstream transformation and loading into BigQuery.
- **Error Handling and Logging**: Comprehensive logging and error handling with SLF4J.

## Getting Started

### Prerequisites

Ensure you have the following tools and configurations before starting:

- **Google Cloud Account**: Ensure you have access to Google Cloud Storage and the necessary permissions.
- **Spring Boot**: For running the Spring Batch application.
- **Maven**: To build and run the application.
- **JDK 11 or higher**: The application is built with Java.
- **GCS Bucket**: A GCS bucket to store the raw input data and the output files.

### Setup Instructions

1. **Clone the repository**:
   ```bash
   git clone https://github.com/DataArize/recommendations-data-preprocessing-batch.git
   cd recommendations-data-preprocessing-batch
   ```
2. **Install dependencies**:Ensure all necessary dependencies are installed by running the following command:
    ```bash
    gradle clean build
    ```
3. 3. **Configure GCS Buckets**:
      Set up your Google Cloud Storage bucket for both input and output data:
    - **Input Bucket**: Where the raw data resides.
    - **Output Bucket**: Where the preprocessed flat file will be written.

4. **Configure Application Properties**:
   Update the `application.properties` file with the appropriate GCS bucket names, file paths, and other configurations like the Cloud Storage credentials.
   Example configuration:
    ```properties
    spring.cloud.gcp.storage.bucket.input=your-input-bucket-name
    spring.cloud.gcp.storage.bucket.output=your-output-bucket-name
    spring.batch.job.enabled=true
    ```
5. **Run the Application**:
   Run the Spring Batch application to start preprocessing the data. You can start the job by running the following Gradle command:
    ```bash
   gradle bootRun

    ```
6. **Verify Output**:
      After running the batch job, verify that the preprocessed flat file is successfully uploaded to the output GCS bucket.

## Configuration

### GCS Input and Output Buckets
Make sure to specify the correct GCS bucket names in the `application.properties` file.

- **Input Bucket**: The GCS bucket where the raw data resides.
- **Output Bucket**: The GCS bucket where the preprocessed flat file will be saved.

### Batch Job Parameters
The Spring Batch application can be configured with the following parameters:

- `spring.batch.job.enabled`: Set to `true` to enable the batch job.
- `spring.cloud.gcp.storage.bucket.input`: Set the input GCS bucket name.
- `spring.cloud.gcp.storage.bucket.output`: Set the output GCS bucket name.

### Error Handling
The application includes logging and error handling for the batch job. In case of errors (e.g., missing input files, processing errors), appropriate error messages will be logged.

## Usage
Once the Spring Batch application is running, it will:

1. Fetch data from the specified input GCS bucket.
2. Preprocess the data (cleaning, formatting, etc.).
3. Output the preprocessed data to the specified output GCS bucket as a flat file.

This output file can then be consumed by a separate Dataflow pipeline for further transformation and analysis in BigQuery.

## Contributing
We welcome contributions from the community! To contribute, follow these steps:

1. Fork the repository.
2. Create a new branch for your feature or bug fix (`git checkout -b feature/your-feature-name`).
3. Make your changes and commit them (`git commit -m 'Add new feature'`).
4. Push your changes to your fork (`git push origin feature/your-feature-name`).
5. Open a Pull Request to the main branch.

Please ensure that your code follows the existing coding standards and includes appropriate tests.

## Licenses
This project is licensed under the MIT License - see the LICENSE file for details.

## Contact Information
For further information, contact us at:
- **Email**: amiths@dataarize.com