package com.segx01.testfirenase.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = geofencingEvent.let {
                    GeofenceStatusCodes
                        .getStatusCodeString(it.errorCode)
                }
                Log.e(TAG, errorMessage)
                return
            }
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent?.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails = triggeringGeofences?.let {
                getGeofenceTransitionDetails(
                    context,
                    geofenceTransition,
                    it
                )
            }

            // Send notification and log the transition details.
            if (geofenceTransitionDetails != null) {
                sendNotification(geofenceTransitionDetails)
            }
            if (geofenceTransitionDetails != null) {
                Log.i(TAG, geofenceTransitionDetails)
            }
        } else {
            // Log the error.
            Log.e(TAG, "Invalid geofence transition type: $geofenceTransition")
        }
    }

    private fun getGeofenceTransitionDetails(
        context: Context,
        transitionType: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        val geofenceTransitionString = when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Entered the geofence"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Exited the geofence"
            else -> "Unknown transition"
        }

        val triggeringGeofencesIdsList = triggeringGeofences.map { it.requestId }
        val triggeringGeofencesIdsString = triggeringGeofencesIdsList.joinToString(", ")

        return "$geofenceTransitionString: $triggeringGeofencesIdsString"
    }

    private fun sendNotification(message: String) {
        // Implement your notification logic here
        // This can include showing a notification, starting an activity, etc.
        // For simplicity, this example does not include the notification implementation.
        Log.e(TAG, message)

    }
}
