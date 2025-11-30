# TapeTrove

TapeTrove adalah aplikasi Android untuk menjelajahi dan mengelola database film. Aplikasi ini menggunakan The Movie Database (TMDb) API untuk mengambil data film dan Firebase untuk otentikasi pengguna.

## Fitur

*   Menjelajahi film-film populer
*   Mencari film
*   Melihat detail film
*   Menambahkan film ke favorit
*   Otentikasi pengguna dengan Firebase

## Dependensi

Aplikasi ini menggunakan dependensi berikut:

*   [AndroidX Appcompat](https://developer.android.com/jetpack/androidx/releases/appcompat)
*   [AndroidX Activity](https://developer.android.com/jetpack/androidx/releases/activity)
*   [AndroidX ConstraintLayout](https://developer.android.com/jetpack/androidx/releases/constraintlayout)
*   [AndroidX Room](https://developer.android.com/jetpack/androidx/releases/room)
*   [AndroidX Fragment KTX](https://developer.android.com/jetpack/androidx/releases/fragment)
*   [Google Material Components](https://material.io/develop/android)
*   [Firebase Authentication](https://firebase.google.com/docs/auth)
*   [Firebase Realtime Database](https://firebase.google.com/docs/database)
*   [Retrofit](https://square.github.io/retrofit/)
*   [Gson](https://github.com/google/gson)
*   [Glide](https://github.com/bumptech/glide)
*   [Google Play Services Auth](https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignIn)

## Konfigurasi

1.  **Firebase**:
    *   Buat proyek baru di [Firebase Console](https://console.firebase.google.com/).
    *   Tambahkan aplikasi Android ke proyek Firebase Anda.
    *   Unduh file `google-services.json` dan letakkan di direktori `app/`.
    *   Aktifkan metode otentikasi (misalnya, Email/Sandi, Google) di Firebase Console.

2.  **The Movie Database (TMDb) API**:
    *   Buat akun di [TMDb](https://www.themoviedb.org/).
    *   Dapatkan kunci API Anda dari pengaturan akun Anda.
    *   Tambahkan kunci API Anda di dalam file `local.properties` dengan nama `tmdb_api_key="YOUR_API_KEY"`.

## Struktur Kode

*   **`activity`**: Berisi kelas-kelas Activity yang menangani tampilan utama dan interaksi pengguna.
*   **`adapter`**: Berisi kelas-kelas Adapter untuk menampilkan daftar data dalam RecyclerView.
*   **`api`**: Berisi antarmuka Retrofit untuk berinteraksi dengan TMDb API.
*   **`auth`**: Berisi kelas-kelas yang terkait dengan otentikasi pengguna.
*   **`database`**: Berisi kelas-kelas Room Database untuk menyimpan data favorit pengguna.
*   **`fragment`**: Berisi kelas-kelas Fragment untuk setiap layar dalam aplikasi.
*   **`model`**: Berisi kelas-kelas model data untuk film dan respons API.
*   **`viewmodel`**: Berisi kelas-kelas ViewModel yang menyediakan data ke UI dan menangani logika bisnis.

## Cara Menjalankan

1.  Clone repositori ini: `git clone https://github.com/username/project-papb-2024.git`
2.  Buka proyek di Android Studio.
3.  Tambahkan file `google-services.json` ke direktori `app/`.
4.  Tambahkan kunci API TMDb Anda ke file `local.properties`.
5.  Bangun dan jalankan aplikasi.
