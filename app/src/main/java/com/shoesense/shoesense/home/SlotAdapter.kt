package com.shoesense.shoesense.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.R
import com.shoesense.shoesense.home.SlotRow
import java.text.SimpleDateFormat
import java.util.Locale

class SlotAdapter(
    private val onAdd: () -> Unit,
    private val onClick: (Slot) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<SlotRow> = emptyList()

    companion object {
        private const val VT_ADD = 0
        private const val VT_DATA = 1
    }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is SlotRow.Add -> VT_ADD
            is SlotRow.Data -> VT_DATA
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_ADD -> AddVH(LayoutInflater.from(parent.context)
                .inflate(R.layout.add_slot_item, parent, false))
            else   -> DataVH(LayoutInflater.from(parent.context)
                .inflate(R.layout.slot_item, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = items[position]) {
            is SlotRow.Add -> (holder as AddVH).bind(onAdd)
            is SlotRow.Data -> (holder as DataVH).bind(row.slot, onClick)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(rows: List<SlotRow>) {
        items = rows
        notifyDataSetChanged()
    }

    /* ====== ViewHolders ====== */

    private class AddVH(v: View) : RecyclerView.ViewHolder(v) {
        private val icon: ImageView = v.findViewById(R.id.btnAdd)
        private val label: TextView = v.findViewById(R.id.addSlotText)

        fun bind(onAdd: () -> Unit) {
            icon.contentDescription = "Add Slot"
            // label already has "Add Slot" in XML; keep or override if needed:
            // label.text = "Add Slot"
            itemView.setOnClickListener { onAdd() }
        }
    }


    private class DataVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle: TextView = v.findViewById(R.id.tvSlotTitle)
        private val tvStatus: TextView = v.findViewById(R.id.tvStatus)
        private val tvUpdated: TextView = v.findViewById(R.id.tvUpdated)
        private val imgStatus: ImageView = v.findViewById(R.id.imgStatus)

        fun bind(s: Slot, onClick: (Slot) -> Unit) {
            tvTitle.text = s.name
            if (s.occupied) {
                tvStatus.text = "Occupied"
                imgStatus.setImageResource(R.drawable.ic_check_green)
            } else {
                tvStatus.text = "Empty"
                imgStatus.setImageResource(R.drawable.ic_close_red) // ensure this exists
            }
            tvUpdated.text = "Last Updated:\n${formatTime(s.lastUpdated)}"
            itemView.setOnClickListener { onClick(s) }
        }

        private fun formatTime(raw: String): String {
            val inFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss"
            )
            val out = SimpleDateFormat("h:mm a", Locale.getDefault())
            for (p in inFormats) {
                try {
                    val df = SimpleDateFormat(p, Locale.getDefault())
                    val d = df.parse(raw)
                    if (d != null) return out.format(d)
                } catch (_: Exception) {}
            }
            return raw
        }
    }
}
