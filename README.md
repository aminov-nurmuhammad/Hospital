

# 🏥 Hospital Management System

**Hospital Management System** — bu zamonaviy kasalxona faoliyatini avtomatlashtirishga mo‘ljallangan ochiq manbali veb-ilova bo‘lib, Super Admin, Admin, Doktor va Bemor kabi foydalanuvchi rollarini qo‘llab-quvvatlaydi. Tizim qog‘oz asosidagi ish yurituvni yo‘qotish, xodimlar ishini soddalashtirish, bemor tajribasini yaxshilash va insoniy xatolarni kamaytirish orqali sog‘liqni saqlash muassasalarining samaradorligini oshiradi.

> 📌 **Loyiha asosiy vazifasi:** Qog‘ozsiz ish yuritish, jarayonlarni avtomatlashtirish, vaqtni tejash va xavfsiz ma’lumotlar boshqaruvi orqali kasalxona faoliyatini optimallashtirish.

---

## 🔍 Umumiy Ma’lumot

Loyiha Java (Spring Boot) texnologiyasida yozilgan, PostgreSQL ma’lumotlar bazasiga ulangan va Thymeleaf hamda Bootstrap yordamida zamonaviy foydalanuvchi interfeysiga ega. Kod ochiq manba sifatida GitHub’da joylashtirilgan bo‘lib, real hayotdagi kasalxona jarayonlarini raqamlashtirishga yo‘naltirilgan.

---

## 🎯 Loyihaning Maqsadlari

- ✍️ **Qog‘ozsiz ish yuritish:** Bemorlar, navbatlar, retseptlar va boshqa jarayonlar to‘liq raqamli shaklda yuritiladi.
- ⏱️ **Ish samaradorligini oshirish:** Doktorlar va bemorlar uchun vaqt tejaydi, jarayonlar tezlashtiriladi.
- 🧠 **Insoniy xatolarni kamaytirish:** Ma’lumotlar avtomatik kiritiladi va tizim orqali nazorat qilinadi.
- 🗃 **Markazlashtirilgan boshqaruv:** Barcha bemor va xizmatlar tarixi xavfsiz serverda saqlanadi va zaxiralanadi.

---

## 💡 Real Hayotdagi Muammolar va Yechimlar

| Muammo                                               | Yechimi                                                                            |
|------------------------------------------------------|------------------------------------------------------------------------------------|
| 📝 Qog‘oz hujjatlarda chalkashliklar va yo‘qolishlar | Barcha ma’lumotlar raqamli shaklda saqlanadi, qidirish va arxivlash osonlashtiriladi |
| ⌛️ Navbatlarda vaqt yo‘qotish                       | Avtomatik navbat rejalashtirish va shaxsiy kabinet orqali kuzatish                  |
| 💸 Noto‘g‘ri hisob-kitoblar                         | Xizmatlar narxi tizim tomonidan avtomatik hisoblanadi                              |
| 🧾 Retseptlar bilan xatolik                         | Retseptlar elektron shaklda saqlanadi va bemor profiliga ulanadi                   |
| 📂 Hujjatlar ishonchsizligi                        | Ma’lumotlar xavfsiz serverda saqlanadi, avtomatik zaxiralash tizimi mavjud         |

---

## 🧩 Xususiyatlar (Features)

### 👑 Super Admin
- Tizimning barcha modullari va rollari ustidan to‘liq nazorat.
- Doktorlar, bemorlar va mutaxassisliklarni qo‘shish, o‘chirish yoki tahrirlash.
- Statistik ma’lumotlar va tizim faoliyatini monitoring qilish.

### 🛡 Admin
- Yangi bemorlarni ro‘yxatga olish va ularga login/parol berish.
- Navbatlarni rejalashtirish, qabul qilish yoki bekor qilish.
- Doktorlarning bandlik jadvalini ko‘rish va boshqarish.

### 👨‍⚕️ Doktor
- Bemorning to‘liq tarixini (tashxislar, retseptlar, jarayonlar) ko‘rish.
- Muolajalar va jarayonlarni rejalashtirish, holatini yangilash (masalan, scheduled, completed).
- Retseptlar yozish va tavsiyalar berish.
- Uchrashuvlarni yakunlash va xarajatlarni hisoblash.

### 🧑‍⚕️ Bemor
- Shaxsiy profil va ma’lumotlarni (ism, telefon, manzil) ko‘rish.
- Oldingi navbatlar, retseptlar va jarayonlar tarixini kuzatish.
- Onlayn tizim orqali navbat olish va ma’lumotlarni boshqarish.

---

## 🔐 Rollar va Ruxsatlar

| Rol             | Imkoniyatlar                                                                 |
|-----------------|------------------------------------------------------------------------------|
| **Super Admin** | Tizimning barcha qismlarini boshqarish va monitoring qilish                  |
| **Admin**       | Bemorlarni ro‘yxatga olish, navbatlarni tashkil qilish                      |
| **Doktor**      | Faqat o‘z bemorlari bilan ishlash, retsept va jarayonlarni boshqarish        |
| **Bemor**       | Faqat o‘z ma’lumotlari va tarixini ko‘rish, navbat olish                     |

---

## 🛠 Texnik Tuzilish

- **Back-end:** Java 17, Spring Boot, Spring Security, Spring Data JPA
- **Front-end:** Thymeleaf, Bootstrap 5, HTML/CSS/JavaScript
- **Ma’lumotlar bazasi:** PostgreSQL
- **Build Tool:** Maven
- **Version Control:** Git, GitHub

### 📁 Kod Strukturasi
```
└── src
    ├── controller       // Har bir rol uchun alohida controller (AdminController, DoctorController)
    ├── entity           // User, Doctor, Patient, Admission, Prescription
    ├── repository       // JPA Repository interfeyslar
    ├── service          // Biznes logika (AdmissionService, PatientService)
```

---

## 🚀 Ishga Tushirish (Deployment)

Tizimni real serverda ishga tushurish uchun quyidagi qadamlar bajariladi:

1. **JAR fayl yaratish:**
   ```
   mvn clean package
   ```

2. **Serverda ishga tushirish:**
   ```
   java -jar hospital.jar
   ```

3. **Nginx sozlash:** Reverse proxy orqali trafikni boshqarish.
4. **HTTPS:** Let’s Encrypt yordamida SSL sertifikat o‘rnatish.
5. **Monitoring:** Grafana, Prometheus yoki New Relic integratsiyasi.
6. **CI/CD:** GitHub Actions bilan avtomatik deployment sozlash.

---

## 🔑 Sinov (Testing) Login Ma’lumotlari

| Rol                          | F.I.Sh              | Tel           | Parol        |
|------------------------------|---------------------|---------------|--------------|
| 👑 Super Admin               | Javohir Nasriddinov | +998901234002 | `superadmin` |
| 🛡 Admin                     | Kamola Rustamova    | +998901234001 | `admin`      |
| 👨‍⚕️ Doktor (Stomatologiya) | Iskandar Khodjaev   | +998901234003 | `doctor`     |
| 👩‍⚕️ Doktor (Kardiologiya)  | Dilorom Saidova     | +998901234004 | `doctor2`    |
| 🧑‍💼 Bemor                  | Sherzod Mahkamov    | +998901234005 | `patient`    |
| 🧑‍💼 Bemor                  | Nigora Rashidova    | +998901234006 | `patient2`   |

**Login sahifasi:** `http://localhost:8080/auth/login`

---

## 🔎 Foydalanish Qo‘llanmasi

| Rol         | Yo‘nalishlar                                               |
|-------------|------------------------------------------------------------|
| Super Admin | `/super/doctors`, `/super/patients`, `/super/specialities` |
| Admin       | `/admin/page`, `/admin/patient/{id}`                       |
| Doktor      | `/doctor`, `/doctor/patient?patientId={id}`                |
| Bemor       | `/patient`                                                 |



## 💡 Qo‘shimcha Maslahatlar

- **Test qilish:** Tizimni turli holatlarda sinab ko‘ring (masalan, bir vaqtda bir nechta bemor navbat olsa).
- **Fikr-mulohaza:** Xato topsangiz yoki taklifingiz bo‘lsa, GitHub Issues orqali xabar bering.
- **Dokumentatsiya:** Kod ichidagi sharhlarni o‘qib, qo‘shimcha ma’lumot oling.
