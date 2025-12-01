# TapeTrove: Movie Database & Secure Transaction Logging

TapeTrove adalah aplikasi Android untuk menjelajahi database film, yang dilengkapi dengan sistem pembayaran modern dan pencatatan transaksi yang aman menggunakan teknologi blockchain.

## Fitur Utama

*   **Menjelajahi Film**: Melihat daftar film populer dan mencari film berdasarkan judul.
*   **Detail Film**: Mengakses informasi rinci untuk setiap film dari TMDb API.
*   **Otentikasi Pengguna**: Sistem login dan registrasi yang aman menggunakan Firebase Authentication.
*   **Integrasi Pembayaran QRIS via Midtrans**: Memungkinkan pengguna melakukan pembayaran menggunakan metode QRIS yang aman dan terstandarisasi.
*   **Pencatatan Transaksi Immutable via Blockchain**: Setiap transaksi pembayaran yang sukses akan dicatat pada jaringan blockchain (Ethereum Sepolia Testnet) untuk memastikan data transaksi tidak dapat diubah (*immutable*) dan transparan.

---

## Arsitektur Fitur Lanjutan

Fitur pembayaran dan pencatatan transaksi di aplikasi ini menggunakan arsitektur multi-komponen yang dirancang untuk keamanan dan keandalan.

### 1. Payment Gateway (Midtrans)

*   **Peran**: Midtrans bertindak sebagai jembatan antara aplikasi dan berbagai metode pembayaran (dalam kasus ini, QRIS). Ini menyederhanakan proses pembayaran yang kompleks dan memastikan keamanan sesuai standar industri.
*   **Alur Kerja**:
    1.  Aplikasi Android tidak pernah mengirim detail pembayaran langsung. Sebagai gantinya, ia meminta "token transaksi" dari server backend.
    2.  Server backend (menggunakan **Server Key** rahasia) berkomunikasi dengan Midtrans untuk membuat sesi pembayaran dan mendapatkan **Snap Token**.
    3.  Snap Token ini dikirim kembali ke aplikasi Android.
    4.  Aplikasi menggunakan Snap Token untuk menampilkan antarmuka pembayaran (UI Kit) dari Midtrans, di mana pengguna dapat menyelesaikan pembayaran.

### 2. Blockchain Logging (Ethereum & Web3j)

*   **Peran**: Setelah pembayaran berhasil, aplikasi mencatat bukti transaksi tersebut ke jaringan blockchain publik (Ethereum Sepolia Testnet). Tujuannya adalah untuk menciptakan catatan permanen yang tidak dapat diubah atau dihapus, berfungsi sebagai lapisan audit dan transparansi tertinggi.
*   **Alur Kerja**:
    1.  Setelah Midtrans mengonfirmasi pembayaran berhasil, aplikasi mengambil detail kunci transaksi (ID Transaksi, Jumlah, Waktu).
    2.  Menggunakan library **Web3j**, aplikasi terhubung ke node Ethereum melalui layanan **Infura**.
    3.  Hash dari detail transaksi dibuat dan dikirim sebagai data dalam sebuah transaksi baru ke blockchain.
    4.  Karena transaksi ini ditandatangani dengan **Private Key** yang aman, integritas catatan dijamin.

---

## Dependensi Utama

Aplikasi ini menggunakan dependensi modern, termasuk:

*   **Android Jetpack**: `AppCompat`, `Activity`, `ConstraintLayout`, `Room`, `Fragment-KTX`.
*   **Firebase**: `firebase-auth`, `firebase-database`.
*   **Networking**: `Retrofit` & `Gson` untuk komunikasi API.
*   **Payment Gateway**: `com.midtrans:uikit` untuk antarmuka pembayaran.
*   **Blockchain**: `org.web3j:core` untuk interaksi dengan Ethereum.
*   **Lainnya**: `Glide` untuk memuat gambar, `Material Components` untuk UI.

## Konfigurasi Proyek

Untuk menjalankan aplikasi ini, Anda perlu mengkonfigurasi beberapa kunci API dan layanan eksternal. Semua kunci rahasia disimpan di file `local.properties` untuk keamanan.

1.  **Buat file `local.properties`**: Salin konten dari `local.properties.example` dan buat file baru bernama `local.properties` di direktori root proyek.

2.  **Isi Kunci di `local.properties`**:
    *   `SEPOLIA_PRIVATE_KEY`: Kunci privat dari dompet Ethereum Anda di jaringan Sepolia Testnet.
    *   `INFURA_PROJECT_ID`: Project ID dari akun Infura Anda untuk koneksi ke node Sepolia.
    *   `MIDTRANS_CLIENT_KEY`: Client Key dari dashboard Midtrans (mode Sandbox).
    *   `MERCHANT_BASE_URL`: URL publik dari server backend Anda. Untuk pengembangan lokal, Anda bisa menggunakan layanan tunnel seperti `ngrok`.

3.  **Konfigurasi Firebase**:
    *   Buat proyek baru di [Firebase Console](https://console.firebase.google.com/).
    *   Tambahkan aplikasi Android dengan package name `com.example.tapetrove`.
    *   Unduh file `google-services.json` yang baru dibuat dan letakkan di direktori `app/`.
    *   Aktifkan metode otentikasi **Email/Password** dan **Google** di Firebase Console.

## Cara Menjalankan

Aplikasi ini terdiri dari klien Android dan server backend (Node.js). Keduanya harus berjalan secara bersamaan.

### 1. Jalankan Backend Server

Backend bertanggung jawab untuk membuat Snap Token dari Midtrans secara aman.

1.  Navigasi ke direktori backend (Anda perlu membuatnya sesuai panduan).
2.  Instal dependensi: `npm install`
3.  Pastikan Anda telah mengisi **Midtrans Server Key** di dalam file `server.js` (atau lebih baik, sebagai environment variable).
4.  Jalankan server: `node server.js`

### 2. Jalankan Aplikasi Android

1.  Buka proyek ini di Android Studio.
2.  Pastikan Anda telah membuat dan mengisi file `local.properties` dan `google-services.json` sesuai panduan konfigurasi di atas.
3.  Sinkronkan proyek dengan Gradle.
4.  Jalankan aplikasi di Emulator Android atau perangkat fisik.

Setelah aplikasi berjalan, alur pembayaran akan secara otomatis berkomunikasi dengan backend yang sedang berjalan di `localhost`.
