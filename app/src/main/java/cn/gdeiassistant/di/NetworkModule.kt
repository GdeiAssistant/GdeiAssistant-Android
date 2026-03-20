package cn.gdeiassistant.di

import cn.gdeiassistant.BuildConfig
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.network.AuthInterceptor
import cn.gdeiassistant.network.BaseUrlOverrideInterceptor
import cn.gdeiassistant.network.MockInterceptor
import cn.gdeiassistant.network.NetworkConstants
import cn.gdeiassistant.network.ResponseInterceptor
import cn.gdeiassistant.network.api.AnnouncementApi
import cn.gdeiassistant.network.api.AuthApi
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
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        sessionManager: SessionManager,
        baseUrlOverrideInterceptor: BaseUrlOverrideInterceptor,
        authInterceptor: AuthInterceptor,
        responseInterceptor: ResponseInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(NetworkConstants.CONNECT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .readTimeout(NetworkConstants.READ_WRITE_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .writeTimeout(NetworkConstants.READ_WRITE_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .cookieJar(sessionManager.cookieJar)
        .addInterceptor(baseUrlOverrideInterceptor)
        .addInterceptor(MockInterceptor())
        .addInterceptor(authInterceptor)
        .addInterceptor(responseInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
    @Provides @Singleton fun provideAnnouncementApi(retrofit: Retrofit): AnnouncementApi = retrofit.create(AnnouncementApi::class.java)
    @Provides @Singleton fun provideBookApi(retrofit: Retrofit): BookApi = retrofit.create(BookApi::class.java)
    @Provides @Singleton fun provideCardApi(retrofit: Retrofit): CardApi = retrofit.create(CardApi::class.java)
    @Provides @Singleton fun provideCetApi(retrofit: Retrofit): CetApi = retrofit.create(CetApi::class.java)
    @Provides @Singleton fun provideChargeApi(retrofit: Retrofit): ChargeApi = retrofit.create(ChargeApi::class.java)
    @Provides @Singleton fun provideDataCenterApi(retrofit: Retrofit): DataCenterApi = retrofit.create(DataCenterApi::class.java)
    @Provides @Singleton fun provideDatingApi(retrofit: Retrofit): DatingApi = retrofit.create(DatingApi::class.java)
    @Provides @Singleton fun provideDeliveryApi(retrofit: Retrofit): DeliveryApi = retrofit.create(DeliveryApi::class.java)
    @Provides @Singleton fun provideEvaluateApi(retrofit: Retrofit): EvaluateApi = retrofit.create(EvaluateApi::class.java)
    @Provides @Singleton fun provideExpressApi(retrofit: Retrofit): ExpressApi = retrofit.create(ExpressApi::class.java)
    @Provides @Singleton fun provideGradeApi(retrofit: Retrofit): GradeApi = retrofit.create(GradeApi::class.java)
    @Provides @Singleton fun provideGraduateExamApi(retrofit: Retrofit): GraduateExamApi = retrofit.create(GraduateExamApi::class.java)
    @Provides @Singleton fun provideLostFoundApi(retrofit: Retrofit): LostFoundApi = retrofit.create(LostFoundApi::class.java)
    @Provides @Singleton fun provideMarketplaceApi(retrofit: Retrofit): MarketplaceApi = retrofit.create(MarketplaceApi::class.java)
    @Provides @Singleton fun provideMessageApi(retrofit: Retrofit): MessageApi = retrofit.create(MessageApi::class.java)
    @Provides @Singleton fun provideNoticeApi(retrofit: Retrofit): NoticeApi = retrofit.create(NoticeApi::class.java)
    @Provides @Singleton fun providePhotographApi(retrofit: Retrofit): PhotographApi = retrofit.create(PhotographApi::class.java)
    @Provides @Singleton fun provideProfileApi(retrofit: Retrofit): ProfileApi = retrofit.create(ProfileApi::class.java)
    @Provides @Singleton fun provideScheduleApi(retrofit: Retrofit): ScheduleApi = retrofit.create(ScheduleApi::class.java)
    @Provides @Singleton fun provideSecretApi(retrofit: Retrofit): SecretApi = retrofit.create(SecretApi::class.java)
    @Provides @Singleton fun provideSpareApi(retrofit: Retrofit): SpareApi = retrofit.create(SpareApi::class.java)
    @Provides @Singleton fun provideTopicApi(retrofit: Retrofit): TopicApi = retrofit.create(TopicApi::class.java)
    @Provides @Singleton fun provideUpgradeApi(retrofit: Retrofit): UpgradeApi = retrofit.create(UpgradeApi::class.java)
}
