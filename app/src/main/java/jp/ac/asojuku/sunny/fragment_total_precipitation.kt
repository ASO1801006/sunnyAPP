package jp.ac.asojuku.sunny

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_ranking.*
import kotlinx.android.synthetic.main.fragment_total_precipitation.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [TotalPrecipitationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class fragment_total_precipitation : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var value: Float = 0.0f
    var apavg: Float = 0.0f

    override fun onViewCreated(view: View,savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState);
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        graph()
    }
    private fun getBarData(): ArrayList<IBarDataSet> {
        value = arguments!!.getString("value")!!.toFloat() //自分の総降水量
        apavg = arguments!!.getString("ap")!!.toFloat() //全体平均の総降水量

        //表示させるデータ
        val entries = ArrayList<BarEntry>().apply {
            add(BarEntry(1f, value))
            add(BarEntry(2f, apavg))
        }
        val dataSet = BarDataSet(entries, "bar").apply {
            //整数で表示
            valueFormatter = IValueFormatter { value, _, _, _ -> "" + value.toInt() }
            //ハイライトさせない
            isHighlightEnabled = false
            //Barの色をセット
            setColors(intArrayOf(R.color.material_blue, R.color.material_green), activity)
        }
        val bars = ArrayList<IBarDataSet>()
        bars.add(dataSet)
        return bars
    }
    private fun graph(){
        //表示データ取得
        bar_chart.data = BarData(getBarData())
        //Y軸(左)の設定
        bar_chart.axisLeft.apply {
            axisMinimum = 0f
            when {
                value <= 100f && apavg <= 100f -> axisMaximum = 100f
                (value > 100f && value <= 500f) || (apavg > 100f && apavg <= 500f)-> axisMaximum = 500f
                (value > 500f && value <= 1000f) || (apavg > 500f && apavg <= 1000f) -> axisMaximum = 1000f
                (value > 1000f && value <= 3000f) || (apavg > 1000f && apavg <= 3000f) -> axisMaximum = 3000f
                (value > 3000f && value <= 5000f) || (apavg > 3000f && apavg <= 5000f) -> axisMaximum = 5000f
                (value > 5000f && value <= 10000f) || (apavg > 5000f && apavg <= 10000f) -> axisMaximum = 10000f
                (value > 10000f && value <= 50000f) || (apavg > 10000f && apavg <= 50000f) -> axisMaximum = 50000f
                (value > 50000f && value <= 100000f) || (apavg > 50000f && apavg <= 100000f) -> axisMaximum = 100000f
            }
            labelCount = 10
            setDrawTopYLabelEntry(true)
            setValueFormatter { value, axis -> "" + value.toInt()}
        }
        //Y軸(右)の設定
        bar_chart.axisRight.apply {
            setDrawLabels(false)
            setDrawGridLines(false)
            setDrawZeroLine(false)
            setDrawTopYLabelEntry(true)
        }
        //X軸の設定
        val labels = arrayOf("","あなた","ユーザー平均") //最初の””は原点の値
        bar_chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            labelCount = 1 //表示させるラベル数
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(true)
            setDrawGridLines(false)
            setDrawAxisLine(true)
        }
        //グラフ上の表示
        bar_chart.apply {
            setDrawValueAboveBar(true)
            description.isEnabled = false
            isClickable = false
            legend.isEnabled = false //凡例
            setScaleEnabled(false)
            animateY(2000, Easing.EasingOption.Linear)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_total_precipitation, container, false)
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment fragment_total_precipitation.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            fragment_total_precipitation().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}