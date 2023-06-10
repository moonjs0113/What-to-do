package com.example.whattodo

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
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
                                adapter.sortItemwithDescendingPriority()
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

            //리사이클러뷰 초기설정
            binding!!.prioirtyRecyclerView.layoutManager = LinearLayoutManager(context)
            //리스트 내의 아이템 클릭시 이벤트 처리 - 수정, 삭제
            adapter.itemLongClickListener = object : MyAdapter.OnItemClickListener,
                MyAdapter.OnLongItemClickListener {
                override fun OnItemClick(position: Int) {
                    TODO("Not yet implemented")
                }

                override fun OnItemLongClick(position: Int): Boolean {
                    // Todo 객체 삭제
                    val builder = AlertDialog.Builder(mainActivity)
                    builder.setMessage("수정 또는 삭제하시겠습니까?")
                        .setPositiveButton("삭제") { dialog, which ->
                            // 삭제 작업 수행
                            // 삭제 작업 수행
                            CoroutineScope(Dispatchers.IO).launch {
                                PersistenceService.share.registerContext(mainActivity)
                                var list2 = PersistenceService.share.getAllTodo(mainActivity)
                                PersistenceService.share.deleteTodo(list2[position])
                            }
                            adapter.items.removeAt(position)
                            adapter.notifyDataSetChanged()
                            dialog.dismiss()
                        }
                        .setNegativeButton("수정") { dialog, which ->
                            // 수정
                            mainActivity.binding.todoInput.setText(adapter.items[position].explanation)
                            mainActivity.binding.timeToSpend.setText("${adapter.items[position].time_taken.toInt()}시간")
                            val time = adapter.items[position].deadLine.split("T")
                            mainActivity.binding.datePickedText.setText(
                                "${time[0].split("-")[0]}년 ${time[0].split("-")[1].toInt()}월 " +
                                        "${time[0].split("-")[2].toInt()}일 ${time[1].split(":")[0].toInt()}시 ${
                                            time[1].split(
                                                ":"
                                            )[1].toInt()
                                        }분"
                            )
                            mainActivity.binding.importance.setText("${adapter.items[position].importance}/10")

                            // 수정 모드로 변경 - 등록 버튼이 수정 버튼으로 변경. 수정 버튼을 누르기 전까지는 모드 해제 불가
                            mainActivity.isAmend = true
                            mainActivity.binding.registerBtn.text = "수정"
                            if (mainActivity.animator != null && !mainActivity.isInputFormOpen) {
                                mainActivity.animator.start()
                                mainActivity.isInputFormOpen = true
                            }
                            mainActivity.idToAmend = adapter.items[position].id

                            dialog.dismiss()
                        }
                        .show()
                    return true
                }
            }
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