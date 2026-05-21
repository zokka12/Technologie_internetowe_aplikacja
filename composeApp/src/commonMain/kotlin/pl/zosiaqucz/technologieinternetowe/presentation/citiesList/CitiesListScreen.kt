package pl.zosiaqucz.technologieinternetowe.presentation.citiesList

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.zosiaqucz.technologieinternetowe.data.network.CityDto
import pl.zosiaqucz.technologieinternetowe.data.network.ApiClient
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

@Composable
fun CitiesListScreen(viewModel: CitiesListViewModel = viewModel { CitiesListViewModel() }) {
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Wszystkie", "Chcę odwiedzić", "Odwiedzone")

    var searchQuery by remember { mutableStateOf("") }
    var wantToVisitSet by remember { mutableStateOf(setOf<String>()) }
    var visitedSet by remember { mutableStateOf(setOf<String>()) }

    // Stan pamiętający wartości wpisywane do formularza POST
    var isFormVisible by remember { mutableStateOf(false) }
    var newCityName by remember { mutableStateOf("") }
    var newImageUrl by remember { mutableStateOf("") }
    var newAttrRating by remember { mutableStateOf(5f) }
    var newSafeRating by remember { mutableStateOf(5f) }
    var newFoodRating by remember { mutableStateOf(5f) }

    // Nowe zmienne przechowujące pobrane stopnie
    var latDegree by remember { mutableStateOf("") }
    var lonDegree by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Szukaj miasta (min. 3 litery)...") },
            singleLine = true
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Panel dodawania punktu",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { isFormVisible = !isFormVisible },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(if (isFormVisible) "Zwiń ▲" else "Rozwiń ▼", fontSize = 12.sp)
                    }
                }

                AnimatedVisibility(visible = isFormVisible) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = newCityName,
                            onValueChange = { newCityName = it },
                            label = { Text("Nazwa miasta") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newImageUrl,
                            onValueChange = { newImageUrl = it },
                            label = { Text("Plik graficzny (np. madrid.jpg)") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("wpisz samą nazwę pliku z rozszerzeniem") },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sekcja pobierania zewnętrznych współrzędnych
                        Button(
                            onClick = {
                                if (newCityName.isNotBlank()) {
                                    scope.launch {
                                        val coords = ApiClient.getCityCoordinates(newCityName)
                                        if (coords != null) {
                                            latDegree = coords.first
                                            lonDegree = coords.second
                                        } else {
                                            latDegree = "Brak danych"
                                            lonDegree = "Brak danych"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text("Pobierz stopnie geograficzne (Zewnętrzne API)")
                        }

                        if (latDegree.isNotEmpty()) {
                            Text(
                                text = "Szerokość (N/S): $latDegree°\nDługość (E/W): $lonDegree°",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ustaw wstępne oceny terenu:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        RatingSlider("Atrakcje", newAttrRating) { newAttrRating = it }
                        RatingSlider("Bezpieczeństwo", newSafeRating) { newSafeRating = it }
                        RatingSlider("Jedzenie", newFoodRating) { newFoodRating = it }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (newCityName.isNotBlank() && newImageUrl.isNotBlank()) {
                                    scope.launch {
                                        val fullUrl = "http://10.0.2.2:8080/${newImageUrl.trim()}"

                                        val isSuccess = ApiClient.addCity(
                                            cityName = newCityName.trim(),
                                            imageUrl = fullUrl,
                                            attractions = newAttrRating.toDouble(),
                                            safety = newSafeRating.toDouble(),
                                            food = newFoodRating.toDouble()
                                        )

                                        if (isSuccess) {
                                            newCityName = ""
                                            newImageUrl = ""
                                            newAttrRating = 5f
                                            newSafeRating = 5f
                                            newFoodRating = 5f
                                            latDegree = ""
                                            lonDegree = ""
                                            isFormVisible = false

                                            viewModel.fetchCitiesFromNetwork()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Wyślij do bazy (Zgłoszenie POST)")
                        }
                    }
                }
            }
        }

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (val currentState = state) {
            is CitiesState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is CitiesState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = currentState.message, color = Color.Red)
                }
            }
            is CitiesState.Success -> {
                val filteredList = currentState.cities.filter { city ->
                    val matchesSearch = if (searchQuery.length >= 3) {
                        city.cityName.contains(searchQuery, ignoreCase = true)
                    } else true

                    val matchesTab = when (selectedTabIndex) {
                        1 -> wantToVisitSet.contains(city.id)
                        2 -> visitedSet.contains(city.id)
                        else -> true
                    }

                    matchesSearch && matchesTab
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(filteredList) { city ->
                        RichCityItem(
                            city = city,
                            tabIndex = selectedTabIndex,
                            isWantToVisit = wantToVisitSet.contains(city.id),
                            isVisited = visitedSet.contains(city.id),
                            onWantToVisitChange = { isChecked ->
                                if (isChecked) {
                                    wantToVisitSet = wantToVisitSet + city.id
                                    visitedSet = visitedSet - city.id
                                } else {
                                    wantToVisitSet = wantToVisitSet - city.id
                                }
                            },
                            onVisitedChange = { isChecked ->
                                if (isChecked) {
                                    visitedSet = visitedSet + city.id
                                    wantToVisitSet = wantToVisitSet - city.id
                                } else {
                                    visitedSet = visitedSet - city.id
                                }
                            },
                            onWikiClick = {
                                uriHandler.openUri("https://pl.wikipedia.org/wiki/${city.cityName}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RichCityItem(
    city: CityDto,
    tabIndex: Int,
    isWantToVisit: Boolean,
    isVisited: Boolean,
    onWantToVisitChange: (Boolean) -> Unit,
    onVisitedChange: (Boolean) -> Unit,
    onWikiClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Box(modifier = Modifier.fillMaxWidth().height(180.dp).padding(bottom = 8.dp)) {
                KamelImage(
                    resource = asyncPainterResource(data = city.imageUrl),
                    contentDescription = city.cityName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onLoading = { _ -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) },
                    onFailure = { exception ->
                        Text(
                            text = "Błąd zdjęcia: ${exception.message}",
                            modifier = Modifier.align(Alignment.Center).padding(8.dp),
                            color = Color.Red,
                            fontSize = 10.sp
                        )
                    }
                )
            }

            Text(
                text = city.cityName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onWikiClick() }
            )
            Text(text = "Kliknij nazwę, aby otworzyć Wikipedię", fontSize = 10.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Oceny ogólne:", fontWeight = FontWeight.Bold)
            Text(text = "Atrakcje: ${city.attractionsRating} / 10")
            Text(text = "Bezpieczeństwo: ${city.safetyRating} / 10")
            Text(text = "Jedzenie: ${city.foodRating} / 10")

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            if (tabIndex == 2) {
                Text(text = "TWOJA OCENA:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                var attrRating by remember { mutableStateOf(5f) }
                var safeRating by remember { mutableStateOf(5f) }
                var foodRating by remember { mutableStateOf(5f) }

                RatingSlider("Atrakcje", attrRating) { attrRating = it }
                RatingSlider("Bezpieczeństwo", safeRating) { safeRating = it }
                RatingSlider("Jedzenie", foodRating) { foodRating = it }

                val myAverage = (attrRating + safeRating + foodRating) / 3f
                val roundedAverage = (myAverage * 10).toInt() / 10f
                Text("Twoja średnia: $roundedAverage / 10", fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isWantToVisit, onCheckedChange = onWantToVisitChange)
                Text("Chcę odwiedzić")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isVisited, onCheckedChange = onVisitedChange)
                Text("Odwiedzone")
            }
        }
    }
}

@Composable
fun RatingSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text(text = "$label: ${value.toInt()} / 10", fontSize = 12.sp)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 9
        )
    }
}