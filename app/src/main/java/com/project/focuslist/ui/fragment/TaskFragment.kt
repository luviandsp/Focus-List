package com.project.focuslist.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.project.focuslist.databinding.FragmentTaskBinding
import com.project.focuslist.ui.activity.DetailTaskActivity
import com.project.focuslist.ui.adapter.HomeViewPagerAdapter
import com.project.focuslist.ui.viewmodel.TaskViewModel

class TaskFragment : Fragment() {

    private lateinit var binding: FragmentTaskBinding
    private val viewModel by viewModels<TaskViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        with(binding) {

            val viewPagerAdapter = HomeViewPagerAdapter(this@TaskFragment)
            vpHome.adapter = viewPagerAdapter

            TabLayoutMediator(tlHome, vpHome) { tab, position ->
                tab.text = viewPagerAdapter.getPageTitle(position)
            }.attach()

            fabAdd.setOnClickListener {
                val intent = Intent(activity, DetailTaskActivity::class.java)
                intent.putExtra(DetailTaskActivity.INTENT_KEY, DetailTaskActivity.CREATE_KEY)
                startActivity(intent)
            }

        }
    }
}