package com.lmteixeira.happyplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.*
import java.lang.StringBuilder
import java.util.*
import kotlin.coroutines.CoroutineContext

class GetAddressFromLatLng(
    private val context: Context,
    private val latitude: Double,
    private val longitude: Double
) : CoroutineScope {

    private val geoCoder: Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var mAddressListener: AddressListener

    interface AddressListener {
        fun onAddressFound(address: String?)
        fun onError()
    }

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun cancel() {
        job.cancel()
    }

    private fun execute() = launch {
        onPreExecute()
        val result = doInBackground()
        onPostExecute(result)
    }

    private suspend fun doInBackground(): String = withContext(Dispatchers.IO) {
        try {
            val addressList: List<Address>? = geoCoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]
                var stringBuilder = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    stringBuilder.append(address.getAddressLine(i)).append(" ")
                }
                stringBuilder.deleteAt(stringBuilder.length - 1)
                return@withContext stringBuilder.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext ""
    }

    private fun onPreExecute() {
        // show progress
    }

    private fun onPostExecute(result: String) {
        if (result == null) {
            mAddressListener.onError()
        } else{
            mAddressListener.onAddressFound(result)
        }
    }

    fun setAddressListener(addressListener: AddressListener) {
        this.mAddressListener = addressListener
    }

    fun getAddress() {
        execute()
    }
}