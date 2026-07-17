package com.nova.assistant.features.ocr

import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.nova.assistant.domain.model.OcrResult
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Optical Character Recognition using ML Kit Text Recognition.
 */
@Singleton
class OCRManager @Inject constructor() {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Recognize text from a [Uri] pointing to an image.
     */
    suspend fun recognizeFromUri(uri: Uri, context: android.content.Context): OcrResult =
        suspendCoroutine { cont ->
            try {
                val image = InputImage.fromFilePath(context, uri)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text.trim()
                        Timber.d("OCR: recognized ${text.length} chars")
                        cont.resume(OcrResult(text = text))
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "OCR: recognition failed")
                        cont.resumeWithException(e)
                    }
            } catch (e: Exception) {
                Timber.e(e, "OCR: exception creating InputImage")
                cont.resumeWithException(e)
            }
        }

    /**
     * Recognize text from a [android.graphics.Bitmap].
     */
    suspend fun recognizeFromBitmap(bitmap: android.graphics.Bitmap): OcrResult =
        suspendCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    cont.resume(OcrResult(text = visionText.text.trim()))
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "OCR: bitmap recognition failed")
                    cont.resumeWithException(e)
                }
        }

    fun close() {
        recognizer.close()
    }
}
