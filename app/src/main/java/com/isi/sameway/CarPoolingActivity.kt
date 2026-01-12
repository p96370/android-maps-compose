package com.isi.sameway

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.isi.sameway.firebase.FirebaseDatabaseHelper
import com.isi.sameway.screens.NavGraph
import com.isi.sameway.utils.ClientEntry
import org.lighthousegames.logging.logging
import kotlin.time.Duration.Companion.seconds

internal var clients = listOf(
    ClientEntry("Client1", "12.5", 5f, 0xFF0000FF),
    ClientEntry("Client2", "10", 5f, 0xFFAABBCC)
)
internal val loadingPeriod = 5.seconds

class MainActivity : ComponentActivity(), OnMapsSdkInitializedCallback {

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
        requestPermissions(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
            ), 0
        )
        FirebaseDatabaseHelper.signIn(signInLauncher)
        setContent {
            NavGraph()
        }
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d(
                "MapsDemo", "The latest version of the renderer is used."
            )

            MapsInitializer.Renderer.LEGACY -> Log.d(
                "MapsDemo", "The legacy version of the renderer is used."
            )

            else -> {}
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        val log = logging()

        if (result.resultCode == RESULT_OK) {
            FirebaseDatabaseHelper.addCurrentUser()
        } else {
            // Sign in failed
            response?.error?.let {
                log.e { "Sign-in error: ${it.errorCode}, ${it.message}" }
            } ?: log.d { "Sign-in canceled by user." }
        }
    }


}
