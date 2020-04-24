package save.newwords.vocab.remember.core.newword

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.repository.WordRepository
import java.io.File
import java.io.IOException

class NewWordViewModel(private val repository: WordRepository): ViewModel() {

    /**
     * for capturing audio saving functionalities
     * */
    //if user is still holding the mic button or not
    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording : LiveData<Boolean> get() = _isRecording

    //if user has released the mic button and audio is recorded
    private val _isRecorded = MutableLiveData<Boolean>()
    val isRecorded : LiveData<Boolean> get() = _isRecorded

    //if audio is being played or not
    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying : LiveData<Boolean> get() = _isPlaying

    //for media recording and playing
    private var mediaRecorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    //to track if recording time has exceeded 5 seconds
    private val  _isTimeExceeded = MutableLiveData<Boolean>()
    val isTimeExceeded : LiveData<Boolean> get() = _isTimeExceeded

    //timer instance
    private lateinit var timer: CountDownTimer

    private var _filename = String()

    init {
        _isRecorded.value = false
    }


    fun startRecording(fileName: String) {

        _filename =  fileName

        //start recording
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e("AUDIO STATUS ERROR: ", "prepare() failed")
            }
            start()
        }

        //start the timer for 5 seconds
        timer = object : CountDownTimer(5000, 1000){
            override fun onFinish() {
                _isTimeExceeded.value = true
            }

            override fun onTick(p0: Long) {
                //nothing to do here
            }
        }
        timer.start()


        //helps change text in the view through observer
        _isRecording.value = true
        _isRecorded.value = false

        //helps change the mic icon to play icon
        if (_isRecorded.value!!){
            _isRecorded.value = false
        }
    }

    fun stopRecording() {
        //stop recording
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        //observer text and icon changes through these
        _isRecording.value = false
        _isRecorded.value = true

        //reset the timer boolean
        _isTimeExceeded.value = false

        //stop the timer
        timer.cancel()
    }


    //to delete the temp cache audio file
    fun deleteCacheAudioRecording(){
        val file = File(_filename)
        file.delete()

        _isRecorded.value = false
    }

    //to play the recorded audio file
    fun playIfRecorded() {
        player = MediaPlayer().apply {
            try {
                setDataSource(_filename)
                prepare()
                start()
                _isPlaying.value =  true
            } catch (e: IOException) {
                Log.e("AUDIO PLAYING ERROR: ", "prepare() failed")
            }
        }

        player!!.setOnCompletionListener {
            _isPlaying.value = false
        }

    }

    /*
    get and save the permanent audio file into local storage
     */
    fun getandSavePermanentFile(root: File, wordName: String) {
        //create Audio Files directory if not already there
        if (!root.isDirectory){
            if (!root.mkdirs()) Log.e("Folder not", " created")
        }
        val file = File(_filename)

        //copy the cache to permanent directory
        val newFile = File(root, "$wordName.3gp")
        file.copyTo(newFile, overwrite = true)

        //delete the cache file
        file.delete()
    }

    // save word into db
    fun saveWord(word: Word){
        repository.saveWordToDb(word)
    }

}