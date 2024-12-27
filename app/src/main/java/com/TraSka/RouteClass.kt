package com.TraSka

data class Route(
    var name: String? = null,
    val travelMode: String? = null,
    val len: Float? = null,
    val point: List<Point>? = null,
    var id: String? = null,
    var vehicle: Vehicle? = null,
    var time: Float? = null,
    var co2: Float? = null,
    val shareUrl: String? = null,
    val avoid: String? = null
)
