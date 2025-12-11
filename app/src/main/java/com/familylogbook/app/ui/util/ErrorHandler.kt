package com.familylogbook.app.ui.util

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException

object ErrorHandler {
    
    /**
     * Converts exceptions to user-friendly Croatian error messages
     */
    fun getFriendlyErrorMessage(exception: Throwable?): String {
        if (exception == null) {
            return "Došlo je do nepoznate greške. Molimo pokušaj ponovo."
        }
        
        val message = exception.message ?: ""
        
        return when {
            // Network errors
            exception is IOException || 
            message.contains("network", ignoreCase = true) ||
            message.contains("connection", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("unreachable", ignoreCase = true) ->
                "Greška mreže. Provjeri internetsku vezu i pokušaj ponovo."
            
            // Firestore errors
            exception is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.UNAVAILABLE,
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                        "Servis nije dostupan. Provjeri internetsku vezu."
                    
                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                        "Nemate dozvolu za spremanje podataka. Provjeri da li si prijavljen/na. Ako problem traje, odjavi se i prijavi ponovo."
                    
                    FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                        "Sesija je istekla. Molimo odjavi se i prijavi ponovo."
                    
                    FirebaseFirestoreException.Code.NOT_FOUND ->
                        "Podatak nije pronađen."
                    
                    FirebaseFirestoreException.Code.ALREADY_EXISTS ->
                        "Ovaj podatak već postoji."
                    
                    FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ->
                        "Previše zahtjeva. Molimo pričekajte i pokušajte ponovo."
                    
                    else ->
                        "Greška pri spremanju podataka. Molimo pokušaj ponovo."
                }
            }
            
            // Auth errors (already handled in LoginScreen, but useful here too)
            message.contains("email address is badly formatted", ignoreCase = true) ->
                "Molimo unesi valjanu email adresu."
            
            message.contains("password is too weak", ignoreCase = true) ->
                "Lozinka je previše slaba. Molimo koristi jaču lozinku."
            
            message.contains("email address is already in use", ignoreCase = true) ||
            message.contains("ERROR_EMAIL_ALREADY_IN_USE", ignoreCase = true) ->
                "Ovaj email je već registriran."
            
            message.contains("account exists with different credential", ignoreCase = true) ||
            message.contains("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL", ignoreCase = true) ->
                "Račun s ovim emailom već postoji, ali je kreiran na drugi način. Pokušaj se prijaviti sa email/lozinkom ili kontaktiraj podršku."
            
            message.contains("credential already in use", ignoreCase = true) ||
            message.contains("ERROR_CREDENTIAL_ALREADY_IN_USE", ignoreCase = true) ->
                "Ovaj Google račun je već povezan s drugim računom. Pokušaj se prijaviti direktno sa Google-om."
            
            message.contains("there is no user record", ignoreCase = true) ->
                "Nije pronađen račun s ovim emailom."
            
            message.contains("password is invalid", ignoreCase = true) ||
            message.contains("wrong password", ignoreCase = true) ->
                "Netočna lozinka."
            
            message.contains("too many requests", ignoreCase = true) ->
                "Previše pokušaja. Molimo pričekaj trenutak i pokušaj ponovo."
            
            // Offline mode errors
            message.contains("offline", ignoreCase = true) ||
            message.contains("no internet", ignoreCase = true) ->
                "Nema internetske veze. Provjeri povezanost i pokušaj ponovo."
            
            // Firestore quota errors
            message.contains("quota", ignoreCase = true) ||
            message.contains("resource exhausted", ignoreCase = true) ->
                "Dostignut je limit zahtjeva. Molimo pričekajte i pokušajte ponovo kasnije."
            
            // Network timeout details
            message.contains("timeout", ignoreCase = true) ->
                "Zahtjev je istekao. Provjeri internetsku vezu i pokušaj ponovo."
            
            // Default
            else ->
                exception.message ?: "Došlo je do greške. Molimo pokušaj ponovo."
        }
    }
    
    /**
     * Checks if the error indicates offline/network issue
     */
    fun isNetworkError(exception: Throwable?): Boolean {
        if (exception == null) return false
        
        val message = exception.message ?: ""
        return exception is IOException ||
               exception is FirebaseFirestoreException && 
               (exception.code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                exception.code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED) ||
               message.contains("network", ignoreCase = true) ||
               message.contains("connection", ignoreCase = true) ||
               message.contains("timeout", ignoreCase = true)
    }
    
    /**
     * Checks if device is online
     */
    fun isOnline(connectivityManager: ConnectivityManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }
}

