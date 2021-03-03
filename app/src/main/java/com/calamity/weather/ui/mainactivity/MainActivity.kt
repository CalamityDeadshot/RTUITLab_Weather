package com.calamity.weather.ui.mainactivity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.calamity.weather.R
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), LifecycleOwner {

    private val viewModel: InternetViewModel by viewModels()

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.findNavController()

        setupActionBarWithNavController(navController)

        viewModel.internetAccess.observe(this) {
            Snackbar.make(nav_host_fragment_container,
                if (it)
                    resources.getString(R.string.connection_established)
                else resources.getString(R.string.connection_lost),
                Snackbar.LENGTH_LONG)
                .setBackgroundTint(resources.getColor(
                    if (it)
                        android.R.color.holo_green_light
                    else android.R.color.holo_red_dark
                ))
                .show()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}