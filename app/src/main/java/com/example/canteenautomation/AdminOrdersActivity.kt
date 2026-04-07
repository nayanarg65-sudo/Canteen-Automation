package com.example.canteenautomation

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AdminOrdersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var db: DatabaseReference
    private var orderList = ArrayList<OrderModel>()
    private lateinit var adapter: AdminOrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_orders)

        // ✅ Back button logic
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.rvAdminOrders)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = FirebaseDatabase.getInstance().getReference("Orders")

        adapter = AdminOrderAdapter(orderList)
        recyclerView.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                for (orderSnapshot in snapshot.children) {
                    try {
                        val order = orderSnapshot.getValue(OrderModel::class.java)
                        if (order != null) {
                            order.orderId = orderSnapshot.key ?: ""
                            orderList.add(order)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                orderList.reverse()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminOrdersActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    inner class AdminOrderAdapter(private val list: List<OrderModel>) :
        RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder>() {

        inner class OrderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvId = v.findViewById<TextView>(R.id.tvOrderId)
            val tvName = v.findViewById<TextView>(R.id.tvCustomerName)
            val tvItems = v.findViewById<TextView>(R.id.tvOrderItems)
            val tvAmount = v.findViewById<TextView>(R.id.tvTotalAmount)
            val strip = v.findViewById<View>(R.id.orderStatusStrip)
            val btnUpdate = v.findViewById<Button>(R.id.btnManageStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_order, parent, false)
            return OrderViewHolder(v)
        }

        override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
            val order = list[position]

            holder.tvId.text = "Order #${order.orderId.takeLast(5)}"
            holder.tvName.text = order.customerName ?: "Student"

            val formattedItems = order.items.replace(", ", "\n• ").replace(",", "\n• ")
            holder.tvItems.text = if (formattedItems.startsWith("• ")) formattedItems else "• $formattedItems"

            holder.tvAmount.text = "Total: ₹${order.total}"

            // ✅ These now point to your colors.xml instead of hardcoded hex codes
            when (order.status) {
                "Pending" -> holder.strip.setBackgroundColor(ContextCompat.getColor(this@AdminOrdersActivity, R.color.category_default))
                "Ready" -> holder.strip.setBackgroundColor(ContextCompat.getColor(this@AdminOrdersActivity, R.color.primaryColor))
                "Delivered" -> holder.strip.setBackgroundColor(ContextCompat.getColor(this@AdminOrdersActivity, R.color.primaryDark))
                else -> holder.strip.setBackgroundColor(Color.LTGRAY)
            }

            holder.btnUpdate.setOnClickListener { showStatusDialog(order) }
        }

        override fun getItemCount() = list.size
    }

    private fun showStatusDialog(order: OrderModel) {
        val options = arrayOf("Pending", "Ready", "Delivered")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update Status")
        builder.setItems(options) { _, which ->
            val selectedStatus = options[which]
            db.child(order.orderId).child("status").setValue(selectedStatus)
                .addOnSuccessListener {
                    Toast.makeText(this, "Order is now $selectedStatus", Toast.LENGTH_SHORT).show()
                }
        }
        builder.show()
    }
}