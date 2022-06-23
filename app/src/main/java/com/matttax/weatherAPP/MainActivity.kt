package com.matttax.weatherAPP

import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.text.format.Time
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    var CITY: String = "moscow, russia"
    var API: String = "06c921750b9a82d8f5d1294e1586276f" // Use API key
    lateinit var flc: FusedLocationProviderClient
    lateinit var address: String

    private fun fetchLocation() {
        val task = flc.lastLocation
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }
        task.addOnSuccessListener {
            if (it != null)
                address = "http://api.openweathermap.org/geo/1.0/reverse?lat=${it.latitude}&lon=${it.longitude}&appid=$API"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //flc = LocationServices.getFusedLocationProviderClient(this)
        setContentView(R.layout.activity_main)
        weatherTask().execute()
        val today = Time(Time.getCurrentTimezone())
        today.setToNow()
        containerMainWeather.setBackgroundResource(R.drawable.anon)
        (findViewById<View>(R.id.editTextTextPersonName) as EditText).setOnEditorActionListener(
            OnEditorActionListener { v, actionId, event ->
                if ((actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action === KeyEvent.ACTION_DOWN && event.keyCode === KeyEvent.KEYCODE_ENTER) && (event == null || !event.isShiftPressed)) {
                    CITY = editTextTextPersonName.text.toString()
                    weatherTask().execute()
                    return@OnEditorActionListener true
                }
                false
            }
        )
    }

    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.containerMainWeather).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
//                fetchLocation()
//                if (address == null) {
//                    response = URL(address).readText(Charsets.UTF_8)
//                } else
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(Charsets.UTF_8)
            }catch (e: Exception){
                response = null
            }
            return response
        }

        //@OptIn(ExperimentalStdlibApi::class)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: ${
                    SimpleDateFormat(
                        "dd/MM/yyyy hh:mm a",
                        Locale.ENGLISH
                    ).format(Date(updatedAt * 1000))
                }"
                val temp = "${String.format("%.1f", main.getString("temp").toDouble())}°C"
                val tempMin = "Min Temp: ${main.getString("temp_min")}°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")
                val cloudInformation = main.getString("feels_like")

                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val address = "${jsonObj.getString("name")}, ${sys.getString("country")}"

                findViewById<TextView>(R.id.countryNameText).text = address
                findViewById<TextView>(R.id.updatedWeatherText).text =  updatedAtText
                findViewById<TextView>(R.id.statusOfWeather).text = weatherDescription.capitalize()
                when (findViewById<TextView>(R.id.statusOfWeather).text) {
                    "Clear sky" -> containerMainWeather.setBackgroundResource(R.drawable.clear_sky)
                    "Shower rain" -> containerMainWeather.setBackgroundResource(R.drawable.shower_rain)
                    "Rain" -> containerMainWeather.setBackgroundResource(R.drawable.rain)
                    "Thunderstorm" -> containerMainWeather.setBackgroundResource(R.drawable.thunderstorm)
                    "Mist" -> containerMainWeather.setBackgroundResource(R.drawable.mist)
                    "Snow" -> containerMainWeather.setBackgroundResource(R.drawable.snow)
                    else -> containerMainWeather.setBackgroundResource(R.drawable.clouds)
                }
                findViewById<TextView>(R.id.temperatureOfWeather).text = temp
                findViewById<TextView>(R.id.minimumTemperature).text = tempMin
                findViewById<TextView>(R.id.about).text = cloudInformation;
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.containerMainWeather).visibility = View.VISIBLE
            } catch (e: Exception) {
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(applicationContext, "Error", duration)
                toast.show()
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.containerMainWeather).visibility = View.VISIBLE
            }

        }
    }
}
