package com.example.whattodo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whattodo.databinding.FragmentPriorityBinding
import com.example.whattodo.manager.Persistence.PersistenceService
import com.example.whattodo.manager.Persistence.toLocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
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

    lateinit var adapter: MyAdapter
    lateinit var mainActivity: MainActivity

    lateinit var broadcastReceiver: BroadcastReceiver

    //LocalDate로 바꾸면서 위 구문을 아래 구문으로 바꿔줬습니다.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PersistenceService.share.registerContext(mainActivity)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity

        broadcastReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("onReceive", "onReceive")
                if(intent != null)
                {
                    if(intent.hasExtra("message")){
                        CoroutineScope(Dispatchers.IO).launch{
                            adapter.items = PersistenceService.share.getAllTodo()

                            withContext(Dispatchers.Main)
                            {
                                adapter.CalcItemsPrority()
                                adapter.sortItemwithDescendingPriority()
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }else if(intent.hasExtra("colorChanged1"))
                    {
                        val color1 = intent.getStringExtra("colorChanged1")!!
                        val color2 = intent.getStringExtra("colorChanged2")!!
                        val color3 = intent.getStringExtra("colorChanged3")!!

                        adapter.setPriorityColor( color1 , color2, color3)
                        adapter.notifyDataSetChanged()
                        Log.d("Prioirty", "color received")
                    }else if(intent.hasExtra("spareTimeScalar"))
                    {
                        val spareTimeScalar = intent.getIntExtra("spareTimeScalar", 100)!!
                        val priorityScalar = intent.getIntExtra("priorityScalar", 5)!!
                        val ifLeftTimeChecked = intent.getBooleanExtra("ifLeftTimeChecked", true)!!

                        if(ifLeftTimeChecked)
                        {
                            adapter.calculatePriorityListener = object : MyAdapter.OnCalculatePriorityListener{
                                override fun calculatePriority(
                                    item : ToDo
                                ): Float {
                                    val currentTime = LocalDateTime.now()
                                    val remainingTime = if (item.deadLine.toLocalDateTime() > currentTime) {
                                        val duration = Duration.between(currentTime, item.deadLine.toLocalDateTime())
//                val diffHour = (item.deadLine.toLocalDateTime().atZone(ZoneId.systemDefault()).toEpochSecond()/360
//                        - currentTime.atZone(ZoneId.systemDefault()).toEpochSecond()/360)
                                        duration.toHours().toFloat()
                                    } else {                -0.00001f

                                    }

                                    val urgencyFactor = item.time_taken / remainingTime * (spareTimeScalar * 10)
                                    val priorityScore = urgencyFactor + item.importance * priorityScalar

                                    return priorityScore
                                }
                            }
                        }else
                        {
                            adapter.calculatePriorityListener = object : MyAdapter.OnCalculatePriorityListener{
                                override fun calculatePriority(
                                    item : ToDo
                                ): Float {
                                    val currentTime = LocalDateTime.now()
                                    val remainingTime = if (item.deadLine.toLocalDateTime() > currentTime) {
                                        val duration = Duration.between(currentTime, item.deadLine.toLocalDateTime())
//                val diffHour = (item.deadLine.toLocalDateTime().atZone(ZoneId.systemDefault()).toEpochSecond()/360
//                        - currentTime.atZone(ZoneId.systemDefault()).toEpochSecond()/360)
                                        duration.toHours().toFloat()
                                    } else {                -0.00001f

                                    }

                                    val urgencyFactor = item.time_taken / remainingTime * spareTimeScalar
                                    val priorityScore = urgencyFactor + item.importance * priorityScalar * 3

                                    return priorityScore
                                }
                            }
                        }

                        adapter.CalcItemsPrority()
                        adapter.sortItemwithDescendingPriority()
                        adapter.notifyDataSetChanged()
                    }
                }
            }

        }

        val intentFilter = IntentFilter("Todo added")
        intentFilter.addAction("color changed")
        intentFilter.addAction("calc changed")
        requireActivity().registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().unregisterReceiver(broadcastReceiver)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_priority, container, false)

        val binding = FragmentPriorityBinding.inflate(inflater, container, false)
        binding.prioirtyRecyclerView.layoutManager = LinearLayoutManager(context)

        CoroutineScope(Dispatchers.IO).launch{
            var list = PersistenceService.share.getAllTodo()

            withContext(Dispatchers.Main)
            {
                adapter  = MyAdapter(list)
                adapter.sortItemwithAscendingPriority()

                adapter.CalcItemsPrority()
                adapter.sortItemwithDescendingPriority()
                binding.prioirtyRecyclerView.adapter = adapter

            }
        }

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

            }
    }

}