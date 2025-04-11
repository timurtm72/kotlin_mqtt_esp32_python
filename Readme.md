# Android MQTT IoT Control Application

Это Android приложение на Kotlin для мониторинга данных с датчика DHT (температура и влажность) и управления RGB светодиодом через MQTT протокол. Приложение взаимодействует с устройством ESP32, отображает данные в виде графиков и позволяет настраивать цвет и яркость светодиода.

## Основы Kotlin и структура проекта

### 1. Объявление пакета и импорты

```kotlin
package com.example.mqtt  // Определяет пакет, в котором находится класс

// Импорты необходимых библиотек и классов
import android.os.Bundle
import android.util.Log
// ...и другие импорты
```

Пакеты в Kotlin, как и в Java, используются для организации кода. Импорты подключают внешние классы.

### 2. Константы

```kotlin
private const val PREFS_NAME = "RGBSettings"  // Константа для имени файла настроек
private const val KEY_RED = "red"              // Константа для ключа, хранящего значение красного цвета
```

`const val` - это неизменяемая константа, доступная во время компиляции.

### 3. Классы и наследование

```kotlin
class MainActivity : ComponentActivity() {
    // Тело класса
}
```

Здесь `MainActivity` - это класс, который наследуется от `ComponentActivity`. Двоеточие (`:`) в Kotlin используется для обозначения наследования.

### 4. Свойства класса

```kotlin
private val TAG = "MainActivity"  // Создание приватного свойства для тега логов
private val viewModel: MainViewModel by viewModels()  // Получение ViewModel с использованием делегата
```

- `val` - неизменяемое свойство (как final в Java)
- `var` - изменяемое свойство
- `by viewModels()` - это делегирование, которое позволяет автоматически создавать и управлять жизненным циклом ViewModel

### 5. Функции

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    try {
        Log.d(TAG, "onCreate: Запуск приложения")
        super.onCreate(savedInstanceState)
        setContent {
            // Код
        }
    } catch (e: Exception) {
        Log.e(TAG, "onCreate: Ошибка при запуске", e)
    }
}
```

- `override fun` - переопределение функции из родительского класса
- `onCreate` - метод жизненного цикла активности, вызывается при создании
- `savedInstanceState: Bundle?` - параметр с типом `Bundle?`, где `?` означает, что параметр может быть null
- `try/catch` - обработка исключений

### 6. Функции верхнего уровня

```kotlin
// Функция для сохранения настроек RGB в SharedPreferences
fun saveRGBSettings(context: Context, red: Int, green: Int, blue: Int, brightness: Int) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    with(sharedPrefs.edit()) {
        putInt(KEY_RED, red)
        putInt(KEY_GREEN, green)
        putInt(KEY_BLUE, blue)
        putInt(KEY_BRIGHTNESS, brightness)
        apply()
    }
}
```

Функции верхнего уровня не принадлежат никакому классу. `with` - это функция-расширение для удобной работы с объектом.

### 7. Классы данных

```kotlin
data class RGBSettings(
    val red: Int,
    val green: Int,
    val blue: Int,
    val brightness: Int
)
```

`data class` - специальный тип класса в Kotlin, который автоматически реализует equals(), hashCode(), toString() и copy().

## Jetpack Compose и UI компоненты

### 8. Composable функции

```kotlin
@Composable
fun MainScreen(viewModel: MainViewModel) {
    // Код
}
```

Аннотация `@Composable` означает, что функция может использоваться для создания пользовательского интерфейса в Jetpack Compose.

### 9. Состояния в Compose

```kotlin
var selectedTabIndex by remember { mutableStateOf(0) }
```

- `remember` - функция, которая сохраняет состояние между перерисовками
- `mutableStateOf` - создает наблюдаемое состояние
- `by` - делегирование свойства, упрощает синтаксис для работы с состояниями

### 10. UI компоненты

```kotlin
Column {
    // Вкладки для переключения между экранами
    TabRow(selectedTabIndex = selectedTabIndex) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = { Text(title) }
            )
        }
    }
}
```

- `Column` - вертикальный контейнер
- `TabRow` - контейнер для вкладок
- `forEachIndexed` - перебор элементов списка с индексом
- `onClick = { ... }` - лямбда-выражение для обработки нажатия

### 11. Модификаторы

```kotlin
modifier = Modifier
    .fillMaxSize()
    .padding(16.dp)
    .verticalScroll(scrollState)
```

Модификаторы в Compose цепочкой применяют разные трансформации к UI-элементам. В данном случае:
- `fillMaxSize()` - занять максимальное доступное пространство
- `padding(16.dp)` - добавить внутренний отступ 16dp со всех сторон
- `verticalScroll(scrollState)` - добавить вертикальную прокрутку

## Архитектура MVVM и управление данными

### 12. ViewModel и LiveData/StateFlow

```kotlin
val sensorData by viewModel.sensorData.collectAsState()
val isConnected by viewModel.isConnected.collectAsState()
```

- `viewModel` - экземпляр MainViewModel, который хранит и управляет данными UI
- `collectAsState()` - преобразует StateFlow в State для использования в Compose
- `by` - делегирование для автоматической подписки на изменения

### 13. Обработка событий и обновление данных

```kotlin
Button(
    onClick = {
        // Создаем объект с данными RGB
        val rgbData = RGBData(
            red = red.toInt(),
            green = green.toInt(),
            blue = blue.toInt(),
            brightness = brightness.toInt()
        )
        
        // Отправляем данные через ViewModel
        viewModel.sendRGBValues(rgbData)
        
        // Сохраняем настройки в SharedPreferences
        saveRGBSettings(
            context,
            red.toInt(),
            green.toInt(),
            blue.toInt(),
            brightness.toInt()
        )
    }
) {
    Text("Отправить на ESP32")
}
```

Здесь:
- Создается объект `RGBData` с текущими значениями
- Вызывается метод `sendRGBValues` у ViewModel для отправки данных
- Сохраняются настройки в SharedPreferences

### 14. Работа с внешним хранилищем (SharedPreferences)

```kotlin
val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
with(sharedPrefs.edit()) {
    putInt(KEY_RED, red)
    // ...
    apply()
}
```

SharedPreferences - это механизм для хранения простых данных в виде ключ-значение. Метод `apply()` сохраняет изменения асинхронно.

## Работа с MQTT и сетью

### 15. MQTT клиент

```kotlin
class MQTTClient(
    private val context: Context,
    private val serverUri: String = "tcp://$MQTT_BROKER:$MQTT_PORT",
    private val clientId: String = MqttClient.generateClientId()
) {
    private val mqttClient = MqttClient(serverUri, clientId, null)
    // ...
}
```

Класс для работы с MQTT протоколом, который инициализирует соединение с брокером.

### 16. Асинхронная работа с suspend функциями

```kotlin
suspend fun connect(): Boolean = suspendCoroutine { continuation ->
    try {
        // ...
        mqttClient.connect(mqttConnectOptions)
        isConnected = true
        continuation.resume(true)
    } catch (e: Exception) {
        // ...
        continuation.resume(false)
    }
}
```

`suspend` - специальный модификатор для корутин в Kotlin, позволяющий приостанавливать выполнение функции без блокировки потока.

## Работа с данными и графиками

### 17. Обработка и фильтрация данных

```kotlin
val displayedData = remember(sensorData) {
    if (sensorData.size > 20) {
        sensorData.takeLast(20)
    } else {
        sensorData
    }
}
```

Здесь:
- `remember(sensorData)` - кэширование результата, пересчитывается только при изменении sensorData
- `takeLast(20)` - взять только последние 20 элементов, если их больше 20

### 18. Работа с графиками (Vico библиотека)

```kotlin
Chart(
    chart = lineChart(),
    model = entryModelOf(*temperatureEntries),
    modifier = Modifier
        .padding(8.dp)
        .fillMaxSize(),
    startAxis = startAxis(),
    bottomAxis = bottomAxis(
        valueFormatter = { value, _ ->
            ""
        }
    )
)
```

- `lineChart()` - создает линейный график
- `entryModelOf(*temperatureEntries)` - преобразует массив точек в модель для графика
- `startAxis()` и `bottomAxis()` - настройка осей
- `valueFormatter` - форматирование значений на оси

### 19. Форматирование даты и времени

```kotlin
val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
val formattedTime = sdf.format(Date(latestData.timestamp))
```

Форматирование временной метки в читаемый формат часы:минуты:секунды.

## Структура проекта
app/src/main/java/com/example/mqtt/
├── MainActivity.kt # Главная активность с UI компонентами
├── MQTTApplication.kt # Класс приложения с инициализацией
├── model/ # Пакет с моделями данных
│ └── SensorData.kt # Классы данных для сенсора и RGB
├── mqtt/ # Пакет для работы с MQTT
│ ├── MQTTClient.kt # Клиент для работы с MQTT брокером
│ └── MqttService.kt # Фоновый сервис для MQTT соединения
├── viewmodel/ # Пакет с ViewModel
│ └── MainViewModel.kt # ViewModel для управления данными и MQTT
└── ui/theme/ # Пакет с темой оформления
├── Color.kt # Определение цветов
├── Theme.kt # Настройка темы
└── Type.kt # Настройка типографики


## Зависимости проекта

```gradle
dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // MQTT
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")

    // AndroidX Support
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation("com.google.android.material:material:1.11.0")

    // Библиотека для графиков Vico
    implementation("com.patrykandpatrick.vico:compose:1.12.0")
    implementation("com.patrykandpatrick.vico:compose-m3:1.12.0")
    implementation("com.patrykandpatrick.vico:core:1.12.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

## Заключение

Это приложение следует архитектуре MVVM (Model-View-ViewModel):
- **Model**: Классы данных (SensorData, RGBData) и бизнес-логика (MQTTClient)
- **View**: Composable функции, определяющие UI (MainScreen, RGBControlTab, DHTDataTab)
- **ViewModel**: MainViewModel, который управляет данными и бизнес-логикой

Приложение использует современные библиотеки и подходы:
- Jetpack Compose для UI
- Kotlin Coroutines для асинхронного программирования
- Kotlin Flow для реактивных потоков данных
- MQTT для связи с ESP32
- Vico для графиков
- SharedPreferences для хранения настроек

Основной поток данных:
1. Пользователь взаимодействует с UI (меняет цвета, нажимает кнопки)
2. UI вызывает методы ViewModel
3. ViewModel обрабатывает данные и отправляет их через MQTT
4. ViewModel получает данные от MQTT и обновляет StateFlow
5. UI реагирует на изменения в StateFlow и обновляется

Этот проект является типичным примером современного Android-приложения на Kotlin с использованием рекомендуемых практик и библиотек.

## Заключение

Это приложение следует архитектуре MVVM (Model-View-ViewModel):
- **Model**: Классы данных (SensorData, RGBData) и бизнес-логика (MQTTClient)
- **View**: Composable функции, определяющие UI (MainScreen, RGBControlTab, DHTDataTab)
- **ViewModel**: MainViewModel, который управляет данными и бизнес-логикой

Приложение использует современные библиотеки и подходы:
- Jetpack Compose для UI
- Kotlin Coroutines для асинхронного программирования
- Kotlin Flow для реактивных потоков данных
- MQTT для связи с ESP32
- Vico для графиков
- SharedPreferences для хранения настроек

Основной поток данных:
1. Пользователь взаимодействует с UI (меняет цвета, нажимает кнопки)
2. UI вызывает методы ViewModel
3. ViewModel обрабатывает данные и отправляет их через MQTT
4. ViewModel получает данные от MQTT и обновляет StateFlow
5. UI реагирует на изменения в StateFlow и обновляется

Этот проект является типичным примером современного Android-приложения на Kotlin с использованием рекомендуемых практик и библиотек.