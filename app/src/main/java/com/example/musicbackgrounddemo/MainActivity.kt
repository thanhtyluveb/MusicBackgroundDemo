package com.example.musicbackgrounddemo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.notif_music_control.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        var IsPlayingLiveData: MutableLiveData<Boolean> = MutableLiveData()
        var CurrentNameSongLiveData: MutableLiveData<String> = MutableLiveData()
        var CurrentPositionSeekBarLiveData: MutableLiveData<Int> = MutableLiveData()
        var DurationLiveData: MutableLiveData<Int> = MutableLiveData()
    }

    private var _intentMusicService: Intent? = null
    private var _musicAdapter: AdapterMusic = AdapterMusic(this)
    private var _listSongs: ArrayList<SongModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        _intentMusicService = Intent(this, MusicService::class.java)
        initOnclickListener()
        initRecycleView()
        startMusicService()
        initObserveMusicService()
        initAnimation()
    }

    private fun initAnimation() {
        imgSong.animation = animationImgSong
        tvSongName.animation = animationTextBlink
    }

    private fun initOnclickListener() {
        btnPlay.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnPrevious.setOnClickListener(this)
        btnStop.setOnClickListener(this)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                onClick(btnPlay)
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                _intentMusicService?.action = ACTION_SEEK_BAR_CHANGE
                _intentMusicService?.putExtra(SEEK_BAR_EXTRA, p0?.progress)
                startMusicService()
            }
        })
    }

    private fun initObserveMusicService() {
        IsPlayingLiveData.observe(this, Observer {
            btnPlay.setImageResource(if (it) R.drawable.ic_pause else R.drawable.ic_play)
        })
        CurrentNameSongLiveData.observe(this, Observer {
            tvSongName.text = it
        })
        CurrentPositionSeekBarLiveData.observe(this, Observer {
            seekBar.progress = it
        })
        DurationLiveData.observe(this, Observer {
            seekBar.max = it
        })
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

    private val animationTextBlink: Animation
        get() {
            return AlphaAnimation(0.0f, 1.0f).apply {
                this.duration = 200 //You can manage the blinking time with this parameter
                this.startOffset = 20
                this.repeatMode = Animation.REVERSE
                this.repeatCount = Animation.INFINITE
            }
        }

    private fun initRecycleView() {
        val songModel = SongModel("song_gio", "Jack - KICM", R.raw.song_gio)
        val songMode2 = SongModel("buoc_qua_doi_nhau", "Le Bao Binh", R.raw.buoc_qua_doi_nhau)
        val songMode3 =
            SongModel("co_tat_ca_nhung_thieu_em", "Erik", R.raw.co_tat_ca_nhung_thieu_em)
        val songMode4 = SongModel("het_thuong_can_nho", "Duc Phuc", R.raw.het_thuong_can_nho)
        val songMode5 =
            SongModel("la_ban_khong_the_yeu", "Unknown", R.raw.la_ban_khong_the_yeu)
        val songMode6 = SongModel("loi_yeu_ngay_dai", "Unknown", R.raw.loi_yeu_ngay_dai)
        val songMode7 =
            SongModel(
                "nuoc_mat_lau_bang_tinh_yeu",
                "Unknown",
                R.raw.nuoc_mat_lau_bang_tinh_yeu
            )
        val songMode8 = SongModel("the_tu", "Minh Vuong", R.raw.the_tu)
        val songMode9 = SongModel("tuong_quan", "Minh Vuong", R.raw.tuong_quan)
        _listSongs = arrayListOf(
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
        _musicAdapter.setListMusic(_listSongs)
        recyclerViewMusic.adapter = _musicAdapter
        recyclerViewMusic.layoutManager = LinearLayoutManager(this)
    }

    private fun startMusicService() {
        _intentMusicService?.putParcelableArrayListExtra(LIST_SONGS_EXTRA, _listSongs)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(_intentMusicService)
        } else {
            startService(_intentMusicService)
        }
    }

    override fun onClick(view: View) {
        when (view) {
            btnPlay -> {
                _intentMusicService?.action = ACTION_PLAY_OR_PAUSE
            }
            btnNext -> {
                _intentMusicService?.action = ACTION_NEXT
            }
            btnPrevious -> {
                _intentMusicService?.action = ACTION_PREVIOUS
            }
            btnStop -> {
                IsPlayingLiveData.value = false
                _intentMusicService?.action = ACTION_STOP
            }
        }
        startMusicService()
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (IsPlayingLiveData.value == false) {
                stopService(Intent(this, MusicService::class.java))
            }
        } else {
            stopService(Intent(this, MusicService::class.java))
        }
        super.onDestroy()
    }
}

