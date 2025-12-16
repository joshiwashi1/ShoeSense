package com.shoesense.shoesense.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shoesense.shoesense.Model.SlotEvent
import com.shoesense.shoesense.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ---------- TOP-LEVEL SEALED CLASS ----------
sealed class HistoryItem {
    data class DateHeader(val label: String) : HistoryItem()
    data class EventRow(val event: SlotEvent) : HistoryItem()
}
// -------------------------------------------

class HistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<HistoryItem>()

    // ✅ latest slot names (slotId -> slotName)
    private var slotNameMap: Map<String, String> = emptyMap()

    companion object {
        private const val TYPE_DATE_HEADER = 0
        private const val TYPE_EVENT_ROW = 1
    }

    /**
     * ✅ Call this whenever slot names change (rename)
     * Example: adapter.updateSlotNames(map)
     */
    fun updateSlotNames(map: Map<String, String>) {
        slotNameMap = map
        notifyDataSetChanged() // refresh visible rows with latest names
    }

    // Public API – called from HistoryActivity
    fun submitList(events: List<SlotEvent>) {
        items.clear()

        // Sort newest → oldest (just in case)
        val sorted = events.sortedByDescending { it.lastUpdated }

        var lastDateLabel: String? = null

        for (e in sorted) {
            val dateLabel = formatDateOnly(e.lastUpdated) ?: continue

            // Insert date header when date changes
            if (dateLabel != lastDateLabel) {
                items.add(HistoryItem.DateHeader(dateLabel))
                lastDateLabel = dateLabel
            }

            items.add(HistoryItem.EventRow(e))
        }

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HistoryItem.DateHeader -> TYPE_DATE_HEADER
            is HistoryItem.EventRow -> TYPE_EVENT_ROW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_DATE_HEADER -> {
                val view = inflater.inflate(R.layout.item_history_date_header, parent, false)
                DateHeaderVH(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.activity_item_history, parent, false)
                EventVH(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HistoryItem.DateHeader -> (holder as DateHeaderVH).bind(item)
            is HistoryItem.EventRow -> (holder as EventVH).bind(item.event, slotNameMap)
        }
    }

    override fun getItemCount(): Int = items.size

    // ---------- ViewHolders ----------

    class DateHeaderVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDateHeader: TextView = itemView.findViewById(R.id.tvDateHeader)

        fun bind(item: HistoryItem.DateHeader) {
            tvDateHeader.text = item.label
        }
    }

    class EventVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imgShoe: ImageView = itemView.findViewById(R.id.imgShoeIcon)
        private val tvAction: TextView = itemView.findViewById(R.id.tvAction)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        fun bind(event: SlotEvent, slotNameMap: Map<String, String>) {
            val timeStr = parseTime(event.lastUpdated)

            // ✅ Always show latest slot name if available
            val latestName = slotNameMap[event.slotId]
            val label = when {
                !latestName.isNullOrBlank() -> latestName
                else -> event.displaySlotLabel() // fallback (old saved name or slotId)
            }

            tvAction.text = "$label • ${event.status.replaceFirstChar { it.uppercase() }}"
            tvTime.text = timeStr

            tvStatus.text = event.status
            tvStatus.setBackgroundResource(
                if (event.status.equals("occupied", ignoreCase = true))
                    R.drawable.status_occupied_bg
                else
                    R.drawable.status_empty_bg
            )

            imgShoe.setImageResource(R.drawable.shoe)
        }

        private fun parseTime(iso: String): String {
            return try {
                val date: Date? = isoFormat.parse(iso)
                if (date != null) timeFormat.format(date) else "-"
            } catch (e: Exception) {
                "-"
            }
        }
    }

    // ---------- Date formatting helpers ----------

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    private fun formatDateOnly(iso: String): String? {
        return try {
            val d: Date? = isoFormat.parse(iso)
            if (d != null) dateFormat.format(d) else null
        } catch (e: Exception) {
            null
        }
    }
}
