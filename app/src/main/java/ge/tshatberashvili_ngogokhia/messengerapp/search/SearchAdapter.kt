package ge.tshatberashvili_ngogokhia.messengerapp.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ge.tshatberashvili_ngogokhia.messengerapp.R
import ge.tshatberashvili_ngogokhia.messengerapp.User
import ge.tshatberashvili_ngogokhia.messengerapp.databinding.ProfileBasicBinding

    class SearchAdapter(private val onUserClick: (User) -> Unit) :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private val users = mutableListOf<User>()
    private var onLoadMoreListener: (() -> Unit)? = null
    private var isLoading = false
    private var hasMoreData = true

    private val loadMoreThreshold = 3

    inner class SearchViewHolder(private val binding: ProfileBasicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.root.setOnClickListener {
                user.userId?.let { onUserClick(user) }
            }

            if (user.avatar != null) {
                Glide.with(binding.root)
                    .load(user.avatar)
                    .circleCrop()
                    .placeholder(R.drawable.avatar_image_placeholder)
                    .error(R.drawable.avatar_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache for better performance
                    .into(binding.imageView)
            } else {
                Glide.with(binding.root)
                    .load(R.drawable.avatar_image_placeholder)
                    .circleCrop()
                    .into(binding.imageView)
            }

            binding.textViewNickname.text = user.nickname ?: "Unknown"
            binding.textViewOccupation.text = user.profession ?: "No profession"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            ProfileBasicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        if (position < users.size) {
            holder.bind(users[position])

            if (shouldLoadMore(position)) {
                triggerLoadMore()
            }
        }
    }

    private fun shouldLoadMore(position: Int): Boolean {
        return !isLoading &&
                hasMoreData &&
                position >= users.size - loadMoreThreshold &&
                users.isNotEmpty()
    }

    private fun triggerLoadMore() {
        if (!isLoading && hasMoreData) {
            isLoading = true
            onLoadMoreListener?.invoke()
        }
    }

    fun getLastUser(): User? {
        return if (users.isNotEmpty()) users.last() else null
    }

    fun addUsers(newUsers: List<User>) {
        val currentSize = this.users.size
        this.users.addAll(newUsers)
        notifyItemRangeInserted(currentSize, newUsers.size)

        isLoading = false

        if (newUsers.isEmpty()) {
            hasMoreData = false
        }
    }

    fun clearUsers() {
        val size = users.size
        this.users.clear()
        notifyItemRangeRemoved(0, size)

        isLoading = false
        hasMoreData = true
    }

}