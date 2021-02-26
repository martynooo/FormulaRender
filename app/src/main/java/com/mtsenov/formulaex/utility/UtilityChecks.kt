package com.mtsenov.formulaex.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class UtilityChecks {

    companion object {
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capability = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return capability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        }
    }
}