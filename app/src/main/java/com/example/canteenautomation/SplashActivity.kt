package com.example.canteenautomation

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var splashLoader: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize the horizontal loading bar from your XML
        splashLoader = findViewById(R.id.splashLoader)

        // Start the process
        startLoadingProcess()
    }

    private fun startLoadingProcess() {
        // Ensure loader is visible
        splashLoader.visibility = View.VISIBLE

        // Wait 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            checkConnection()
        }, 3000)
    }

    private fun checkConnection() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true

        if (isConnected) {
            // Internet connected - proceed to login
            splashLoader.visibility = View.GONE
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // No internet - hide loader and show alert
            splashLoader.visibility = View.GONE
            showNoInternetAlert()
        }
    }

    private fun showNoInternetAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("No Internet Access")
        builder.setMessage("BMS Bites requires an internet connection. Please check your network settings.")
        builder.setCancelable(false)

        builder.setPositiveButton("Dismiss") { dialog, _ ->
            dialog.dismiss()
            finish() // Close the app
        }

        builder.setNegativeButton("Retry") { dialog, _ ->
            dialog.dismiss()
            // When user hits retry, restart the loading bar and the 3-second check
            startLoadingProcess()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}