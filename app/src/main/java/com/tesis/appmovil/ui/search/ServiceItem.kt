package com.tesis.appmovil.ui.search

data class ServiceItem(
    val id: Int,
    val name: String,
    val imageUrl: String? = null,
    val isOpenNow: Boolean = false,
    val schedule: String = "",
    val price1: String = "",
    val price2: String = ""
)

