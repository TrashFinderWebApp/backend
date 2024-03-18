# 사용할 베이스 이미지 선택 (Java 17 Corretto)
FROM amazoncorretto:17

# 애플리케이션 파일을 컨테이너 내부로 복사하기 위한 디렉토리 생성
WORKDIR /app

# 빌드한 애플리케이션의 jar 파일을 컨테이너 내 /app 디렉토리로 복사
COPY build/libs/tfinder.jar app.jar

# 컨테이너가 구동될 때 실행될 명령어 지정
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
