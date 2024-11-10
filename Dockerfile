FROM eclipse-temurin:21
WORKDIR /app
RUN mkdir -p /data/out && chmod -R 777 /data/out
COPY build/libs/recommendations-0.0.1-SNAPSHOT.jar recommendations-preprocessing-job.jar
CMD ["java", "-jar", "recommendations-preprocessing-job.jar"]

