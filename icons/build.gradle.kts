import com.android.ide.common.vectordrawable.Svg2Vector
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File
import java.nio.file.Files
import kotlin.text.replace

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.ipack.icons"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

val svgTask = tasks.register<SvgToVectorTask>("generateSvgVectors") {
    inputDir = file("$projectDir/../external/icons/svg")
}

tasks.named("preBuild") {
    dependsOn(svgTask)
}

androidComponents {
    onVariants { variant ->
        // This automatically sets the task's outputDir and registers it as a source
        variant.sources.res?.addGeneratedSourceDirectory(
            svgTask,
            SvgToVectorTask::outputDir
        )
    }
}

abstract class SvgToVectorTask : DefaultTask() {

    @Internal
    val reservedKeywords = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
        "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
        "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
        "interface", "long", "native", "new", "null", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while", "as", "fun",
        "in", "is", "object", "typealias", "val", "var", "when"
    )

    @InputDirectory
    lateinit var inputDir: File

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun run() {
        val outDir = outputDir.get().asFile
        val drawableFolder = File(outDir, "drawable")
        if (!drawableFolder.exists()) drawableFolder.mkdirs()
        if (!inputDir.exists()) return

        inputDir.walkTopDown()
            .filter { it.isFile && it.extension == "svg" }
            .forEach { svgFile ->
                val rawName = svgFile.nameWithoutExtension.lowercase().replace("-", "_")
                val fileName = when {
                    reservedKeywords.contains(rawName) -> "${rawName}_icon"
                    rawName.firstOrNull()?.isDigit() == true -> "icon_$rawName"
                    else -> rawName
                }

                val drawableFile = File(drawableFolder, "$fileName.xml")
                drawableFile.parentFile.mkdirs()
                val errorLog  = Svg2Vector.parseSvgToXml(svgFile.toPath(), Files.newOutputStream(drawableFile.toPath()))
                if (errorLog.isNotEmpty()) {
                    println("SVG conversion warning for ${svgFile.name}:")
                    println(errorLog.toString())
                }
            }
    }
}
