package com.example.mqtt

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.mqtt.model.RGBData
import com.example.mqtt.ui.theme.MQTTTheme
import com.example.mqtt.viewmodel.MainViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Константы для SharedPreferences
private const val PREFS_NAME = "RGBSettings"
private const val KEY_RED = "red"
private const val KEY_GREEN = "green"
private const val KEY_BLUE = "blue"
private const val KEY_BRIGHTNESS = "brightness"

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    // Создаем ViewModel для управления данными
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            Log.d(TAG, "onCreate: Запуск приложения")
            super.onCreate(savedInstanceState)
            setContent {
                Log.d(TAG, "onCreate: Настройка UI")
                MQTTTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen(viewModel)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Ошибка при запуске", e)
        }
    }
}

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
    Log.d("RGBSettings", "Настройки сохранены: R=$red, G=$green, B=$blue, Яркость=$brightness")
}

// Функция для загрузки настроек RGB из SharedPreferences
fun loadRGBSettings(context: Context): RGBSettings {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val red = sharedPrefs.getInt(KEY_RED, 0)
    val green = sharedPrefs.getInt(KEY_GREEN, 0)
    val blue = sharedPrefs.getInt(KEY_BLUE, 0)
    val brightness = sharedPrefs.getInt(KEY_BRIGHTNESS, 100)
    Log.d("RGBSettings", "Настройки загружены: R=$red, G=$green, B=$blue, Яркость=$brightness")
    return RGBSettings(red, green, blue, brightness)
}

// Класс для хранения настроек RGB
data class RGBSettings(
    val red: Int,
    val green: Int,
    val blue: Int,
    val brightness: Int
)

// Главный экран приложения с вкладками
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("RGB Control", "DHT Data")

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

        // Отображение содержимого выбранной вкладки
        when (selectedTabIndex) {
            0 -> RGBControlTab(viewModel)
            1 -> DHTDataTab(viewModel)
        }
    }
}

// Вкладка управления RGB светодиодом
@Composable
fun RGBControlTab(viewModel: MainViewModel) {
    // Получаем контекст для работы с SharedPreferences
    val context = LocalContext.current

    // Создаем скролл-стейт для вертикальной прокрутки
    val scrollState = rememberScrollState()

    // Загружаем сохраненные настройки при первом рендеринге
    val savedSettings = remember { loadRGBSettings(context) }

    // Состояния для слайдеров с инициализацией из SharedPreferences
    var red by remember { mutableStateOf(savedSettings.red.toFloat()) }
    var green by remember { mutableStateOf(savedSettings.green.toFloat()) }
    var blue by remember { mutableStateOf(savedSettings.blue.toFloat()) }
    var brightness by remember { mutableStateOf(savedSettings.brightness.toFloat()) }

    // Вычисляем цвет на основе выбранных значений RGB и яркости
    val selectedColor = Color(
        red = (red / 255f),
        green = (green / 255f),
        blue = (blue / 255f),
        alpha = brightness / 255f
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState) // Добавляем вертикальную прокрутку
    ) {
        // Слайдеры для управления цветом
        Text("Красный: ${red.toInt()}")
        Slider(
            value = red,
            onValueChange = { red = it },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = Color.Red,
                activeTrackColor = Color.Red.copy(alpha = 0.7f)
            )
        )

        Text("Зеленый: ${green.toInt()}")
        Slider(
            value = green,
            onValueChange = { green = it },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = Color.Green,
                activeTrackColor = Color.Green.copy(alpha = 0.7f)
            )
        )

        Text("Синий: ${blue.toInt()}")
        Slider(
            value = blue,
            onValueChange = { blue = it },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = Color.Blue,
                activeTrackColor = Color.Blue.copy(alpha = 0.7f)
            )
        )

        Text("Яркость: ${brightness.toInt()}")
        Slider(
            value = brightness,
            onValueChange = { brightness = it },
            valueRange = 0f..255f
        )

        // Виджет предпросмотра цвета
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Предпросмотр цвета",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = selectedColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Информация о цвете в HEX и RGB форматах
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "RGB: (${red.toInt()}, ${green.toInt()}, ${blue.toInt()})",
                    fontSize = 14.sp
                )

                Text(
                    text = "HEX: #${red.toInt().toString(16).padStart(2, '0')}${green.toInt().toString(16).padStart(2, '0')}${blue.toInt().toString(16).padStart(2, '0')}",
                    fontSize = 14.sp
                )

                Text(
                    text = "Яркость: ${brightness.toInt()} / 255",
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка отправки данных на ESP32
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
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = selectedColor.copy(alpha = 1f),
                contentColor = if ((red + green + blue) / 3 < 128) Color.White else Color.Black
            )
        ) {
            Text("Отправить на ESP32", fontWeight = FontWeight.Bold)
        }
    }
}

// Вкладка отображения данных с датчика DHT
@Composable
fun DHTDataTab(viewModel: MainViewModel) {
    // Создаем скролл-стейт для вертикальной прокрутки
    val scrollState = rememberScrollState()

    val sensorData by viewModel.sensorData.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    // Берем только последние 20 показаний для графика
    val displayedData = remember(sensorData) {
        if (sensorData.size > 20) {
            sensorData.takeLast(20)
        } else {
            sensorData
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState) // Добавляем вертикальную прокрутку
    ) {
        // Индикатор состояния подключения
        Text(
            text = if (isConnected) "Статус: Подключено" else "Статус: Отключено",
            color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Отображение последних полученных данных
        if (displayedData.isNotEmpty()) {
            val latestData = displayedData.last()

            // Карточка с текущими показаниями
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Текущие показания",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Температура",
                                color = Color(0xFF2196F3)
                            )
                            Text(
                                text = "${latestData.temperature}°C",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                        Column {
                            Text(
                                text = "Влажность",
                                color = Color(0xFF9C27B0)
                            )
                            Text(
                                text = "${latestData.humidity}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                    }

                    // Отображение времени получения данных
                    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val formattedTime = sdf.format(Date(latestData.timestamp))
                    Text(
                        text = "Получено: $formattedTime",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // График температуры
            if (displayedData.size > 1) {
                Text(
                    text = "График температуры",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)  // Увеличиваем высоту, чтобы вместить вертикальные метки
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Создаем модель данных для графика
                        val temperatureEntries = displayedData.mapIndexed { index, data ->
                            index.toFloat() to data.temperature
                        }.toTypedArray()

                        // Создаем метки времени для оси X
                        val timeLabels = displayedData.map { data ->
                            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            sdf.format(Date(data.timestamp))
                        }

                        // График занимает 80% высоты карточки
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .fillMaxWidth()
                        ) {
                            Chart(
                                chart = lineChart(),
                                model = entryModelOf(*temperatureEntries),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize(),
                                startAxis = startAxis(),
                                bottomAxis = bottomAxis(
                                    valueFormatter = { value, _ ->
                                        // Оставляем пустыми метки на самом графике
                                        ""
                                    }
                                )
                            )
                        }

                        // Отдельный блок с вертикальными метками времени (20% высоты)
                        Row(
                            modifier = Modifier
                                .weight(0.2f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Отображаем только некоторые метки, чтобы не было перегруженности
                            // Определяем шаг отображения в зависимости от количества точек
                            val step = maxOf(1, timeLabels.size / 5)

                            timeLabels.forEachIndexed { index, label ->
                                if (index % step == 0 || index == timeLabels.size - 1) {
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        modifier = Modifier.rotate(90f),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    // Пустой Spacer для других меток
                                    Spacer(modifier = Modifier.width(1.dp))
                                }
                            }
                        }
                    }
                }

                // График влажности
                Text(
                    text = "График влажности",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)  // Увеличиваем высоту, чтобы вместить вертикальные метки
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Создаем модель данных для графика
                        val humidityEntries = displayedData.mapIndexed { index, data ->
                            index.toFloat() to data.humidity
                        }.toTypedArray()

                        // Создаем метки времени для оси X
                        val timeLabels = displayedData.map { data ->
                            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            sdf.format(Date(data.timestamp))
                        }

                        // График занимает 80% высоты карточки
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .fillMaxWidth()
                        ) {
                            Chart(
                                chart = lineChart(),
                                model = entryModelOf(*humidityEntries),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize(),
                                startAxis = startAxis(),
                                bottomAxis = bottomAxis(
                                    valueFormatter = { value, _ ->
                                        // Оставляем пустыми метки на самом графике
                                        ""
                                    }
                                )
                            )
                        }

                        // Отдельный блок с вертикальными метками времени (20% высоты)
                        Row(
                            modifier = Modifier
                                .weight(0.2f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Отображаем только некоторые метки, чтобы не было перегруженности
                            // Определяем шаг отображения в зависимости от количества точек
                            val step = maxOf(1, timeLabels.size / 5)

                            timeLabels.forEachIndexed { index, label ->
                                if (index % step == 0 || index == timeLabels.size - 1) {
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        modifier = Modifier.rotate(90f),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    // Пустой Spacer для других меток
                                    Spacer(modifier = Modifier.width(1.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Недостаточно данных для построения графика",
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Color.Gray
                )
            }
        } else {
            // Если данных нет
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ожидаем данные...",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}