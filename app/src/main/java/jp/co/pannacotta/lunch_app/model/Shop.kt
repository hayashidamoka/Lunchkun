package jp.co.pannacotta.lunch_app.model

import com.google.gson.annotations.SerializedName
import org.parceler.Parcel

@Parcel
class Shop {
    @JvmField
    var lat: String? = null
    @JvmField
    var lng: String? = null
    @JvmField
    var lunch: String? = null
    @JvmField
    var name: String? = null
    @JvmField
    var photo: Photo? = null
    @JvmField
    var urls: Urls? = null

    @JvmField
    @SerializedName("catch")
    var catchCopy: String? = null
    //public Genre genre;
    //public String access;
    //public String free_food;
    // public String private_room;
}