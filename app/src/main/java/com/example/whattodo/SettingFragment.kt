package com.example.whattodo

import android.R
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.whattodo.databinding.FragmentSettingBinding
import com.example.whattodo.manager.Persistence.PersistenceService
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.example.whattodo.manager.Persistence.SharedPreferencesManager.PriorityItem

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingFragment : Fragment() {
    lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PersistenceService.share.registerContext(mainActivity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentSettingBinding.inflate(inflater, container, false)
        var colorList = PersistenceService.share.getColorArray()
        binding.changeColor1.setBackgroundColor(Color.parseColor( "#" + colorList[0]))
        binding.changeColor2.setBackgroundColor(Color.parseColor( "#" + colorList[1]))
        binding.changeColor3.setBackgroundColor(Color.parseColor( "#" + colorList[2]))

        val intent = Intent("color changed")
        intent.putExtra("colorChanged1",colorList[0])
        intent.putExtra("colorChanged2",colorList[1])
        intent.putExtra("colorChanged3",colorList[2])
        mainActivity.sendBroadCastInMainActivity(intent)

        binding.changeColor1.setOnClickListener(
            object : OnClickListener{
                override fun onClick(v: View?) {
                    showColorPicker(binding,0) {
                        binding.changeColor1.setBackgroundColor(Color.parseColor(it))
                    }
                }
            }
        )

        binding.changeColor2.setOnClickListener(
            object : OnClickListener{
                override fun onClick(v: View?) {
                    showColorPicker(binding,1) {
                        binding.changeColor2.setBackgroundColor(Color.parseColor(it))
                    }
                }
            }
        )

        binding.changeColor3.setOnClickListener(
            object : OnClickListener{
                override fun onClick(v: View?) {
                    showColorPicker(binding,2) {
                        binding.changeColor3.setBackgroundColor(Color.parseColor(it))
                    }
                }
            }
        )

        binding.changeCal.setOnClickListener {
            val layout = layoutInflater.inflate(com.example.whattodo.R.layout.dialog_prioritycalc_select, null)
            val priorityValue = PersistenceService.share.getPriorityItem()
            var leftTimePrefer : RadioButton = layout.findViewById(com.example.whattodo.R.id.leftTimePrefer)
            var priorityPrefer : RadioButton = layout.findViewById(com.example.whattodo.R.id.priorityPrefer)

            leftTimePrefer.isChecked = (priorityValue.first == PriorityItem.TIME)
            priorityPrefer.isChecked = (priorityValue.first == PriorityItem.IMPORTANCE)

            var spareTimeScalar: NumberPicker = layout.findViewById(com.example.whattodo.R.id.spareTimeScalar)
            spareTimeScalar.minValue = 1
            spareTimeScalar.maxValue = 10
            spareTimeScalar.value = priorityValue.third // leftTimeNumPick

            var priorityScalar: NumberPicker = layout.findViewById(com.example.whattodo.R.id.priorityScalar)
            priorityScalar.minValue = 1
            priorityScalar.maxValue = 10
            priorityScalar.value = priorityValue.second //priorityNumPick

            val build = AlertDialog.Builder(mainActivity).apply {
                setView(layout)
            }
            val dialog = build.create()
            dialog.show()

            var btnCancel: Button = layout.findViewById(com.example.whattodo.R.id.btn_cancel2)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            var btnOK: Button = layout.findViewById(com.example.whattodo.R.id.btn_ok2)
            btnOK.setOnClickListener {
                dialog.dismiss()
                PersistenceService.share.setPriorityItem(
                    if (leftTimePrefer.isChecked) 0 else 1,
                    priorityScalar.value,
                    spareTimeScalar.value
                )

                val intent = Intent("calc changed")
                intent.putExtra("spareTimeScalar",spareTimeScalar.value)
                intent.putExtra("priorityScalar", priorityScalar.value)
                intent.putExtra("ifLeftTimeChecked",leftTimePrefer.isChecked)
                mainActivity.sendBroadCastInMainActivity(intent)
            }

        }

        val priorityValue = PersistenceService.share.getPriorityItem()
        val intent2 = Intent("calc changed")
        intent2.putExtra("spareTimeScalar",priorityValue.third)
        intent2.putExtra("priorityScalar", priorityValue.second)
        intent2.putExtra("ifLeftTimeChecked",(priorityValue.first == PriorityItem.TIME))
        mainActivity.sendBroadCastInMainActivity(intent2)

        binding.alramOk.isChecked = PersistenceService.share.getNotificationValue()
        binding.alramNo.isChecked = !(PersistenceService.share.getNotificationValue())
        binding.alramOk.setOnCheckedChangeListener { _, b ->
            PersistenceService.share.setNotificationValue(b)
            if(b)
            {
                mainActivity.startForegroundService()
            }else
            {
                mainActivity.stopForegroundService()
            }
        }

        if(binding.alramOk.isChecked)
        {
            mainActivity.startForegroundService()
        }else
        {
            mainActivity.stopForegroundService()
        }

        return binding.root
    }

    fun showColorPicker(binding: FragmentSettingBinding, index: Int, completeHandler: (String) -> Unit) {
        ColorPickerDialog.Builder(mainActivity)
            .setTitle("Select Color")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton("OK",
                ColorEnvelopeListener { envelope, fromUser ->
                    val color = envelope.hexCode.substring(2,8)
                    completeHandler("#$color")
                    PersistenceService.share.setColor(index, color)
                    var colorList = PersistenceService.share.getColorArray()
                    val intent = Intent("color changed")
                    intent.putExtra("colorChanged1",colorList[0])
                    intent.putExtra("colorChanged2",colorList[1])
                    intent.putExtra("colorChanged3",colorList[2])
                    mainActivity.sendBroadCastInMainActivity(intent)
                })
            .setNegativeButton(
                "Cancel"
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(true) // the default value is true.
            .attachBrightnessSlideBar(true) // the default value is true.
            .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
            .show()
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingFragment().apply {

            }
    }
}