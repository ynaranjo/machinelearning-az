# KidsGuard — reglas R8/ProGuard para el build de release.
#
# Los componentes declarados en el manifest (activities, services,
# receivers) y las clases de ViewBinding se conservan automáticamente por
# las reglas AAPT generadas. Las bibliotecas androidx usadas (security-crypto/
# Tink, biometric, work) incluyen sus propias reglas de consumidor.

# Tink (usado por EncryptedSharedPreferences) referencia clases opcionales
# que no están en el classpath de Android; silenciar esos avisos.
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
