# Keep Kotlinx serialization
-keepclassmembers class **$Companion {
    ** serializer(...);
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
