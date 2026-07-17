package com.nova.assistant.domain.model

/**
 * Result from OCR text recognition.
 */
data class OcrResult(
    val text: String,
    val confidence: Float = 1.0f,
    val language: String = ""
) {
    val isEmpty: Boolean get() = text.isBlank()
}
