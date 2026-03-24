package com.weather.app.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.weather.app.R
import com.weather.app.domain.model.Forecast
import com.weather.app.domain.model.Weather

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.any { it.value }
        if (granted) {
            activity?.let { viewModel.loadCurrentLocation(it) }
        }
    }
    
    LaunchedEffect(Unit) {
        if (activity != null && !hasLocationPermission(context)) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.refreshWeather() },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.currentLocationWeather == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.currentLocationWeather?.let { weather ->
                        item {
                            CurrentLocationWeatherCard(
                                weather = weather,
                                formatTemperature = { viewModel.formatTemperature(it) }
                            )
                        }
                    }
                    
                    if (uiState.otherCitiesWeather.isNotEmpty()) {
                        items(uiState.otherCitiesWeather) { weather ->
                            CityWeatherCard(
                                weather = weather,
                                formatTemperature = { viewModel.formatTemperature(it) }
                            )
                        }
                    }
                    
                    if (uiState.currentLocationWeather == null && uiState.otherCitiesWeather.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_cities),
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun CurrentLocationWeatherCard(
    weather: Weather,
    formatTemperature: (Double) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.current_location),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = weather.cityName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = formatTemperature(weather.temp),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = weather.condition,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem(
                    icon = Icons.Default.Thermostat,
                    label = stringResource(R.string.feels_like),
                    value = formatTemperature(weather.feelsLike)
                )
                WeatherDetailItem(
                    icon = Icons.Default.WaterDrop,
                    label = stringResource(R.string.humidity),
                    value = "${weather.humidity}%"
                )
                WeatherDetailItem(
                    icon = Icons.Default.Air,
                    label = stringResource(R.string.wind),
                    value = "${weather.windSpeed} km/h"
                )
            }
            
            if (weather.forecast.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.forecast),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                ForecastRow(
                    forecasts = weather.forecast.take(3),
                    formatTemperature = formatTemperature
                )
            }
        }
    }
}

@Composable
fun CityWeatherCard(
    weather: Weather,
    formatTemperature: (Double) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = weather.cityName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatTemperature(weather.temp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val todayForecast = weather.forecast.firstOrNull()
                    if (todayForecast != null) {
                        Text(
                            text = "${formatTemperature(todayForecast.tempMax)}/${formatTemperature(todayForecast.tempMin)} ${weather.condition}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (weather.forecast.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                ForecastRow(
                    forecasts = weather.forecast.take(5),
                    formatTemperature = formatTemperature
                )
            }
        }
    }
}

@Composable
fun WeatherDetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ForecastRow(
    forecasts: List<Forecast>,
    formatTemperature: (Double) -> String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        forecasts.forEachIndexed { index, forecast ->
            ForecastItem(
                forecast = forecast,
                index = index,
                formatTemperature = formatTemperature
            )
        }
    }
}

@Composable
fun ForecastItem(
    forecast: Forecast,
    index: Int,
    formatTemperature: (Double) -> String
) {
    val today = java.time.LocalDate.now()
    val forecastDate = try {
        val parts = forecast.date.split("-")
        if (parts.size >= 3) {
            java.time.LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        } else null
    } catch (e: Exception) { null }
    
    val dayLabel = if (forecastDate != null) {
        val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(today, forecastDate).toInt()
        when (daysDiff) {
            0 -> "今天"
            1 -> "明天"
            2 -> "后天"
            else -> {
                val dayOfWeek = forecastDate.dayOfWeek
                when (dayOfWeek) {
                    java.time.DayOfWeek.MONDAY -> "周一"
                    java.time.DayOfWeek.TUESDAY -> "周二"
                    java.time.DayOfWeek.WEDNESDAY -> "周三"
                    java.time.DayOfWeek.THURSDAY -> "周四"
                    java.time.DayOfWeek.FRIDAY -> "周五"
                    java.time.DayOfWeek.SATURDAY -> "周六"
                    java.time.DayOfWeek.SUNDAY -> "周日"
                    else -> ""
                }
            }
        }
    } else {
        when (index) {
            0 -> "今天"
            1 -> "明天"
            2 -> "后天"
            else -> ""
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        val dateText = try {
            val parts = forecast.date.split("-")
            if (parts.size >= 3) "${parts[1]}/${parts[2]}" else forecast.date
        } catch (e: Exception) {
            forecast.date.takeLast(5).replace("-", "/")
        }
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${formatTemperature(forecast.tempMax)}/${formatTemperature(forecast.tempMin)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = forecast.condition,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
