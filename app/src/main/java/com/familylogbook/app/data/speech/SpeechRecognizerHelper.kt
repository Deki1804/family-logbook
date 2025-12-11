package com.familylogbook.app.data.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat

/**
 * Helper class for speech recognition on Croatian language.
 * Uses Android's built-in SpeechRecognizer API.
 */
class SpeechRecognizerHelper(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var onResultCallback: ((String?) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    
    /**
     * Checks if speech recognition is available on this device.
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }
    
    /**
     * Checks if microphone permission is granted.
     */
    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Starts listening for speech input in Croatian.
     * @param onResult Callback with recognized text (null if cancelled)
     * @param onError Callback with error message
     */
    fun startListening(
        onResult: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isAvailable()) {
            onError("Glasovno prepoznavanje nije dostupno na ovom uređaju.")
            return
        }
        
        if (!hasPermission()) {
            onError("Potrebna je dozvola za mikrofon.")
            return
        }
        
        // Cancel any existing recognition first
        stopListening()
        
        this.onResultCallback = onResult
        this.onErrorCallback = onError
        
        // Create new recognizer
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        if (recognizer == null) {
            onError("Ne mogu kreirati glasovno prepoznavanje. Provjeri da li je Google Voice/Speech servis instaliran.")
            return
        }
        
        speechRecognizer = recognizer.apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    android.util.Log.d("SpeechRecognizerHelper", "Ready for speech")
                }
                
                override fun onBeginningOfSpeech() {
                    android.util.Log.d("SpeechRecognizerHelper", "Beginning of speech")
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // Audio level changed - could be used for visual feedback
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    // Buffer received
                }
                
                override fun onEndOfSpeech() {
                    android.util.Log.d("SpeechRecognizerHelper", "End of speech")
                }
                
                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Greška audio snimanja. Provjeri da li mikrofon radi."
                        SpeechRecognizer.ERROR_CLIENT -> {
                            // ERROR_CLIENT usually means the recognizer wasn't properly initialized or was cancelled
                            // Clean up and provide helpful message
                            android.util.Log.e("SpeechRecognizerHelper", "ERROR_CLIENT: Recognizer may not be properly initialized")
                            "Glasovno prepoznavanje nije spremno. Pokušaj ponovno."
                        }
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Nedovoljne dozvole za mikrofon. Provjeri postavke aplikacije."
                        SpeechRecognizer.ERROR_NETWORK -> "Greška mreže. Provjeri internetsku vezu."
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout mreže. Provjeri internetsku vezu i pokušaj ponovno."
                        SpeechRecognizer.ERROR_NO_MATCH -> "Nisam mogao razumjeti. Pokušaj govoriti jasnije ili bliže mikrofonu."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Prepoznavanje je zauzeto. Pričekaj trenutak i pokušaj ponovno."
                        SpeechRecognizer.ERROR_SERVER -> "Greška servera. Pokušaj ponovno kasnije."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nisam čuo ništa. Provjeri da li mikrofon radi i pokušaj ponovno."
                        else -> "Nepoznata greška: $error"
                    }
                    android.util.Log.e("SpeechRecognizerHelper", "Speech recognition error: $error - $errorMessage")
                    // Clean up before calling callback
                    stopListening()
                    // Use post to ensure callback is called after cleanup
                    onErrorCallback?.invoke(errorMessage)
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val confidenceScores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                    
                    if (!matches.isNullOrEmpty()) {
                        // Get the best match (first one, usually has highest confidence)
                        val recognizedText = matches[0]
                        android.util.Log.d("SpeechRecognizerHelper", "Recognized: $recognizedText")
                        onResultCallback?.invoke(recognizedText)
                    } else {
                        android.util.Log.w("SpeechRecognizerHelper", "No results")
                        onResultCallback?.invoke(null)
                    }
                    stopListening()
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    // Partial results - could be used for real-time feedback
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        android.util.Log.d("SpeechRecognizerHelper", "Partial: ${matches[0]}")
                    }
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Event received
                }
            })
        }
        
        // Create intent for Croatian speech recognition
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hr-HR") // Croatian
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hr-HR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1) // We only need the best match
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // Enable partial results
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Govori...") // Prompt text
        }
        
        try {
            val recognizer = speechRecognizer
            if (recognizer == null) {
                onError("Prepoznavanje nije spremno. Pokušaj ponovno.")
                return
            }
            recognizer.startListening(intent)
            android.util.Log.d("SpeechRecognizerHelper", "Started listening")
        } catch (e: IllegalStateException) {
            android.util.Log.e("SpeechRecognizerHelper", "IllegalStateException starting recognition: ${e.message}", e)
            onError("Prepoznavanje nije spremno. Pokušaj ponovno.")
            stopListening()
        } catch (e: Exception) {
            android.util.Log.e("SpeechRecognizerHelper", "Error starting recognition: ${e.message}", e)
            onError("Greška pri pokretanju glasovnog prepoznavanja: ${e.message ?: "Nepoznata greška"}")
            stopListening()
        }
    }
    
    /**
     * Stops listening and cleans up resources.
     */
    fun stopListening() {
        try {
            speechRecognizer?.apply {
                try {
                    cancel()
                } catch (e: Exception) {
                    android.util.Log.w("SpeechRecognizerHelper", "Error cancelling recognizer: ${e.message}")
                }
                try {
                    destroy()
                } catch (e: Exception) {
                    android.util.Log.w("SpeechRecognizerHelper", "Error destroying recognizer: ${e.message}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SpeechRecognizerHelper", "Error stopping recognition: ${e.message}", e)
        } finally {
            speechRecognizer = null
            onResultCallback = null
            onErrorCallback = null
        }
    }
    
    /**
     * Cancels current recognition.
     */
    fun cancel() {
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            android.util.Log.w("SpeechRecognizerHelper", "Error cancelling: ${e.message}")
        }
    }
}
