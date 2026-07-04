FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

COPY mvnw .
COPY .mvn/ .mvn
COPY pom.xml .
COPY src/ ./src

RUN chmod +x mvnw
RUN ./mvnw clean install -DskipTests

# Database Configuration - WITHOUT username/password in URL
ENV SPRING_DATASOURCE_URL="jdbc:postgresql://ep-silent-leaf-aosufnal-pooler.c-2.ap-southeast-1.aws.neon.tech:5432/neondb?sslmode=require"
ENV SPRING_DATASOURCE_USERNAME="neondb_owner"
ENV SPRING_DATASOURCE_PASSWORD="npg_S2ZeAawdO7Cr"
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.postgresql.Driver"

# JPA Configuration
ENV SPRING_JPA_HIBERNATE_DDL_AUTO="update"
ENV SPRING_JPA_SHOW_SQL="false"
ENV SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL="false"

# Email Configuration
ENV SPRING_MAIL_HOST="smtp.gmail.com"
ENV SPRING_MAIL_PORT="587"
ENV SPRING_MAIL_USERNAME="shantanukumbhar310@gmail.com"
ENV SPRING_MAIL_PASSWORD="vjxv altu swar otcg"
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH="true"
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE="true"

# Cloudinary
ENV CLOUDINARY_URL="cloudinary://544913587722279:we-kLLF_mAzIgKP6IWXuQd5V5cUaDevzgyotn"

# JWT
ENV JWT_SECRET="9a4f2c8d3e1b7a6c5f8e3d2a1b4c7f9e8d5b2a6c3f7e9d1b5c8a2f4e7d9b1c3"

# Server
ENV SERVER_PORT="8080"
ENV SERVER_TOMCAT_MAX_PART_COUNT="500"

# Other Config
ENV ADMIN_WHATSAPP_NUMBER="918767739911"
ENV SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE="20MB"
ENV SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE="20MB"
ENV SPRING_THYMELEAF_CACHE="false"

# Java Options
ENV JAVA_OPTS="-Xmx256m -Xss512k -XX:MaxMetaspaceSize=100m -XX:+UseContainerSupport -XX:MaxRAMPercentage=70.0"

EXPOSE 8080

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar target/*.jar"]