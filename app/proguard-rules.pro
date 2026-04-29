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

# Keep Firestore DTOs and their no-arg constructors
-keep class com.apartment.watertracker.data.remote.model.** { *; }

# Google Sign-In & Credentials Manager
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# iText (PDF)
-dontwarn com.itextpdf.**
-dontwarn org.slf4j.**

# Razorpay
-keep class com.razorpay.** {*;}
-dontwarn com.razorpay.**
