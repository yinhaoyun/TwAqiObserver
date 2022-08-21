package com.haoyun.twaqiobserver

import com.google.gson.annotations.SerializedName


data class Fields (

  @SerializedName("id"   ) var id   : String? = null,
  @SerializedName("type" ) var type : String? = null,
  @SerializedName("info" ) var info : Info?   = Info()

)