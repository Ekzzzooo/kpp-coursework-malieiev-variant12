package ua.edu.nupp.kpp.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Початкова (послідовна) частина CSV-імпорту: читає файл, пропускає заголовок,
 * розбирає один рядок і передає його серверу через HTTP-клієнт.
 * Формат: id;code;name;category;price_per_day (id ігнорується, його призначає MySQL).
 * Фоновий імпорт усього файлу (Task, ProgressBar) буде реалізовано на етапі 4.
 */
public class CsvImporter {
    private final HttpRentalClient api;

    public CsvImporter(HttpRentalClient api) {
        this.api = api;
    }

    /** Імпортує перший рядок даних (після заголовка) і повертає створений на сервері запис. */
    public Equipment importFirstRow(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).replace("﻿", "").trim();
            if (!line.isEmpty()) {
                return api.addEquipment(parse(line, i + 1));
            }
        }
        throw new IllegalArgumentException("У файлі немає рядків даних");
    }

    /** Розбирає один рядок CSV у клієнтську модель; некоректний рядок не створює модель. */
    Equipment parse(String line, int lineNumber) {
        String[] p = line.split(";");
        if (p.length != 5) {
            throw new IllegalArgumentException("Рядок " + lineNumber + ": очікується 5 полів, знайдено " + p.length);
        }
        Equipment e = new Equipment();
        e.setCode(p[1].trim());
        e.setName(p[2].trim());
        e.setCategory(p[3].trim());
        try {
            e.setPricePerDay(Double.parseDouble(p[4].trim().replace(',', '.')));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Рядок " + lineNumber + ": некоректна ціна '" + p[4] + "'");
        }
        return e;
    }
}
