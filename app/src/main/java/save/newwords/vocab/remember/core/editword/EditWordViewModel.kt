package save.newwords.vocab.remember.core.editword

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    // -------------------------Audio members----------------------------------------

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

    //the root of local storage for audio
    private lateinit var audioRoot : File

    //to play audio from local storage
    private var player: MediaPlayer? = null

    //--------------------------------------------------------------------------------

    init {
        _isAudioToBeDeleted.value = false
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
     */
    fun updateWord(originalName: String){
        uiScope.launch {
            repository.updateWordInDb(originalName, clickedWordMutable.value!!)
        }
    }

    /**
     * to save a new word into db
     */
    fun insertNewWord(word: Word) {
        uiScope.launch {
            repository.saveWordToDb(word)
        }
    }

    /**
     * save and update/insert word into db and local storage
     */
    fun saveAndUpdate() {

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
    }

    /**
     * to undo the deletion of audio
     */
    fun undoDeleteAudio() {
        //to reflect change in UI when undo is clicked
        setIsAudioPresentProperty(true)

        //to make sure audio is not deleted
        _isAudioToBeDeleted.value = false
    }

    /**
     * to actually delete the audio from local storage
     */
    fun deleteAudioForWord() {
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

    fun startRecording() {

    }

    fun stopRecording() {

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