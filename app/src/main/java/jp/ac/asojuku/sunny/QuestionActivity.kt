package jp.ac.asojuku.sunny

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_question.*

class QuestionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        val userId = intent.getStringExtra("USER_ID")
        val userName = intent.getStringExtra("USER_NAME")
        val userMail = intent.getStringExtra("USER_MAIL")

        returnButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            if (userId != null) {
                intent.putExtra("USER_ID", userId)
                    .putExtra("USER_NAME", userName)
                    .putExtra("USER_MAIL", userMail)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.in_up, R.anim.out_down)//下から画面
        }

    }
}