package com.example.menu
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPager2Adapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 4

    // Fragment 연결
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Pizza_Fragment()
            1 -> Chicken_Fragment()
            2 -> Hamburger_Fragment()
            else -> Bunsik_Fragment()
        }

    }
}



