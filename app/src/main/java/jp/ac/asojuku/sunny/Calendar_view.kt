package jp.ac.asojuku.sunny

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_calendar_insert.*
import kotlinx.android.synthetic.main.activity_calendar_insert.ReturnButton
import kotlinx.android.synthetic.main.activity_calendar_view.*
import java.text.SimpleDateFormat
import java.util.*


class Calendar_view : AppCompatActivity() {
    var hasCalendarPermission: Boolean = false;


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_view)

        // CalendarViewにリスナーを設定
        this.findViewById<CalendarView>(R.id.calendarView).setOnDateChangeListener(listener);

        val myText: TextView = findViewById(R.id.textView)
        val customFont = Typeface.createFromAsset(assets, "GN-KillGothic-U-KanaO.ttf")
        myText.typeface = customFont

    }

    override fun onResume() {
        super.onResume()

        //DetailActivityに画面に遷移する
        val userId = intent.getStringExtra("USER_ID")
        val userMail = intent.getStringExtra("USER_MAIL")
        ReturnButton.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("USER_ID", userId).putExtra("USER_MAIL", userMail)
            startActivity(intent)
            overridePendingTransition(R.anim.in_left, R.anim.out_right)//左から画面

        }


        //Calendar_insertに画面に遷移する
        addButton.setOnClickListener {
            val intent = Intent(this, Calendar_insert::class.java)
            intent.putExtra("USER_ID", userId).putExtra("USER_MAIL", userMail)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)//右から画面
        }

        /** カレンダー情報の権限チェック */
        val permissions = arrayOf<String>(
            Manifest.permission.READ_CALENDAR,
            // 以下は必要に応じ利用
            Manifest.permission.READ_CONTACTS
        );
        this.hasCalendarPermission = this.checkCalendarPermission(permissions);


    }

    private fun checkCalendarPermission(permissions: Array<String>): Boolean {
        var result: Boolean = true;
        for (permission: String in permissions) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(applicationContext, "permission を持たないルート", Toast.LENGTH_SHORT)
                    .show();
                return false;
            }
        }
        return true;
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.size <= 0) {
            // 許可が取れなかった場合
//            Toast.makeText(this,
//                "カレンダーへのアクセスを許可してください", Toast.LENGTH_LONG).show();
            /** 画面終了 */
            // finish();
        }
    }

    //Android標準の戻るボタンを押したとき
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.in_down, R.anim.out_up)
    }

    var listener =
        CalendarView.OnDateChangeListener { view, year, month, dayOfMonth ->


            /**
             * 日付部分タップ時に実行される処理
             * @param view 押下されたカレンダーのインスタンス
             * @param year タップされた日付の「年」
             * @param month タップされた日付の「月」※月は0月から始まるから、+1して調整が必要
             * @param dayOfMonth タップされた日付の「日」
             */
            /**
             * 日付部分タップ時に実行される処理
             * @param view 押下されたカレンダーのインスタンス
             * @param year タップされた日付の「年」
             * @param month タップされた日付の「月」※月は0月から始まるから、+1して調整が必要
             * @param dayOfMonth タップされた日付の「日」
             */

            Log.d("年月日","${year}${month}${dayOfMonth}")

            val DEBUG_TAG: String = "MyActivity"

            val INSTANCE_PROJECTION: Array<String> = arrayOf(
                CalendarContract.Instances.EVENT_ID, // 0
                CalendarContract.Instances.BEGIN, // 1
                CalendarContract.Instances.TITLE // 2

            )

            // The indices for the projection array above.
            val PROJECTION_ID_INDEX: Int = 0
            val PROJECTION_BEGIN_INDEX: Int = 1
            val PROJECTION_TITLE_INDEX: Int = 2

            // Specify the date range you want to search for recurring
            // event instances
            val startMillis: Long = Calendar.getInstance().run {
                set(year, month, dayOfMonth,0,0)
                timeInMillis
            }
            val endMillis: Long = Calendar.getInstance().run {
                set(year, month, dayOfMonth,23,59,59)
                timeInMillis
            }

            // The ID of the recurring event whose instances you are searching
            // for in the Instances table
            val selection: String = "${CalendarContract.Instances.EVENT_ID} = ?"
            val selectionArgs: Array<String> = arrayOf("1")

            // Construct the query with the desired date range.
            val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, startMillis)
            ContentUris.appendId(builder, endMillis)
            // Submit the query
            val cur = contentResolver.query(
                builder.build(),
                INSTANCE_PROJECTION,
                null,
                null,
                null)

            var calendar_info = ""
            var cnt = 0
            while (cur!!.moveToNext()) {
                // Get the field values

                val eventID: Long = cur.getLong(PROJECTION_ID_INDEX)
                val beginVal: Long = cur.getLong(PROJECTION_BEGIN_INDEX)
                val title: String = cur.getString(PROJECTION_TITLE_INDEX)

                // Do something with the values.
                Log.i(DEBUG_TAG, "Event: $title")
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = beginVal
                }
                val formatter = SimpleDateFormat("MM/dd/yyyy")
                Log.i(DEBUG_TAG, "Date: ${formatter.format(calendar.time)}")
                calendar_info += "　イベント名: ${title}　\n"
                cnt++
            }
            if (cnt > 0){
                calendar_info+="　${year}/${month+1}/${dayOfMonth}の予定は${cnt}件です　"
                //よていひょうじ
                val textview = findViewById(R.id.info) as TextView
                textview.setText(calendar_info)
            }else{
                val textview = findViewById(R.id.info) as TextView
                textview.setText(calendar_info)
            }

        }

}