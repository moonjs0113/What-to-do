package com.example.whattodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whattodo.databinding.FragmentPriorityBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [PriorityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PriorityFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var adapter: MyAdapter

//    val dataformat = SimpleDateFormat("yyyy-MM-dd")

//    var arrayList = arrayListOf<ToDo>(
//        ToDo("example1", dataformat.parse("2023-05-29"), 12f, 5 , 0f) ,
//        ToDo("example1-1", dataformat.parse("2023-05-29"), 10f, 9, 0f) ,
//        ToDo("example1-2", dataformat.parse("2023-05-30"), 8f, 7, 0f) ,
//        ToDo("example1-3", dataformat.parse("2023-05-31"), 4f, 4, 0f) ,
//        ToDo("example2", dataformat.parse("2023-06-01"), 5f, 4, 0f),
//        ToDo("example3", dataformat.parse("2023-06-04"), 4f, 3, 0f),
//        ToDo("example4", dataformat.parse("2023-06-15"), 3f, 2, 0f),
//        ToDo("example5", dataformat.parse("2023-06-30"), 1f, 1, 0f)
//    )

    //LocalDate로 바꾸면서 위 구문을 아래 구문으로 바꿔줬습니다.
    var arrayList = arrayListOf<ToDo>(
        ToDo("example1", LocalDate.parse("2023-05-29"), 12f, 5 , 0f) ,
        ToDo("example1-1", LocalDate.parse("2023-05-29"), 10f, 9, 0f) ,
        ToDo("example1-2", LocalDate.parse("2023-05-30"), 8f, 7, 0f) ,
        ToDo("example1-3", LocalDate.parse("2023-05-31"), 4f, 4, 0f) ,
        ToDo("example2", LocalDate.parse("2023-06-01"), 5f, 4, 0f),
        ToDo("example3", LocalDate.parse("2023-06-04"), 4f, 3, 0f),
        ToDo("example4", LocalDate.parse("2023-06-15"), 3f, 2, 0f),
        ToDo("example5", LocalDate.parse("2023-06-30"), 1f, 1, 0f)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_priority, container, false)


        val binding = FragmentPriorityBinding.inflate(inflater, container, false)
        binding.prioirtyRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter  = MyAdapter(arrayList)

        adapter.itemClickListener = object : MyAdapter.OnItemClickListener{
            override fun OnItemClick(position: Int) {
                adapter.setPriorityColor("2f22e0", "ca22e0", "db184f")
                adapter.sortItemwithAscendingPriority()
            }

        }

        adapter.calculatePriorityListener = object : MyAdapter.OnCalculatePriorityListener{
            override fun calculatePriority(
                _importance: Int,
                _timeLeft: Long,
                _time_taken: Float
            ): Float {
                //넘어오는 값이 초에서 일수로 변경되면서 구문 수정했습니다.
//                val timeLeft = (_timeLeft / (60 * 60 * 1000)).toInt() // 남은 시간
                val timeLeft = _timeLeft.toInt() // 남은 시간
                var spareTime = timeLeft - _time_taken

                if(timeLeft < 0)
                {
                    return -1.0f // 아예 기간이 지나면 음수를 반환함
                }

                if(spareTime < 0) // 만약 남은 시간 보다 소요 시간이 더 걸리면
                {
                    spareTime = 0.001f // 극단적으로 줄여서 우선도 상에서 매우 높은 비중을 가지게 해준다
                }

                return 1 / spareTime + _importance * 10
            }
        }

        binding.prioirtyRecyclerView.adapter = adapter


        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PriorityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PriorityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}