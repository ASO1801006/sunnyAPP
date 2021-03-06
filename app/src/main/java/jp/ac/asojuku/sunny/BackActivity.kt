package jp.ac.asojuku.sunny

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.ArrayList

class BackActivity : AppCompatActivity() {
    private val REQUEST_MULTI_PERMISSIONS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_back)

        if (intent?.getBooleanExtra("onReceive",false) == true ){
            Log.d("main","通過")
            // Android 6, API 23以上でパーミッシンの確認
            if (Build.VERSION.SDK_INT >= 23) {
                Log.d("permission","通過")
                checkMultiPermissions()
            } else {
                Log.d("start","通過")
                startLocationService()
            }

        }
        val intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
        finish()

    }

    // 位置情報許可の確認、外部ストレージのPermissionにも対応できるようにしておく
    private fun checkMultiPermissions() {
        // 位置情報の Permission
        val permissionLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val reqPermissions = ArrayList<Any>()

        // 位置情報の Permission が許可されているか確認
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            // 許可済
        } else {
            // 未許可
            reqPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }


        // 未許可
        if (!reqPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                reqPermissions.toArray(arrayOfNulls(0)),
                REQUEST_MULTI_PERMISSIONS
            )
            // 未許可あり
        } else {
            // 許可済
            Log.d("checkPermission","通過")

            startLocationService()

        }
    }

    // 結果の受け取り
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_MULTI_PERMISSIONS) {
            if (grantResults.size > 0) {
                for (i in permissions.indices) {
                    // 位置情報
                    if (permissions[i] == Manifest.permission.ACCESS_FINE_LOCATION) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // 許可された
                        } else {
                            // それでも拒否された時の対応
                            //toastMake("位置情報の許可がないので計測できません")
                        }
                    } else if (permissions[i] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // 許可された
                        } else {
                            // それでも拒否された時の対応
                            //toastMake("外部書込の許可がないので書き込みできません")
                        }
                    }
                }
                startLocationService()
            }
        } else {
            //
        }
    }

    //位置情報取得メソッド
    private fun startLocationService(){
        setContentView(R.layout.activity_back)
        Log.d("startLocationService","通過")
        val userId = intent.getStringExtra("USER_ID")
        val hours = intent.getIntExtra("TOTAL_HOURS",1)
        val intent = Intent(application, LocationService::class.java)
            .putExtra("TOTAL_HOURS",hours).putExtra("USER_ID",userId)
        // API 26 以降
        startForegroundService(intent)

        //stopService(intent)

    }
}