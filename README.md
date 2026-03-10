# nataly_bot
antispam bot

# План проекта: Telegram модератор‑бот на Java + Spring Boot

Этот документ — пошаговый, практический план разработки твоего модератор‑бота для групп в Telegram. Ничего лишнего, всё как есть: что сделать, зачем, как и примерные оценки по времени. Цель — от MVP до рабочего, надёжного решения, пригодного для развития.

---

## Краткая цель

Сделать бота, который в групповом чате:

* фильтрует очевидный спам по первым сообщениям;
* для новых/неавторизованных пользователей даёт простую "капчу" с inline‑кнопкой;
* временно ограничивает права до подтверждения; при неуспехе — удаляет/банит;
* имеет хранение состояния (Postgres/Redis) и простую панель команд для админов (через команды в чате).

Это pet‑project для личного чата — но сделаем его надёжным и готовым к расширению.

---

## Общее техстек‑рекомендации

* Язык: **Java 17+** (рекомендуется LTS).
* Фреймворк: **Spring Boot** (ты с ним знаком — используем преимущества: DI, планировщик, конфиг, Actuator).
* Библиотека для Telegram Bot API: `org.telegram:telegrambots-spring-boot-starter` (или официальная `rubenlagus/TelegramBots` + Spring интеграция). Есть готовые стартеры для Spring Boot.
* База для постоянных данных: **PostgreSQL** (хранение авторизованных пользователей, логов).
* Быстрое хранилище с TTL для ожидания капчи: **Redis** (или можно использовать Postgres + timestamp, но Redis удобнее).
* Docker для локальной сборки/деплоя; VPS (или provider) для запуска; на проде — Docker Compose + systemd или сервис на Docker.
* Секреты: **переменные окружения** (TOKEN, DB URL, REDIS URL). Никогда не коммитить токен.

---

## Архитектура (high level)

```
Telegram → Bot (Spring Boot) → Controller → Services → Repositories → Postgres/Redis
                             ↑
                         Scheduler (Spring @Scheduled)
```

Компоненты:

* `Controller` — принимаем `Update` от Telegram (через LongPollingBot или Webhook endpoint).
* `UserService` — логика состояния пользователя (AUTHORIZED/PENDING/BANNED).
* `SpamService` — набор проверок (ключевые слова, ссылки, эвристики).
* `CaptchaService` — отдаёт inline‑кнопку, проверяет CallbackQuery.
* `Repository` — интерфейсы для Postgres; Redis клиенты для временных данных.
* `PendingUserChecker` — планировщик, который удаляет/банит пользователей при прошедшем TTL.
* `AdminCommandsService` — команды типа `/allow`, `/ban`, `/stats`.

---

## Модель состояния пользователя

* **AUTHORIZED** — прошёл проверку; пишущие права оставлены/восстановлены.
* **PENDING** — получил капчу; права ограничены; ждём подтверждения (TTL).
* **BANNED** — удалён или заблокирован.

Для PENDING храним: `userId`, `chatId`, `messageId` (удаляем оригинал), `issuedAt` (timestamp), `captchaId` (hash для защиты).

---

## Фазы разработки (пошагово)

### Фаза 0 — Подготовка окружения (1–2 часа)

* Создать репозиторий (GitHub/GitLab).
* Настроить Gradle/Maven (я предложу Gradle). Версии: Java 17+, Spring Boot 3.x.
* Добавить `.gitignore`, `README.md`, `docker-compose.yml` для локального Postgres + Redis (опционально).
* Получить токен от BotFather.

### Фаза 1 — Минимальный Spring Boot бот (MVP) (2–6 часов)

Цель: бот запускается, получает Update и отвечает.

* Создать Spring Boot приложение.
* Добавить зависимость `telegrambots-spring-boot-starter`.
* Конфигурация: `application.yml` с placeholder для `BOT_TOKEN` как переменная окружения.
* Реализовать класс бота, который расширяет `TelegramLongPollingBot` (или использует стартер) и логирует входящие Update.
* Проверить: бот отвечает на любой текст `echo`.

**Результат:** бот работает локально и отвечает.

### Фаза 2 — Скелет архитектуры (4–8 часов)

* Создать пакеты: `controller`, `service`, `repository`, `model`, `scheduler`, `config`.
* Реализовать `UpdateController` — разбирает Update и перенаправляет в `ProcessingService`.
* Добавить `UserRepository` (интерфейс JPA) и сущность `UserEntity` (Postgres).
* Реализовать in‑memory хранение для PENDING (Map или Redis абстракция) — чтобы сразу тестить логику.

**Результат:** чистая архитектура, можно писать логику без хака в одном классе.

### Фаза 3 — Капча (inline‑кнопка) и рестрикты (4–8 часов)

* Добавить `CaptchaService`:

    * Генерирует `callbackData` в виде `captcha:{userId}:{nonceHash}`.
    * Отправляет `SendMessage` с `InlineKeyboardMarkup`.
* На первый контакт: бот вызывает `restrictChatMember(chatId, userId, permissions)` (запрет писать).
* Сохраняем PENDING в Redis/Postgres с TTL (например, 10 минут).
* Обработка `CallbackQuery`: проверяем `from.id` совпадает с пользовательским `userId` в `callbackData`. Если да — `promote`/`unrestrict` через `restrictChatMember` с полномочиями.
* Удаляем оригинальное подозрительное сообщение (`deleteMessage`) при выдаче капчи или при бане.

**Особенность:** Telegram позволяет ограничивать права и снимать их через API, бот должен быть админом с правами `can_restrict_members`.

### Фаза 4 — Таймауты и планировщик (2–4 часа)

* `PendingUserChecker` — Spring компонент с `@Scheduled(fixedRate = 60_000)` (или более реальный подход: Redis Keyspace notifications или Sorted Set + pop по времени).
* Проходим PENDING записи старше 10 минут → `banChatMember` или `kickChatMember` и удаляем/логируем.
* Реализовать опцию (конфиг) — что делать по таймауту: `kick`, `restrict` или `notify admin`.

**Результат:** автоматическая очистка невыполнивших капчу.

### Фаза 5 — Простые антиспам‑правила (2–5 часов)

* Правила на основе шаблонов/ключевых слов/регулярных выражений:

    * блокировка ссылок (`http`, `t.me/`, домены казино/крипто — набор фильтров);
    * слишком длинные сообщения/спам символов.
* Реализовать `SpamService.check(message)` → `SPAM / OK / NEED_CAPTCHA`.
* При SPAM — `deleteMessage` + `ban`.

**Результат:** базовая фильтрация до капчи.

### Фаза 6 — Хранение логов и админ‑команды (2–4 часа)

* Таблица `moderation_logs` в Postgres: `userId`, `chatId`, `action`, `reason`, `timestamp`, `adminId`.
* Команды `/allow <userId>`, `/ban <userId>`, `/stats`.
* Разрешать админам просматривать логи.

### Фаза 7 — Dockerize & Deploy (4–8 часов)

* Написать `Dockerfile` для Spring Boot (multi‑stage JDK build → slim runtime).
* `docker-compose.yml` (app + postgres + redis). Локально тестируем весь стек.
* Прод deploy: VPS (Ubuntu) — установить Docker + Docker Compose, скопировать `docker-compose.yml` и `.env`, запуск `docker compose up -d`.
* Альтернатива: запуск без Docker: `java -jar app.jar` + systemd unit.

**Важно:** хранить токен в переменных окружения (`ENV BOT_TOKEN`) или в Vault. Никогда не в repo.

### Фаза 8 — Нагрузочное тестирование и мелкие улучшения (опционально)

* Логирование, метрики (Prometheus + Micrometer), health checks (Spring Actuator).
* Тестирование на staging группе.
* Улучшение правил антиспама.

---

## Конфигурация и примеры

### Пример `build.gradle.kts` (фрагмент)

```kotlin
plugins {
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.9.0" apply false // если не нужен kotlin, убери
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.telegram:telegrambots-spring-boot-starter:6.5.0")
    implementation("org.postgresql:postgresql")
    implementation("redis.clients:jedis:4.3.0")
    // тесты и т.д.
}
```

> Примечание: версии укажи актуальные. Я привёл пример структуры зависимостей.

### Пример `application.yml` (фрагмент)

```yaml
telegram:
  bot-token: ${BOT_TOKEN}
  bot-username: ${BOT_USERNAME}

spring:
  datasource:
    url: ${JDBC_DATABASE_URL}
    username: ${JDBC_DATABASE_USERNAME}
    password: ${JDBC_DATABASE_PASSWORD}

redis:
  uri: ${REDIS_URL}

moderation:
  captcha-ttl-seconds: 600
  spam-keywords: ["casino","buy followers","http://", "t.me/"]
```

### Пример Dockerfile (минимальный)

```dockerfile
FROM eclipse-temurin:17-jdk-jammy as build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV BOT_TOKEN=""
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

---

## Проверки безопасности и эксплуатационные советы

* **Токен**: только в env vars, rotируй при утечке.
* **Права бота**: должен быть админ с правом restrict/kick.
* **Логи**: логируй только id и причины — не хранить личные данные.
* **Тестирование**: сначала в приватной тестовой группе.
* **Мониторинг**: health endpoint и базовый алертинг (если бот упал).

---

## Оценка сроков (честно)

* MVP (локально, без Docker, с in‑memory state): **4–12 часов** (если работаешь непрерывно).
* Надёжная версия (Postgres + Redis + docker + базовые правила + scheduler): **2–4 дня**.
* Дополнительные улучшения (ui/админка, ML, сложные эвристики): **несколько недель** в зависимости от уровня.

---

## Что я могу сделать дальше (предложения)

1. Сгенерировать skeleton Spring Boot проекта с Gradle + базовыми классами (controller, services, entities). (Предпочтительно — даём стартовую ветку.)
2. Реализовать фазу 1–3 пошагово прямо тут: писать код, ты проверяешь на своей машине, исправляем по ходу.
3. Написать `docker-compose.yml` для локальной разработки (app + postgres + redis).
4. Подготовить unit‑ и integration‑тесты для основных сценариев.

---

## Риски и ограничения (честно, без прикрас)

* Полной защиты от ботов не получится: Telegram API не даёт всех метрик (время создания аккаунта, IP и т.п.). Всегда будут ложные срабатывания.
* Пользователи могут жаловаться, если бот будет агрессивно кикать — настраивай политику.
* Webhook требует публичный HTTPS URL и сертификат; long polling проще, но требует, чтобы процесс работал непрерывно на сервере.

---

Если хочешь — я сейчас могу:

* создать skeleton проекта на Spring Boot (Gradle) с базовой конфигурацией и минимальным `EchoBot`;
* или сразу — `docker-compose.yml` и `Dockerfile`.

Скажи, что предпочитаешь — сгенерировать skeleton проекта или пойти шаг за шагом вручную (с моей поддержкой и код‑ревью каждого шага).
