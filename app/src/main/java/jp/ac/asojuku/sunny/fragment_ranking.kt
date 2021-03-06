package jp.ac.asojuku.sunny

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_ranking.*
import kotlinx.android.synthetic.main.fragment_unknown.*
import java.lang.StringBuilder

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [fragment_ranking.newInstance] factory method to
 * create an instance of this fragment.
 */
class fragment_ranking : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        var database = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("score")

        var getdata = object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                var num = 1
                for(users in p0.children) {
                    var score = users.child("score").getValue()
                    if(score != null){
                        var name = users.child("name").getValue() as String
                        when {
                            num == 1 -> Text1.setText(name)
                            num == 2 -> Text2.setText(name)
                            num == 3 -> Text3.setText(name)
                            num == 4 -> Text4.setText(name)
                            num == 5 -> Text5.setText(name)
                            num == 6 -> Text6.setText(name)
                            num == 7 -> Text7.setText(name)
                            num == 8 -> Text8.setText(name)
                            num == 9 -> Text9.setText(name)
                            num == 10 -> Text10.setText(name)
                        }
                        num++
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                //エラー処理
            }
        }
        database.addValueEventListener(getdata)
        database.addListenerForSingleValueEvent(getdata)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment fragment_ranking.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            fragment_ranking().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}