# Інформаційна система прокату обладнання (варіант №12)

Клієнт–серверна система для обліку обладнання, клієнтів, заявок на прокат і договорів. Заявка проходить чотири стани
(`CREATED → CONFIRMED → ISSUED → RETURNED`), а її пріоритет розраховується за інтегральним показником
`I = 0,40·F1 + 0,35·F2 + 0,25·F3`. Через JavaFX-клієнт можна переглядати дані, виконувати CRUD-операції для всіх
сутностей, змінювати стан заявки, оформлювати договори та імпортувати каталог обладнання з CSV.

Основні сутності: **Equipment** (обладнання), **Client** (клієнт), **RentalOrder** (заявка на прокат), **RentalContract** (договір).

## Технології

Java 21, Maven, JavaFX 21, Servlet API 4.0.1 (javax) + Apache Tomcat 9.x, JDBC + MySQL (Connector/J 9.7.0),
Jackson 2.18.2, JUnit 5.11.4, Git. Клієнт взаємодіє із сервером лише через HTTP/JSON; прямого доступу до MySQL
у клієнта немає.

## Структура репозиторію

```
README.md            – цей файл
.gitignore           – виключення target, .idea, журналів, .env
database.sql         – створення БД equipment_rental, таблиць і демонстраційних даних
server/              – серверна частина (WAR): Servlet, Service, DAO, моделі, тести
  src/main/java/ua/edu/nupp/kpp/server/
  src/main/webapp/WEB-INF/web.xml
  src/test/java/ua/edu/nupp/kpp/server/   – JUnit 5
client/              – JavaFX-клієнт
  src/main/java/ua/edu/nupp/kpp/client/
  sample-data/equipment.csv               – приклад для CSV-імпорту
```

## Підготовка бази даних

1. Запустіть MySQL (наприклад, через XAMPP Control Panel).
2. Відкрийте `http://localhost/phpmyadmin`, на вкладці «Імпорт» виконайте файл `database.sql`
   (або виконайте його вміст на вкладці SQL).
3. Переконайтеся, що створено базу `equipment_rental` з таблицями `equipment`, `client`, `rental_order`, `rental_contract`.

Параметри підключення (`DB_URL`, `DB_USER`, `DB_PASSWORD`) задаються змінними середовища; за відсутності використовуються
локальні значення за замовчуванням з `DBUtil.java` (`root` без пароля). Реальні паролі в репозиторій не додаються.

## Запуск сервера

```
cd server
mvn clean test
mvn clean package
```

Скопіюйте `server/target/equipment-rental-server.war` до каталогу `webapps` Apache Tomcat 9.x і запустіть Tomcat
(`bin/catalina.bat run`). Перевірка: у браузері `http://localhost:8080/equipment-rental-server/equipment` повертає JSON.
Інші маршрути: `/clients`, `/orders`, `/contracts`. Видалення, додавання й редагування виконуються запитами POST
(`action=delete`, `action=transition&state=...` для зміни стану заявки).

## Запуск JavaFX-клієнта

Перед запуском мають працювати MySQL і Tomcat із розгорнутим WAR.

```
cd client
mvn javafx:run
```

Клієнт звертається за адресою `http://localhost:8080/equipment-rental-server`. Запускати його потрібно з каталогу
`client`, оскільки шлях до CSV відносний (`sample-data/equipment.csv`).

## Тестування

JUnit 5-тести розташовані в `server/src/test/java` і запускаються командою `cd server` → `mvn clean test`.
Перевіряються нормування фактора F1, інтегральний показник, дозволені й заборонені переходи життєвого циклу,
валідація моделей та розрахунок вартості договору. MySQL і Tomcat для цих тестів не потрібні.

## CSV-імпорт

Файл `client/sample-data/equipment.csv` (UTF-8, роздільник `;`) має заголовок `id;code;name;category;price_per_day`
і п'ять полів у кожному рядку; десятковий роздільник — крапка. Імпорт запускається на вкладці «Обладнання» кнопкою
«Імпортувати CSV» і виконується у фоновому потоці (JavaFX `Task`) з індикатором прогресу; кожен запис передається серверу
через HTTP. Некоректні рядки пропускаються з записом у журнал. Повторний імпорт того самого файлу не додає дублікатів,
оскільки код обладнання унікальний: сервер відхиляє такі рядки.
