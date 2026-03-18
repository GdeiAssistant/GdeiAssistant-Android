package cn.gdeiassistant.di

import cn.gdeiassistant.network.RetrofitClient
import cn.gdeiassistant.network.api.AuthApi
import cn.gdeiassistant.network.api.AnnouncementApi
import cn.gdeiassistant.network.api.BookApi
import cn.gdeiassistant.network.api.CardApi
import cn.gdeiassistant.network.api.CetApi
import cn.gdeiassistant.network.api.ChargeApi
import cn.gdeiassistant.network.api.DataCenterApi
import cn.gdeiassistant.network.api.DatingApi
import cn.gdeiassistant.network.api.DeliveryApi
import cn.gdeiassistant.network.api.EvaluateApi
import cn.gdeiassistant.network.api.ExpressApi
import cn.gdeiassistant.network.api.GradeApi
import cn.gdeiassistant.network.api.GraduateExamApi
import cn.gdeiassistant.network.api.LostFoundApi
import cn.gdeiassistant.network.api.MarketplaceApi
import cn.gdeiassistant.network.api.MessageApi
import cn.gdeiassistant.network.api.NoticeApi
import cn.gdeiassistant.network.api.PhotographApi
import cn.gdeiassistant.network.api.ProfileApi
import cn.gdeiassistant.network.api.ScheduleApi
import cn.gdeiassistant.network.api.SecretApi
import cn.gdeiassistant.network.api.SpareApi
import cn.gdeiassistant.network.api.TopicApi
import cn.gdeiassistant.network.api.UpgradeApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthApi(): AuthApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideAnnouncementApi(): AnnouncementApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideBookApi(): BookApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideCardApi(): CardApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideCetApi(): CetApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideChargeApi(): ChargeApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideDataCenterApi(): DataCenterApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideDatingApi(): DatingApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideDeliveryApi(): DeliveryApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideEvaluateApi(): EvaluateApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideExpressApi(): ExpressApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideGradeApi(): GradeApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideGraduateExamApi(): GraduateExamApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideLostFoundApi(): LostFoundApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideMarketplaceApi(): MarketplaceApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideMessageApi(): MessageApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideNoticeApi(): NoticeApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun providePhotographApi(): PhotographApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideProfileApi(): ProfileApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideScheduleApi(): ScheduleApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideSecretApi(): SecretApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideSpareApi(): SpareApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideTopicApi(): TopicApi = RetrofitClient.create()

    @Provides
    @Singleton
    fun provideUpgradeApi(): UpgradeApi = RetrofitClient.create()
}
