==============================================================
KULKASKIS - IF570L Proyek Ujian Tengah Semester (Ganjil 2026/2027)
==============================================================

Nama Kelompok : Pejuang Nilai Bagus
Nama Aplikasi : Kulkaskis
Mata Kuliah   : IF570L Praktikum Pemrograman Aplikasi Bergerak


1. DESKRIPSI APLIKASI
--------------------------------------------------------------
Kulkaskis adalah aplikasi pelacak isi kulkas yang dirancang untuk
membantu rumah tangga mengurangi limbah makanan. Pengguna dapat
mencatat bahan makanan yang ada di kulkas, melihat status kesegaran
(segar, hampir kadaluwarsa, atau sudah kadaluwarsa), serta mendapatkan
rekomendasi resep yang memprioritaskan bahan-bahan yang harus segera
digunakan.

Layar yang diimplementasikan (9) + Navigasi Bawah (Bottom Navigation Bar):
1. Login            (Formulir, validasi)
2. Register         (Formulir, validasi)
3. Home             (Pilihan bebas: banner ringkasan + item terbaru)
4. My Fridge        (DAFTAR/LIST: pencarian + filter kategori)
5. Food detail      (DETAIL: menerima ID item dari daftar)
6. Add / edit item  (FORMULIR: validasi bidang wajib, angka, & tanggal)
7. Scan Fridge      (Layar FITUR LOKAL/NATIVE)
8. Recipes          (Pilihan bebas: kartu resep yang dapat diperluas)
9. Profile          (Statistik, pengingat, logout)

Tab Navigasi Bawah: Home, Fridge, Scan, Recipes, Profile.

Cakupan Pemenuhan Persyaratan UTS:
- Layar ........ List, detail, formulir, fitur native, pilihan bebas
- Tema ......... Tema kustom Material 3 (warna, tipografi Poppins/Inter,
  bentuk/shapes) pada direktori ui/theme/
- State ........ remember, mutableStateOf, ViewModel + StateFlow
  (loading, input, validasi, seleksi, filter)
- Navigasi ..... NavHost/NavController, argumen "detail/{id}"
  dan "edit/{id}", popUpTo saat login/logout
- Validasi ..... Format email, bidang wajib diisi, jumlah hanya angka
  (1-999), tipe keyboard yang sesuai, isError + supportingText,
  tombol kirim dinonaktifkan sampai input valid
- Data Dummy ... 10 item realistis (data class FridgeItem) dalam
  berbagai status, 8 resep (data class Recipe)

Catatan Fitur Tambahan:
Proyek ini juga mencakup integrasi Room, Supabase, serta Kamera/ML Kit.
Fitur-fitur ini TIDAK diwajibkan untuk ujian. Untuk menjalankan aplikasi
tanpa server, tekan tombol "Continue as guest (demo)" di layar login;
aplikasi akan memuat data dummy secara otomatis.


2. ANGGOTA KELOMPOK
--------------------------------------------------------------
1. Bryant Rick Zhang   - 00000112782
2. Sean Antonio Herman - 00000113412
3. Ryan Fernando       - 00000112913


3. PEMBAGIAN TUGAS DAN SISTEM KERJA KELOMPOK
--------------------------------------------------------------
- Pembagian tugas dilakukan per layar/modul untuk menghindari konflik
  saat penggabungan kode (merge conflict).
- Kode dikelola menggunakan GitHub. Setiap anggota bekerja di branch
  masing-masing sebelum digabungkan (merge) ke branch utama.
- Koordinasi rutin dilakukan melalui WhatsApp dan Discord, serta meeting
  berkala untuk menyelaraskan progres.
- Seluruh anggota melakukan pengujian (testing) dan review aplikasi
  secara bersama-sama sebelum submit.


4. KONTRIBUSI INDIVIDUAL
--------------------------------------------------------------
Bryant Rick Zhang:
- Mengimplementasikan layar Home dan Recipes, membuat ViewModel resep, menyusun dokumen README

Sean Antonio Herman:
- Mengimplementasikan fitur Auth (Login/Register), navigasi utama (NavHost), integrasi Supabase/Room

Ryan Fernando:
- Mengimplementasikan layar EditItemScreen & My Fridge, validasi formulir, kustomisasi tema Material 3


5. CARA MENJALANKAN APLIKASI
--------------------------------------------------------------
- Pasang (install) file APK (Kulkaskis.apk) pada perangkat Android (minimal Android 8.1+).
- Tekan "Continue as guest (demo)" untuk menggunakan aplikasi dengan data dummy.
  - Atau buat akun/login jika ingin mencoba fitur daring (memerlukan koneksi internet via Supabase).