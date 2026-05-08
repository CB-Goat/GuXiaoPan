package com.guxiaopan

import android.app.Application
import com.guxiaopan.data.AppDatabase

class GuXiaoPanApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
}
