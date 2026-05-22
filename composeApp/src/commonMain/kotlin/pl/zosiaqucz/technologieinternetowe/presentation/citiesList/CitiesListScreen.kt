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
    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    // 🌟 NOWOŚĆ: Dodana czwarta zakładka na geodezyjne stopnie
    val tabs = listOf("Wszystkie", "Chcę odwiedzić", "Odwiedzone", "Szukaj Stopni")

    var searchQuery by remember { mutableStateOf("") }
    var isFormVisible by remember { mutableStateOf(false) }
    var newCityName by remember { mutableStateOf("") }
    var newImageUrl by remember { mutableStateOf("") }
    var newAttrRating by remember { mutableStateOf(5f) }
    var newSafeRating by remember { mutableStateOf(5f) }
    var newFoodRating by remember { mutableStateOf(5f) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Szukaj miasta (min. 3 litery)...") },
            singleLine = true
        )

        // --- Panel dodawania miasta (Oczyszczony ze stopni geograficznych) ---
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
                    Text("Panel dodawania punktu", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Button(onClick = { isFormVisible = !isFormVisible }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                        Text(if (isFormVisible) "Zwiń ▲" else "Rozwiń ▼", fontSize = 12.sp)
                    }
                }

                AnimatedVisibility(visible = isFormVisible) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(value = newCityName, onValueChange = { newCityName = it }, label = { Text("Nazwa miasta") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = newImageUrl, onValueChange = { newImageUrl = it }, label = { Text("Plik graficzny") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("wpisz samą nazwę pliku z rozszerzeniem") }, singleLine = true)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ustaw wstępne oceny terenu:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        RatingSlider("Atrakcje", newAttrRating) { newAttrRating = it }
                        RatingSlider("Bezpieczeństwo", newSafeRating) { newSafeRating = it }
                        RatingSlider("Jedzenie", newFoodRating) { newFoodRating = it }
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = {
                            if (newCityName.isNotBlank() && newImageUrl.isNotBlank()) {
                                coroutineScope.launch {
                                    val fullUrl = "http://10.0.2.2:8080/${newImageUrl.trim()}"
                                    val isSuccess = ApiClient.addCity(newCityName.trim(), fullUrl, newAttrRating.toDouble(), newSafeRating.toDouble(), newFoodRating.toDouble())
                                    if (isSuccess) {
                                        newCityName = ""; newImageUrl = ""; newAttrRating = 5f; newSafeRating = 5f; newFoodRating = 5f; isFormVisible = false
                                        viewModel.fetchCitiesFromNetwork()
                                    }
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()
                        ) { Text("Wyślij do bazy (Zgłoszenie POST)") }
                    }
                }
            }
        }

        // --- Zakładki ---
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 0.dp // Usuwa pusty margines po lewej stronie
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        // 🌟 NOWOŚĆ: Logika sterująca głównym widokiem ekranu
        if (selectedTabIndex == 3) {
            // Jeśli wybrana jest 4 zakładka, uruchamiamy nowy komponent wyszukiwarki
            GeodeticSearchTab()
        } else {
            // W przeciwnym razie wyświetlamy standardową listę miast
            when (val currentState = state) {
                is CitiesState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is CitiesState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = currentState.message, color = Color.Red) }
                is CitiesState.Success -> {

                    val filteredList = currentState.cities.filter { city ->
                        val matchesSearch = if (searchQuery.length >= 3) {
                            city.cityName.contains(searchQuery, ignoreCase = true)
                        } else true

                        val matchesTab = when (selectedTabIndex) {
                            1 -> city.status == "TO_SEE"
                            2 -> city.status == "VISITED"
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
                                onWikiClick = { uriHandler.openUri("https://pl.wikipedia.org/wiki/${city.cityName}") },
                                onDataSaved = { viewModel.fetchCitiesFromNetwork() }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Karta Pojedynczego Miasta (Bez Zmian) ---
@Composable
fun RichCityItem(
    city: CityDto,
    tabIndex: Int,
    onWikiClick: () -> Unit,
    onDataSaved: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var attrRating by remember(city) { mutableStateOf(city.attractionsRating.toFloat()) }
    var safeRating by remember(city) { mutableStateOf(city.safetyRating.toFloat()) }
    var foodRating by remember(city) { mutableStateOf(city.foodRating.toFloat()) }

    var isVisited by remember(city) { mutableStateOf(city.status == "VISITED") }
    var wantsToSee by remember(city) { mutableStateOf(city.status == "TO_SEE") }

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {

            Box(modifier = Modifier.fillMaxWidth().height(180.dp).padding(bottom = 8.dp)) {
                KamelImage(
                    resource = asyncPainterResource(data = city.imageUrl),
                    contentDescription = city.cityName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onLoading = { _ -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) },
                    onFailure = { _ -> Text(text = "Błąd zdjęcia", modifier = Modifier.align(Alignment.Center).padding(8.dp), color = Color.Red, fontSize = 10.sp) }
                )
            }

            Text(text = city.cityName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onWikiClick() })
            Text(text = "Kliknij nazwę, aby otworzyć Wikipedię", fontSize = 10.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Oceny ogólne:", fontWeight = FontWeight.Bold)
            Text(text = "Atrakcje: ${city.attractionsRating} / 10")
            Text(text = "Bezpieczeństwo: ${city.safetyRating} / 10")
            Text(text = "Jedzenie: ${city.foodRating} / 10")

            val dbAverage = (city.attractionsRating + city.safetyRating + city.foodRating) / 3.0
            val roundedDbAverage = (dbAverage * 10).toInt() / 10f
            Text("Średnia ogólna punktu: $roundedDbAverage / 10", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = wantsToSee, onCheckedChange = { isChecked ->
                    wantsToSee = isChecked
                    if (isChecked) isVisited = false

                    coroutineScope.launch {
                        val newStatus = if (isChecked) "TO_SEE" else "NONE"
                        ApiClient.updateCityDetails(id = city.id, status = newStatus)
                        onDataSaved()
                    }
                })
                Text("Chcę odwiedzić")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isVisited, onCheckedChange = { isChecked ->
                    isVisited = isChecked
                    if (isChecked) wantsToSee = false

                    coroutineScope.launch {
                        val newStatus = if (isChecked) "VISITED" else "NONE"
                        ApiClient.updateCityDetails(id = city.id, status = newStatus)
                        onDataSaved()
                    }
                })
                Text("Odwiedzone")
            }

            if (tabIndex == 2) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(text = "TWOJA OCENA (Edytuj i zapisz):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                RatingSlider("Atrakcje", attrRating) { attrRating = it }
                RatingSlider("Bezpieczeństwo", safeRating) { safeRating = it }
                RatingSlider("Jedzenie", foodRating) { foodRating = it }

                val myAverage = (attrRating + safeRating + foodRating) / 3f
                val roundedAverage = (myAverage * 10).toInt() / 10f
                Text("Twoja średnia suwaków: $roundedAverage / 10", fontWeight = FontWeight.ExtraBold)

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = ApiClient.updateCityDetails(
                                id = city.id,
                                attractions = attrRating.toDouble(),
                                safety = safeRating.toDouble(),
                                food = foodRating.toDouble(),
                                status = "VISITED"
                            )
                            if (success) onDataSaved()
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
                ) {
                    Text("Zapisz oceny")
                }
            }
        }
    }
}

// --- Komponent Suwaka (Bez Zmian) ---
@Composable
fun RatingSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text(text = "$label: ${value.toInt()} / 10", fontSize = 12.sp)
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..10f, steps = 9)
    }
}

// 🌟 NOWOŚĆ: Dedykowana wyszukiwarka stopni geograficznych
@Composable
fun GeodeticSearchTab() {
    val coroutineScope = rememberCoroutineScope()
    var searchCity by remember { mutableStateOf("") }
    var latDegree by remember { mutableStateOf("") }
    var lonDegree by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Geodezyjny Lokalizator Stopni",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = searchCity,
            onValueChange = { searchCity = it },
            label = { Text("Wpisz nazwę miasta") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (searchCity.isNotBlank()) {
                    isLoading = true
                    coroutineScope.launch {
                        val coords = ApiClient.getCityCoordinates(searchCity)
                        if (coords != null) {
                            latDegree = coords.first
                            lonDegree = coords.second
                        } else {
                            latDegree = "Błąd / Brak danych"
                            lonDegree = "Błąd / Brak danych"
                        }
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Pobierz stopnie geograficzne")
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (latDegree.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Wynik dla: ${searchCity.uppercase()}", fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(text = "Szerokość (N/S): $latDegree°", fontSize = 18.sp)
                    Text(text = "Długość (E/W): $lonDegree°", fontSize = 18.sp)
                }
            }
        }
    }
}