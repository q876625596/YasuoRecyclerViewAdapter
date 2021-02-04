package com.fusion_nex_gen.yasuorvadapter.decoration

/*当item的宽或者高小于RecyclerView时，可以修改Mode*/
enum class DecorationMode {
    MODE_FILL,//此时的decoration时占满两者相差的空隙
    MODE_CHILD,//此时的decoration按照自身的宽高贴在itemView周围
    MODE_PARENT//此时的decoration贴在在RecyclerView内边
}