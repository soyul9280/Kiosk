package com.example.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kioskt.MainActivity
import com.example.kioskt.databinding.ItemRecyclerviewBinding


class RecyclerViewAdapter(val itemList: ArrayList<ItemData>, val updateTotalPrice: () -> Unit
) : RecyclerView.Adapter<RecyclerViewAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }
    fun addItem(newItem: ItemData){
        itemList.add(newItem)
        notifyItemInserted(itemList.size-1)
        updateTotalPrice()
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = itemList[position]
        holder.bind(item)}

    override fun getItemCount(): Int {
        return itemList.size
    }
    inner class Holder(private val binding: ItemRecyclerviewBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item:ItemData){
            binding.name.text=item.ItemName
            binding.cnt.text=item.Itemcnt.toString()
            binding.price.text=(item.ItemPriceText.toInt()*item.Itemcnt).toString()

            binding.minusBtn.setOnClickListener{
                if (item.Itemcnt > 1) {
                    item.Itemcnt--
                    binding.cnt.text = item.Itemcnt.toString()
                    binding.price.text = (item.ItemPriceText.toInt() * item.Itemcnt).toString()
                    updateTotalPrice()
                }
            }
            binding.plusBtn.setOnClickListener{
                    item.Itemcnt++
                    binding.cnt.text = item.Itemcnt.toString()
                    binding.price.text = (item.ItemPriceText.toInt() * item.Itemcnt).toString()
                    updateTotalPrice()
                }
            binding.DelBtn.setOnClickListener{
                val position=adapterPosition
                if (position !=RecyclerView.NO_POSITION){
                    (itemView.context as MainActivity).itemList.removeAt(position)
                    (itemView.context as MainActivity).adapter.notifyItemRemoved(position)
                    (itemView.context as MainActivity).updateTotalPrice()
                }
            }
        }

    }
}