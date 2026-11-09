# TeaHub

CRUD-сервис для управления каталогом чая, реализующий полный набор основных операций:
добавление, поиск по ID и фильтрам, обновление, удаление и восстановление ранее удалённых позиций в каталоге.


## Запуск

Поднимите базу данных PostgreSQL. Можно сделать это из docker-compose проекта:
```bash
docker compose up -d db
```

Заполните переменные окружения для базы данных. Например в корне проекта в файле .env или любым удобным способом.
- DB_HOST - адрес хоста
- DB_NAME - имя
- DB_USER - имя сервисной учетки
- DB_PASSWORD - пароль пользователя

Пример:
```
DB_HOST=localhost:5432
DB_NAME=teahub
DB_USER=teahub
DB_PASSWORD=your_local_password
```

Введите команду в терминале из корня проекта
```bash
./gradlew bootRun
```

Приложение будет запущено на 8085 порту

## Стек

Tech stack: Java 21, Spring Boot, PostgreSQL, Hibernate, Liquibase, MapStruct, Testcontainers.
