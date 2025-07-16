package ge.tshatberashvili_ngogokhia.messengerapp.conversation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ge.tshatberashvili_ngogokhia.messengerapp.databinding.MessageInBinding
import ge.tshatberashvili_ngogokhia.messengerapp.databinding.MessageOutBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationAdapter() :
    RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    private val messages = mutableListOf<Message>()

    companion object {
        const val MESSAGE_RECEIVED = 0
        const val MESSAGE_SENT = 1
    }

    abstract class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(message: Message)
    }

    private inner class MessageReceivedHolder(private val binding: MessageInBinding) :
        ConversationViewHolder(binding.root) {
        override fun bind(message: Message) {
            binding.textMessageContent.visibility = View.VISIBLE
            binding.textMessageContent.text = message.message
            binding.root.isClickable = false
            binding.textTimestamp.text = message.getDate()
        }
    }

    private inner class MessageSentHolder(private val binding: MessageOutBinding) :
        ConversationViewHolder(binding.root) {
        override fun bind(message: Message) {
            binding.textMessageSent.visibility = View.VISIBLE
            binding.textMessageSent.text = message.message
            binding.root.isClickable = false
            binding.textTimeSent.text = message.getDate()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        return if (viewType == MESSAGE_RECEIVED) {
            MessageReceivedHolder(
                MessageInBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            MessageSentHolder(
                MessageOutBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSentByMe == true) {
            MESSAGE_SENT
        } else {
            MESSAGE_RECEIVED
        }
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        if (position < messages.size) {
            holder.bind(messages[position])
        }
    }

    fun addMessages(messages: List<Message>) {
        val currentSize = this.messages.size
        this.messages.addAll(messages.subList(currentSize, messages.size))
        notifyItemRangeInserted(currentSize, messages.size - currentSize)
    }
}

fun Message.getDate(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp!!))
}