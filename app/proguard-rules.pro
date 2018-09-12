-keepattributes *Annotation*, Signature, Exception

-keep class com.squareup.picasso.** { *; }
-keep class com.google.firebase.** { *; }
-keepclassmembers class com.perumaltt.quizfirebasestorage.model.** { *; }
-keepclassmembers class com.perumaltt.quizfirebasestorage.adapter.** { *; }

-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.* { *; }

-dontwarn okhttp3.internal.platform.*