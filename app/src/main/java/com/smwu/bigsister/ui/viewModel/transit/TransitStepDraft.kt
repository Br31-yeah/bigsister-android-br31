package com.smwu.bigsister.ui.viewModel.transit

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TransitStepDraft(
    val name: String = "이동",
    val fromName: String,
    val fromLatLng: String,
    val toName: String,
    val toLatLng: String,
    val transportMode: String,
    val baseDuration: Long,
    val baseDepartureTime: String
) : Parcelable