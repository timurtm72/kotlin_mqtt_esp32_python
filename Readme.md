# Мой сайт и резюме:

- [Мой сайт:](https://technocom.site123.me/)
- [Мое резюме инженер программист microcontrollers, PLC:](https://innopolis.hh.ru/resume/782d86d5ff0e9487200039ed1f6f3373384b30)
- [Мое резюме инженер программист Java backend developer (Spring):](https://innopolis.hh.ru/resume/9e3b451aff03fd23830039ed1f496e79587649)
- [Linkedin](https://www.linkedin.com/public-profile/settings?trk=d_flagship3_profile_self_view_public_profile)
  
# Android MQTT IoT Control Application

Android приложение на Kotlin для управления ESP32 через MQTT протокол. Система позволяет мониторить данные с датчика температуры/влажности DHT и управлять RGB светодиодами через графический пользовательский интерфейс.

## Архитектура проекта

Приложение построено на основе MVVM архитектуры с Jetpack Compose для UI и Eclipse Paho MQTT для коммуникации:

### Основные компоненты
- `MQTTClient` - класс для работы с MQTT протоколом
- `MainActivity` - главное активити с UI компонентами
- `MainViewModel` - ViewModel для управления данными и бизнес-логикой
- `Chart` - компонент для отображения графиков (Vico библиотека)

### Модели данных
```kotlin
// Данные RGB светодиода
data class RGBData(
    val red: Int,         // 0-255
    val green: Int,       // 0-255
    val blue: Int,        // 0-255
    val brightness: Int   // 0-255
)

// Данные с датчика DHT
data class SensorData(
    val temperature: Float,    // °C
    val humidity: Float,       // %
    val timestamp: Long = System.currentTimeMillis() // Временная метка
)
```

## Основные функции

### Управление RGB светодиодом
- Графические слайдеры для настройки RGB (0-255)
- Слайдер регулировки яркости (0-255)
- Предпросмотр цвета в реальном времени
- Отправка команд через MQTT
- Сохранение настроек в SharedPreferences

### Мониторинг DHT сенсора
- Отображение текущей температуры и влажности
- Построение графиков в реальном времени
- Вертикальное отображение временных меток
- Хранение истории последних 20 измерений

## Технические особенности
- Реактивное программирование с Kotlin Flow
- Асинхронная обработка с корутинами
- Автоматическое переподключение к MQTT брокеру
- JSON сериализация для обмена данными
- Архитектура MVVM с однонаправленным потоком данных
- Вертикальная прокрутка для адаптации к поворотам экрана

## Потоки данных

### Входящие (MQTT)
```json
{
  "temperature": 25.6,
  "humidity": 45.2
}
```

### Исходящие (MQTT)
```json
{
  "red": 255,
  "green": 0,
  "blue": 0,
  "brightness": 100
}
```

## Структура проекта
```plaintext
app/src/main/java/com/example/mqtt/
├── MainActivity.kt           # Главная активность с UI компонентами
├── MQTTApplication.kt        # Класс приложения с инициализацией
├── model/                    # Пакет с моделями данных
│   └── SensorData.kt         # Классы данных для сенсора и RGB
├── mqtt/                     # Пакет для работы с MQTT
│   ├── MQTTClient.kt         # Клиент для работы с MQTT брокером
│   └── MqttService.kt        # Фоновый сервис для MQTT соединения
├── viewmodel/                # Пакет с ViewModel
│   └── MainViewModel.kt      # ViewModel для управления данными и MQTT
└── ui/theme/                 # Пакет с темой оформления
    ├── Color.kt              # Определение цветов
    ├── Theme.kt              # Настройка темы
    └── Type.kt               # Настройка типографики
```

## Конфигурация
```kotlin
// MQTT настройки
private const val MQTT_BROKER = "193.43.147.210"
private const val MQTT_PORT = 1883
private const val MQTT_USERNAME = "timur"
private const val MQTT_PASSWORD = "timur_1972"

// Топики MQTT
"esp32/sensor/dht"   // Для получения данных с датчика
"esp32/control/rgb"  // Для управления RGB светодиодом

// SharedPreferences
private const val PREFS_NAME = "RGBSettings"
private const val KEY_RED = "red"
private const val KEY_GREEN = "green"
private const val KEY_BLUE = "blue"
private const val KEY_BRIGHTNESS = "brightness"
```

## Требования
- Android 6.0 (API 24) или выше
- Библиотеки:
  - Jetpack Compose
  - Kotlin Coroutines
  - Paho MQTT Client
  - Vico Charts
  - Material3 Components

## Интерфейс пользователя
- Вкладка RGB Control:
  - Слайдеры для настройки цвета
  - Предпросмотр выбранного цвета
  - Кнопка отправки настроек на ESP32
- Вкладка DHT Data:
  - Текущие значения температуры/влажности
  - Графики изменения показателей во времени
  - Вертикальные временные метки

## Функциональные возможности
- Подключение/отключение к MQTT брокеру
- Индикация статуса соединения
- Обработка ошибок при отправке/получении данных
- Автоматическое форматирование значений на графиках
- Ограничение истории данных для оптимальной производительности
- Сохранение настроек RGB между сеансами приложения

## Сборка и запуск
```bash
# Клонировать репозиторий
git clone https://github.com/timurtm72/AndroidMqttProject.git

# Открыть проект в Android Studio
# Собрать и запустить на устройстве или эмуляторе
```

## Связанные проекты
- [ESP32 прошивка](https://github.com/timurtm72/esp_idf_esp32_mqtt_android)
- [Flutter приложение](https://github.com/timurtm72/flutter_android_mqtt_python_esp32)
- [Python GUI приложение](https://github.com/timurtm72/python_mqtt_esp32_android)
