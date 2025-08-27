package com.example.fitconnect.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.activity.ViewUserProfileActivity
import com.example.fitconnect.adapter.SearchUsersAdapter
import com.example.fitconnect.databinding.FragmentSearchBinding
import com.example.fitconnect.tools.DBTools
import com.example.fitconnect.ui.SearchUserViewModel
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "SearchFragment"

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbTools: DBTools
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchAdapter: SearchUsersAdapter
    private lateinit var viewModel: SearchUserViewModel


    /**
     * Sets the bindings for this fragment
     *
     */
    private fun setBindings() {
        binding.fragmentSearchView.queryHint = "Friend users"
        binding.fragmentSearchView.setOnQueryTextListener(object:
        androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, "onQueryTextSubmit() called")
                query?.let {
                    viewModel.searchUsers(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    Log.d(TAG, "onQueryTextChanged() called")
                    viewModel.searchUsers(it)
                }
                return true
            }
        })
    }

    /**
     * Sets up the adapters for this fragment
     *
     */
    private fun setAdapters() {
        val activity = requireActivity()
        searchAdapter = SearchUsersAdapter(
            mutableListOf(),
            onUserClickListener = {user ->
                val intent = Intent(activity, ViewUserProfileActivity::class.java).apply {
                    putExtra("PROFILE_OWNER_ID", user.id)
                    putExtra("USER_ID", FirebaseAuth.getInstance().currentUser?.uid.toString())
                }
                startActivity(intent)
            }
        )
    }

    /**
     * Sets u pthe views for this fragment and sets observers
     *
     */
    private fun setViews() {
        searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchRecyclerView.adapter = searchAdapter

        viewModel.userList.observe(viewLifecycleOwner) {users ->
            searchAdapter.updateList(users)
        }

        searchRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!viewModel.isLoading && dy > 0 && viewModel.lastVisibleDocument != null) {
                    val visibleItemCount = searchRecyclerView.layoutManager?.childCount ?: 0
                    val totalItemCount = searchRecyclerView.layoutManager?.itemCount ?: 0
                    val pastVisibleItems = (searchRecyclerView.layoutManager as LinearLayoutManager)
                        .findLastVisibleItemPosition()
                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        viewModel.loadUsers(isPagination = true)
                    }
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called")
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[SearchUserViewModel::class.java]
        dbTools = DBTools()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "OnViewCreated() called")
        super.onViewCreated(view, savedInstanceState)
        searchRecyclerView = binding.searchRecyclerView
        setBindings()
        setAdapters()
        setViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}