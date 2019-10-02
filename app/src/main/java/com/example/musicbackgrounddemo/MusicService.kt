package com.example.musicbackgrounddemo

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat
import android.content.BroadcastReceiver
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.widget.RemoteViews
import androidx.lifecycle.ViewModelProviders


open class MusicService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    val CHANNEL_ID = "ForegroundServiceChannel"

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        createNotificationChannel()

        val notificationLayout = RemoteViews(packageName, R.layout.notif_music_control)

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        setOnclickPendingIntent(this, ACTION_PLAY, notificationLayout, R.id.btnPlay)
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
        return START_STICKY
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
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
    remoteViews.setOnClickPendingIntent(resourceId, pendingIntent)
}

class MusicBroadCastReceiver : BroadcastReceiver() {

    companion object {
        private var mPlayer: MediaPlayer? = MediaPlayer()
        var isPlaying = false
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        mPlayer?.setOnCompletionListener { mediaPlayer ->
            mediaPlayer.release()
            isPlaying = false

        }
        when (p1?.action) {
            ACTION_PLAY -> playMusic(p0, p1)
            ACTION_NEXT -> nextSong(p0)
            ACTION_PREVIOUS -> previousSong()
            ACTION_STOP -> stopMusic()
        }
    }

    private fun playMusic(context: Context?, intent: Intent?) {
        if (isPlaying) {
            mPlayer?.pause()
            isPlaying = false
        } else {
            mPlayer?.stop()
            mPlayer?.release()
            mPlayer = null
            intent?.let {
                val resourceSong = it.getIntExtra(SONG_NAME_EXTRA, 0)
                if (resourceSong != 0) {
                    mPlayer = MediaPlayer.create(context, resourceSong)
                    mPlayer?.start()
                    isPlaying = true
                }
            }
        }
    }

    fun nextSong(context: Context?) {
    }

    fun previousSong() {
        creatDataSoure()
    }

    fun stopMusic() {
        mPlayer?.stop()
        mPlayer?.release()
        mPlayer = null
    }

    fun creatDataSoure() {
        val url = "https://zingmp3.vn/bai-hat/Song-Gio-Jack-K-ICM/ZWAEIUUB.html"
        val mediaPlayer: MediaPlayer? = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setDataSource(url)
            prepareAsync()
        }
        mediaPlayer?.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.start()
        }
    }

}

