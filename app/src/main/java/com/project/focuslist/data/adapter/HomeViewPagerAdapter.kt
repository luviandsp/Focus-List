package com.project.focuslist.data.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.focuslist.ui.fragment.AllTaskFragment
import com.project.focuslist.ui.fragment.TaskCompletedFragment
import com.project.focuslist.ui.fragment.TaskInProgressFragment

class HomeViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragments: List<Fragment> = listOf(
        AllTaskFragment(),
        TaskInProgressFragment(),
        TaskCompletedFragment()
    )

    private val fragmentTitles: List<String> = listOf(
        "All Task",
        "In Progress",
        "Completed"
    )
    
    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun getPageTitle(position: Int): CharSequence = fragmentTitles[position]
}
