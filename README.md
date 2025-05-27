# ☁️ Cloud File Storage

![Java](https://img.shields.io/badge/Java-21%2B-red.svg?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-24%2B-blue.svg?logo=docker&logoColor=white)

Многофункциональное облачное хранилище файлов с надежным REST API и интуитивно понятным веб-интерфейсом для управления. Проект вдохновлен функциональностью Google Drive и предоставляет безопасную и эффективную платформу для хранения данных.

## ✨ Ключевые особенности

- **Управление пользователями:** Простая регистрация, аутентификация и авторизация.
- **Операции с файлами:**
    - Загрузка и скачивание файлов и папок (папки скачиваются как ZIP-архивы).
    - Управление файловой системой: удаление, переименование, перемещение и копирование.
    - Поддержка рекурсивной загрузки папок для удобной передачи больших объемов данных.
- **Расширенный поиск:** Быстрый поиск файлов и папок по имени.
- **Масштабируемое хранилище:** Интеграция с MinIO для надежного и совместимого с S3 объектного хранилища.
- **Управление сессиями:** Использование Redis для масштабируемого управления пользовательскими сессиями.
- **Интерактивная документация API:** Автоматически генерируемая документация через Swagger.

## 🛠 Технологии и инструменты

Современный стек технологий обеспечивает высокую производительность, масштабируемость и удобство поддержки.

### Backend

| Категория         | Технология/Инструмент | Описание                                                                 | Иконка                                                                                              |
|-------------------|-----------------------|--------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| **Ядро**          | Java                  | Основной язык программирования, обеспечивающий надежность и кроссплатформенность. | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/java/java-original-wordmark.svg" width="40" height="40" alt="Java"/> |
|                   | Maven                 | Инструмент автоматизации сборки, управление зависимостями и жизненным циклом. | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/maven/maven-original-wordmark.svg" width="40" height="40" alt="Maven"/> |
| **Фреймворки**    | Spring Boot           | Упрощает настройку приложений на Spring для быстрой разработки.           | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/spring/spring-original-wordmark.svg" width="40" height="40" alt="Spring Boot"/> |
|                   | Spring Security       | Мощная и настраиваемая аутентификация и контроль доступа.                | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/spring/spring-original-wordmark.svg" width="40" height="40" alt="Spring Security"/> |
|                   | Spring Session        | Управление HTTP-сессиями в распределенной среде с поддержкой Redis.      | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/spring/spring-original-wordmark.svg" width="40" height="40" alt="Spring Session"/> |
|                   | Spring Data JPA       | Упрощает разработку уровня доступа к данным с помощью репозиториев.      | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/spring/spring-original-wordmark.svg" width="40" height="40" alt="Spring Data JPA"/> |
|                   | Hibernate             | ORM-фреймворк для абстракции взаимодействия с базой данных.              | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/hibernate/hibernate-original-wordmark.svg" width="40" height="40" alt="Hibernate"/> |
| **Документация API** | Swagger / OpenAPI   | Генерация интерактивной документации REST API.                           | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/swagger/swagger-original.svg" width="40" height="40" alt="Swagger"/> |

### Базы данных и хранилище

| Технология/Инструмент | Описание                                                                      | Иконка                                                                                                                                                     |
|-----------------------|-------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **PostgreSQL**        | Реляционная база данных для хранения пользовательских данных и метаданных.     | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/postgresql/postgresql-original-wordmark.svg" width="40" height="40" alt="PostgreSQL"/> |
| **MinIO**             | S3-совместимое объектное хранилище для файлов.                                | <img src="https://github.com/user-attachments/assets/ce6c904c-0f05-4b7f-9766-68bbd8e3a766" width="40" height="20" alt="MinIO"/>                            |
| **Redis**             | Хранилище ключ-значение для управления сессиями и кэширования.                | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/redis/redis-original-wordmark.svg" width="40" height="40" alt="Redis"/>                |

## 📐 Архитектура

Проект использует микросервисную архитектуру, разделяя функциональность на независимые компоненты:

- **API Gateway (Spring Boot):** Основной вход для всех запросов, обеспечивающий маршрутизацию и безопасность.
- **Storage Service:** Управляет операциями с файлами, взаимодействуя с MinIO.
- **Auth Service:** Отвечает за регистрацию, аутентификацию и управление пользователями.

Архитектурная диаграмма:

```
[Клиент] <-> [API Gateway] <-> [File Service] <-> [MinIO]
                           <-> [User Service] <-> [PostgreSQL]
                           <-> [Session Service] <-> [Redis]
```

## ⚙️ Требования

Для запуска проекта убедитесь, что установлены:

- **JDK:** Версия 21 или выше.
- **Docker:** Версия 24.0 или выше.
- **IntelliJ IDEA:** Рекомендуется для разработки благодаря поддержке Spring Boot.
- **Оперативная память:** Минимум 4 ГБ свободной памяти.

## 🚀 Установка и запуск

1. **Клонируйте репозиторий:**
   ```bash
   git clone https://github.com/syncline139/cloud-file-storage.git
   cd cloud-file-storage
   ```

2. **Откройте в IntelliJ IDEA:**
    - Импортируйте проект, выбрав папку репозитория.

3. **Настройте переменные окружения:**
    - Переименуйте файл `.env.example` в `.env` (находится в корне проекта, на уровне `pom.xml`).
    - Отредактируйте `.env`, заменив значения на свои:

   ```properties
   # Конфигурация базы данных
   DB_NAME=your_db_name
    DB_USER=your_user
    DB_PASSWORD=your_password

   # Конфигурация MinIO
    MINIO_URL=http://minio:9000
    MINIO_USER=your_user
    MINIO_PASSWORD=your_password
   ```

4. **Запустите сервисы с Docker Compose:**
    - В терминале в корне проекта выполните:
   ```bash
   docker compose up --build -d
   ```

5. **Доступ к приложению:**
    - Приложение доступно по адресу: `http://localhost:8080`
    - Swagger UI: `http://localhost:8080/swagger-ui.html`

## 🧪 Тестирование

- **Интеграционное тестирование:** Проверяет взаимодействие компонентов и внешних сервисов.
- **JUnit:** Для модульного тестирования отдельных компонентов.
- **Testcontainers:** Используется для создания изолированных контейнеров базы данных и других сервисов.

Пример запуска тестов:
```bash
mvn test
```