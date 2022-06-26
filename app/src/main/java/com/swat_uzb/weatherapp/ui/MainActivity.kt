package com.swat_uzb.weatherapp.ui

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.databinding.ActivityMainBinding
import com.swat_uzb.weatherapp.ui.viewmodels.SharedViewModel
import com.swat_uzb.weatherapp.utils.Constants.REQUEST_CHECK_SETTINGS
import com.swat_uzb.weatherapp.utils.CustomAlertDialogFragment
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var customAlertDialogFragment: CustomAlertDialogFragment
    private val sharedViewModel: SharedViewModel by viewModels { viewModelFactory }
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //In first launch set settings default values
        PreferenceManager.setDefaultValues(this, R.xml.preference_settings, false)

//        checkPermission()

        // observe current name
        subscribe()

        // setup toolbar
        setupToolbar()

        // set app version
        binding.appVersion.text = getString(R.string.version_text, getAppVersion())

        // setting icon to HomeUpIndicator
        settingBackArrowIcon()

        setupDialogFragmentListener()
    }

    private fun subscribe() {

        lifecycleScope.launch {
            sharedViewModel.showProgressBar
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    binding.appBarMain.containerProgressBar.visibility = if (it) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
        }

        sharedViewModel.current.observe(this) {
            with(binding.appBarMain.toolbarLocationName) {
                it?.let {
                    text = it.region
                    if (it.current_location) {
                        setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_location_on,
                            0,
                            0,
                            0
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            sharedViewModel.checkPermission
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    Log.i("TTTT", "checkPermission collect")
                    checkPermission()
                }
        }
    }

    private fun checkPermission() {
        Log.i("TTTT", "checkPermission work")
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // when the GPS is off
                if (isLocationEnabled()) {
                    sharedViewModel.addCurrentLocation()
                } else {
                    enableGps()
                }
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                Log.i("TTTT", "checkPermission should show")

                // Request permission
                requestPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            else -> {
                Log.i("TTTT", "checkPermission should else")
                // Request permission
                requestPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { actionMap ->
            when (actionMap.key) {
                Manifest.permission.ACCESS_FINE_LOCATION -> {
                    if (actionMap.value) {
                        Log.i("TTTT", "ACCESS_FINE_LOCATION Granted")
                        checkPermission()

                    } else {
                        customAlertDialogFragment.show(
                            supportFragmentManager, CustomAlertDialogFragment.TAG
                        )
                        Log.i("TTTT", " requestPermissionsLauncher ACCESS_FINE_LOCATION Denied")
                    }
                }
            }
        }
    }

    private fun setupDialogFragmentListener() {
        CustomAlertDialogFragment.setupListener(supportFragmentManager, this) {
            when (it) {
                DialogInterface.BUTTON_NEGATIVE -> {

                }
                DialogInterface.BUTTON_POSITIVE -> {
                    goToAppSettings()
                }

            }
        }
    }

    private fun goToAppSettings() {
        // send to app settings if permission is denied permanently
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(getString(R.string.package_text), this.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun settingBackArrowIcon() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.appBarMain.toolbarContainer.isVisible = destination.id == R.id.menu_main

            if (destination.id == R.id.menu_main) {
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
                supportActionBar?.setTitle(getString(R.string.toolbar_tashkent))
            } else {
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.appBarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_view)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.menu_main
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun getAppVersion(): String {
        var version = ""
        try {
            version = packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return version
    }

    private fun isLocationEnabled() =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    private fun enableGps() {

        val mLocationRequest = LocationRequest.create()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000)
            .setFastestInterval(10000 / 2)
        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)
        settingsBuilder.setAlwaysShow(true)
        val result =
            LocationServices.getSettingsClient(this).checkLocationSettings(settingsBuilder.build())
        result.addOnSuccessListener(this) {
            checkPermission()
        }

        result.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {

                    it.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)

                } catch (sendIntentException: IntentSender.SendIntentException) {
                    sendIntentException.printStackTrace()
                }
            }
        }

    }

}