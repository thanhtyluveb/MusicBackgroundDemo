package com.example.musicbackgrounddemo

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.item_songs.view.*
import java.util.zip.Inflater

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
        val itemSong = listSongs[position]
        holder.itemView.apply {
            this.tvSongName.text = itemSong.nameSong
            this.tvSongAuthor.text = itemSong.authorSong
        }.setOnClickListener {
            val intent = Intent(context, MusicBroadCastReceiver::class.java)
            intent.action = ACTION_PLAY
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra(SONG_NAME_EXTRA, itemSong.songLocalUri)
            context.sendBroadcast(intent)
        }
    }

    class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}