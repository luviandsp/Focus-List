package com.project.focuslist.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.focuslist.ui.fragment.AllTaskFragment
import com.project.focuslist.ui.fragment.TaskDoneFragment
import com.project.focuslist.ui.fragment.TaskInProgressFragment

class HomeViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragments = listOf(
        AllTaskFragment(),
        TaskInProgressFragment(),
        TaskDoneFragment()
    )

    private val fragmentTitles = listOf(
        "All Task",
        "In Progress",
        "Done"
    )
    
    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun getPageTitle(position: Int): CharSequence = fragmentTitles[position]
}