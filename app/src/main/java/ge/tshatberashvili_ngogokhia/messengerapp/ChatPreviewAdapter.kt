package ge.tshatberashvili_ngogokhia.messengerapp

import ChatPreview
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ge.tshatberashvili_ngogokhia.messengerapp.databinding.ItemChatPreviewBinding

class ChatPreviewAdapter(
    private val originalList: List<ChatPreview>,
    private val onItemClick: (ChatPreview) -> Unit
) : RecyclerView.Adapter<ChatPreviewAdapter.ChatViewHolder>() {

    private var displayedList: List<ChatPreview> = originalList

    inner class ChatViewHolder(val binding: ItemChatPreviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val preview = displayedList[position]
        val user = preview.user

        holder.binding.tvNickname.text = user.nickname
        holder.binding.tvLastMessage.text = preview.lastMessage

        if (!user.avatar.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.avatar)
                .circleCrop()
                .into(holder.binding.ivAvatar)
        } else {
            holder.binding.ivAvatar.setImageResource(R.drawable.avatar_image_placeholder)
        }

        holder.itemView.setOnClickListener {
            onItemClick(preview)
        }
    }

    override fun getItemCount(): Int = displayedList.size

    fun updateList(filtered: List<ChatPreview>) {
        displayedList = filtered
        notifyDataSetChanged()
    }

    fun resetList() {
        displayedList = originalList
        notifyDataSetChanged()
    }
}
