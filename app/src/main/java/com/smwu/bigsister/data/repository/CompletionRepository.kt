package com.smwu.bigsister.data.repository

import com.smwu.bigsister.data.local.CompletionEntity
import com.smwu.bigsister.data.local.dao.CompletionDao
import javax.inject.Inject

class CompletionRepository @Inject constructor(
    private val completionDao: CompletionDao
) {
    suspend fun addCompletion(completion: CompletionEntity) {
        completionDao.insertCompletion(completion)
    }

    // TODO: 통계용 데이터 조회 함수들...
}