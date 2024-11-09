FROM eclipse-temurin:21
WORKDIR /app
COPY build/libs/recommendations-0.0.1-SNAPSHOT.jar recommendations-preprocessing-job.jar
CMD ["java", "-jar", "recommendations-preprocessing-job.jar"]

