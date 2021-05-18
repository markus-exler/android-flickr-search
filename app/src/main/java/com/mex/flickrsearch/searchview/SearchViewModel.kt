package com.mex.flickrsearch.searchview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mex.flickrsearch.data.IFlickrPictureRepository
import com.mex.flickrsearch.data.model.Picture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val _repository: IFlickrPictureRepository) :
    ViewModel() {

    private var _searchJob: Job? = null

    private val _pictures = MutableLiveData<PagingData<Picture>>()
    val pictures: LiveData<PagingData<Picture>>
        get() = _pictures


    private var _currentQuery = MutableLiveData<String>()
    val currentQuery: LiveData<String>
        get() = _currentQuery

    init {
        searchPictures("helicopter")
    }

    fun searchPictures(queryString: String) {
        if (queryString == _currentQuery.value) return
        _searchJob?.cancel()
        _currentQuery.value = queryString
        _searchJob = viewModelScope.launch(Dispatchers.IO) {
            _repository.getPictureSearchResultStream(queryString).cachedIn(viewModelScope)
                .collectLatest {
                    _pictures.postValue(it)
                }
        }
    }
}