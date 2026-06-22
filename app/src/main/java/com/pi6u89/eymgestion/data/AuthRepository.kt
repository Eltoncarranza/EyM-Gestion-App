package com.pi6u89.eymgestion.data

import android.util.Log
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {

    suspend fun iniciarSesion(correo: String, contrasena: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    email = correo
                    password = contrasena
                }
                true
            } catch (e: Exception) {
                Log.e("AuthRepo", "Error al iniciar sesion: ${e.message}", e)
                false
            }
        }
    }

    suspend fun cerrarSesion() {
        withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signOut()
            } catch (e: Exception) {
                Log.e("AuthRepo", "Error al cerrar sesion: ${e.message}", e)
            }
        }
    }

    fun sesionActiva(): Boolean {
        return try {
            SupabaseClient.client.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }
}