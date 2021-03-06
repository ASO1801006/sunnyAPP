package jp.ac.asojuku.sunny

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.sign_in_button


class LoginActivity : AppCompatActivity(), View.OnClickListener  {
    private var mGoogleSignInClient: GoogleSignInClient? = null
    var mAccount: GoogleSignInAccount? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        sign_in_button.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val myText: TextView = findViewById(R.id.appName)
        val customFont = Typeface.createFromAsset(assets, "Cinzel-Regular.otf")
        myText.typeface = customFont

        val startText: TextView = findViewById(R.id.detail_button)
        val startFont = Typeface.createFromAsset(assets, "GN-KillGothic-U-KanaO.ttf")
        startText.typeface = startFont

        val st: Button = findViewById(R.id.detail_button)
        st.setBackgroundResource(R.drawable.button_start)
        val lo: Button = findViewById(R.id.sign_out_button)
        lo.setBackgroundResource(R.drawable.button_logout)
    }

    public override fun onStart() {
        super.onStart()
        mAccount = GoogleSignIn.getLastSignedInAccount(this)
        updateUI()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            mAccount = completedTask.getResult(ApiException::class.java)
            updateUI()
        } catch (e: ApiException) {
            Log.w(TAG,"signInResult:failed code=" + e.statusCode)
            updateUI()
        }
    }

    private fun signIn() {
        if (mAccount == null) {
            val signInIntent = mGoogleSignInClient!!.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }else {
            Toast.makeText(this, "既にログイン済みです", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signOut() {
        if (mAccount == null) {
            Toast.makeText(this, "既にログアウト済みです", Toast.LENGTH_SHORT).show()
        }
        mGoogleSignInClient!!.signOut().addOnCompleteListener(this) {
            mAccount = null
            updateUI()
        }
        val intent = Intent(application, LocationService::class.java)
        stopService(intent)

    }

    //Detail画面遷移
    private fun goToDetailActivity() {
        if (mAccount != null) {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("USER_ID", mAccount!!.id)
                .putExtra("USER_NAME", mAccount!!.displayName)
                .putExtra("USER_MAIL", mAccount!!.email)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)//右から画面
        }else{
            Toast.makeText(this, "ログインしてください", Toast.LENGTH_SHORT).show()
        }
    }

    private fun revokeAccess() {
        mGoogleSignInClient!!.revokeAccess()
            .addOnCompleteListener(this) {
                updateUI()
            }
    }

    private fun updateUI() {
        if (mAccount != null) {
            user!!.text = getString(R.string.signed_in_fmt, mAccount!!.displayName)
        } else {
            user.setText(R.string.signed_out)
        }
        print("updated")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sign_in_button -> signIn()
            R.id.sign_out_button -> signOut()
            R.id.detail_button -> goToDetailActivity()
        }
    }

    companion object {
        private const val TAG = "SignInActivity"
        private const val RC_SIGN_IN = 9001
    }

    //Android標準の戻るボタンを押したとき
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.in_down, R.anim.out_up)
    }
}