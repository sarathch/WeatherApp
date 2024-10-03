package com.denshaotoko.weatherapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.denshaotoko.weatherapp.data.WeatherResponse

@Composable
fun WeatherScreen(
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val placeholderPrompt = stringResource(R.string.prompt_placeholder)
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    val locationPlaceholder = stringResource(R.string.location_placeholder)
    var locationInput by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by weatherViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.baking_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        Row(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            TextField(
                value = locationInput,
                label = { Text(stringResource(R.string.label_prompt)) },
                placeholder = { Text(locationPlaceholder) },
                onValueChange = { locationInput = it },
                modifier = Modifier
                    .weight(0.8f)
                    .padding(end = 16.dp)
                    .align(Alignment.CenterVertically)
            )

            Button(
                onClick = {
                    weatherViewModel.getWeatherData(locationInput)
                },
                enabled = locationInput.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                Text(text = stringResource(R.string.action_go))
            }
        }

        if (uiState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            var textColor = MaterialTheme.colorScheme.onSurface
            var weatherData: WeatherResponse? = null
            var city = ""
            if (uiState is UiState.Error) {
                textColor = MaterialTheme.colorScheme.error
                result = (uiState as UiState.Error).errorMessage
            } else if (uiState is UiState.Success) {
                textColor = MaterialTheme.colorScheme.onSurface
                result = (uiState as UiState.Success).outputText
                weatherData = (uiState as UiState.Success).weatherResponse
                city = (uiState as UiState.Success).city
            }
            val scrollState = rememberScrollState()
            weatherData?.let { data ->
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Current temperature in $city:"
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                ) {
                    Text(
                        text = "${kelvinToCelsius(data.main.temp)}",
                        fontSize = 100.sp, // Set font size to 100sp
                        lineHeight = 100.sp, // Adjust line height to match font size
                        maxLines = 1,
                        softWrap = false
                    )

                    Text(
                        text = "°C",
                        fontSize = 25.sp, // Set font size to 50sp
                    )
                }

                Image(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(300.dp),
                    painter = rememberImagePainter(
                        data = "https://openweathermap.org/img/wn/${weatherData.weather[0].icon}@2x.png"
                    ),
                    contentDescription = "My Image Description"
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(16.dp),
                    text = "Description: ${data.weather[0].description}"
                )
            }

            Text(
                text = result,
                textAlign = TextAlign.Start,
                color = textColor,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .size(300.dp)
                    .verticalScroll(scrollState)
            )
        }

        Text(
            modifier = Modifier
                .align(Alignment.Start)
                .padding(16.dp),
            text = "Developer: Sarath Yennamani"
        )
        val uriHandler = LocalUriHandler.current
        val annotatedString =buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.White)) {
                append("Submitted to: ")
            }

            pushStringAnnotation(tag = "URL", annotation = "https://www.pmaccelerator.io/")
            withStyle(style = SpanStyle(
                color = Color.Cyan,
                textDecoration = TextDecoration.Underline
            )
            ) {
                append("PMA")
            }
            pop()
        }
        ClickableText(
            modifier = Modifier
                .align(Alignment.Start)
                .padding(16.dp),
            text = annotatedString,
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        uriHandler.openUri(annotation.item)
                    }
            }
        )
    }
}

fun kelvinToCelsius(kelvin: Double): Double {
    return String.format("%.2f", kelvin - 273.15).toDouble()
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    WeatherScreen()
}