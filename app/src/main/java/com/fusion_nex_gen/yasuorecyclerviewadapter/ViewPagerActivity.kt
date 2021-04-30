package com.fusion_nex_gen.yasuorecyclerviewadapter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.ActivityViewPagerBinding
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.EmptyLayoutOneDataBindingBinding
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.EmptyLayoutTwoDataBindingBinding
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.EmptyBeanOne
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.EmptyBeanTwo
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.viewPager.adapterDataBinding
import com.fusion_nex_gen.yasuorvadapter.viewPager.holderConfig

class ViewPagerActivity : AppCompatActivity() {
    lateinit var binding:ActivityViewPagerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //itemList
        val list = YasuoList<Any>()
        list.add(EmptyBeanOne())
        list.add(EmptyBeanTwo())
        binding.viewPager.adapterDataBinding(this,list){
            holderConfig(R.layout.empty_layout_one_data_binding,EmptyBeanOne::class,EmptyLayoutOneDataBindingBinding::class){

            }
            holderConfig(R.layout.empty_layout_two_data_binding,EmptyBeanTwo::class,EmptyLayoutTwoDataBindingBinding::class){

            }
        }
    }
}