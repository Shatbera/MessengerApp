package ge.tshatberashvili_ngogokhia.messengerapp.conversation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import ge.tshatberashvili_ngogokhia.messengerapp.MainActivity
import ge.tshatberashvili_ngogokhia.messengerapp.R
import ge.tshatberashvili_ngogokhia.messengerapp.databinding.ActivityConversationBinding

class ConversationActivity : AppCompatActivity() {
    private var userId: String = ""
    private val viewModel: ConversationViewModel by viewModels()
    private lateinit var binding: ActivityConversationBinding
    private lateinit var adapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationBinding.inflate(layoutInflater)

        userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""

        adapter = ConversationAdapter()
        binding.conversationRecyclerView.adapter = adapter

        viewModel.error.observe(this) {
            if (it.getContentIfNotHandled() == true) {
                Toast.makeText(this, getText(R.string.error), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.messages.observe(this) {
            adapter.addMessages(it)
            scrollToBottom()
        }

        viewModel.user.observe(this) { user ->
            if (user != null) {
                binding.nickname.text = user.nickname ?: "Unknown"
            }
        }

        binding.sendButton.setOnClickListener {
            val msgText = binding.editText.text.toString()
            if (msgText.isNotBlank()) {
                viewModel.addMessage(userId, Message(message = msgText))
                binding.editText.text.clear()
            }
        }

        binding.backButton.setOnClickListener {
            navigateToHome()
        }

        viewModel.startConversation(userId)

        setContentView(binding.root)

    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun scrollToBottom() {
        binding.conversationRecyclerView.smoothScrollToPosition(adapter.itemCount - 1)
    }

    companion object {
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
    }
}
