package com.example.userlistapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class MainActivity : AppCompatActivity() {

    private val userList = mutableListOf<User>()
    private lateinit var adapter: UserAdapter
    private lateinit var fetchButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    companion object {
        private const val API_URL = "https://jsonplaceholder.typicode.com/users"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Keep content clear of the status bar and navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchButton = findViewById(R.id.fetchButton)
        progressBar = findViewById(R.id.progressBar)
        emptyText = findViewById(R.id.emptyText)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        adapter = UserAdapter(userList) { user ->
            Toast.makeText(this, "You selected ${user.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchButton.setOnClickListener { fetchUsers() }
    }

    private fun fetchUsers() {
        setLoading(true)

        val request = JsonArrayRequest(
            Request.Method.GET, API_URL, null,
            { response ->
                setLoading(false)
                try {
                    userList.clear()
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        userList.add(
                            User(
                                name = obj.getString("name"),
                                email = obj.getString("email"),
                                phone = obj.getString("phone")
                            )
                        )
                    }
                    adapter.notifyDataSetChanged()
                    emptyText.visibility = if (userList.isEmpty()) View.VISIBLE else View.GONE
                    Toast.makeText(this, "Loaded ${userList.size} users", Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    showError("Failed to read the data.")
                }
            },
            { _ ->
                setLoading(false)
                showError("Failed to fetch data. Check your connection and try again.")
            }
        )

        // Always hit the network so connection failures are caught
        request.setShouldCache(false)

        Volley.newRequestQueue(this).add(request)
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        fetchButton.isEnabled = !isLoading
        if (isLoading) emptyText.visibility = View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        if (userList.isEmpty()) {
            emptyText.text = message
            emptyText.visibility = View.VISIBLE
        }
    }
}