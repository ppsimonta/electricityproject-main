package com.example.electricityapp.datatypes.TimeValue

import com.google.gson.annotations.SerializedName


data class TimeValue (

    @SerializedName("time"  ) var time  : String,
    @SerializedName("value" ) var value : Double

)