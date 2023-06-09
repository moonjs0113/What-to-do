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
                            filterListByDate()
                            withContext(Dispatchers.Main)
                            {

                                adapter.CalcItemsPrority()
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

//            CoroutineScope(Dispatchers.IO).launch {
//                var arrayList = PersistenceService.share.getAllTodo(requireContext())
//                for(list in arrayList){
//                    PersistenceService.share.deleteTodo(list)
//                }
//                for (i in ToDo.previewData){
//                    PersistenceService.share.insertTodo(i)
//                }
//            }
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
                adapter.CalcItemsPrority()
                adapter.sortItemwithDescendingPriority()
                adapter.notifyDataSetChanged()
            }
        }

    }
}