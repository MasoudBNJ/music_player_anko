package bn.masoud.media

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.progur.droidmelody.SongFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import java.util.*

class MainActivity : AppCompatActivity() {

    var albumArt: ImageView? = null

    var playButton: ImageButton? = null
    var shuffleButton: ImageButton? = null

    var songTitle: TextView? = null
    var songArtist: TextView? = null

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    , 0)
        } else {
            // Start creating the user interface
            createPlayer()
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }

    private fun createPlayer() {
        GlobalScope.launch {
            val songFinder = SongFinder(contentResolver)
            songFinder.prepare()
            val songs = songFinder.allSongs
            GlobalScope.launch (Dispatchers.Main) {
                createPlayerUI(songs)
            }
        }
    }

    private fun createPlayerUI(songs: MutableList<SongFinder.Song>) {
        val playerUI = object : AnkoComponent<MainActivity> {
            override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
                relativeLayout {
                    backgroundColor = Color.BLACK
                    albumArt = imageView {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    }.lparams(matchParent, matchParent)

                    verticalLayout {
                        backgroundColor = Color.parseColor("#99000000")

                        songTitle = textView {
                            textColor = Color.WHITE
                            typeface = Typeface.DEFAULT_BOLD
                            textSize = 18f
                        }

                        songArtist = textView {
                            textColor = Color.WHITE
                        }

                        linearLayout {

                            playButton = imageButton {
                                imageResource = R.drawable.baseline_play_arrow_black_18dp
                                setOnClickListener {
                                    playOrPause()
                                }
                            }.lparams(0, wrapContent, 0.5f)

                            shuffleButton = imageButton {
                                imageResource = R.drawable.baseline_shuffle_black_18dp
                                setOnClickListener {
                                    playRandom()
                                }
                            }.lparams(0, wrapContent, 0.5f)

                        }.lparams(matchParent, wrapContent) {
                            topMargin = dip(5)
                        }
                    }.lparams(matchParent, matchParent) {
                        alignParentBottom()
                    }
                }
            }

            fun playRandom() {
                songs.shuffle()
                val song = songs[0]
                mediaPlayer?.reset()
                mediaPlayer = MediaPlayer.create(ctx, song.uri)
                mediaPlayer?.setOnCompletionListener {
                    playRandom()
                }

                albumArt?.imageURI = song.albumArt
                songTitle?.text = song.title
                songArtist?.text = song.artist

                mediaPlayer?.start()
                playButton?.imageResource = R.drawable.baseline_pause_black_18dp
            }

            fun playOrPause() {
                val isPlayingSong: Boolean? = mediaPlayer?.isPlaying

                if(isPlayingSong == true) {
                    mediaPlayer?.pause()
                    playButton?.imageResource = R.drawable.baseline_play_arrow_black_18dp
                } else {
                    mediaPlayer?.start()
                    playButton?.imageResource = R.drawable.baseline_pause_black_18dp
                }
            }
        }

        playerUI.setContentView(this@MainActivity)
        playerUI.playRandom()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createPlayer()
        } else {
            longToast("Permission not granted so app cannot work!")
            finish()
        }
    }
}
