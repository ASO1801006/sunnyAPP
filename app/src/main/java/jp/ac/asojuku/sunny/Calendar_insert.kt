package jp.ac.asojuku.sunny

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.activity_calendar_insert.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.random.Random

class Calendar_insert : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    var hasCalendarPermission:Boolean=false;
    private val REQUEST_MULTI_PERMISSIONS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_insert)

    }

    // 開始、終了どちらのボタンが押下されたのか判定するための変数
    var flg = ""

    override fun onResume() {
        super.onResume()

        /** 開始時間終了時間に初期値を格納 */
        val date = Date()
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val StartD = format.format(date)
        // 追加画面を開いた時、開始時間の欄に初期値を指定(現在時刻)
        Startdate.setText(StartD.toString())

        // 1時間後の日付操作
        var cal = Calendar.getInstance()
        cal.time = format.parse(StartD)
        cal.add(Calendar.HOUR, 1)
        val plusD = format.format(cal.getTime())
        // 開始時間と同様に終了時間に初期値を指定(現在時刻より一時間後)
        Finishdate.setText(plusD.toString())

        // ログインしたアカウント情報を取得するために必要らしい
        val userId = intent.getStringExtra("USER_ID")

        /** 戻るボタン押下時 */
        ReturnButton.setOnClickListener {
            val intent = Intent(this, Calendar_view::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            overridePendingTransition(R.anim.in_left, R.anim.out_right)//左から画面
        }


        /** 開始終了時間ボタン押下時の処理 */
        /** flgにより識別している*/
        StartButton.setOnClickListener {
            // 開始時間ボタン押下時
            flg = "1"
            // DatePickerを作成(DatePick()を呼び出す)
            val newFragment = DatePick()
            //  表示
            newFragment.show(supportFragmentManager, "datePicker")
        };

        /** 終了時間ボタン押下時の処理 */
        FinishButton.setOnClickListener {
            // 終了時間ボタン押下時
            flg = "2"
            // DatePickerを作成(DatePick()を呼び出す)
            val newFragment = DatePick()
            //  表示
            newFragment.show(supportFragmentManager, "datePicker")
        };


        /** ーーーーーーーーーーーーーーーーーーーーーーーカレンダー情報の権限チェックーーーーーーーーーーーーーーーーーーーーーーー */

        val permissions = arrayOf<String>(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            // 以下は必要に応じ利用
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION
        );

        this.hasCalendarPermission = this.checkCalendarPermission(permissions);

        // カレンダー情報にアクセスする権限を動的に取得（Android6.0以降必須
        if(!this.hasCalendarPermission)requestPermissions(permissions, 123);

        if(this.hasCalendarPermission){
            //  カレンダー情報を取得して先頭のカレンダーのIDを取得
            this.getMyCalendar().let{
                when{
                    it.size>0 ->{

                        /** ログインしているアカウントでカレンダーIDを指定する*/

                        val userMail = intent.getStringExtra("USER_MAIL")
                        //Toast.makeText(this, userMail, Toast.LENGTH_LONG).show();

                        var caleID:Long = 0L
                        it.forEach{i->
                            if (userMail == i.name.toString()) {
                                // ログインしているアカウントと一致する情報のIDを取得
                                caleID = i.id
                            }
                        }
                        //Toast.makeText(this, caleID, Toast.LENGTH_LONG).show();

                        val  calendarId:Long =  caleID;

                        /** 予定追加ボタン押下された時の処理(onEventInsert())を設定*/
                        Log.d("getCALENDAR", "ボタンにリスナーセット");
//                        Toast.makeText(this,
//                            "ボタンにリスナーをセット", Toast.LENGTH_LONG).show();

                        // 追加ボタン押下時(InsertButtonの処理)
                        InsertButton.setOnClickListener {
                            onEventInsert(calendarId, eventName.text.toString(),
                                Startdate.text.toString(), Finishdate.text.toString(), eventPlace.text.toString(), description.text.toString()) ;
                        };
                    }
                    else -> {
                        Log.d("getCALENDAR", "カレンダー情報がない");
//                        Toast.makeText(this,
//                            "カレンダー情報が見つかりません", Toast.LENGTH_LONG).show();
                        -1;
                    }
                }
            }
        }

    }

    /** Android標準の戻るボタンを押したとき */
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.in_down, R.anim.out_up)
    }

    /** アプリの権限をチェック（Android6.0以降必須 */
    private fun checkCalendarPermission(permissions:Array<String>):Boolean{
        var result:Boolean = true
        for(permission:String in permissions){
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "permission を持たないルート", Toast.LENGTH_SHORT).show();
                return false
            }
        }
        return true;
    }

    /** 権限リクエストのユーザー応答を受け取るコールバックメソッド */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.size<=0){
            // 許可が取れなかった場合
//            Toast.makeText(this,
//                "カレンダーへのアクセスを許可してください", Toast.LENGTH_LONG).show();
            /** 画面終了 */
            // finish();
        }
        //--------------------------------hikaru----------------------------------
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
                //startLocationService()
            }
        } else {
            Log.d("パーミッションエラー","権限がありません")
        }
        //--------------------------------hikaru----------------------------------終わり
    }


    /** ーーーーーーーーーーーーーーーーーーーーーーーーーー追加ボタン押下時に行う処理ーーーーーーーーーーーーーーーーーーーーーーーーーー */

    // 引数(入力値)を元に追加するイベント情報を生成
    fun onEventInsert(calendarId: Long, // ID
                      title: String, // イベント名
                      startMillis: String, // 開始時間
                      endMillis: String, // 終了時間
                      place: String, // 場所　
                      description: String // 詳細
    ): Long? {

        val cr = contentResolver
//
        //　入力値をミリ秒表記に変換する(追加する際変換が必要)
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm")
        // 開始時間ミリ秒変換
        val dt = df.parse(startMillis)
        var startMillis = dt.time
        // 終了時間ミリ秒変換
        val dh = df.parse(endMillis)
        var endMillis = dh.time

        // イベント名(TITLE),開始時間(DTSTART),終了時間(DTEND),場所(EVENT_LOCATION),説明(DESCRIPTION)
        val values = ContentValues()
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId) // ID
        if(title != ""){
            values.put(CalendarContract.Events.TITLE, title) // イベント名
        }
        values.put(CalendarContract.Events.DTSTART, startMillis) // 開始時間
        values.put(CalendarContract.Events.DTEND, endMillis) // 終了時間
        if(place != "") {
            values.put(CalendarContract.Events.EVENT_LOCATION, place)  // 場所
        }
        if(description != "") {
            values.put(CalendarContract.Events.DESCRIPTION, description)  // 詳細
        }
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID())

        var uri: Uri? = null

        // カレンダーにイベント挿入
        uri = cr.insert(CalendarContract.Events.CONTENT_URI, values)

        //---------------------------hikaru-----------------------------------
        val userId = intent.getStringExtra("USER_ID")

        val longHours = endMillis-startMillis          //予定の合計時間
        val doubleHours = ((longHours.toDouble()/1000)/3600)  //ダブル型に変換 (msからsに変換し、hにした)
        val totalHours = ceil(doubleHours).toInt()       //時間に変換（int）
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent_alarm = Intent(this,AlarmBroadcastReceiver::class.java)
        .putExtra("TOTAL_HOURS",totalHours).putExtra("USER_ID", userId)
        val ramId = Random.nextInt(500)
        val pending = PendingIntent.getBroadcast(
            this, ramId, intent_alarm, PendingIntent.FLAG_UPDATE_CURRENT)
        Log.d("ひかるの処理",totalHours.toString())
        Log.d("ひかるの処理start",startMillis.toString())

        when{
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ->{
                val info = AlarmManager.AlarmClockInfo(
                    startMillis,null)
                //時間になったら実行される
                Log.d("ひかるの処理","通過1")
                am.setAlarmClock(info,pending)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ->{
                //時間になったら実行される
                Log.d("ひかるの処理","通過2")
                am.setExact(AlarmManager.RTC,//前はRTC_WAKEUP
                    startMillis,pending)
            }
            else ->{
                Log.d("ひかるの処理","通過3")
                am.set(AlarmManager.RTC,//前はRTC_WAKEUP
                    startMillis,pending)

            }
        }


        //---------------------------hikaru-----------------------------------終わり

        Log.d("getCALENDAR", "登録した情報 values = "+ values.toString())

        // 前に画面に遷移する()
        val intent = Intent(this, Calendar_view::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
        Toast.makeText(this, "登録しました", Toast.LENGTH_SHORT).show()

        return uri?.lastPathSegment?.toLong()
    }


    /** ーーーーーーーーーーーーーーーーーーーーーーーー自分のアカウントのカレンダー情報（id,名前）を取得ーーーーーーーーーーーーーーーーーーーーーーーー*/

    @SuppressLint("Recycle")
    private fun getMyCalendar():List<CalendarInfo>{

        val cr = contentResolver
        // 戻り値のカレンダー情報リスト
        val result = ArrayList<CalendarInfo>();
        // カラム
        val projection = arrayOf("_id", "name")
        // Uriの作成
        val calendars = CalendarContract.Calendars.CONTENT_URI
        // クエリ実行
        val c:Cursor? = cr.query(calendars, projection, null, null, null);
//        val c: Cursor = managedQuery(calendars, projection, null, null, null)

        // 値を取得
        var name: String?=""
        var id:Long=-1
        when (c?.moveToFirst()) {
            true->{
                val nameColumn: Int = c.getColumnIndex("name")
                val idColumn: Int = c.getColumnIndex("_id")
                do {
                    name = c.getString(nameColumn)
                    id = c.getLong(idColumn)
                    val info = CalendarInfo();
                    Log.d("Calendar Data", "id=$id,name=$name")

                    info.id = id;
                    info.name = name;
                    result.add(info);   // カレンダー情報登録
                } while (c.moveToNext())
            }
        }
        return result;
    }

    /** カレンダー情報（id, 名前）のインナークラス */
    private inner class CalendarInfo(){
        var id:Long=-1;
        var name:String?="";
    }


    /** ーーーーーーーーーーーーーーーーーーーーーーーーーー開始、終了時間が入力された際の処理ーーーーーーーーーーーーーーーーーーーーーーーーーー */

    // 異なるクラスで操作可能な変数
    // 最終的に画面に表示される
    var date_time = ""

    // 日付を選択された際の処理
    // 値を内部変数に格納,時間取得処理を行う
    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {

        // 選択値を内部変数に格納
        val DateText = String.format(Locale.US, "%d-%d-%d", year, monthOfYear + 1, dayOfMonth)

        // 入力値を内部関数に格納
        date_time = DateText + " "

        // 日付取得後時間を取得する処理に移行
        val newFragment = TimePick()
        newFragment.show(supportFragmentManager, "timePicker")
    }

    // 時間ボタンが押下された際の処理
    // 内部変数の値と結合、表示
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // 選択された値(時間)を内部変数に格納
        val TimeText = String.format(Locale.US, "%d:%d", hourOfDay, minute)

        /** 開始時間より終了時間が早い場合の処理 */
        /** ミリ秒に変換して判定(関数など、既存のものがある場合はそれを使う) */
        // 開始時間編集時に上記が起こったら、更新をせずにユーザに警告メッセージを表示
        // 終了時間編集時も同様に行う。
        // 下の処理をif文で囲む
        // 取得した値を内部変数に結合
        date_time += TimeText

        var kinisinaide = "1"

        // 表示
        // 更新可能か判定
        var flg_Judg =  false

        val df = SimpleDateFormat("yyyy-MM-dd HH:mm")
        // 開始時間をミリ秒に変換
        if(flg == "1") {
            /** 開始時間編集中(1)*/
            if (Finishdate.text.toString() != "") { /** 終了時間がnull値でない場合(3)*/
            val dt = df.parse(Finishdate.text.toString())
                var Ftime = dt.time // 終了時間ミリ秒変換
                //Toast.makeText(this, "判定完了", Toast.LENGTH_LONG).show();
                val dh = df.parse(date_time.toString())
                var Input_value = dh.time // 入力値ミリ秒変換
                if (Ftime > Input_value) {
                    /** 入力値と終了時間判定(5) */
                    // 終了時間 > 入力値の場合
                    flg_Judg = true

                    // 48時間以内に設定されているか判定
                    // 入力値(Input_value)と終了時間の48時間前(Day2)の時刻を比較
                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    var cal = Calendar.getInstance()
                    cal.time = format.parse(Finishdate.text.toString())
                    cal.add(Calendar.DAY_OF_YEAR, -2)
                    val MinsD = format.format(cal.getTime())

                    // 48時間後の時刻をミリ秒に変換
                    val difhour = df.parse(MinsD.toString())
                    val Day2 = difhour.time

                    if(Input_value < Day2) {
                        flg_Judg = false
                        kinisinaide = "2"
                        Toast.makeText(this, "イベントの期間を48時間以内に設定してください。", Toast.LENGTH_LONG).show();
                    }

                }else{
                    // 終了時間(Ftime) < 入力値(Input_value)の場合
                    // 入力値を開始時間に設定
                    // 終了時間を入力値の一時間後設定

                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    var cal = Calendar.getInstance()
                    cal.time = format.parse(date_time)
                    cal.add(Calendar.HOUR, 1)
                    val plusD = format.format(cal.getTime())
                    Finishdate.setText(plusD.toString())
                    flg_Judg = true
                }
            }else{
                /** 終了時間がnull値である場合*/
                flg_Judg = true
            }
        }else{
            /** 終了時間編集中(2) */
            if (Startdate.text.toString() != "") { /** 開始時間がnull値でない場合(4)*/
            val dt = df.parse(Startdate.text.toString())
                var Stime = dt.time // 開始時間ミリ秒変換
                //Toast.makeText(this, "判定完了", Toast.LENGTH_LONG).show();
                val dh = df.parse(date_time.toString())
                var Input_value = dh.time // 入力値ミリ秒変換
                if (Stime < Input_value) { /** 入力値と終了時間判定(6) */
                    // 終了時間 > 入力値の場合
                    flg_Judg = true
                }

                // イベントの期間が48時間以内か判定
                // 入力値：終了時間(Input_value)と開始時間の48時間後(Day2)の時刻を比較
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                var cal = Calendar.getInstance()
                cal.time = format.parse(Startdate.text.toString())
                cal.add(Calendar.DAY_OF_YEAR, 2)
                val plusD = format.format(cal.getTime())

                // 48時間後の時刻をミリ秒に変換
                val difhour = df.parse(plusD.toString())
                val Day2 = difhour.time

                if(Input_value > Day2){
                    flg_Judg = false
                    kinisinaide = "2"
                    Toast.makeText(this, "イベントの期間を48時間以内に設定してください。", Toast.LENGTH_LONG).show();
                }

            }else{
                /** 開始時間がnull値である場合*/
                flg_Judg = true
            }
        }
        /** 更新可能か判定 */
        if(flg_Judg == true) {
            if (flg == "1") {
                Startdate.setText(date_time.toString())
            } else {
                Finishdate.setText(date_time.toString())
            }
        }else{
            if(kinisinaide == "1"){
                Toast.makeText(this, "適切な時間ではありません。", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 日付画面作成処理
    class DatePick : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // カレンダ-作成
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)


            return DatePickerDialog(
                this.context as Context,
                activity as Calendar_insert?,
                year,
                month,
                day)
        }

        override fun onDateSet(view: android.widget.DatePicker, year: Int,
                               monthOfYear: Int, dayOfMonth: Int) {
            // 今回、処理はしない
        }
    }

    // 時間画面作成処理
    class TimePick : DialogFragment(), TimePickerDialog.OnTimeSetListener {

        // Bundle sould be nullable, Bundle?
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // to initialize a Calender instance
            val c = Calendar.getInstance()

            // at the first, to get the system current hour and minute
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            return TimePickerDialog(
                activity,
                // in order to return parameters to MainActivity
                activity as Calendar_insert?,
                hour,
                minute,
                true)
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            // 今回、処理はしない
        }
    }
}