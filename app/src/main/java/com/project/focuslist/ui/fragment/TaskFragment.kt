package com.project.focuslist.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.project.focuslist.databinding.FragmentTaskBinding
import com.project.focuslist.ui.activity.DetailTaskActivity
import com.project.focuslist.data.adapter.HomeViewPagerAdapter

class TaskFragment : Fragment() {

    private lateinit var binding: FragmentTaskBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
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

            ivHelp.setOnClickListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@TaskFragment.requireContext())
                builder
                    .setTitle("Help")
                    .setMessage("This is the main screen where you can see all your tasks. \n\n" +
                            "1. You can add a new task by clicking the floating action button. \n\n" +
                            "2. Click on a task to read its contents. \n\n" +
                            "3. Long click on a task to open the detail screen where you can edit or delete the task. \n\n" +
                            "4. Click the checkbox to complete the task.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.cancel()
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }
}
