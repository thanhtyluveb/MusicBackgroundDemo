package com.example.musicbackgrounddemo

data class SongModel(
    var nameSong: String,
    var authorSong: String,
    var songLocalUri: Int = 0,
    var imgSong: String = ""
)