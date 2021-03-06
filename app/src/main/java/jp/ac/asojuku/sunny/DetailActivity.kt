package jp.ac.asojuku.sunny

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_detail.*
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt


class DetailActivity : AppCompatActivity() {
    var mainNumber = 0
    var apAvg = 0.0 //全体の平均総降水量

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val database = FirebaseDatabase.getInstance().getReference("users")
        var ap = 0.0//全体の総降水量
        var num = 0.0 //人数
        var avg: Double //全体の1時間当たりの平均降水量
        var sd: Double //標準偏差
        var x = 0.0 //一時的に値を保存
        var score: Double //雨率スコア

        val userId = intent.getStringExtra("USER_ID")
        val userName = intent.getStringExtra("USER_NAME")
        var auth = false
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                var n = 1
                for(users in p0.children) {
                    var id = users.child("id").value
                    if(id == userId){
                        auth = true
                        mainNumber = n
                    }
                    n++
                }
                if(!auth){
                    val mId = FirebaseDatabase.getInstance().getReference("users/$n/id")
                    val mName = FirebaseDatabase.getInstance().getReference("users/$n/name")
                    val mNumber = FirebaseDatabase.getInstance().getReference("users/$n/number")
                    mId.setValue(userId)
                    mName.setValue(userName)
                    mNumber.setValue(n)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                //エラー処理
            }
        })

        //雨率スコア算出
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p1: DataSnapshot) {
                var mainhour = p1.child("$mainNumber/hour").value //個人特定
                var mainprecipitation = p1.child("$mainNumber/precipitation").value //個人特定

                if(mainprecipitation == null || mainhour == null){
                    val fragment = UnknownFragment()
                    val fragmentManager = supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.container,fragment)
                        .addToBackStack(null)
                        .commit()
                }else{
                    mainhour = mainhour.toString().toDouble()
                    if(mainprecipitation is Long){
                        mainprecipitation = mainprecipitation.toString().toDouble()
                    }
                    mainprecipitation as Double
                    val mR = mainprecipitation / mainhour //個人の1時間当たりの平均降水量
                    for(users1 in p1.children) { //全体の1時間当たりの平均降水量
                        var hour = users1.child("hour").value
                        var precipitation = users1.child("precipitation").value
                        if(hour != null && precipitation != null){
                            hour = hour.toString().toDouble() //Long型は直接Double型にキャストできないので1度String型にキャスト
                            if(precipitation is Long){
                                precipitation = precipitation.toString().toDouble()
                            }
                            precipitation as Double
                            ap += precipitation / hour
                            apAvg += precipitation
                            num++
                        }
                    }
                    avg = (floor(ap / num *1000)) / 1000
                    apAvg = (floor(apAvg / num *1000)) / 1000
                    num = 0.0 //人数リセット
                    for(users2 in p1.children) { //標準偏差
                        var hour = users2.child("hour").value
                        var precipitation = users2.child("precipitation").value
                        if(hour != null && precipitation != null){
                            hour = hour.toString().toDouble()
                            if(precipitation is Long){
                                precipitation = precipitation.toString().toDouble()
                            }
                            precipitation as Double
                            var p = (floor((precipitation / hour - avg).pow(2.0) * 1000)) / 1000
                            x += p
                            num++
                        }
                    }
                    sd = floor(sqrt(floor(x /num * 1000) / 1000) * 1000) / 1000
                    score = floor((mR - avg) / sd * 10 + 50)
                    val ref = FirebaseDatabase.getInstance().getReference("users/$mainNumber/score") //個人特定
                    ref.setValue(score)
                }
            }

            override fun onCancelled(p1: DatabaseError) {
                //エラー処理
            }
        })
    }

    override fun onResume() {
        super.onResume()

        val userId = intent.getStringExtra("USER_ID")
        val userMail = intent.getStringExtra("USER_MAIL")
        //カレンダー画面遷移
        add_plan.setOnClickListener {
            val intent = Intent(this, Calendar_view::class.java)
            intent.putExtra("USER_ID", userId).putExtra("USER_MAIL", userMail)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)//右から画面
        }

        //ログイン画面遷移
        logout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.in_left, R.anim.out_right)//左から画面
        }

        //スコアフラグメント表示
        this.ameritu_score.setOnClickListener{
            //ボタンデザイン変更
            val rk: Button = findViewById(R.id.ranking)
            rk.setBackgroundResource(R.drawable.button_no_push)
            val tp: Button = findViewById(R.id.total_precipitation)
            tp.setBackgroundResource(R.drawable.button_no_push)
            val sc: Button = findViewById(R.id.ameritu_score)
            sc.setBackgroundResource(R.drawable.button_push)

            //個人特定
            val database = FirebaseDatabase.getInstance().reference.child("users/$mainNumber/score")

            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    var score = snapshot.value

                    val bundle = Bundle()
                    bundle.putString("value", "$score")

                    if (score != null) {

                        score = score as Long

                        //雨率判定
                        if (score <= 45) { //晴れ
                            val fragment = AmerituSunnyFragment()
                            fragment.arguments = bundle
                            val fragmentManager = supportFragmentManager
                            val fragmentTransaction = fragmentManager.beginTransaction()
                            fragmentTransaction.replace(R.id.container,fragment)//画面の「id:container」の部分にフラグメントを切り替え
                                .addToBackStack(null)//元のフラグメントをバックスタックに保存（今回は何もしない ）
                                .commit()//トランザクション完了
                            if (score <=35) {
                                user.text = "超晴れ男"
                                val myText: TextView = findViewById(R.id.user)
                                val customFont = Typeface.createFromAsset(assets, "GN-KillGothic-U-KanaO.ttf")
                                myText.typeface = customFont
                            }else {
                                user.text = "晴れ男"
                                val myText: TextView = findViewById(R.id.user)
                                val customFont = Typeface.createFromAsset(assets, "GN-KillGothic-U-KanaO.ttf")
                                myText.typeface = customFont
                            }
                        } else if (score in 46..55){ //普通
                            val fragment = AmerituNomalFragment()
                            fragment.arguments = bundle
                            val fragmentManager = supportFragmentManager
                            val fragmentTransaction = fragmentManager.beginTransaction()
                            fragmentTransaction.replace(R.id.container,fragment)
                                .addToBackStack(null)
                                .commit()
                            user.text = "平　凡"
                            val myText: TextView = findViewById(R.id.user)
                            val customFont = Typeface.createFromAsset(assets, "GN-KillGothic-U-KanaO.ttf")
                            myText.typeface = customFont
                        } else if (score >= 56){ //雨
                            val fragment = AmerituRainyFragment()
                            fragment.arguments = bundle
                            val fragmentManager = supportFragmentManager
                            val fragmentTransaction = fragmentManager.beginTransaction()
                            fragmentTransaction.replace(R.id.container,fragment)
                                .addToBackStack(null)
                                .commit()
                            if (score >= 66){
                                user.text = "超雨男"
                                val myText: TextView = findViewById(R.id.user)
                                val customFont = Typeface.createFromAsset(assets, "GN-KillGothic-U-KanaO.ttf")
                                myText.typeface = customFont
                            }else {
                                user.text = "雨　男"
                                val myText: TextView = findViewById(R.id.user)
                                val customFont = Typeface.createFromAsset(assets, "GN-KillGothic-U-KanaO.ttf")
                                myText.typeface = customFont
                            }
                        }
                    } else { //scoreがnullの場合
                        val fragment = UnknownFragment()
                        val fragmentManager = supportFragmentManager
                        val fragmentTransaction = fragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.container,fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    //エラー処理
                }

            })
        }
        //ランキング
        this.ranking.setOnClickListener{
            val fragment = fragment_ranking()
            val fragmentManager = this.supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container,fragment)
                .addToBackStack(null)
                .commit()

            //ボタンデザイン変更
            val rk: Button = findViewById(R.id.ranking)
            rk.setBackgroundResource(R.drawable.button_push)
            val tp: Button = findViewById(R.id.total_precipitation)
            tp.setBackgroundResource(R.drawable.button_no_push)
            val sc: Button = findViewById(R.id.ameritu_score)
            sc.setBackgroundResource(R.drawable.button_no_push)

            user.text = "ユーザーランキング"
            val myText: TextView = findViewById(R.id.user)
            val customFont = Typeface.createFromAsset(assets, "GN-KillGothic-U-KanaO.ttf")
            myText.typeface = customFont
        }
        //総降水量
        this.total_precipitation.setOnClickListener{
            //ボタンデザイン変更
            val rk: Button = findViewById(R.id.ranking)
            rk.setBackgroundResource(R.drawable.button_no_push)
            val tp: Button = findViewById(R.id.total_precipitation)
            tp.setBackgroundResource(R.drawable.button_push)
            val sc: Button = findViewById(R.id.ameritu_score)
            sc.setBackgroundResource(R.drawable.button_no_push)

            val database = FirebaseDatabase.getInstance().reference.child("users/$mainNumber/precipitation")
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var precipitation = snapshot.value
                    if(precipitation != null) {
                        val bundle = Bundle()
                        bundle.putString("value", "$precipitation")
                        bundle.putString("ap", "$apAvg")
                        val fragment = fragment_total_precipitation()
                        fragment.arguments = bundle
                        val fragmentManager = supportFragmentManager
                        val fragmentTransaction = fragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.container,fragment)
                            .addToBackStack(null)
                            .commit()
                        user.text = "総降水量"
                        val myText: TextView = findViewById(R.id.user)
                        val customFont = Typeface.createFromAsset(assets, "GN-KillGothic-U-KanaO.ttf")
                        myText.setTypeface(customFont)
                    }else {
                        val fragment = UnknownFragment()
                        val fragmentManager = supportFragmentManager
                        val fragmentTransaction = fragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.container,fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    //エラー処理
                }
            })


        }

    }

    //Android標準の戻るボタンを押したとき
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.in_down, R.anim.out_up)
    }
}
