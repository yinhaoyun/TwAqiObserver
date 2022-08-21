package com.haoyun.twaqiobserver

import com.google.gson.annotations.SerializedName


data class Records (

  @SerializedName("sitename"    ) var sitename    : String? = null,
  @SerializedName("county"      ) var county      : String? = null,
  @SerializedName("aqi"         ) var aqi         : String? = null,
  @SerializedName("pollutant"   ) var pollutant   : String? = null,
  @SerializedName("status"      ) var status      : String? = null,
  @SerializedName("so2"         ) var so2         : String? = null,
  @SerializedName("co"          ) var co          : String? = null,
  @SerializedName("o3"          ) var o3          : String? = null,
  @SerializedName("o3_8hr"      ) var o38hr       : String? = null,
  @SerializedName("pm10"        ) var pm10        : String? = null,
  @SerializedName("pm2.5"       ) var pm2_5       : String? = null,
  @SerializedName("no2"         ) var no2         : String? = null,
  @SerializedName("nox"         ) var nox         : String? = null,
  @SerializedName("no"          ) var no          : String? = null,
  @SerializedName("wind_speed"  ) var windSpeed   : String? = null,
  @SerializedName("wind_direc"  ) var windDirec   : String? = null,
  @SerializedName("publishtime" ) var publishtime : String? = null,
  @SerializedName("co_8hr"      ) var co8hr       : String? = null,
  @SerializedName("pm2.5_avg"   ) var pm2_5Avg    : String? = null,
  @SerializedName("pm10_avg"    ) var pm10Avg     : String? = null,
  @SerializedName("so2_avg"     ) var so2Avg      : String? = null,
  @SerializedName("longitude"   ) var longitude   : String? = null,
  @SerializedName("latitude"    ) var latitude    : String? = null,
  @SerializedName("siteid"      ) var siteid      : String? = null

)