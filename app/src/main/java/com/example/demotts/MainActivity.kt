package com.example.demotts

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.example.demotts.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.schedule
import kotlin.coroutines.coroutineContext
import kotlin.math.log


//RequestCode
private  const val REQUEST_CODE_STT = 1

private const val  delay = 7000

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TextToSpeech
    private var recognizedText: String? = null
    private var answer: Int? = 0
    private var number1: Int = 0;
    private var number2: Int = 0;

    var handler: Handler = Handler()
    var runnable: Runnable? = null
    var delay = 7000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //initializing tts
        tts = TextToSpeech(this, this)

        binding.Generate.setOnClickListener {
            generateRandomNumber()
            var count = 0
            handler.postDelayed(Runnable {
                handler.postDelayed(runnable!!, delay.toLong())
                generateRandomNumber()
                count++
                if(count == 9){
                    handler.removeCallbacks(runnable!!)
                }
                Log.d("Count",count.toString())
            }.also { runnable = it }, delay.toLong())
        }

        binding.StopGenerating.setOnClickListener {
            handler.removeCallbacks(runnable!!)
            Toast.makeText(this@MainActivity,"Generating Stopped",Toast.LENGTH_SHORT).show()
        }

    }

   private fun generateRandomNumber(){
        number1 = (0..10).random()
        number2 = (0..10).random()

        var expression = "$number1 * $number2"

        binding.number.text = expression

        tts.speak(expression, TextToSpeech.QUEUE_FLUSH, null, "")

        answer = number1 * number2
        Log.d("Answer",answer.toString())

        // Calling method after 1.5 seconds
        Handler().postDelayed({
            speakUp()
        }, 1300)

    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language not supported!")
            }
        }
    }

    //method to take speech Input
    private fun speakUp() {
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        // Text that shows up on the Speech input prompt.
        sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Answer!")
        try {
            startActivityForResult(sttIntent, REQUEST_CODE_STT)
        } catch (e: ActivityNotFoundException) {
            // Handling error when the service is unavailable
            e.printStackTrace()
            Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Handle the result for our request code.
            REQUEST_CODE_STT -> {
                // Safety checks to ensure data is available.
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Retrieve the result array.
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    // Ensure result array is not null or empty to avoid errors.
                    if (!result.isNullOrEmpty()) {
                        recognizedText = result[0]
                        // Comparing the actual result with the provided one
                        if (recognizedText.equals(answer.toString())) {
                            tts.speak("Correct", TextToSpeech.QUEUE_FLUSH, null, "")
                            Toast.makeText(this@MainActivity,"Correct",Toast.LENGTH_SHORT).show()
                        } else {
                            tts.speak("Incorrect", TextToSpeech.QUEUE_FLUSH, null, "")
                            Toast.makeText(this@MainActivity,"Incorrect",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }


    override fun onPause() {
        tts.stop()
        super.onPause()
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }

}


