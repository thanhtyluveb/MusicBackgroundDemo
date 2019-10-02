package com.example.musicbackgrounddemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MusicViewModel : ViewModel() {
    val _isPlaying: MutableLiveData<Boolean> = MutableLiveData()

    init {
        _isPlaying.value = false
    }

    fun setIsPlaying() {

    }
}