package ge.tshatberashvili_ngogokhia.messengerapp

import ChatPreview
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ge.tshatberashvili_ngogokhia.messengerapp.conversation.ConversationActivity
import ge.tshatberashvili_ngogokhia.messengerapp.databinding.ActivityMainBinding
import ge.tshatberashvili_ngogokhia.messengerapp.search.SearchActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var adapter: ChatPreviewAdapter
    private val allUsers = mutableListOf<ChatPreview>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        adapter = ChatPreviewAdapter(allUsers) { preview ->
            val userId = preview.user.userId
            if (!userId.isNullOrEmpty()) {
                val intent = Intent(this, ConversationActivity::class.java)
                intent.putExtra(ConversationActivity.EXTRA_USER_ID, userId)
                startActivity(intent)
            }
        }

        binding.recyclerChats.layoutManager = LinearLayoutManager(this)
        binding.recyclerChats.adapter = adapter

        binding.root.findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        binding.root.findViewById<ImageView>(R.id.nav_add).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                val filtered = allUsers.filter {
                    it.user.nickname?.lowercase()?.contains(query) == true
                }
                adapter.updateList(filtered)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.root.findViewById<ImageView>(R.id.nav_home).isSelected = true

        loadUsersWithLastMessages()
    }

    private fun loadUsersWithLastMessages() {
        val currentUid = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = android.view.View.VISIBLE
        allUsers.clear()
        adapter.updateList(allUsers)

        database.getReference("users").get().addOnSuccessListener { snapshot ->
            val children = snapshot.children.filter {
                val user = it.getValue(User::class.java)
                user?.userId = it.key
                user?.userId != currentUid
            }

            var remaining = children.size
            if (remaining == 0) {
                binding.progressBar.visibility = android.view.View.GONE
                return@addOnSuccessListener
            }

            for (child in children) {
                val user = child.getValue(User::class.java) ?: continue
                user.userId = child.key!!

                val chatId1 = "$currentUid-${user.userId}"
                val chatId2 = "${user.userId}-$currentUid"

                val messagesRef = database.getReference("messages")

                messagesRef.child(chatId1).get().addOnSuccessListener { snapshot1 ->
                    if (snapshot1.exists()) {
                        val lastMessage = snapshot1.children.maxByOrNull {
                            it.child("timestamp").getValue(Long::class.java) ?: 0L
                        }

                        val text = lastMessage?.child("message")?.getValue(String::class.java) ?: ""
                        val ts = lastMessage?.child("timestamp")?.getValue(Long::class.java) ?: 0L

                        allUsers.add(ChatPreview(user, text, ts))
                        adapter.updateList(allUsers)
                        checkDone(--remaining)
                    } else {
                        messagesRef.child(chatId2).get().addOnSuccessListener { snapshot2 ->
                            if (snapshot2.exists()) {
                                val lastMessage = snapshot2.children.maxByOrNull {
                                    it.child("timestamp").getValue(Long::class.java) ?: 0L
                                }

                                val text = lastMessage?.child("message")?.getValue(String::class.java) ?: ""
                                val ts = lastMessage?.child("timestamp")?.getValue(Long::class.java) ?: 0L

                                allUsers.add(ChatPreview(user, text, ts))
                                adapter.updateList(allUsers)
                            }
                            checkDone(--remaining)
                        }.addOnFailureListener { checkDone(--remaining) }
                    }
                }.addOnFailureListener { checkDone(--remaining) }
            }
        }.addOnFailureListener {
            binding.progressBar.visibility = android.view.View.GONE
        }
    }

    private fun checkDone(remaining: Int) {
        if (remaining <= 0) {
            binding.progressBar.visibility = android.view.View.GONE
        }
    }
}
