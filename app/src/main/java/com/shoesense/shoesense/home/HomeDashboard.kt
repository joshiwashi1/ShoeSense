package com.shoesense.shoesense.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shoesense.shoesense.R
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.adapter.SlotAdapter

class HomeDashboard : AppCompatActivity() {

    private lateinit var slotRecyclerView: RecyclerView
    private lateinit var slotAdapter: SlotAdapter
    private lateinit var slotList: MutableList<Slot>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_dashboard)

        // Initialize RecyclerView and Slot List
        slotRecyclerView = findViewById(R.id.slotRecyclerView)
        slotList = mutableListOf()

        // Initialize SlotAdapter
        slotAdapter = SlotAdapter(slotList) {
            addSlot("New Slot", true, "5:00 PM")  // Add new slot when the button is clicked
        }

        // Set Layout Manager and Adapter
        slotRecyclerView.layoutManager = GridLayoutManager(this, 2)
        slotRecyclerView.adapter = slotAdapter

        // Add some initial slots (you can fetch this from a database or API)
        addSlot("Slot 1", true, "10:30 AM")
        addSlot("Slot 2", false, "9:45 AM")
        addSlot("Slot 3", false, "--")
        addSlot("Slot 4", false, "8:15 AM")
    }

    // Function to add a slot to the list
    private fun addSlot(title: String, isOccupied: Boolean, lastUpdated: String) {
        val newSlot = Slot(title, isOccupied, lastUpdated)
        slotList.add(newSlot)  // Add new slot to the list
        slotAdapter.notifyItemInserted(slotList.size - 1)  // Notify adapter of new item
    }
}