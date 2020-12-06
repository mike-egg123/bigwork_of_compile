FROM java:11
WORKDIR /app/
COPY ./* ./
RUN javac src/test/AnalyserTest.java
RUN chmod +x src/test/AnalyserTest
