# TeaHub

CRUD-сервис для управления каталогом чая.

Поддерживает создание, просмотр, обновление и удаление чая, поиск по фильтрам, а также восстановление удалённых записей в каталоге.


## Запуск

Поднимите базу данных PostgreSQL. Можно сделать это из docker-compose проекта:
```bash
docker compose up -d db
```

Заполните переменные окружения для базы данных. Например в корне проекта в файле .env или любым удобным способом.
- DB_HOST - адрес сервера БД
- DB_PORT - порт подключения к БД
- DB_NAME - имя
- DB_USER - имя сервисной учетки
- DB_PASSWORD - пароль пользователя

Пример:
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=teahub
DB_USER=teahub_user
DB_PASSWORD=your_local_password
```

Введите команду в терминале из корня проекта.
```bash
./gradlew bootRun
```

Приложение будет запущено на 8085 порту.

### OpenApi

OpenApi доступно по стандартным путям 

```
/v3/api-docs
```

```
/swagger-ui.html
```

Экспорт OpenAPI для локального проекта:

```bash
curl -f http://localhost:8085/v3/api-docs.yaml \
  -o openapi.yaml
```

## Стек

Tech stack: Java 21, Spring Boot, PostgreSQL, Hibernate, Liquibase, MapStruct, Testcontainers, OpenAPI.
