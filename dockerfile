# 기존의 Amazon Corretto 이미지를 기반으로 설정
FROM amazoncorretto:17

# 작업 디렉토리를 /app로 설정
WORKDIR /app

# 빌드된 jar 파일과 application-dev.yaml 파일을 이미지 내로 복사
COPY build/libs/tfinder.jar app.jar
COPY backend_env/yml/application-dev.yaml application-dev.yaml

# 애플리케이션이 사용할 포트를 지정 (예: 8080)
EXPOSE 8080

# 애플리케이션 실행. 외부 설정 파일 위치를 지정
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=application-dev.yaml"]
