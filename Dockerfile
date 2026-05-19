FROM eclipse-temurin:20

WORKDIR /app

COPY . .

RUN find ./src/main/java -name "*.java" > sources.txt

RUN javac -d out @sources.txt

CMD ["java", "-cp", "out", "sa.edu.kau.fcit.cpit252.project.App"]