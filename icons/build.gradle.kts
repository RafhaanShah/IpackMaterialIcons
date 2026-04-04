import com.android.ide.common.vectordrawable.Svg2Vector
import java.nio.file.Files

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.ipack.icons"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro", "proguard-rules.pro")
    }

    buildTypes {
        release {
            isShrinkResources = false
            isMinifyEnabled = false
            isPseudoLocalesEnabled = false
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

    androidResources {
        // Force AAPT2 to use and emit stable IDs
        additionalParameters.add("--emit-ids")
        additionalParameters.add("${projectDir}/stable-ids.txt")
        additionalParameters.add("--stable-ids")
        additionalParameters.add("${projectDir}/stable-ids.txt")
    }
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

val svgTask = tasks.register<SvgToVectorTask>("generateSvgVectors") {
    inputDir = file("$projectDir/../external/icons/svg")
}

tasks.named("preBuild") {
    dependsOn(svgTask)
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
        val drawableFolder = File(outDir, "drawable").apply { if (!exists()) mkdirs() }
        if (!inputDir.exists()) return

        inputDir.walkTopDown()
            .filter { it.isFile && it.extension == "svg" }
            .forEach { svgFile ->
                val rawName = svgFile.nameWithoutExtension.lowercase().replace("-", "_")
                val baseName = when {
                    reservedKeywords.contains(rawName) -> "${rawName}_icon"
                    rawName.firstOrNull()?.isDigit() == true -> "icon_$rawName"
                    else -> rawName
                }

                // 1. Generate Dark Version (Original)
                val darkFile = File(drawableFolder, "${baseName}_dark.xml")
                convertSvg(svgFile, darkFile)

                // 2. Generate Light Version (Modified Fill)
                val lightFile = File(drawableFolder, "${baseName}_light.xml")
                convertSvg(svgFile, lightFile)
                applyWhiteFill(lightFile)
            }
    }

    private fun convertSvg(source: File, destination: File) {
        destination.parentFile.mkdirs()
        val errorLog =
            Svg2Vector.parseSvgToXml(source.toPath(), Files.newOutputStream(destination.toPath()))
        if (errorLog.isNotEmpty()) {
            println("SVG conversion warning for ${source.name}: $errorLog")
        }
    }

    private fun applyWhiteFill(file: File) {
        val content = file.readText()
        // Regex targets android:fillColor attributes.
        // This replaces any hex or named color with white (#FFFFFFFF)
        val modifiedContent = content.replace(
            Regex("""android:fillColor="[^"]*""""),
            """android:fillColor="#FFFFFFFF""""
        )
        file.writeText(modifiedContent)
    }
}
