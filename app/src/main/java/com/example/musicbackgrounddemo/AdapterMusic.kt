package com.example.musicbackgrounddemo

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_songs.view.*

class AdapterMusic(var context: Context) : RecyclerView.Adapter<AdapterMusic.MusicViewHolder>() {
    private var listSongs: ArrayList<SongModel> = ArrayList()
    fun setListMusic(arrayList: ArrayList<SongModel>) {
        this.listSongs = arrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_songs, parent, false)
        return MusicViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listSongs.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val intentMusicService = Intent(context, MusicService::class.java)
        val itemSong = listSongs[position]
        holder.itemView.apply {
            this.tvSongNameItem.text = itemSong.nameSong
            this.tvSongAuthorItem.text = itemSong.authorSong
        }.setOnClickListener {
            intentMusicService.action = ACTION_NEW_PLAY
            intentMusicService.putExtra(SONG_INDEX_EXTRA, position)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intentMusicService)
            }else{
                context.startService(intentMusicService)
            }
        }
    }

    class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}