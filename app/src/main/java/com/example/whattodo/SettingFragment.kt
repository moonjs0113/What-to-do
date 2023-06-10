package com.example.whattodo

import android.R
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
// 여기는 환경 설정 shared Reference 설정되면 그 데이터를 가지고 올 것임///
    var colorList = arrayListOf<String>("e03b22", "eff238", "24f064")

    var leftTimeNumPick = 10
    var priorityNumPick = 5
    var isLeftTimeLeft = true

    // 여기는 환경 설정 shared Reference 설정되면 그 데이터를 가지고 올 것임///

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

        binding.changeColor1.setBackgroundColor(Color.parseColor( "#" + colorList[0]))
        binding.changeColor2.setBackgroundColor(Color.parseColor( "#" + colorList[1]))
        binding.changeColor3.setBackgroundColor(Color.parseColor( "#" + colorList[2]))

        binding.changeColor1.setOnClickListener(
            object : OnClickListener{
                override fun onClick(v: View?) {
                    ColorPickerDialog.Builder(mainActivity)
                        .setTitle("Select Color")
                        .setPreferenceName("MyColorPickerDialog")
                        .setPositiveButton("OK",
                            ColorEnvelopeListener { envelope, fromUser ->
                                val color = envelope.hexCode.substring(2,8)
                                binding.changeColor1.setBackgroundColor(Color.parseColor("#" + color))
                                colorList[0] = color

                                val intent = Intent("color changed")
                                intent.putExtra("colorChanged1",colorList[0])
                                intent.putExtra("colorChanged2",colorList[1])
                                intent.putExtra("colorChanged3",colorList[2])
                                mainActivity.sendBroadCastInMainActivity(intent)

                                Toast.makeText(requireActivity(), intent.action, Toast.LENGTH_SHORT).show()
                            })
                        .setNegativeButton(
                            "Cancel"
                        ) { dialogInterface, i -> dialogInterface.dismiss() }
                        .attachAlphaSlideBar(true) // the default value is true.
                        .attachBrightnessSlideBar(true) // the default value is true.
                        .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                        .show()
                }
            }
        )

        binding.changeColor2.setOnClickListener(
            object : OnClickListener{
                override fun onClick(v: View?) {
                    ColorPickerDialog.Builder(mainActivity)
                        .setTitle("Select Color")
                        .setPreferenceName("MyColorPickerDialog")
                        .setPositiveButton("OK",
                            ColorEnvelopeListener { envelope, fromUser ->
                                val color = envelope.hexCode.substring(2,8)
                                binding.changeColor2.setBackgroundColor(Color.parseColor("#" + color))
                                colorList[1] = color

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
            }
        )

        binding.changeColor3.setOnClickListener(
            object : OnClickListener{
                override fun onClick(v: View?) {
                    ColorPickerDialog.Builder(mainActivity)
                        .setTitle("Select Color")
                        .setPreferenceName("MyColorPickerDialog")
                        .setPositiveButton("OK",
                            ColorEnvelopeListener { envelope, fromUser ->
                                val color = envelope.hexCode.substring(2,8)

                                binding.changeColor3.setBackgroundColor(Color.parseColor("#" + color))
                                colorList[2] = color

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
            }
        )

        binding.changeCal.setOnClickListener {
            val layout = layoutInflater.inflate(com.example.whattodo.R.layout.dialog_prioritycalc_select, null)
            val build = AlertDialog.Builder(mainActivity).apply {
                setView(layout)
            }
            val dialog = build.create()
            dialog.show()

            var leftTimePrefer : RadioButton = layout.findViewById(com.example.whattodo.R.id.leftTimePrefer)
            var priorityPrefer : RadioButton = layout.findViewById(com.example.whattodo.R.id.priorityPrefer)

            leftTimePrefer.isChecked = isLeftTimeLeft
            priorityPrefer.isChecked = !isLeftTimeLeft


            var spareTimeScalar: NumberPicker = layout.findViewById(com.example.whattodo.R.id.spareTimeScalar)
            spareTimeScalar.minValue = 1
            spareTimeScalar.maxValue = 10
            spareTimeScalar.value = leftTimeNumPick

            var priorityScalar: NumberPicker = layout.findViewById(com.example.whattodo.R.id.priorityScalar)
            priorityScalar.minValue = 1
            priorityScalar.maxValue = 10
            priorityScalar.value = priorityNumPick

            var btnCancel: Button = layout.findViewById(com.example.whattodo.R.id.btn_cancel2)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            var btnOK: Button = layout.findViewById(com.example.whattodo.R.id.btn_ok2)
            btnOK.setOnClickListener {
                dialog.dismiss()

                leftTimeNumPick = spareTimeScalar.value
                priorityNumPick = priorityScalar.value
                isLeftTimeLeft = leftTimePrefer.isChecked

                val intent = Intent("calc changed")
                intent.putExtra("spareTimeScalar",spareTimeScalar.value)
                intent.putExtra("priorityScalar",priorityScalar.value)
                intent.putExtra("ifLeftTimeChecked",leftTimePrefer.isChecked)
                mainActivity.sendBroadCastInMainActivity(intent)
            }

        }



        return binding.root
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingFragment().apply {

            }
    }
}