package com.example.biketrackingapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.biketrackingapp.db.RunDataBase
import com.example.biketrackingapp.other.Constants.KET_WEIGHT
import com.example.biketrackingapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.biketrackingapp.other.Constants.KEY_NAME

import com.example.biketrackingapp.other.Constants.RUNNING_DATABASE_NAME
import com.example.biketrackingapp.other.Constants.SHARED_PREF_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRunningDataBase(
        @ApplicationContext context: Context
    )=Room.databaseBuilder(
        context,
        RunDataBase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideRunningDao(db:RunDataBase)=db.getRunDao()


    @Provides
    @Singleton
    fun provideSharedPref(
        @ApplicationContext app:Context
    )=app.getSharedPreferences(SHARED_PREF_NAME,MODE_PRIVATE)

    @Provides
    @Singleton
    @Named("isDemo")
    fun ProvideName(
        sharedPref:SharedPreferences
    )=sharedPref.getString(KEY_NAME,"")?:""

    @Provides
    @Singleton
    fun ProvideWeight(
        sharedPref:SharedPreferences
    )=sharedPref.getFloat(KET_WEIGHT,80f)

    @Provides
    @Singleton
    @Named("isDDemo")
    fun ProvideFristTimeToggle(
        sharedPref:SharedPreferences
    )=sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE,true)
}