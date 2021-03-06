package jp.ac.asojuku.sunny

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.internal.concurrent.TaskRunner.Companion.logger

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Log.d("アラームセット", "処理後:降水量取得前")

        val hours = intent.getIntExtra("TOTAL_HOURS",1)
        val userId = intent.getStringExtra("USER_ID")
        val back_intent = Intent(context,BackActivity::class.java)
            .putExtra("onReceive",true).putExtra("TOTAL_HOURS",hours)
            .putExtra("USER_ID",userId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(back_intent)

    }


}
