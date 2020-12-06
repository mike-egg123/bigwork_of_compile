FROM openjdk:11
WORKDIR /app/
COPY ./* ./
RUN javac -encoding UTF-8 *.java
