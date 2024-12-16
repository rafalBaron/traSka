package com.TraSka

import com.google.firebase.database.IgnoreExtraProperties
@IgnoreExtraProperties
data class Point(
    var address: String? = null,
    var id: String? = null,
    var latLng: List<Double>? = null,
    var lazyColumnId: String? = null,
) {}