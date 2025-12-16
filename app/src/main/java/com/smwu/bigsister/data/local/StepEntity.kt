package com.smwu.bigsister.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_table") // ⭐ 반드시 필요
data class StepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val routineId: Long,
    val name: String,
    val duration: Long,

    val orderIndex: Int = 0,        // ⭐ Dao에서 쓰고 있으므로 필수

    val isTransport: Boolean = false,
    val from: String? = null,
    val to: String? = null,
    val transportMode: String? = null,
    val calculatedDuration: Long? = null,
    val memo: String? = null
)