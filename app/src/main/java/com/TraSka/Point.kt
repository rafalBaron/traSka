package com.TraSka

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.IgnoreExtraProperties
@IgnoreExtraProperties
data class Point(
    var address: String? = null,
    var id: String? = null,
    var latLng: List<Double>? = null,
    var index: Int? = null
) {}