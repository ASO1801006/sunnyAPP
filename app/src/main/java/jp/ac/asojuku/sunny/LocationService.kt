package jp.ac.asojuku.sunny

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class LocationService : Service(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var context: Context
    private lateinit var intent1: Intent
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var URL=""

    override fun onCreate() {
        super.onCreate()
        Log.d("oncreate", "通過")

        context = applicationContext

        // LocationManager インスタンス生成
        locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intent1 = intent
        val requestCode = 0
        val channelId = "default"
        val title = context!!.getString(R.string.app_name)
        val pendingIntent = PendingIntent.getActivity(
            context, requestCode,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        // ForegroundにするためNotificationが必要、Contextを設定
        val notificationManager =
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification　Channel 設定
        val channel = NotificationChannel(
            channelId, title, NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Silent Notification"
        // 通知音を消さないと毎回通知音が出てしまう
        // この辺りの設定はcleanにしてから変更
        channel.setSound(null, null)
        // 通知ランプを消す
        channel.enableLights(false)
        channel.lightColor = Color.BLUE
        // 通知バイブレーション無し
        channel.enableVibration(false)
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel)
            val notification =
                Notification.Builder(context, channelId)
                    .setContentTitle(title) // 本来なら衛星のアイコンですがandroid標準アイコンを設定
                    .setSmallIcon(android.R.drawable.btn_star)
                    .setContentText("GPS")
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .build()

            // startForeground
            startForeground(1, notification)
        }
        Log.d("startcommand", "通過")
        startGPS()
        return START_NOT_STICKY
    }

    fun startGPS() {
        Log.d("GPS", "通過")
        val gpsEnabled =
            locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsEnabled) {
            // GPSを設定するように促す
            enableLocationSettings()
        }
        if (locationManager != null) {
            try {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MinTime.toLong(),
                    MinDistance,
                    this
                )

                //ひかる追記
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location ->

                        //非同期処理でHTTP GETを実行します。
                        GlobalScope.launch(Dispatchers.Main) {
                            val hours = intent1.getIntExtra("TOTAL_HOURS",1)
                            Log.d("降水量取得処理","通過")
                            URL="https://api.openweathermap.org/data/2.5/onecall?lat=${location.latitude}&lon=${location.longitude}&appid=5ad3e753e3e30ef147b30c96a3545a4d&lang=ja&units=metric"
                            Log.d("URL: ",URL)
                            val http = HttpUtil()
                            val list = mutableListOf<String>()
                            var total_prec = 0.0
                            //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
                            async(Dispatchers.Default) {
                                http.httpGet(URL)
                            }.await().let {
                                //minimal-jsonを使って　jsonをパース
                                val result: JsonObject = Json.parse(it).asObject() as JsonObject
                                val a = result.get("hourly") as JsonArray
                                var cnt_hours= 0
                                Log.d("hours",hours.toString())
                                for (i in a) {
                                    if (i.asObject().get("rain") != null) {
                                        //list.add(i.asObject().get("rain").asObject().get("1h").toString())
                                        total_prec += i.asObject().get("rain").asObject().get("1h").toString().toDouble()
                                    }else{
                                        list.add("0")
                                        total_prec += 0.0
                                    }
                                    if (cnt_hours >= hours-1) {
                                        break
                                    }
                                    cnt_hours++
                                }

                            }
                            var mainNumber = 0
                            val database = FirebaseDatabase.getInstance().getReference("users") //Firebaseから/users以下のデータを全て取得
                            val userId = intent1.getStringExtra("USER_ID") //インテント時にuseridを取得

                            database.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(p0: DataSnapshot) {
                                    var n = 1
                                    //firebaseのusersの子の数だけループし、一致するuseridを探す
                                    for(users in p0.children) {
                                        var id = users.child("id").value
                                        //useridが一致したらnをmainNumberとし、Firebaseで個人を特定するための値とする
                                        if(id == userId){
                                            mainNumber = n
                                        }
                                        n++
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    //エラー処理
                                }
                            })

                            database.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(p1: DataSnapshot) {
                                    var mainhour = p1.child("$mainNumber/hour").value//個人時間 ここでmainNumberを使用
                                    var mainprecipitation = p1.child("$mainNumber/precipitation").value//個人総降水量
                                        if(mainhour != null && mainprecipitation != null) {
                                        mainhour = mainhour.toString().toLong()
                                        mainhour += hours

                                        if(mainprecipitation is Long){
                                            mainprecipitation = mainprecipitation.toLong()
                                            mainprecipitation += total_prec
                                        }else if(mainprecipitation is Double){
                                            mainprecipitation = mainprecipitation.toDouble()
                                            mainprecipitation += total_prec
                                        }

                                    }
                                    Log.d("firebase 追加後時間：",mainhour.toString())
                                    Log.d("firebase 追加後降水量：",mainprecipitation.toString())
                                    Log.d("${hours}時間分の降水量: ",list.toString())
                                    val mainhour2 =  FirebaseDatabase.getInstance().getReference("users/$mainNumber/hour")
                                    val mainprec2 = FirebaseDatabase.getInstance().getReference("users/$mainNumber/precipitation")

                                    //Firebaseに登録
                                    mainhour2.setValue(mainhour)
                                    mainprec2.setValue(mainprecipitation)
                                }
                                override fun onCancelled(p1: DatabaseError) {
                                    //エラー処理
                                }
                            })
                        }
                    }


            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Log.d("locationManager", "null")
        }
    }


    //位置情報が変化した時
    override fun onLocationChanged(location: Location) {

    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(
        provider: String,
        status: Int,
        extras: Bundle
    ) {
        // Android 6, API 23以上でパーミッシンの確認
        if (Build.VERSION.SDK_INT <= 28) {
            when (status) {
                LocationProvider.AVAILABLE -> {
                }
                LocationProvider.OUT_OF_SERVICE -> Log.d("エラー","LocationProvider.OUT_OF_SERVICE")
                LocationProvider.TEMPORARILY_UNAVAILABLE -> Log.d("エラー","LocationProvider.TEMPORARILY_UNAVAILABLE")
            }
        }
    }

    private fun enableLocationSettings() {
        val settingsIntent =
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(settingsIntent)
    }

    private fun stopGPS() {
        if (locationManager != null) {
            // update を止める
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            locationManager!!.removeUpdates(this)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        stopGPS()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private const val MinTime = 1000
        private const val MinDistance = 50f
    }
}

