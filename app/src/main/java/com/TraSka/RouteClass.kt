package com.TraSka

data class Route(
    val name: String? = null,
    val travelMode: String? = null,
    val len: Float? = null,
    val point: List<Point>? = null,
    var id: String? = null,
    val shareUrl: String? = null
)