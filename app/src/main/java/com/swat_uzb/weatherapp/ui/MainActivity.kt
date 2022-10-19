package com.swat_uzb.weatherapp.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.work.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.databinding.ActivityMainBinding
import com.swat_uzb.weatherapp.ui.viewmodels.SharedViewModel
import com.swat_uzb.weatherapp.utils.Constants.TAG_WORKER
import com.swat_uzb.weatherapp.utils.CustomAlertDialogFragment
import com.swat_uzb.weatherapp.utils.cancelNotifications
import com.swat_uzb.weatherapp.utils.sendNotification
import com.swat_uzb.weatherapp.workmanager.UpdateWeatherDataWorker
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {


    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var customAlertDialogFragment: CustomAlertDialogFragment

    @Inject
    lateinit var sharedViewModel: SharedViewModel

    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //In first launch set settings default values
        PreferenceManager.setDefaultValues(this, R.xml.preference_settings, false)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        // observe current name
        subscribe()

        // setup toolbar
        setupToolbar()

        // set app version
        binding.appVersion.text = getString(R.string.version_text, getAppVersion())

        // setting icon to HomeUpIndicator
        settingBackArrowIcon()

        setupDialogFragmentListener()

        createChannel()
    }

    private fun startWorkManager(intervalTime: Long) {


        val workManager = WorkManager.getInstance(this)

        val weatherConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateWeatherWorkerRequest =
            PeriodicWorkRequestBuilder<UpdateWeatherDataWorker>(intervalTime, TimeUnit.HOURS)
                .setConstraints(weatherConstraints)
                .setInitialDelay(intervalTime/2, TimeUnit.HOURS)
                .build()

        if (intervalTime == 0L) {
            workManager.cancelUniqueWork(TAG_WORKER)
        } else {
            workManager.enqueueUniquePeriodicWork(
                TAG_WORKER,
                ExistingPeriodicWorkPolicy.REPLACE,
                updateWeatherWorkerRequest
            )
        }

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
                    } else {
                        setCompoundDrawablesWithIntrinsicBounds(
                            0,
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
                    enableGps()
                }
        }
//        subscribe notification to the currentUi data
        subscribeNotification()
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {

                // add current location
                with(sharedViewModel) {
                    if (!isCurrentLocationAdded()) {
                        addCurrentLocation()
                    }
                }
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {

                // Request permission
                requestPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            else -> {
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
                        checkPermission()

                    } else {
                        customAlertDialogFragment.show(
                            supportFragmentManager, CustomAlertDialogFragment.TAG
                        )
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
            ), drawerLayout, fallbackOnNavigateUpListener = ::onSupportNavigateUp
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun getAppVersion(): String {
        val version: String = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }
        return version
    }

    private fun enableGps() {
        val mLocationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }


        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)
        settingsBuilder.setAlwaysShow(true)
        val result =
            LocationServices.getSettingsClient(this).checkLocationSettings(settingsBuilder.build())
        result.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(it.resolution).build()
                    launcher.launch(intentSenderRequest)
                } catch (sendIntentException: IntentSender.SendIntentException) {
                    // ignore this error

                }
            } else {
                if (it is ApiException && it.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    sharedViewModel.setLocation(1)
                }
            }
        }
        result.addOnSuccessListener {
            checkPermission()
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK && sharedViewModel.isNotLocationGranted()) {
                checkPermission()
            }
        }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

        when (key) {
            getString(R.string.key_auto_refresh) -> {
                startWorkManager(sharedViewModel.getAutoRefresh().toLong())
            }
            getString(R.string.key_notification) -> {
                subscribeNotification()
            }
            getString(R.string.key_temperature) -> {
                subscribeNotification()
            }
        }

    }

    private fun subscribeNotification() {
        lifecycleScope.launch {
            sharedViewModel.allLocations.flowWithLifecycle(
                lifecycle, Lifecycle.State.STARTED
            )
                .collect {
                    if (sharedViewModel.getNotification() && it.isNotEmpty()) {
                        notificationManager.sendNotification(it.first(), this@MainActivity)
                    } else {
                        notificationManager.cancelNotifications()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                getString(R.string.channel_id),
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.apply {
                enableLights(true)
                lightColor = Color.RED
                description = getString(R.string.weather_notification_channel_description)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}