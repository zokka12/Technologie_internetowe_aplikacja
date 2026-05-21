package pl.zosiaqucz.technologieinternetowe

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

import androidx.lifecycle.viewmodel.compose.viewModel
import pl.zosiaqucz.technologieinternetowe.presentation.citiesList.CitiesListScreen
import pl.zosiaqucz.technologieinternetowe.presentation.citiesList.CitiesListViewModel

@Composable
fun App() {
    MaterialTheme {

        val viewModel: CitiesListViewModel = viewModel {
            CitiesListViewModel()
        }

        CitiesListScreen(viewModel = viewModel)
    }
}