package com.example.musicbackgrounddemo

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.startForegroundService


open class MusicService : Service() {
    private var _percentSongLength = 0
    private var _listSongResult: ArrayList<SongModel> = ArrayList()
    private var _player: MediaPlayer? = MediaPlayer()
    private var _notificationLayout: RemoteViews? = null
    private var _pendingIntentToMainActivity: PendingIntent? = null
    private var _notificationBuilder: NotificationCompat.Builder? = null

    companion object {
        var _isPlaying = false
        var _currentIndexSong = 0
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        _notificationLayout = RemoteViews(packageName, R.layout.notif_music_control)

        setOnclickPendingIntent(this, ACTION_PLAY_OR_PAUSE, _notificationLayout!!, R.id.btnPlay)
        setOnclickPendingIntent(this, ACTION_PREVIOUS, _notificationLayout!!, R.id.btnPrevious)
        setOnclickPendingIntent(this, ACTION_NEXT, _notificationLayout!!, R.id.btnNext)
        setOnclickPendingIntent(this, ACTION_STOP, _notificationLayout!!, R.id.btnStop)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        _pendingIntentToMainActivity = PendingIntent.getActivity(this, 0, intent, 0)
        _player?.setOnCompletionListener {
            nextSong()
        }
        _notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.app_name))
            .setContent(_notificationLayout)
            .setSmallIcon(R.drawable.zing_mp3)
            .setContentIntent(_pendingIntentToMainActivity)
            .setAutoCancel(true)
        startForeground(1, _notificationBuilder?.build())
    }

    private fun updateSeekBar() {
        _player?.let {
            if (_isPlaying) {
                Handler().postDelayed({
                    updateSeekBar()
                    MainActivity._currentPositonSeekbar.value = it.currentPosition
                }, 100)
            }
        }
    }

    override fun onLowMemory() {
        stopSelf()
        super.onLowMemory()
    }

    override fun onDestroy() {
        stopMusic()
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        _listSongResult = intent.getParcelableArrayListExtra(LIST_SONGS_EXTRA) ?: _listSongResult
        _currentIndexSong = intent.getIntExtra(SONG_INDEX_EXTRA, _currentIndexSong)
        checkActionIntent(intent)
        initNotification()
        MainActivity._isPlayingLiveData.value = _isPlaying
        return START_STICKY
    }

    private fun initNotification() {
        _notificationLayout?.setImageViewResource(
            R.id.btnPlay,
            if (_isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
        val nameSong = _listSongResult[_currentIndexSong].nameSong
        _notificationLayout?.setTextViewText(
            R.id.tvNameSong,
            nameSong
        )
        MainActivity._currentNameSongLivedata.value = nameSong
        _notificationBuilder?.setContent(_notificationLayout)
        with(NotificationManagerCompat.from(this)) {
            _notificationBuilder?.build()?.let { notify(1, it) }
        }
    }

    private fun checkActionIntent(intent: Intent) {
        when (intent.action) {
            ACTION_PLAY_OR_PAUSE -> playOrPause()
            ACTION_NEXT -> nextSong()
            ACTION_PREVIOUS -> previousSong()
            ACTION_STOP -> {
                stopMusic()
                stopSelf()
            }
            ACTION_NEW_PLAY -> playNewSong()
        }
    }

    private fun playNewSong() {
        playSongWithIndex(_currentIndexSong)
    }

    private fun playOrPause() {
        _player?.let { media ->
            if (media.isPlaying) {
                media.pause()
                _isPlaying = false
                _percentSongLength = media.currentPosition
            } else {
                _isPlaying = true
                media.seekTo(_percentSongLength)
                media.start()
            }
        }
    }

    private fun nextSong() {
        var index = _currentIndexSong
        playSongWithIndex(++index)
    }

    private fun previousSong() {
        var index = _currentIndexSong
        playSongWithIndex(--index)
    }

    private fun stopMusic() {
        _isPlaying = false
        _player?.stop()
        _player?.release()
        _player = null
    }

    private fun playSongWithIndex(indexSong: Int) {
        if (indexSong in 0.._listSongResult.size) {
            stopMusic()
            _player = MediaPlayer.create(this, _listSongResult[indexSong].songLocalUri)
            _player?.start()
            _currentIndexSong = indexSong
            _isPlaying = true
            MainActivity._duration.value = _player?.duration
            updateSeekBar()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }
}

fun setOnclickPendingIntent(
    context: Context?,
    action: String,
    remoteViews: RemoteViews,
    resourceId: Int
) {
    val intent = Intent(context, MusicBroadCastReceiver::class.java)
    intent.action = action
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
    remoteViews.setOnClickPendingIntent(resourceId, pendingIntent)
}

class MusicBroadCastReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val intentMusicService = Intent(p0, MusicService::class.java)
        when (p1?.action) {
            ACTION_PLAY_OR_PAUSE -> intentMusicService.action = ACTION_PLAY_OR_PAUSE
            ACTION_NEXT -> intentMusicService.action = ACTION_NEXT
            ACTION_PREVIOUS -> intentMusicService.action = ACTION_PREVIOUS
            ACTION_STOP -> intentMusicService.action = ACTION_STOP
        }
        p0?.let { startForegroundService(it, intentMusicService) }
    }
}


