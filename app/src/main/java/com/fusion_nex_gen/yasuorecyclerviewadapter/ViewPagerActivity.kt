package com.fusion_nex_gen.yasuorecyclerviewadapter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.ActivityViewPagerBinding
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.EmptyLayoutOneDataBindingBinding
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.EmptyLayoutTwoDataBindingBinding
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.EmptyBeanOne
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.EmptyBeanTwo
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.viewPager.adapterDataBinding
import com.fusion_nex_gen.yasuorvadapter.viewPager.holderConfigVP

class ViewPagerActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewPagerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //itemList
        val list = YasuoList<Any>()
        list.add(EmptyBeanOne())
        list.add(EmptyBeanTwo())
        binding.viewPager.adapterDataBinding(this, list) {
            holderConfigVP(R.layout.empty_layout_one_data_binding, EmptyBeanOne::class, EmptyLayoutOneDataBindingBinding::class) {

            }
            holderConfigVP(R.layout.empty_layout_two_data_binding, EmptyBeanTwo::class, EmptyLayoutTwoDataBindingBinding::class) {

            }
        }
    }
}