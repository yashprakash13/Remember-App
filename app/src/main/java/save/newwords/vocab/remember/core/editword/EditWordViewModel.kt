package save.newwords.vocab.remember.core.editword

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.android.synthetic.main.fragment_edit_word.*
import kotlinx.coroutines.*
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.repository.WordRepository
import java.io.File
import java.io.IOException

class EditWordViewModel (private val repository: WordRepository) : ViewModel() {

    //Coroutine for all the db actions for this view model
    private var viewmodelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewmodelJob)

    //the mutable live data for the clicked word
    val clickedWordMutable  = MutableLiveData<Word>()

    // -------------------------Audio members-------------------------------------------------------

    //if audio is associated with the word
    private val _isAudioPresent  = MutableLiveData<Boolean>()
    val isAudioPresent : LiveData<Boolean> get() = _isAudioPresent

    //observable for if recording or not
    private val _isRecording = MutableLiveData<Boolean> ()
    val isRecording : LiveData<Boolean> get() = _isRecording

    //observable for if recorded or not
    private val _isRecorded = MutableLiveData<Boolean> ()
    val isRecorded : LiveData<Boolean> get() = _isRecorded

    //observable to know if the audio shall be deleted or not
    private val _isAudioToBeDeleted = MutableLiveData<Boolean> ()
    val isAudioToBeDeleted : LiveData<Boolean> get() = _isAudioToBeDeleted

    //if the audioPath of original word is null, but new audio has been recorded when editing
    val isAudioAvailableInCache = MutableLiveData<Boolean> ()

    //the root of local storage for audio
    private lateinit var audioRoot : File

    //to play audio from local storage
    private var player: MediaPlayer? = null

    //for recording audio
    private var mediaRecorder: MediaRecorder? = null

    //to track if recording time has exceeded 5 seconds
    private val  _isTimeExceeded = MutableLiveData<Boolean>()
    val isTimeExceeded : LiveData<Boolean> get() = _isTimeExceeded

    //timer instance
    private lateinit var timer: CountDownTimer

    //this will be null if audio is not present in cache
    private var _filename = String()

    //----------------------------------------------------------------------------------------------

    init {
        _isRecorded.value = false
        _isAudioToBeDeleted.value = false
        isAudioAvailableInCache.value = false
    }

    /**
     * to fetch the word from db
     */
    fun getWordFromName(name : String) {
        uiScope.launch {
            clickedWordMutable.value = repository.getWordFromName(name)
        }

    }

    /**
     * to update the word in db
     * @param originalName : since name is the primary key, original name is required for updation
     */
    fun updateWord(originalName: String){
        uiScope.launch {
            repository.updateWordInDb(originalName, clickedWordMutable.value!!)
        }
    }


    /**
     * save and update word into db and local storage
     */
    fun saveAndUpdate(originalName: String) {
        if (_isAudioToBeDeleted.value!! && clickedWordMutable.value!!.audioPath != null){
            deleteAudioForWord()
        }
        updateWord(originalName)
    }

    //-------------------ALL AUDIO METHODS----------------------------------------------------------

    /**
     * change audio property
     */
    fun setIsAudioPresentProperty(state: Boolean){
        _isAudioPresent.value = state
    }

    /**
     * to prepare to delete the audio of the word if delete pronunciation button
     * is clicked
     */
    fun prepareToDeleteAudioForWord(root: File) {
        //set the audioRoot property
        audioRoot = root

        //set is audio to be deleted property
        _isAudioToBeDeleted.value = true

        //change observable audio property to reflect change in UI
        setIsAudioPresentProperty(false)

        //if new audio was recorded and then delete was tapped,
        // make sure the observer knows not to save the cache file
        if (_isRecorded.value!!){
            isAudioAvailableInCache.value =  false
        }


    }

    /**
     * to undo the deletion of audio
     */
    fun undoDeleteAudio() {

        //to reflect change in UI when undo is clicked
        setIsAudioPresentProperty(true)

        //to make sure audio is not deleted
        _isAudioToBeDeleted.value = false

        if (_isRecorded.value!!){
            isAudioAvailableInCache.value =  true
        }

    }

    /**
     * to actually delete the audio of the original word from local storage
     */
    private fun deleteAudioForWord() {

        //delete file from local storage
        val file = File(audioRoot, clickedWordMutable.value!!.audioPath!!)
        file.delete()

        //delete the audio path field for word in db
        clickedWordMutable.value!!.audioPath = null
    }

    /**
     * to play audio file from local storage
     * @param root: the internal storage path
     */
    fun playAudio(root: File){
        val audioFilePath = File(root, clickedWordMutable.value!!.audioPath!!).absolutePath

        player = MediaPlayer().apply {
            try {
                setDataSource(audioFilePath)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("AUDIO PLAYING ERROR: ", "prepare() failed")
            }
        }

        player!!.setOnCompletionListener {
            //do something
        }
    }

    /**
     * save new audio to storage
     */
    fun saveAudioToStorage(root: File, wordName: String){
        //create Audio Files directory if not already there
        if (!root.isDirectory){
            if (!root.mkdirs()) Log.e("Folder not", " created")
        }
        //the cache file
        val file = File(_filename)

        //copy the cache to permanent directory
        val newFile = File(root, "$wordName.3gp")
        file.copyTo(newFile, overwrite = true)

        //delete the cache file
        file.delete()
    }

    /**
     * play audio file from the cache
     */
    fun playAudioFromCache(){
        player = MediaPlayer().apply {
            try {
                setDataSource(_filename)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("AUDIO PLAYING ERROR: ", "prepare() failed")
            }
        }

        player!!.setOnCompletionListener {
            //do something here
        }

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

        //to know that cache audio is available
        isAudioAvailableInCache.value = true
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

    fun initRecordingMembers() {
        _isRecording.value = false
        _isRecorded.value = false
    }

    //----------------------------------------------------------------------------------------------

    /**
     * To cancel the coroutine jobs when view model is deleted upon activity close
     */
    override fun onCleared() {
        super.onCleared()
        viewmodelJob.cancel()
    }
}