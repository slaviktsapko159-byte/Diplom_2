# Diplom_2 – API автотесты для Stellar Burgers

## Описание проекта
Проект содержит набор автотестов для тестирования API сервиса [Stellar Burgers](https://stellarburgers.education-services.ru/).

Тестируемые эндпоинты:
- Создание пользователя (`POST /api/auth/register`)
- Логин пользователя (`POST /api/auth/login`)
- Создание заказа (`POST /api/orders`)

## Технологии
| Технология       | Версия   |
|------------------|----------|
| Java             | 11       |
| Maven            | 3.9.0+   |
| JUnit            | 4.13.2   |
| REST Assured     | 5.3.0    |
| Allure           | 2.20.1   |
| Lombok           | 1.18.30  |

## Настройка проекта
- Установите JDK 11.
- Установите Maven.
- (Опционально) Установите плагин Lombok в IntelliJ IDEA.

## Запуск тестов
```bash
mvn clean test