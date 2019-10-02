package com.example.musicbackgrounddemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.os.Build
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.notif_music_control.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var intentBroadcast: Intent? = null
    private var _musicAdapter: AdapterMusic = AdapterMusic(this)
    private lateinit var _musicViewModel: MusicViewModel
    private var listSongs: ArrayList<SongModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        _musicViewModel = ViewModelProviders.of(this).get(MusicViewModel::class.java)

        _musicViewModel._isPlaying.value = false
        btnPlay.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnPrevious.setOnClickListener(this)
        intentBroadcast = Intent(this, MusicBroadCastReceiver::class.java)
        intentBroadcast?.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        initMusicService()


        imgSong.animation = animationImgSong
        animationImgSong.start()
        initRecycleView()
    }

    private val animationImgSong: Animation
        get() {
            return RotateAnimation(
                0f,
                360f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            ).apply {
                this.interpolator = LinearInterpolator()
                this.repeatCount = Animation.INFINITE
                this.duration = 5000
            }
        }

    private fun initRecycleView() {
        val songModel = SongModel("Nguyen Duc Thanh", "Thanh tyluven", R.raw.song_gio)
        val songMode2 = SongModel("Nguyen Duc Thanh", "Thanh tyluven", R.raw.buoc_qua_doi_nhau)
        val songMode3 =
            SongModel("Nguyen Duc Thanh", "Thanh tyluven", R.raw.co_tat_ca_nhung_thieu_em)
        val songMode4 = SongModel("Nguyen Duc Thanh", "Thanh tyluven", R.raw.het_thuong_can_nho)
        val songMode5 = SongModel("Nguyen Duc Thanh", "Thanh tyluven", R.raw.la_ban_khong_the_yeu)
        val songMode6 = SongModel("Nguyen Duc Thanh", "Thanh tyluven", R.raw.loi_yeu_ngay_dai)
        val songMode7 =
            SongModel("Nguyen Duc Thanh", "Thanh tyluven", R.raw.nuoc_mat_lau_bang_tinh_yeu)
        val songMode8 = SongModel("Nguyen Duc Thanh", "Thanh tyluven", R.raw.the_tu)
        val songMode9 = SongModel("Nguyen Duc Thanh", "Thanh tyluven", R.raw.tuong_quan)
        listSongs = arrayListOf(
            songModel,
            songMode2,
            songMode3,
            songMode4,
            songMode5,
            songMode6,
            songMode7,
            songMode8,
            songMode9
        )
        _musicAdapter.setListMusic(listSongs)
        recyclerViewMusic.adapter = _musicAdapter
        recyclerViewMusic.layoutManager = LinearLayoutManager(this)
    }

    private fun initMusicService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(
                Intent(
                    this,
                    MusicService::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
    }

    override fun onClick(view: View) {
        when (view) {
            btnPlay -> {
                intentBroadcast?.putExtra(SONG_NAME_EXTRA, listSongs[0].songLocalUri)
                intentBroadcast?.action = ACTION_PLAY
            }
            btnNext -> {
                intentBroadcast?.action = ACTION_NEXT
            }
            btnPrevious -> {
                intentBroadcast?.action = ACTION_PREVIOUS
            }
        }
        sendBroadcast(intentBroadcast)
    }
}

