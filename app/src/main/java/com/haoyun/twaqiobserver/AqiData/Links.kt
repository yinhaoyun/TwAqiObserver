package com.haoyun.twaqiobserver

import com.google.gson.annotations.SerializedName


data class Links (

  @SerializedName("start" ) var start : String? = null,
  @SerializedName("next"  ) var next  : String? = null

)