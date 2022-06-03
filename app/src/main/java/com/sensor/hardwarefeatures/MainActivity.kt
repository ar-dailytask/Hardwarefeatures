package com.sensor.hardwarefeatures

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationResult
    val PERMISSION_ID = 1010
    var nfcAdapter: NfcAdapter? = null
    var usbAccessory:UsbAccessory?= null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Declaring TextView and Button from the layout file

        val mButton = findViewById<Button>(R.id.button)
        val getpos = findViewById<Button>(R.id.button1)
        val getNfcCheck = findViewById<Button>(R.id.button2)


        // What happens when button is clicked

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getpos.setOnClickListener {
            Log.d("Debug:", CheckPermission().toString())
            Log.d("Debug:", isLocationEnabled().toString())
            RequestPermission()
            /* fusedLocationProviderClient.lastLocation.addOnSuccessListener{location: Location? ->
                 textView.text = location?.latitude.toString() + "," + location?.longitude.toString()
             }*/
            getLastLocation()
        }
        mButton.setOnClickListener {

            hsasgpsdevice(this@MainActivity)
        }
        getNfcCheck.setOnClickListener {
           // getNfccheck()
            //getUsbAceesoriesCheck()
            checkInfo()
        }
    }


    fun hsasgpsdevice(context: Context): Boolean {
        val mTextView = findViewById<TextView>(R.id.text_view)
        val mTextView1 = findViewById<TextView>(R.id.text_view1)
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Log.e("LocationManager",locationManager.toString())
        val gps: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        Log.e("is Gps Enabled------", gps.toString())
        if (gps == true)
            mTextView.text = "GPS IS ENABLED  $gps"
        else
            mTextView.text = "GPS IS NOT ENABLED$gps"
        if (locationManager == null) {
            return locationManager
            mTextView1.text = "The GPS is not featrured in this device"
        } else {
            mTextView1.text = "The GPS is  featured in this device"

        }
        val providers: List<String> = locationManager.getAllProviders()

        Log.e("GPS Providers--------- ", providers.toString())

        //return providers
        return providers.contains(LocationManager.GPS_PROVIDER)

    }


    fun getLastLocation() {
        val mTextView3 = findViewById<TextView>(R.id.text_view3)

        if (CheckPermission()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        NewLocationData()
                    } else {
                        Log.d("Debug:", "Your Location:" + location.longitude)
                        mTextView3.text =
                            "You Current Location is : Long: " + location.longitude + " , Lat: " + location.latitude + "\n" + "City Name:" + getCityName(
                                location.latitude,
                                location.longitude
                            )
                    }
                }
            } else {
                Toast.makeText(this, "Please Turn on Your device Location", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            RequestPermission()
        }
    }


    fun NewLocationData() {
        var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            Log.d("Debug:", "your last last location: " + lastLocation.longitude.toString())
            // textView.text = "You Last Location is : Long: "+ lastLocation.longitude + " , Lat: " + lastLocation.latitude + "\n" + getCityName(lastLocation.latitude,lastLocation.longitude)
        }
    }

    private fun CheckPermission(): Boolean {
        //this function will return a boolean
        //true: if we have permission
        //false if not
        if (
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false

    }

    fun RequestPermission() {
        //this function will allows us to tell the user to requesut the necessary permsiion if they are not garented
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    fun isLocationEnabled(): Boolean {
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Debug:", "You have the Permission")
            }
        }
    }

    private fun getCityName(lat: Double, long: Double): String {
        var cityName: String = ""
        var countryName = ""
        var geoCoder = Geocoder(this, Locale.getDefault())
        var Adress = geoCoder.getFromLocation(lat, long, 3)

        cityName = Adress.get(0).locality
        countryName = Adress.get(0).countryName
        Log.d("Debug:", "Your City: " + cityName + " ; your Country " + countryName)
        return cityName
    }

    private fun getNfccheck() {
        //val isNfcSupported: Boolean = this.nfcAdapter != null
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Log.d("Debug:", "NfcCheck: " + nfcAdapter)
        } else {
            if (!nfcAdapter!!.isEnabled()) {
                Log.d("Debug:", "please go to setting and enable it : " + nfcAdapter)
            } else
                Log.d("Debug:", " NFC is enabled  : " + nfcAdapter)
        }

    }

    private fun checkInfo() {
        val usbManager = getSystemService(USB_SERVICE) as UsbManager
        val deviceList: HashMap<String, UsbDevice> = usbManager.getDeviceList()
        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
        var deviceString = ""
        while (deviceIterator.hasNext()) {
            val usbDevice = deviceIterator.next()
            deviceString += """
DeviceID: ${usbDevice.getDeviceId()}
 
    DeviceName: ${usbDevice.getDeviceName()}
   Log.d("USB Info", "USB ${'$'}${usbDevice.getDeviceName()}")
   DeviceClass: ${usbDevice.getDeviceClass()} - 
   Log.d("USB Info", "USB ${'$'}${usbDevice.getDeviceClass()}")
     VendorID: ${usbDevice.getVendorId()}
        Log.d("USB Info", "USB ${'$'}${usbDevice.getVendorId()}")

     ProductID: ${usbDevice.getProductId()}
      
"""
            Log.d("USB Info", "USB1 ${'$'}${usbDevice.getDeviceId()}")
        }
        Log.d("USB Info", "USB $deviceString")
      //  textInfo.setText(deviceString)
    }
}




