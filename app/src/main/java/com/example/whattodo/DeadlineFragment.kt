package com.example.whattodo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whattodo.databinding.FragmentDeadlineBinding
import java.time.LocalDate

class DeadlineFragment : Fragment() {
    var binding:FragmentDeadlineBinding?=null
    var adapter: MyAdapter = MyAdapter(ArrayList<ToDo>())

    //선택된 날짜 데이터 초기값은 오늘날짜
    var seletecdDate:LocalDate = LocalDate.now()

    //아래에 데이터가 저장되어 있다고 가정함
    //최종 단계에선 DB에서 끌어오는 방식으로 처리 해야함
    var arrayList = arrayListOf<ToDo>(
        ToDo("example1", LocalDate.parse("2023-05-29"), 12f, 5 , 0f) ,
        ToDo("example1-1", LocalDate.parse("2023-05-29"), 10f, 9, 0f) ,
        ToDo("example1-2", LocalDate.parse("2023-05-30"), 8f, 7, 0f) ,
        ToDo("example1-3", LocalDate.parse("2023-05-31"), 4f, 4, 0f) ,
        ToDo("example2", LocalDate.parse("2023-06-01"), 5f, 4, 0f),
        ToDo("example2", LocalDate.parse("2023-06-01"), 5f, 3, 0f),
        ToDo("example2", LocalDate.parse("2023-06-01"), 3f, 5, 0f),
        ToDo("example2", LocalDate.parse("2023-06-01"), 3f, 2, 0f),
        ToDo("example2", LocalDate.parse("2023-06-01"), 5f, 1, 0f),
        ToDo("example3", LocalDate.parse("2023-06-04"), 4f, 3, 0f),
        ToDo("example4", LocalDate.parse("2023-06-15"), 3f, 2, 0f),
        ToDo("example5", LocalDate.parse("2023-06-30"), 1f, 1, 0f)
    )

    //설정된 날짜로 필터링된 리스트
    val fliteredList = arrayListOf<ToDo>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeadlineBinding.inflate(layoutInflater, container, false)

        //달력 날짜 변경 이벤트 처리
        binding!!.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            seletecdDate = LocalDate.of(year, month+1, dayOfMonth)
            Log.i("seletecDate", seletecdDate.toString())

            filterListByDate()
        }

        //리스트 내의 아이템 클릭시 이벤트 처리
        //아직 미구현(뭘 하기로 했는지 기억이...)
        adapter.itemClickListener = object :MyAdapter.OnItemClickListener{
            override fun OnItemClick(position: Int) {

            }
        }

        //리사이클러뷰 초기설정
        binding!!.prioirtyRecyclerView.layoutManager = LinearLayoutManager(context)
        binding!!.prioirtyRecyclerView.adapter = this@DeadlineFragment.adapter

        //우선도 세팅
        adapter.calculatePriorityListener = object : MyAdapter.OnCalculatePriorityListener{
            override fun calculatePriority(
                _importance: Int,
                _timeLeft: Long,
                _time_taken: Float
            ): Float {
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

        //시작하면서 현재 날짜로 필터링
        filterListByDate()


        return binding!!.root
    }

    private fun filterListByDate() {
        fliteredList.clear()
        for(item in arrayList){
            if(item.deadLine.isEqual(seletecdDate)){
                fliteredList.add(item)
            }
        }
        adapter.items = fliteredList
        adapter.notifyDataSetChanged()
    }
}