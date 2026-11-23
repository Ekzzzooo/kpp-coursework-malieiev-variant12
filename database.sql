DROP DATABASE IF EXISTS equipment_rental;
CREATE DATABASE equipment_rental
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE equipment_rental;

-- Таблиця обладнання (каталог)
CREATE TABLE equipment (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           code VARCHAR(20) NOT NULL UNIQUE,
                           name VARCHAR(100) NOT NULL,
                           category VARCHAR(50) NOT NULL,
                           price_per_day DECIMAL(10,2) NOT NULL CHECK (price_per_day > 0),

                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця клієнтів
CREATE TABLE client (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        full_name VARCHAR(120) NOT NULL,
                        phone VARCHAR(20) NOT NULL,
                        email VARCHAR(100) NULL,
                        address VARCHAR(200) NULL,

                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця заявок на прокат (життєвий цикл: CREATED -> CONFIRMED -> ISSUED -> RETURNED)
-- f1 - терміновість початку (рахує сервер), f2 - попит, f3 - дефіцит одиниць
-- priority_index = 0.40*f1 + 0.35*f2 + 0.25*f3
CREATE TABLE rental_order (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              client_id BIGINT NOT NULL,
                              equipment_id BIGINT NOT NULL,
                              start_date DATE NOT NULL,
                              end_date DATE NOT NULL,
                              state ENUM('CREATED', 'CONFIRMED', 'ISSUED', 'RETURNED')
        NOT NULL DEFAULT 'CREATED',

                              f1 DECIMAL(5,2) NOT NULL DEFAULT 0 CHECK (f1 BETWEEN 0 AND 100),
                              f2 DECIMAL(5,2) NOT NULL CHECK (f2 BETWEEN 0 AND 100),
                              f3 DECIMAL(5,2) NOT NULL CHECK (f3 BETWEEN 0 AND 100),
                              priority_index DECIMAL(5,2) NOT NULL DEFAULT 0,
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CHECK (end_date >= start_date),
                              FOREIGN KEY (client_id) REFERENCES client(id),
                              FOREIGN KEY (equipment_id) REFERENCES equipment(id)
);

-- Таблиця договорів прокату (один договір на одну заявку)
CREATE TABLE rental_contract (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 order_id BIGINT NOT NULL UNIQUE,
                                 contract_number VARCHAR(30) NOT NULL UNIQUE,
                                 sign_date DATE NOT NULL,
                                 total_price DECIMAL(12,2) NOT NULL CHECK (total_price >= 0),

                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 FOREIGN KEY (order_id) REFERENCES rental_order(id)
);

INSERT INTO equipment (code, name, category, price_per_day) VALUES
                                                                ('EQ-001', 'Ноутбук Dell Latitude 5440', 'Комп''ютерна техніка', 250.00),
                                                                ('EQ-002', 'Відеокамера Sony FX30', 'Фото та відео', 600.00),
                                                                ('EQ-003', 'Мікшерний пульт Behringer X18', 'Аудіо', 150.00);

INSERT INTO client (full_name, phone, email, address) VALUES
                                                          ('Коваленко Олег Петрович', '+380501112233', 'kovalenko@example.com', 'м. Полтава, вул. Соборності, 10'),
                                                          ('Шевченко Ірина Андріївна', '+380677778899', 'shevchenko@example.com', 'м. Полтава, вул. Європейська, 5');

-- F1 = max(0; min(100; (14 - d) / 14 * 100)), d - днів від створення заявки до початку прокату
INSERT INTO rental_order
(client_id, equipment_id, start_date, end_date, state, f1, f2, f3, priority_index) VALUES
                                                                                       (1, 3, '2026-11-25', '2026-11-28', 'CONFIRMED', 78.57, 70.00, 60.00, 70.93),
                                                                                       (2, 2, '2026-12-01', '2026-12-05', 'CREATED',   35.71, 55.00, 45.00, 44.78),
                                                                                       (1, 2, '2026-11-10', '2026-11-14', 'RETURNED',  64.29, 40.00, 30.00, 47.22),
                                                                                       (2, 1, '2026-11-20', '2026-11-24', 'ISSUED',    71.43, 80.00, 90.00, 79.07);

-- total_price = кількість днів (включно) * ціна за день
INSERT INTO rental_contract (order_id, contract_number, sign_date, total_price) VALUES
                                                                                    (3, 'DG-2026-001', '2026-11-09', 3000.00),
                                                                                    (4, 'DG-2026-002', '2026-11-19', 1250.00);
