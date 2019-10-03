package com.example.musicbackgrounddemo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.*
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.notif_music_control.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        var _isPlayingLiveData: MutableLiveData<Boolean> = MutableLiveData()
        var _currentNameSongLivedata: MutableLiveData<String> = MutableLiveData()
        var _currentPositonSeekbar: MutableLiveData<Int> = MutableLiveData()
        var _duration: MutableLiveData<Int> = MutableLiveData()
    }

    private var _intentMusicService: Intent? = null
    private var _musicAdapter: AdapterMusic = AdapterMusic(this)
    private var _isPlaying = false
    private var _listSongs: ArrayList<SongModel> = ArrayList()
    private var _currentIndexSong = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnPlay.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnPrevious.setOnClickListener(this)
        btnStop.setOnClickListener(this)
        _intentMusicService = Intent(this, MusicService::class.java)
        initRecycleView()
        initMusicService()
        imgSong.animation = animationImgSong
        animationImgSong.start()
        tvNameSong.animation = animationTextBlink
        animationTextBlink.start()
        _isPlayingLiveData.observe(this, Observer {
            _isPlaying = it
            updateControlBtn()
        })
        _currentNameSongLivedata.observe(this, Observer {
            tvNameSong.text = it
        })
        _currentPositonSeekbar.observe(this, Observer {
            seekBar.progress = it
        })
        _duration.observe(this, Observer {
            seekBar.max = it
        })


    }

    private fun updateControlBtn() {
        btnPlay.setImageResource(if (_isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
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
        val songModel = SongModel("song_gio", "Thanh tyluven", R.raw.song_gio)
        val songMode2 = SongModel("buoc_qua_doi_nhau", "Thanh tyluven", R.raw.buoc_qua_doi_nhau)
        val songMode3 =
            SongModel("Nco_tat_ca_nhung_thieu_em", "Thanh tyluven", R.raw.co_tat_ca_nhung_thieu_em)
        val songMode4 = SongModel("het_thuong_can_nho", "Thanh tyluven", R.raw.het_thuong_can_nho)
        val songMode5 =
            SongModel("la_ban_khong_the_yeu", "Thanh tyluven", R.raw.la_ban_khong_the_yeu)
        val songMode6 = SongModel("loi_yeu_ngay_dai", "Thanh tyluven", R.raw.loi_yeu_ngay_dai)
        val songMode7 =
            SongModel(
                "nuoc_mat_lau_bang_tinh_yeu",
                "Thanh tyluven",
                R.raw.nuoc_mat_lau_bang_tinh_yeu
            )
        val songMode8 = SongModel("the_tu", "Thanh tyluven", R.raw.the_tu)
        val songMode9 = SongModel("tuong_quan", "Thanh tyluven", R.raw.tuong_quan)
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

    private fun initMusicService() {
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
                _isPlaying = !_isPlaying
                _intentMusicService?.action = ACTION_PLAY_OR_PAUSE
                initMusicService()
            }
            btnNext -> {
                _intentMusicService?.action = ACTION_NEXT
                initMusicService()
            }
            btnPrevious -> {
                _intentMusicService?.action = ACTION_PREVIOUS
                initMusicService()
            }
            btnStop -> {
                _isPlaying = false
                stopService(Intent(this, MusicService::class.java))
            }
        }
        _currentIndexSong = MusicService._currentIndexSong
        updateControlBtn()
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!_isPlaying) {
                stopService(Intent(this, MusicService::class.java))
            }
        } else {
            stopService(Intent(this, MusicService::class.java))
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }

}

