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
	testFixturesImplementation("io.github.classgraph:classgraph:4.8.174")
	testFixturesApi("org.firstinspires.ftc:RobotCore:10.0.0")

	//noinspection GradleDependency
	implementation("androidx.appcompat:appcompat:1.2.0")

	api(project(":Sinister"))

	compileOnly("org.firstinspires.ftc:RobotCore:10.0.0")
	compileOnly("org.firstinspires.ftc:Hardware:10.0.0")
	compileOnly("org.firstinspires.ftc:FtcCommon:10.0.0")
}

publishing {
	repositories {
		maven {
			name = "Dairy"
			url = uri("https://repo.dairy.foundation/releases")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
		maven {
			name = "DairySNAPSHOT"
			url = uri("https://repo.dairy.foundation/snapshots")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
	publications {
		register<MavenPublication>("release") {
			groupId = "dev.frozenmilk.dairy"
			artifactId = "Core"
			version = "1.0.0"

			afterEvaluate {
				from(components["release"])
			}
		}
	}
}
