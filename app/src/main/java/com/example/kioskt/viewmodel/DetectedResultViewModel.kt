package com.example.kioskt.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetectedResultViewModel : ViewModel() {
    private val _detectedResult = MutableLiveData<DetectedResult>()

    init {
        _detectedResult.postValue(DetectedResult("F", 24f))
    }

    suspend fun getValue() : LiveData<DetectedResult> {
        while(!accessible) {
            delay(1000)
            Log.i("kioskT", "Wait for detection result...")
        }
        accessible = false
        return _detectedResult
    }

    private var sumOfAge = 0.0f
    private var sumOfGender = 0
    private var counter = 0

    private var accessible = false

    fun updateData(detectedResult: DetectedResult) {
        sumOfAge += detectedResult.age
        if(detectedResult.gender == "F") sumOfGender += 1

        if(counter++ == 5) {
            val gender : String = if(sumOfGender >= 3) "F" else "M"
            _detectedResult.postValue(DetectedResult(gender, sumOfAge / 5))

            counter = 0
            sumOfAge = 0.0f
            sumOfGender = 0
            accessible = true
        }
    }
}

data class DetectedResult(val gender:String, val age:Float) {
    override fun toString(): String {
        return ageString
    }

    private val ageString = age.toInt().toString()
}