# WITSML Loader
Загрузчик данных из WITSML-совместимых XML-файлов в базу данных PostgreSQL на Java.

## Описание
Программа автоматически парсит XML-файлы с данными о скважинах, стволах, логах и параметрах замеров и загружает их в соответствующие таблицы PostgreSQL (`wells`, `wellbores`, `logs`, `log_data`).  
Все связи между таблицами поддерживаются автоматически — программа сначала добавляет недостающие скважины и стволы, а затем — сами логи и измерения.

## Требования
- Java 11+
- Maven 3.x
- PostgreSQL 10+
- Настроенная база данных с нужными таблицами

## Сборка проекта
mvn clean package
После сборки в папке target/ появится файл witsml-loader-1.0-SNAPSHOT.jar.

## Настройка
Перед запуском необходимо указать параметры подключения к БД в файле application.properties в src/main/resources/:

db.url=jdbc:postgresql://localhost:5432/your_database
db.user=your_db_user
db.password=your_db_password

## Запуск
java -jar target/witsml-loader-1.0-SNAPSHOT.jar путь/к/файлу1.xml путь/к/файлу2.xml ...
Можно указывать сразу несколько XML-файлов (каждый отдельным аргументом), если в пути к файлу есть пробелы, путь нужно обернуть в кавычки

Для каждого файла будет произведена попытка загрузки всех данных.

Пример запуска: java -jar target/witsml-loader-1.0-SNAPSHOT.jar "C:\Oildata\WITSML Realtime drilling data\Norway-Statoil-NO 15_$47$_9-F-15\1\log\2\2\1\00001.xml" 
