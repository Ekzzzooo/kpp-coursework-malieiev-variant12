package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * Фоновий імпорт каталогу обладнання з CSV (id;code;name;category;price_per_day).
 * Файл читається в UTF-8 поза потоком інтерфейсу, а кожен коректний рядок передається на сервер
 * через HttpRentalClient (POST /equipment). Task не працює з MySQL напряму і не змінює елементи GUI:
 * прогрес і текст стану передаються через updateProgress(...) та updateMessage(...).
 * Некоректний рядок пропускається з WARNING; помилка файлу, заголовка або недоступність сервера перериває імпорт.
 */
public class ImportEquipmentTask extends Task<Integer> {
    private static final Logger LOG = Logger.getLogger(ImportEquipmentTask.class.getName());
    private static final String HEADER = "id;code;name;category;price_per_day";
    private static final int FIELDS = 5;
    /**
     * Навчальна затримка між рядками: файл містить лише п'ять записів і без неї ProgressBar
     * заповнювався б миттєво. Для робочого імпорту великого файлу її потрібно встановити в 0.
     */
    private static final long DELAY_MS = 200;

    private final Path file;
    private final HttpRentalClient api;

    public ImportEquipmentTask(Path file, HttpRentalClient api) {
        this.file = file;
        this.api = api;
    }

    @Override
    protected Integer call() throws Exception {
        LOG.info("Початок імпорту CSV: " + file);
        updateMessage("Читання файлу...");
        if (!Files.isReadable(file)) {
            throw new NoSuchFileException(file.toString());
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !HEADER.equals(lines.get(0).replace("﻿", "").trim())) {
            throw new IllegalArgumentException("Некоректний заголовок CSV. Очікується: " + HEADER);
        }

        int total = 0;
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).trim().isEmpty()) {
                total++;
            }
        }
        if (total == 0) {
            updateMessage("Немає рядків для імпорту");
            updateProgress(0, 1);
            LOG.info("Імпорт завершено: у файлі немає рядків даних");
            return 0;
        }

        int processed = 0;
        int imported = 0;
        int skipped = 0;
        for (int i = 1; i < lines.size(); i++) {
            if (isCancelled()) {
                LOG.info("Імпорт скасовано користувачем. Імпортовано: " + imported);
                break;
            }
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] p = line.split(";", -1);
            if (p.length != FIELDS) {
                LOG.warning("Рядок " + (i + 1) + " пропущено: очікується " + FIELDS + " полів, знайдено " + p.length);
                skipped++;
            } else {
                try {
                    Equipment e = new Equipment();
                    e.setCode(p[1].trim());
                    e.setName(p[2].trim());
                    e.setCategory(p[3].trim());
                    e.setPricePerDay(Double.parseDouble(p[4].trim()));
                    api.addEquipment(e);
                    imported++;
                } catch (NumberFormatException ex) {
                    LOG.warning("Рядок " + (i + 1) + " пропущено: некоректна ціна '" + p[4] + "'");
                    skipped++;
                } catch (ApiException ex) {
                    if (ex.getStatus() == 0) {
                        LOG.severe("Сервер недоступний під час імпорту: " + ex.getMessage());
                        throw ex;
                    }
                    LOG.warning("Рядок " + (i + 1) + " відхилено сервером: " + ex.getMessage());
                    skipped++;
                }
            }
            processed++;
            updateProgress(processed, total);
            updateMessage("Оброблено рядків: " + processed + " з " + total);
            try {
                Thread.sleep(DELAY_MS);
            } catch (InterruptedException ex) {
                if (isCancelled()) {
                    break;
                }
                Thread.currentThread().interrupt();
                throw ex;
            }
        }
        updateMessage("Готово. Імпортовано: " + imported + ", пропущено: " + skipped);
        LOG.info("Імпорт завершено. Імпортовано: " + imported + ", пропущено: " + skipped);
        return imported;
    }
}
