package jp.co.pannacotta.lunch_app.model

import org.parceler.Parcel

@Parcel
class Results {
    @JvmField
    var shop: List<Shop>? = null
}