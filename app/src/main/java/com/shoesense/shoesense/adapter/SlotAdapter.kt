package com.shoesense.shoesense.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shoesense.shoesense.R
import com.shoesense.shoesense.Model.Slot

class SlotAdapter(
    private val slotList: MutableList<Slot>,
    private val onAddSlotClickListener: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SLOT = 0
        private const val TYPE_ADD_SLOT = 1
    }

    // ViewHolder for regular slot items
    class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val slotTitle: TextView = itemView.findViewById(R.id.slotTitle)
        val statusIcon: ImageView = itemView.findViewById(R.id.statusIcon)
        val statusText: TextView = itemView.findViewById(R.id.statusText)
        val lastUpdated: TextView = itemView.findViewById(R.id.lastUpdated)
    }

    // ViewHolder for the "Add Slot" button
    class AddSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addSlotText: TextView = itemView.findViewById(R.id.addSlotText)
        val addSlotIcon: ImageView = itemView.findViewById(R.id.addSlotIcon)
    }

    override fun getItemCount(): Int = slotList.size + 1 // extra for "Add Slot"

    override fun getItemViewType(position: Int): Int {
        return if (position == slotList.size) TYPE_ADD_SLOT else TYPE_SLOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ADD_SLOT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.add_slot_item, parent, false)
            AddSlotViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.slot_item, parent, false)
            SlotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SlotViewHolder) {
            val slot = slotList[position]

            // Auto-generate "Slot X" title
            holder.slotTitle.text = "Slot ${position + 1}"

            // Status + icon
            holder.statusIcon.setImageResource(
                if (slot.isOccupied) R.drawable.check else R.drawable.wrong
            )
            holder.statusText.text = if (slot.isOccupied) "Occupied" else "Empty"
            holder.lastUpdated.text = "Last Updated: ${slot.lastUpdated}"

        } else if (holder is AddSlotViewHolder) {
            holder.addSlotIcon.setImageResource(R.drawable.plus)
            holder.addSlotText.text = "Tap to add slot"
            holder.itemView.setOnClickListener {
                onAddSlotClickListener()
            }
        }
    }
}