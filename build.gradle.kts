plugins {
	id("com.android.library")
	id("kotlin-android")
	id("org.jetbrains.dokka") version "1.9.10"
	id("maven-publish")
	id("java-test-fixtures")
}

android {
	namespace = "dev.frozenmilk.dairy.core"
	compileSdk = 29

	defaultConfig {
		minSdk = 24
		//noinspection ExpiredTargetSdkVersion
		targetSdk = 28

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}
	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
		testFixtures {
			enable = true
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8

		kotlin {
			compilerOptions {
				freeCompilerArgs.add("-Xjvm-default=all")
			}
		}
	}
}

dependencies {
	testFixturesApi("junit:junit:4.13.2")
	testFixturesApi("org.firstinspires.ftc:RobotCore:9.1.0")

	//noinspection GradleDependency
	implementation("androidx.appcompat:appcompat:1.2.0")

	api(project(":Util"))
	api(project(":Sinister"))

	compileOnly("org.firstinspires.ftc:RobotCore:9.0.1")
	compileOnly("org.firstinspires.ftc:Hardware:9.0.1")
	compileOnly("org.firstinspires.ftc:FtcCommon:9.0.1")
}

publishing {
	publications {
		register<MavenPublication>("release") {
			groupId = "dev.frozenmilk.dairy"
			artifactId = "Core"
			version = "v0.0.0"

			afterEvaluate {
				from(components["release"])
			}
		}
	}
	repositories {
		maven {
			name = "Core"
			url = uri("${project.buildDir}/release")
		}
	}
}
