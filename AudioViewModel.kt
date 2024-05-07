package screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.viewmodel

import androidx.lifecycle.*
import screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.repository.MediaStoreAudioRepository
import screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.utils.DataLoadingStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(private val audioRepository: MediaStoreAudioRepository) :
    ViewModel() {

    private val _dataLoadingStats = MutableLiveData<DataLoadingStats>()
    val dataLoadingStats: LiveData<DataLoadingStats>
        get() = _dataLoadingStats

    val data = liveData(Dispatchers.IO) {
        _dataLoadingStats.postValue(DataLoadingStats.Loading)

        try {
            audioRepository.getAllAudio().catch { e ->
                _dataLoadingStats.postValue(DataLoadingStats.Failure("Error loading data ${e.localizedMessage}"))
            }.collect {
                _dataLoadingStats.postValue(DataLoadingStats.Completed)
                emit(it)
            }
        } catch (e: IOException) {
            _dataLoadingStats.postValue(DataLoadingStats.Failure("Error loading data"))
        }
    }

    fun loadAllAudiosData() {
        viewModelScope.launch {
            data
        }
    }
}