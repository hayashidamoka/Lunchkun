package jp.co.pannacotta.lunch_app.model

import org.parceler.Parcel

@Parcel
class Budget {
    @JvmField
    var average: String? = null
    @JvmField
    var code: String? = null
    @JvmField
    var name: String? = null
}