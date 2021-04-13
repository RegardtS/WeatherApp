package com.regi.weatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieDrawable
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.CancellationTokenSource
import com.regi.weatherapp.R
import com.regi.weatherapp.data.remote.response.WeatherApiResponse
import com.regi.weatherapp.databinding.ActivityMainBinding
import com.regi.weatherapp.other.Constants.REQUEST_CODE_LOCATION
import com.regi.weatherapp.other.Status
import com.regi.weatherapp.other.Utils.formatUnixDateToString
import com.regi.weatherapp.other.Utils.getWeatherIcon
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInitialUI()
        setupObservers()
    }

    private fun setupInitialUI() {
        binding.button.setOnClickListener {
            requestPermissions()
        }
    }

    private fun setupObservers() {
        viewModel.weather.observe(this) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { weatherItem ->
                        updateUISuccess(weatherItem)
                    }
                }
                Status.ERROR -> {
                    updateUIError(result.message ?: getString(R.string.something_went_very_wrong))
                }
                Status.LOADING -> {
                    updateUILoading()
                }
            }
        }
    }

    // region UI Updates

    private fun updateUISuccess(weatherItem: WeatherApiResponse) {
        showFullDetails(true)
        binding.apply {
            updateLottieAnim(getWeatherIcon(weatherItem.weather[0].icon))

            tvCity.text = weatherItem.name
            tvTemp.text = getString(R.string.param_C, weatherItem.main.temp.toString())
            tvTempDesc.text = weatherItem.weather[0].main
            tvTempFeelsLike.text =
                getString(R.string.feels_like_param_C, weatherItem.main.feels_like.toString())

            tvMinValue.text = getString(R.string.param_C, weatherItem.main.temp_min.toString())
            tvMaxValue.text = getString(R.string.param_C, weatherItem.main.temp_max.toString())
            tvSunriseValue.text = formatUnixDateToString(weatherItem.sys.sunrise)
            tvSunsetValue.text = formatUnixDateToString(weatherItem.sys.sunset)

            button.text = getString(R.string.refresh)
        }
    }

    private fun updateUIError(errorText: String) {
        showFullDetails(false)
        updateLottieAnim(R.raw.astronaut)

        binding.apply {
            tvTempFeelsLike.apply {
                visibility = View.VISIBLE
                text = errorText
            }
            button.apply {
                visibility = View.VISIBLE
                text = getString(R.string.retry)
            }
        }
    }

    private fun updateUILoading() {
        showFullDetails(false)
        updateLottieAnim(R.raw.ripple_loading)
    }

    private fun updateLottieAnim(@RawRes drawRes: Int) {
        binding.animationView.apply {
            setAnimation(drawRes)
            playAnimation()
            repeatCount = LottieDrawable.INFINITE
        }
    }

    private fun showFullDetails(shouldShow: Boolean) {
        val visibility = if (shouldShow) View.VISIBLE else View.INVISIBLE
        binding.apply {
            tvCity.visibility = visibility
            tvTemp.visibility = visibility
            tvTempDesc.visibility = visibility
            tvTempFeelsLike.visibility = visibility

            tvMin.visibility = visibility
            tvMax.visibility = visibility
            tvSunrise.visibility = visibility
            tvSunset.visibility = visibility

            tvMinValue.visibility = visibility
            tvMaxValue.visibility = visibility
            tvSunriseValue.visibility = visibility
            tvSunsetValue.visibility = visibility

            button.text = getString(R.string.get_weather)
            button.visibility = visibility
        }
    }

    // endregion

    @SuppressLint("MissingPermission")
    //permissions are checked by EasyPermissions
    private fun updateLocation() {
        updateUILoading()
        fusedLocationClient.getCurrentLocation(
            PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        )
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.getWeather(location.latitude, location.longitude)
                } else {
                    updateLocation()
                }
            }
            .addOnFailureListener {
                updateUIError(it.localizedMessage ?: getString(R.string.something_went_wrong))
            }
    }

    // region Location Boilerplate code

    private fun requestPermissions() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            updateLocation()
            return
        }

        EasyPermissions.requestPermissions(
            this,
            getString(R.string.location_permission_needed),
            REQUEST_CODE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            updateLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    // endregion
}