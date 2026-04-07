package com.example.canteenautomation

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class InventoryManagerActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var db: DatabaseReference
    private var list = ArrayList<FoodModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This MUST match the XML filename exactly
        setContentView(R.layout.activity_inventory_manager)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // The error was here; ensure R.id.rvInventory exists in activity_inventory_manager.xml
        rv = findViewById(R.id.rvInventory)
        rv.layoutManager = LinearLayoutManager(this)
        db = FirebaseDatabase.getInstance().getReference("FoodItems")

        loadData()
    }

    private fun loadData() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                list.clear()
                for (snap in s.children) {
                    val item = snap.getValue(FoodModel::class.java)
                    if (item != null) {
                        item.id = snap.key ?: ""
                        list.add(item)
                    }
                }
                rv.adapter = SimpleAdapter()
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    inner class SimpleAdapter : RecyclerView.Adapter<SimpleAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val name = v.findViewById<TextView>(R.id.tvFoodName)
            val price = v.findViewById<TextView>(R.id.tvFoodPrice)
            val img = v.findViewById<ImageView>(R.id.imgFood)
            val sw = v.findViewById<Switch>(R.id.switchStatus)
            val edit = v.findViewById<ImageButton>(R.id.btnEdit)
            val delete = v.findViewById<ImageButton>(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val f = list[position]
            holder.name.text = f.name
            holder.price.text = "₹${f.price}"

            holder.sw.setOnCheckedChangeListener(null)
            holder.sw.isChecked = f.available

            Glide.with(this@InventoryManagerActivity).load(f.imageUrl).into(holder.img)

            holder.sw.setOnCheckedChangeListener { _, isChecked ->
                db.child(f.id).child("available").setValue(isChecked)
            }

            holder.edit.setOnClickListener { showEdit(f) }

            // Delete Listener
            holder.delete.setOnClickListener { confirmDelete(f) }
        }

        override fun getItemCount() = list.size
    }

    private fun confirmDelete(f: FoodModel) {
        AlertDialog.Builder(this)
            .setTitle("Delete ${f.name}?")
            .setMessage("This will permanently remove this item.")
            .setPositiveButton("Delete") { _, _ ->
                db.child(f.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Removed Successfully", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEdit(f: FoodModel) {
        val view = layoutInflater.inflate(R.layout.dialog_edit_food, null)
        val p = view.findViewById<EditText>(R.id.etEditPrice)
        val u = view.findViewById<EditText>(R.id.etEditUrl)

        p.setText(f.price)
        u.setText(f.imageUrl)

        AlertDialog.Builder(this)
            .setTitle("Edit ${f.name}")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val updates = mapOf(
                    "price" to p.text.toString(),
                    "imageUrl" to u.text.toString()
                )
                db.child(f.id).updateChildren(updates)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}