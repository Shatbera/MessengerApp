package ge.tshatberashvili_ngogokhia.messengerapp.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import ge.tshatberashvili_ngogokhia.messengerapp.conversation.ConversationActivity
import ge.tshatberashvili_ngogokhia.messengerapp.data.repo.LookupRepository
import ge.tshatberashvili_ngogokhia.messengerapp.databinding.ActivitySearchBinding
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import reactivecircus.flowbinding.android.widget.textChanges


class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val searchViewModel: SearchViewModel by viewModels()
    private val currentUserId = Firebase.auth.currentUser?.uid

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val users = LookupRepository.getUsers(null, null, 50)
                .filter { it.userId != currentUserId }
            Log.d("SearchActivity", "Found ${users.size} users (excluding self)")
            users.forEach { user ->
                Log.d("SearchActivity", "User: ${user.nickname} - ${user.profession}")
            }
        }

        val layoutManager = LinearLayoutManager(this)

        val adapter = SearchAdapter { user ->
            val intent = Intent(this, ConversationActivity::class.java)
            intent.putExtra(ConversationActivity.EXTRA_USER_ID, user.userId)
            startActivity(intent)
        }

        binding.listUsers.layoutManager = layoutManager
        binding.listUsers.adapter = adapter

        searchViewModel.loadingState.observe(this) { isLoading ->
            binding.progressIndicator.loadingLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        binding.listUsers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (layoutManager.itemCount > 0 && lastVisible == layoutManager.itemCount - 1) {
                    lifecycleScope.launch {
                        val query = binding.searchBar.text.toString()
                        val lastUser = adapter.getLastUser()?.nickname ?: return@launch
                        val moreUsers = searchViewModel.loadMore(query, lastUser)
                            .filter { it.userId != currentUserId }
                        adapter.addUsers(moreUsers)
                    }
                }
            }
        })

        binding.searchBar.textChanges()
            .filterNotNull()
            .filter { it.length >= 3 || it.isBlank() }
            .debounce(300)
            .onEach {
                adapter.clearUsers()
                val users = searchViewModel.loadData(it.toString())
                    .filter { user -> user.userId != currentUserId }
                adapter.addUsers(users)
            }
            .launchIn(lifecycleScope)

        binding.btnBack.setOnClickListener { finish() }
    }
}