package com.example.whattodo.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whattodo.MyAdapter
import com.example.whattodo.R
import com.example.whattodo.ToDo
import com.example.whattodo.databinding.FragmentPriorityBinding
import com.example.whattodo.databinding.FragmentSearchBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var viewBinding: FragmentSearchBinding
    lateinit var searchRecyclerAdapter: MyAdapter

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
        viewBinding = FragmentSearchBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return viewBinding.root
    }

    private fun setupRecyclerView() {
        viewBinding.searchRecyclerView.layoutManager = LinearLayoutManager(context)
        searchRecyclerAdapter = MyAdapter(ToDo.previewData)
        searchRecyclerAdapter.itemClickListener = object : MyAdapter.OnItemClickListener{
            override fun OnItemClick(position: Int) {
                searchRecyclerAdapter.setPriorityColor("2f22e0", "ca22e0", "db184f")
                searchRecyclerAdapter.sortItemwithAscendingPriority()
            }

        }

        searchRecyclerAdapter.calculatePriorityListener = object : MyAdapter.OnCalculatePriorityListener{
            override fun calculatePriority(
                _importance: Int,
                _timeLeft: Long,
                _time_taken: Float
            ): Float {
                val timeLeft = (_timeLeft / (60 * 60 * 1000)).toInt() // 남은 시간
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
        viewBinding.searchRecyclerView.adapter = searchRecyclerAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}