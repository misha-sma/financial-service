Инструкция по запуску из командной строки
1) выполнить mvn clean package
2) зайти в папку target
   cd target
3) запустить джарник fin-test-art-0.0.1-SNAPSHOT.jar
   java -jar fin-test-art-0.0.1-SNAPSHOT.jar
4) зайти в браузере на http://localhost:8080/
5) отправить реквест о финансовой операции
curl -X POST -H 'Content-Type: application/json' -d '{"description":"operation description","sum":12.56,"date":"2023-11-09T10:15:30"}' http://localhost:8080/api/addOperation
6) отправить реквест на загрузку курсов валют из центробанка
зайти в браузере на http://localhost:8080/api/loadCourses
7) получить джейсон с отчетом об операциях
зайти в браузере на http://localhost:8080/api/getOperations?startTime=2021-11-09T10:15:30&endTime=2024-11-09T10:15:30&course=EUR
8) получить описание апи через сваггер
зайти в браузере на http://localhost:8080/swagger-ui/index.html