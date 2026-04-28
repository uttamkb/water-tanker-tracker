# WaterTracker ProGuard Rules

# Hilt/Dagger
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Firebase
-keepattributes *Annotation*
-keepattributes Signature
-keepclassmembers class * {
  @com.google.firebase.firestore.PropertyName <fields>;
}

# iText (PDF)
-dontwarn com.itextpdf.**
-dontwarn org.slf4j.**

# Razorpay
-keep class com.razorpay.** {*;}
-dontwarn com.razorpay.**
