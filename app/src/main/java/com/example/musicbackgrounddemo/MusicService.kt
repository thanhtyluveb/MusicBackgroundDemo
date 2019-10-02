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
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startForegroundService


open class MusicService : Service() {
    private var percentSongLengh = 0
    var currentIndexSong = 0
    private var _listSongResult: ArrayList<SongModel> = ArrayList()
    private var _Player: MediaPlayer? = MediaPlayer()
    private val CHANNEL_ID = "ForegroundServiceChannel"

    override fun onCreate() {
        createNotificationChannel()
        val notificationLayout = RemoteViews(packageName, R.layout.notif_music_control)
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        setOnclickPendingIntent(this, ACTION_PLAY_OR_PAUSE, notificationLayout, R.id.btnPlay)
        setOnclickPendingIntent(this, ACTION_PREVIOUS, notificationLayout, R.id.btnPrevious)
        setOnclickPendingIntent(this, ACTION_NEXT, notificationLayout, R.id.btnNext)
        setOnclickPendingIntent(this, ACTION_STOP, notificationLayout, R.id.btnStop)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.app_name))
            .setContent(notificationLayout)
            .setSmallIcon(R.drawable.zing_mp3)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        startForeground(1, notification)

        _Player?.setOnCompletionListener {
            nextSong()
        }
        super.onCreate()
    }

    override fun onLowMemory() {
        stopSelf()
        super.onLowMemory()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        _listSongResult = intent.getParcelableArrayListExtra(LIST_SONGS_EXTRA) ?: _listSongResult
        currentIndexSong = intent.getIntExtra(SONG_INDEX_EXTRA, currentIndexSong)
        checkActionIntent(intent)
        return START_STICKY
    }

    private fun checkActionIntent(intent: Intent) {
        when (intent.action) {
            ACTION_PLAY_OR_PAUSE -> playOrPause(intent)
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
        playSongWithIndex(currentIndexSong)
    }

    private fun playOrPause(intent: Intent) {
        _Player?.let { media ->
            if (media.isPlaying) {
                media.pause()
                percentSongLengh = media.currentPosition
            } else {
                intent?.let {
                    media.seekTo(percentSongLengh)
                    media.start()
                }
            }
        }
    }

    private fun nextSong() {
        var index = currentIndexSong
        playSongWithIndex(++index)
    }

    private fun previousSong() {
        var index = currentIndexSong
        playSongWithIndex(--index)
    }

    fun stopMusic() {
        _Player?.stop()
        _Player?.release()
        _Player = null
    }

    private fun playSongWithIndex(indexSong: Int) {
        if (indexSong in 0.._listSongResult.size) {
            stopMusic()
            _Player = MediaPlayer.create(this, _listSongResult[indexSong].songLocalUri)
            _Player?.start()
            currentIndexSong = indexSong
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
        val _intentMusicService = Intent(p0, MusicService::class.java)
        when (p1?.action) {
            ACTION_PLAY_OR_PAUSE -> _intentMusicService.action = ACTION_PLAY_OR_PAUSE
            ACTION_NEXT -> _intentMusicService.action = ACTION_NEXT
            ACTION_PREVIOUS -> _intentMusicService.action = ACTION_PREVIOUS
            ACTION_STOP -> _intentMusicService.action = ACTION_STOP
        }
        p0?.let { startForegroundService(it, _intentMusicService) }
    }
}


