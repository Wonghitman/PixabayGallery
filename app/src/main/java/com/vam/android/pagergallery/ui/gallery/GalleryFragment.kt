package com.vam.android.pagergallery.ui.gallery

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.vam.android.pagergallery.base.BaseFragment
import com.vam.android.pagergallery.R
import com.vam.android.pagergallery.base.BaseApplication
import com.vam.android.pagergallery.databinding.FragmentGalleryBinding
import kotlinx.coroutines.launch


class GalleryFragment : BaseFragment<FragmentGalleryBinding, GalleryViewModel>(
    FragmentGalleryBinding::inflate,
    GalleryViewModel::class.java,
    true
) {
    private val adapter by lazy { GalleryAdapter() }

    override fun initFragment(
        binding: FragmentGalleryBinding,
        viewModel: GalleryViewModel,
        savedInstanceState: Bundle?
    ) {

        val menuHost = requireActivity()
        lateinit var searchView: SearchView
        //adapter操作

        binding.apply {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            recyclerView.setAdapterWithDefaultFooter(adapter)
        //footer需要优化为独占一行
        }

        adapter.withLoadStateFooter(FooterAdapter())
        adapter.addLoadStateListener {
            if (it.isIdle && adapter.itemCount != 0) {
                publicViewModel!!.PagingList.postValue(adapter.snapshot().items)
            }
            binding.swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading

        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            publicViewModel?.fetchPhotos(publicViewModel?.currentSearchResult?.value.toString())
            adapter.refresh()

        }


        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
                val searchItem: MenuItem = menu.findItem(R.id.app_bar_search)
                searchView = searchItem.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        if (!query.isNullOrEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                "Searching for $query",
                                Toast.LENGTH_SHORT
                            ).show()
                            publicViewModel?.currentSearchResult?.value = query
                            lifecycleScope.launch {
                                publicViewModel?.fetchPhotos(query)


                            }

                        }
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.swipeIndicator -> {
                        binding.swipeRefreshLayout.isRefreshing = true
                        lifecycleScope.launch {
                            publicViewModel?.fetchPhotos(publicViewModel?.currentSearchResult?.value.toString())
                            adapter.refresh()

                        }
                    }
                    R.id.safesearch_switch -> {
                        if (menuItem.isChecked) {
                            menuItem.isChecked = false
                            BaseApplication.SAFE_SEARCH = false
                            Log.d("GalleryFragment", "SAFE_SEARCH: ${BaseApplication.SAFE_SEARCH}")
                        } else {
                            menuItem.isChecked = true
                            BaseApplication.SAFE_SEARCH = true
                            Log.d("GalleryFragment", "SAFE_SEARCH: ${BaseApplication.SAFE_SEARCH}")
                        }
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)



        publicViewModel!!.pagingData.observe(
            viewLifecycleOwner
        ) {
            adapter.submitData(lifecycle, it)


            Log.d("GalleryFragment", adapter.snapshot().items.toString())
        }

    }


}