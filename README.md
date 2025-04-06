
<h1 align="center">
    <img src="https://github.com/user-attachments/assets/28bdb956-3e94-44eb-b035-e48af6cc5508" 
       alt="Castify Logo" 
       width="40" 
       height="40" 
       style="vertical-align: middle; margin-right: 10px;">
Castify
</h1>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
</p>

A modern, sleek, and fully functional podcast player built with Jetpack Compose. Discover, stream, and manage your favorite podcasts — all in one place.

![Explore Screen](https://github.com/user-attachments/assets/f02abb44-cd63-4593-98d8-04342be2087b)

# **Motivation**

I was really sad when *Google* killed the *Google Podcasts* app. I really loved the experience the app provided with managing and listening to podcasts. I could not find a similar app out there so i decided to build one. Castify is heavily inspired by *Google Podcasts*, one can even look at it as a clone. I also got a lot of inspiration from the *NowInAndroid* sample from the Android team at *Google*. Castify is still in development with only the Explore Screen and Now Playing Screen completed at the moment. However, much of the core functionality is already implemented such as downloading of episodes and streaming of downloaded and non-downloaded episodes. The app uses static data but my hope is to develop a backend that will be used to fetch up to date data that will be used by the app. I'm open to contributions on how to improve the app.

## Tech stack & Open-source libraries
- Minimum SDK level 21
- - [Kotlin](https://kotlinlang.org/) based, utilizing [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/) for asynchronous operations.
- Jetpack Libraries:
  - Jetpack Compose: Android’s modern toolkit for declarative UI development.
  - Lifecycle: Observes Android lifecycles and manages UI states upon lifecycle changes.
  - ViewModel: Manages UI-related data and is lifecycle-aware, ensuring data survival through configuration changes.
  - Navigation: Facilitates screen navigation, complemented by [Hilt Navigation Compose](https://developer.android.com/jetpack/compose/libraries#hilt) for dependency injection.
  - Room: Constructs a database with an SQLite abstraction layer for seamless database access.
- Architecture:
  - MVVM Architecture (View - ViewModel - Model): Facilitates separation of concerns and promotes maintainability.
  - Repository Pattern: Acts as a mediator between different data sources and the application's business logic.

- [ksp](https://github.com/google/ksp): Kotlin Symbol Processing API for code generation and analysis.
- [Hilt](https://dagger.dev/hilt/) for dependency Injection.
- [androidx.datastore](https://developer.android.com/jetpack/androidx/releases/datastore) for persisting user preferences.
- [androidx.media3](https://developer.android.com/jetpack/androidx/releases/media3) for audio playback.
- [androidx.work](https://developer.android.com/jetpack/androidx/releases/work) for background work.
