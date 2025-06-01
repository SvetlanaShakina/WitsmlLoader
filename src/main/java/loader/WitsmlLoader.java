package loader;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import model.Logs;
import model.Log;

import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WitsmlLoader {
    public static void main(String[] args) throws Exception {
        // Проверяем, что аргументы переданы
        if (args.length == 0) {
            System.out.println("Укажите xml-файлы для загрузки, например: java -jar witsml-loader.jar file1.xml file2.xml");
            return;
        }

        // 1. Загрузка настроек для подключения к БД
        Properties props = new Properties();
        try (InputStream input = WitsmlLoader.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Файл application.properties не найден в ресурсах!");
                return;
            }
            props.load(input);
        }
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        // 2. Создаём маппер для XML (один раз)
        XmlMapper xmlMapper = new XmlMapper();

        // 3. Открываем соединение к БД один раз на все файлы
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false); // Для ускорения загрузки

            String insertLogSQL = "INSERT INTO logs " +
                    "(uid, wellbore_uid, name, service_company, start_time, end_time, index_type, creation_date, index_curve, direction) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (uid) DO NOTHING";

            String insertSQL = "INSERT INTO log_data (log_uid, time_index, mnemonic, value) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
            try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

                // 4. Для каждого файла из аргументов
                for (String fileName : args) {
                    System.out.println("Обработка файла: " + fileName);
                    try {
                        Logs logsRoot = xmlMapper.readValue(new File(fileName), Logs.class);
                        if (logsRoot.log == null || logsRoot.log.isEmpty()) {
                            System.out.println("Нет данных log в файле " + fileName);
                            continue;
                        }
                        // Для каждого лога (обычно один)
                        for (Log log : logsRoot.log) {
                            String checkWellSQL = "SELECT COUNT(*) FROM wells WHERE uid = ?";
                            String insertWellSQL = "INSERT INTO wells (uid, name) VALUES (?, ?) ON CONFLICT DO NOTHING";


                            try (
                                    PreparedStatement checkWellStmt = conn.prepareStatement(checkWellSQL);
                                    PreparedStatement insertWellStmt = conn.prepareStatement(insertWellSQL)
                            ) {
                                checkWellStmt.setString(1, log.well_uid);
                                ResultSet rs = checkWellStmt.executeQuery();
                                rs.next();
                                int count = rs.getInt(1);
                                if (count == 0) {
                                    insertWellStmt.setString(1, log.well_uid);
                                    insertWellStmt.setString(2, log.nameWell);
                                    insertWellStmt.executeUpdate();
                                }
                            }

                            String checkWellboreSQL = "SELECT COUNT(*) FROM wellbores WHERE uid = ? AND well_uid = ?";
                            String insertWellboreSQL = "INSERT INTO wellbores (uid, well_uid, name) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";
                            try (
                                    PreparedStatement checkStmt = conn.prepareStatement(checkWellboreSQL);
                                    PreparedStatement insertStmt = conn.prepareStatement(insertWellboreSQL)
                            ) {
                                checkStmt.setString(1, log.wellbore_uid);
                                checkStmt.setString(2, log.well_uid);
                                ResultSet rs = checkStmt.executeQuery();
                                rs.next();
                                int count = rs.getInt(1);
                                if (count == 0) {
                                    insertStmt.setString(1, log.wellbore_uid);
                                    insertStmt.setString(2, log.well_uid);
                                    insertStmt.setString(3, log.nameWellbore);
                                    insertStmt.executeUpdate();
                                }
                            }

                            try (PreparedStatement logStmt = conn.prepareStatement(insertLogSQL)) {
                                logStmt.setString(1, log.uid);
                                logStmt.setString(2, log.wellbore_uid);
                                logStmt.setString(3, log.name);
                                logStmt.setString(4, log.service_company);
                                logStmt.setTimestamp(5, safeTimestamp(log.start_time));
                                logStmt.setTimestamp(6, safeTimestamp(log.end_time));
                                logStmt.setString(7, log.index_type);
                                logStmt.setTimestamp(8, safeTimestamp(log.creation_date));
                                logStmt.setString(9, log.index_curve);
                                logStmt.setString(10, log.direction);                 
                                logStmt.executeUpdate();
                            }

                            // Получаем список мнемоник
                            List<String> mnemonics = Arrays.asList(log.logData.mnemonicList.split(","));

                            // Для каждой строки данных
                            for (String dataLine : log.logData.data) {
                                String[] values = dataLine.split(",", -1);
                                String indexTime = values[0];

                                // Вычисляем timestamp только один раз для строки
                                Timestamp timestamp = null;
                                try {
                                    OffsetDateTime odt = OffsetDateTime.parse(indexTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                                    timestamp = Timestamp.from(odt.toInstant());
                                } catch (Exception e) {
                                    try {
                                        timestamp = Timestamp.valueOf(indexTime.replace("T", " ").replace("Z", ""));
                                    } catch (Exception ignored) {}
                                }
                                if (timestamp == null) continue; // если дата не разобралась — пропускаем строку

                                for (int i = 1; i < values.length && i < mnemonics.size(); i++) {
                                    String value = values[i];
                                    String mnemonic = mnemonics.get(i);
                                    if (value == null || value.isEmpty()) continue;
                                    try {
                                        double doubleValue = Double.parseDouble(value);
                                        stmt.setString(1, log.uid);
                                        stmt.setTimestamp(2, timestamp);
                                        stmt.setString(3, mnemonic);
                                        stmt.setDouble(4, doubleValue);
                                        stmt.addBatch();
                                    } catch (NumberFormatException nfe) {
                                        System.out.println("Пропущено нечисловое значение: " + value + " для " + mnemonic);
                                    }
                                }
                            }
                        }
                        System.out.println("Файл " + fileName + " обработан.");
                    } catch (Exception ex) {
                        System.out.println("Ошибка при обработке файла " + fileName + ": " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                stmt.executeBatch();
                conn.commit();
            }
            System.out.println("Загрузка завершена!");
        }
    }

    private static Timestamp safeTimestamp(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            OffsetDateTime odt = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return Timestamp.from(odt.toInstant());
        } catch (Exception e) {
            try {
                return Timestamp.valueOf(value.replace("T", " ").replace("Z", ""));
            } catch (Exception ignored) {}
        }
        return null;
    }
}