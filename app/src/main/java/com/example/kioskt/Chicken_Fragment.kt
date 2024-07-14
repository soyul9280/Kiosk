package com.example.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.kioskt.MainActivity
import com.example.kioskt.databinding.FragmentChickenBinding


class Chicken_Fragment : Fragment() {
    private lateinit var binding: FragmentChickenBinding
    private lateinit var adapter:RecyclerViewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChickenBinding.inflate(inflater, container, false)
        val view= binding.root
        val activity = requireActivity() as MainActivity
        adapter=activity.adapter
        val linearLayouts = listOf(
            binding.friedChicken,
            binding.hotSpicyChicken,
            binding.soyChicken,
            binding.garlicChicken,
            binding.takoChicken,
            binding.spicySoyChicken
        )
        for (linearLayout in linearLayouts) {
            linearLayout.setOnClickListener {
                handleLinearClick(linearLayout)
            }
        }
        return view
    }
    fun setAdapter(adapter:RecyclerViewAdapter){
        this.adapter=adapter
    }
    private fun handleLinearClick(linearLayout: CardView) {

        val textId = resources.getIdentifier(
            "${resources.getResourceEntryName(linearLayout.id)}_text",
            "id",
            requireActivity().packageName
        )

        val priceId = resources.getIdentifier(
            "${resources.getResourceEntryName(linearLayout.id)}_price",
            "id",
            requireActivity().packageName
        )
        //클릭된 <LinearLayout> 에서 메뉴 이름, 가격 가져오기
        var text: String? = null
        var price: Int? = null
        linearLayout.findViewById<TextView>(textId)?.let {
            text = it.text.toString()
        }
        linearLayout.findViewById<TextView>(priceId)?.let {
            price = it.text.toString().toIntOrNull()
        }
        if(text!=null&&price!=null){
            val itemData=ItemData(text!!,price!!,1)
            adapter.addItem(itemData)
        }
    }

}
