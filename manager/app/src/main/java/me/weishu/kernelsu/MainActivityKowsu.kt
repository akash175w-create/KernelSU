package me.weishu.kernelsu

import android.app.Activity
import android.os.Bundle
import android.util.Log
import dalvik.system.DexClassLoader
import java.io.File
import java.security.MessageDigest
import me.weishu.kernelsu.ui.MainActivity

class MainActivityKowsu : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            runCustomStartupDialog()
        } catch (error: Throwable) {
            Log.e("MainActivityKowsu", "Could not load custom startup DEX", error)
        }
    }

    private fun runCustomStartupDialog() {
        val dexBytes = assets.open("classes2.dex").use { it.readBytes() }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(dexBytes)
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        val dexFile = File(codeCacheDir, "dpmods-$digest.dex")
        if (!dexFile.exists()) {
            dexFile.outputStream().use { it.write(dexBytes) }
            check(dexFile.setReadOnly()) { "Could not mark startup DEX read-only" }
        }

        val loader = DexClassLoader(
            dexFile.absolutePath,
            codeCacheDir.absolutePath,
            null,
            classLoader
        )
        val show = loader.loadClass("com.yt.dpmods.dialog")
            .getMethod("show", Activity::class.java)
        show.invoke(null, this)
    }
}
