package com.whisperdroid.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {
    /**
     * Checks if the device has an active internet connection.
     * @param context The context to use for accessing ConnectivityManager.
     * @return True if online, false otherwise.
     */
    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                 activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                 activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                 activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH))
    }
}
