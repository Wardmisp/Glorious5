package com.g5.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun httpEngine(): HttpClientEngine = Js.create()
