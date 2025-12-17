package com.smwu.bigsister.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    /**
     * [중요]
     * RoutineRepository, UserRepository 등 모든 레포지토리 클래스에
     * 이미 @Inject constructor(...)가 붙어있습니다.
     * * 따라서 여기서 수동으로 @Provides 함수를 작성하면 "중복 바인딩" 에러가 발생합니다.
     * Hilt가 알아서 @Inject 생성자를 찾아 주입하므로, 이 파일은 비워두는 것이 정답입니다!
     */
}
