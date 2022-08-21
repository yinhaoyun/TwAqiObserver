package com.haoyun.twaqiobserver

import com.google.gson.annotations.SerializedName


data class AqiData (

  @SerializedName("fields"          ) var fields         : ArrayList<Fields>  = arrayListOf(),
  @SerializedName("resource_id"     ) var resourceId     : String?            = null,
  @SerializedName("__extras"        ) var _extras        : _extras?           = _extras(),
  @SerializedName("include_total"   ) var includeTotal   : Boolean?           = null,
  @SerializedName("total"           ) var total          : String?            = null,
  @SerializedName("resource_format" ) var resourceFormat : String?            = null,
  @SerializedName("limit"           ) var limit          : String?            = null,
  @SerializedName("offset"          ) var offset         : String?            = null,
  @SerializedName("_links"          ) var Links          : Links?             = Links(),
  @SerializedName("records"         ) var records        : ArrayList<Records> = arrayListOf()

)