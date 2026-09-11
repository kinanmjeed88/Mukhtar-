# مختار المنطقة (The Neighborhood Mukhtar)

تطبيق أندرويد يعمل بالكامل بدون إنترنت لإدارة سكنة المنطقة والعوائل وإصدار مضابط تأييد السكن بصيغة PDF.

## التقنيات
- Kotlin + Jetpack Compose (Material 3)
- Room Database (KSP)
- MVVM + Coroutines + StateFlow
- Navigation Compose
- PdfDocument (A4 595×842) + PrintManager
- Storage Access Framework (استيراد/تصدير JSON و CSV/TXT)
- RTL إجباري في كل الشاشات

## بنية المشروع
```
app/src/main/java/com/kinan/mukhtar/
├── MainActivity.kt              فرض LayoutDirection.Rtl على كل التطبيق
├── data/
│   ├── Entities.kt              AppConfigEntity, PersonEntity, StaticData (18 محافظة)
│   ├── Daos.kt                  AppConfigDao, PersonDao
│   ├── AppDatabase.kt           Room Database (singleton)
│   └── AppRepository.kt
├── vm/MainViewModel.kt          الحالة عبر StateFlow + منطق الأعمال
├── util/
│   ├── DateUtils.kt             حساب العمر ديناميكياً + صيغة يوم/شهر/سنة
│   ├── BackupManager.kt         JSON export/import + تحليل ملفات الأسماء
│   ├── PdfGenerator.kt          توليد A4 + حفظ عبر MediaStore
│   └── PrintUtil.kt             PrintDocumentAdapter
└── ui/
    ├── theme/Theme.kt           ألوان + Typography باتجاه RTL
    ├── AppRoot.kt               التنقل + Scaffold + BottomNavigation (4 أقسام)
    ├── components/ArabicDatePicker.kt
    └── screens/
        ├── SetupScreen.kt       الإعداد الأولي + ExposedDropdownMenuBox للمحافظات
        ├── PeopleListScreen.kt  الأفراد والعوائل + بحث + BottomSheet تفاصيل
        ├── AddEditPersonScreen.kt إضافة/تعديل مع حقول الزوجة الشرطية
        ├── ResidencyScreen.kt   اختيار شخص + معاينة A4 + تحرير + حفظ/طباعة
        └── SettingsScreen.kt    الإعدادات + حوار حول المطور
```

## البناء
```bash
# افتح المجلد في Android Studio (Hedgehog+) ثم:
./gradlew assembleDebug     # ينتج app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease
```
> ملاحظة: يجب توليد Gradle Wrapper (`gradle wrapper`) أو فتح المشروع في Android Studio ليقوم بذلك تلقائياً.

## ملاحظات وظيفية
- العمر لا يُخزَّن في قاعدة البيانات، بل يُحسب لحظياً من `birthDate`.
- قسم العوائل يعرض فقط السجلات التي `isMarried == true`.
- حفظ PDF يتم في مجلد Downloads عبر MediaStore (لا يحتاج صلاحيات على Android 10+).
- استيراد النسخة الاحتياطية يستبدل كامل محتوى قاعدة البيانات.
- أيقونات فيسبوك/تيك توك/إنستغرام حالياً روابط فارغة (Placeholders) كما هو مطلوب.
