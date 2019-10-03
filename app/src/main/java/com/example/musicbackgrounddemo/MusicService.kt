package com.example.musicbackgrounddemo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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
    private var _player: MediaPlayer? = null
    private var _notificationLayout: RemoteViews? = null
    private var _pendingIntentToMainActivity: PendingIntent? = null
    private var _notificationBuilder: NotificationCompat.Builder? = null

    companion object {
        var IsPlaying = false
        var CurrentIndexSong = 0
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
        if (_player?.isPlaying == true) {
            Handler().postDelayed({
                MainActivity.CurrentPositionSeekBarLiveData.value =
                    _player?.currentPosition ?: 0
                updateSeekBar()
            }, 100)
        } else {
            return
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        _listSongResult = intent.getParcelableArrayListExtra(LIST_SONGS_EXTRA) ?: _listSongResult
        CurrentIndexSong = intent.getIntExtra(SONG_INDEX_EXTRA, CurrentIndexSong)
        checkActionIntent(intent)
        if (intent.action != ACTION_STOP) {
            initNotification()
        }
        return START_STICKY
    }

    private fun initNotification() {
        _notificationLayout?.setImageViewResource(
            R.id.btnPlay,
            if (IsPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
        val nameSong = _listSongResult[CurrentIndexSong].nameSong
        _notificationLayout?.setTextViewText(R.id.tvSongName, nameSong)
        MainActivity.CurrentNameSongLiveData.value = nameSong
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
            ACTION_SEEK_BAR_CHANGE -> setSeekBar(intent)
        }
        MainActivity.IsPlayingLiveData.value = _player?.isPlaying ?: false
    }

    private fun setSeekBar(intent: Intent) {
        if (_player != null) {
            val currentPosition = intent.getIntExtra(SEEK_BAR_EXTRA, 0)
            _percentSongLength = currentPosition
            _player!!.seekTo(currentPosition)
            _player!!.start()
        } else {
            _percentSongLength = 0
        }
        updateSeekBar()
    }

    private fun playNewSong() {
        playSongWithIndex(CurrentIndexSong)
    }

    private fun playOrPause() {
        if (_player != null) {
            if (_player!!.isPlaying) {
                _player!!.pause()
                IsPlaying = false
                _percentSongLength = _player!!.currentPosition
            } else {
                IsPlaying = true
                _player!!.seekTo(_percentSongLength)
                _player!!.start()
            }
            updateSeekBar()
        } else {
            playNewSong()
        }
    }

    private fun nextSong() {
        var index = CurrentIndexSong
        playSongWithIndex(++index)
    }

    private fun previousSong() {
        var index = CurrentIndexSong
        playSongWithIndex(--index)
    }

    private fun stopMusic() {
        IsPlaying = false
        _player?.stop()
        _player?.release()
        _player = null
    }

    private fun playSongWithIndex(indexSong: Int) {
        if (indexSong in 0.._listSongResult.size) {
            stopMusic()
            _player = MediaPlayer.create(this, _listSongResult[indexSong].songLocalUri)
            _player?.start()
            CurrentIndexSong = indexSong
            IsPlaying = true
            MainActivity.DurationLiveData.value = _player?.duration
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

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun setOnclickPendingIntent(
        context: Context?,
        action: String,
        remoteViews: RemoteViews,
        resourceId: Int
    ) {
        val intent = Intent(context, MusicBroadCastController::class.java)
        intent.action = action
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        remoteViews.setOnClickPendingIntent(resourceId, pendingIntent)
    }
}


class MusicBroadCastController : BroadcastReceiver() {
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


