# FocusList

FocusList is an Android application designed to help users manage their tasks effectively. It features task creation, editing, and deletion, with options for setting priorities, due dates, and reminders. The app also includes user authentication, profile management, and image upload capabilities.

## Features

* **User Authentication**: Register, login, and forgot password functionalities.
* **Task Management**:
    * Create new tasks with titles, descriptions, priorities, due dates, and images.
    * Edit existing tasks.
    * Mark tasks as complete or in progress.
    * Delete tasks.
    * Save tasks as drafts.
* **Task Prioritization**: Assign low, mid, or high priority to tasks.
* **Reminders**: Set up notifications for upcoming tasks.
* **Calendar View**: View tasks organized by date.
* **Profile Management**: Edit user profile information, including profile picture.
* **Data Persistence**:
    * Uses Firebase Firestore for cloud storage of tasks and user data.
    * Uses Supabase Storage for image uploads.
    * Uses Room Persistence Library for local storage of task drafts.
    * Uses DataStore Preferences for user session and cache management.
* **Responsive UI**: Adapts to different screen sizes and orientations, including dark mode support.
* **Paging**: Efficiently loads task lists using Android Paging Library.
* **Sound Effects**: Plays a bell sound when a task is marked as completed.

## Technologies Used

* **Kotlin**: Primary programming language.
* **Android Architecture Components**:
    * `LiveData`: Observable data holders for UI updates.
    * `ViewModel`: UI-related data lifecycle management.
    * `Room`: Local database for task drafts.
    * `Paging 3`: Efficiently loading and displaying large datasets.
    * `WorkManager`: For scheduling background tasks like notifications.
    * `DataStore Preferences`: Modern data storage solution for key-value pairs.
* **Firebase**:
    * `Firebase Authentication`: User authentication (email/password).
    * `Cloud Firestore`: NoSQL database for storing task and user data.
    * `Firebase Analytics`.
* **Supabase**:
    * `Supabase Storage`: Cloud storage for images.
* **Glide**: Image loading and caching library.
* **ImagePicker**: Library for picking images from gallery.
* **uCrop**: Image cropping library.
* **Ktor Client OkHttp**: HTTP client for network requests.
* **Material Design 3**: Modern Android UI components.
* **Android Navigation Component**: For navigating between fragments and activities.
* **AmazingSpinner**: Custom spinner library.

## Project Structure

The project follows a clean architecture approach, separating concerns into distinct layers:

* **`ui`**: Contains Activities and Fragments responsible for displaying the user interface.
* **`data`**:
    * `adapter`: RecyclerView adapters for displaying lists of tasks.
    * `enumData`: Enums for task categories and priorities.
    * `model`: Data classes representing tasks and users.
    * `notification`: Utility classes and Worker for handling notifications.
    * `preferences`: Classes for managing DataStore preferences.
    * `repository`: Abstracts data sources (Firestore, Room, Supabase) and provides data to ViewModels.
    * `room`: Room Database setup and DAO for local task drafts.
    * `utils`: Utility classes like ViewModel factories.
    * `viewmodel`: Prepares and manages data for the UI.

## Setup and Installation

1.  **Clone the repository**:
    ```bash
    git clone [https://github.com/your-username/FocusList.git](https://github.com/luviandsp/FocusList.git)
    ```

2.  **Open in Android Studio**:
    Open the cloned project in Android Studio.

3.  **Firebase Project Setup**:
    * Create a new Firebase project in the [Firebase Console](https://console.firebase.google.com/).
    * Add an Android app to your Firebase project.
    * Download the `google-services.json` file and place it in the `app/` directory of your project. This file is ignored by Git to prevent exposing sensitive information.
    * Enable **Authentication** (Email/Password provider) and **Cloud Firestore** in your Firebase project.

4.  **Supabase Project Setup**:
    * Create a new Supabase project on the [Supabase website](https://supabase.com/).
    * Obtain your `SUPABASE_URL` and `SUPABASE_ANON_KEY`.
    * Create a `local.properties` file in your project's root directory (if it doesn't exist) and add the following:
        ```properties
        SUPABASE_ANON_KEY="YOUR_SUPABASE_ANON_KEY"
        SUPABASE_URL="YOUR_SUPABASE_URL"
        ```
    * These properties are loaded in `app/build.gradle.kts` and used to build `BuildConfig` fields.

5.  **Build the project**:
    Sync the Gradle project and build the application.

## Minimum SDK Version

The application supports devices with a minimum SDK version of 29.
The target SDK version is 35.

## Dependencies

Key dependencies are managed in `gradle/libs.versions.toml`. Some notable ones include:

* `androidx.core.ktx`
* `androidx.appcompat`
* `material`
* `androidx.activity`
* `androidx.constraintlayout`
* `androidx.fragment.ktx`
* `amazingspinner`
* `androidx.work.runtime.ktx`
* `androidx.navigation.fragment.ktx`
* `androidx.navigation.ui.ktx`
* `firebase-bom`
* `firebase-analytics`
* `firebase-auth-ktx`
* `firebase-firestore`
* `supabase-bom`
* `supabase-storage-kt`
* `hilt-android`
* `hilt-compiler`
* `androidx.paging.runtime`
* `androidx.room.paging`
* `glide`
* `ucrop`
* `imagepicker`
* `androidx.core.splashscreen`
* `androidx.lifecycle.viewmodel.ktx`
* `androidx.datastore.preferences`
* `ktor-client-okhttp`
* `androidx.room.compiler`
* `androidx.room.ktx`

## Gradle Configuration

* `compileSdk = 35`
* `minSdk = 29`
* `jvmTarget = "11"` for Kotlin compilation.
* Uses `plugins` block for Gradle plugins.
* `repositories` are configured with `google()`, `mavenCentral()`, and `jitpack.io`.
* `buildFeatures` include `viewBinding` and `buildConfig`.
* ProGuard rules are applied for release builds.

## Contributing

Feel free to fork the repository, create a new branch, and submit pull requests.
