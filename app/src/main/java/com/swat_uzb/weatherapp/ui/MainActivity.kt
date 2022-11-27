package com.swat_uzb.weatherapp.ui

import android.Manifest
import android.app.NotificationManager
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
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
import androidx.work.WorkManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.swat_uzb.weatherapp.BuildConfig
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.databinding.ActivityMainBinding
import com.swat_uzb.weatherapp.ui.viewmodels.SharedViewModel
import com.swat_uzb.weatherapp.utils.*
import com.swat_uzb.weatherapp.workmanager.startWorkManager
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var customAlertDialogFragment: dagger.Lazy<CustomAlertDialogFragment>

    @Inject
    lateinit var sharedViewModel: SharedViewModel

    @Inject
    lateinit var notificationManager: dagger.Lazy<NotificationManager>

    @Inject
    lateinit var locationManager: dagger.Lazy<LocationManager>

    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var _appBarConfiguration: AppBarConfiguration? = null
    private val appBarConfiguration get() = checkNotNull(_appBarConfiguration) { "AppBarConfiguration not initialized" }
    private lateinit var binding: ActivityMainBinding
    private var _navController: NavController? = null
    private val navController get() = checkNotNull(_navController) { "NavController not initialized" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

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
        binding.appVersion.text = getString(R.string.version_text, BuildConfig.VERSION_NAME)

        // setting icon to HomeUpIndicator
        settingBackArrowIcon()

        setupDialogFragmentListener()

        // handle onBackPressed action
        closingNavigationDrawer()
    }

    private fun closingNavigationDrawer() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
                    binding.drawerLayout.closeDrawer(Gravity.START)
                } else {
                    finish()
                }
            }
        })
    }

    private fun subscribe() {

        lifecycleScope.launch {
            sharedViewModel.isLoading.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    binding.appBarMain.containerProgressBar.visibility =
                        if (it) View.VISIBLE else View.GONE
                }
        }

        sharedViewModel.current.observe(this) {
            with(binding.appBarMain.toolbarLocationName) {
                it?.let {
                    text = if (it.region.length < REGION_MAX_LENGTH_SIZE) it.region else it.country
                    if (it.current_location) {
                        setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_location_on, 0, 0, 0
                        )
                    } else {
                        setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, 0, 0
                        )
                    }
                }
            }
        }
        lifecycleScope.launch {
            sharedViewModel.showShort.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    showSnackbar(it)
                }
        }
        lifecycleScope.launch {
            sharedViewModel.checkPermission.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    checkPermission()
                }
        }
        // subscribe notification to the currentUi data
        subscribeNotification()
    }

    private fun checkPermission() {

        when {
            !locationManager.get().isGpsOn() -> {
                enableGps()
            }

            hasLocationPermission() -> {
                // add current location
                with(sharedViewModel) {
                    if (!isCurrentLocationAdded()) {
                        addCurrentLocation()
                        sharedViewModel.setLocation(0)
                    }
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // Request permission
                requestPermission()

            }
            else -> {
                // Request permission
                requestPermission()

            }
        }
    }

    private fun requestPermission() {
        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
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
                        customAlertDialogFragment.get().show(
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

        _navController = findNavController(R.id.nav_host_fragment_content_view)
        _appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.menu_main
            ), drawerLayout, fallbackOnNavigateUpListener = ::onSupportNavigateUp
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_rate_this_app -> {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri.parse(
                                getString(R.string.share_app_link, BuildConfig.APPLICATION_ID)
                            )
                        )
                    )
                }
                R.id.menu_share_this_app -> {
                    ShareCompat.IntentBuilder(this)
                        .setType(getString(R.string.share_app_type))
                        .setChooserTitle(R.string.app_name)
                        .setText(getString(R.string.share_app_link, BuildConfig.APPLICATION_ID))
                        .startChooser()
                }
                R.id.menu_settings -> {
                    navController.navigate(R.id.menu_settings)
                    if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
                        binding.drawerLayout.closeDrawer(Gravity.START)
                    }
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    private fun enableGps() {
        val mLocationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            interval = Location_Min_Update_Interval_Mills
            fastestInterval = Location_Min_Update_Interval_Mills
        }

        val settingsBuilder = LocationSettingsRequest
            .Builder()
            .addLocationRequest(mLocationRequest)
        settingsBuilder.setAlwaysShow(true)

        val result = LocationServices
            .getSettingsClient(this)
            .checkLocationSettings(settingsBuilder.build())

        result.addOnFailureListener {

            if (it is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(it.resolution).build()
                    launcher.launch(intentSenderRequest)
                } catch (sendIntentException: IntentSender.SendIntentException) {
                    sendIntentException.printStackTrace()
                    // ignore this error

                }
            } else {
                if (it is ApiException &&
                    it.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE
                ) {
                    sharedViewModel.setLocation(1)
                }
            }
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK) {
                checkPermission()
            }
        }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

        when (key) {
            getString(R.string.key_auto_refresh) -> {
                WorkManager.getInstance(this)
                    .startWorkManager(sharedViewModel.getAutoRefresh().toLong())
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
        val notificationManager1 = notificationManager.get()
        lifecycleScope.launch {
            sharedViewModel.allLocations.flowWithLifecycle(
                lifecycle, Lifecycle.State.STARTED
            ).collect {
                if (sharedViewModel.getNotification() && it.isNotEmpty()) {
                    notificationManager1.sendNotification(it.first(), this@MainActivity)
                } else {
                    notificationManager1.cancelNotifications()
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

    private fun showSnackbar(
        value: Int
    ) {
        val drawable = when (value) {
            0 -> R.string.unable_to_connect_server
            1 -> R.string.no_network_str
            2 -> R.string.toast_exist
            else -> R.string.something_wrong_str
        }
        hideKeyboard(this)
        val msg = applicationContext.getString(drawable)
        Snackbar.make(binding.appBarMain.coordinator, msg, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        private const val Location_Min_Update_Interval_Mills = 5000L
        private const val REGION_MAX_LENGTH_SIZE = 17
    }
}

