package com.example.whattodo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whattodo.databinding.FragmentDeadlineBinding
import com.example.whattodo.manager.Persistence.PersistenceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DeadlineFragment : Fragment() {
    var binding:FragmentDeadlineBinding?=null
    var adapter: MyAdapter = MyAdapter(ArrayList<ToDo>())

    //선택된 날짜 데이터 초기값은 오늘날짜
    var seletecdDate: LocalDateTime = LocalDateTime.now()

    //아래에 데이터가 저장되어 있다고 가정함
    //최종 단계에선 DB에서 끌어오는 방식으로 처리 해야함
    lateinit var mainActivity: MainActivity

    lateinit var broadcastReceiver: BroadcastReceiver

    //설정된 날짜로 필터링된 리스트
    val fliteredList = arrayListOf<ToDo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PersistenceService.share.registerContext(mainActivity)

        broadcastReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent != null)
                {
                    if(intent.hasExtra("message")){
                        CoroutineScope(Dispatchers.IO).launch{
                            adapter.items = PersistenceService.share.getAllTodo(mainActivity)
                            withContext(Dispatchers.Main)
                            {
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_SEND))
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(broadcastReceiver)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeadlineBinding.inflate(layoutInflater, container, false)

        //달력 날짜 변경 이벤트 처리
        binding!!.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            seletecdDate = LocalDateTime.of(year, month+1, dayOfMonth, 0,0)
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

        CoroutineScope(Dispatchers.IO).launch{
            val list = PersistenceService.share.getAllTodo(mainActivity)
            withContext(Dispatchers.Main)
            {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                for(item in list){
                    if(LocalDate.parse(item.deadLine.substring(0,10), formatter).isEqual(seletecdDate.toLocalDate())){
                        fliteredList.add(item)
                    }
                }
                adapter.items = fliteredList
                adapter.notifyDataSetChanged()
            }
        }

    }
}