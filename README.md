# OpenBanking API

Тестове завдання простого RESTful API для роботи з банківськими рахунками та платежами, що реалізує стандарт PSD2/OpenBanking. Проєкт демонструє інтеграцію із зовнішнім банківським
API та обробку фінансових транзакцій.

## Про проєкт

OpenBanking API - це backend-додаток, що дозволяє:
- Отримувати баланс рахунку
- Переглядати останні транзакції
- Ініціювати IBAN-to-IBAN платежі

Проєкт взаємодіє з mock-сервісом зовнішнього банку для отримання даних рахунків та обробки платежів.

## Функціонал

### Account API
- **GET** `/api/accounts/{iban}/balance` - отримання балансу рахунку
- **GET** `/api/accounts/{iban}/transactions` - отримання останніх 10 транзакцій

### Payment API
- **POST** `/api/payments/initiate` - ініціація платежу з перевіркою балансу

### Ключові особливості
- Валідація вхідних даних
- Перевірка достатності коштів перед платежем
- Збереження історії платежів у БД
- Обробка помилок зовнішнього API
- Відкат статусу платежу при збоях
- OpenAPI/Swagger документація
- Unit тести

## Технології

- **Java 17**
- **Spring Boot 3.3.5**
- **Spring Data JPA** - робота з базою даних
- **PostgreSQL** - основна СУБД
- **Liquibase** - міграції бази даних
- **RestTemplate** - HTTP клієнт
- **Lombok** - зменшення boilerplate коду
- **Spring Validation** - валідація даних
- **OpenAPI/Swagger** - документація API
- **JUnit 5** - тестування
- **Mockito** - моки для unit тестів
- **Gradle** - система збірки

## Швидкий старт

### Вимоги

- Java 17+
- PostgreSQL 15+
- Gradle 8+

### Налаштування бази даних

CREATE DATABASE openbanking_db;

### Конфігурація

Створіть свій файл `application.yaml` або створіть файл .env та додайте такі поля:

DB_URL= ваш url бази данних

DB_USERNAME= ваше ім'я бази данних

DB_PASSWORD= ваш пароль для бази данних

## Запуск

### Клонування репозиторію
git clone 
cd openbanking-api

### Збірка проєкту
./gradlew build

### Запуск
./gradlew bootRun

Додаток запуститься на `http://localhost:8080`

### Swagger UI

http://localhost:8080/swagger-ui.html