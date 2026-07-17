package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp

import java.time.LocalDate
import java.util.UUID

import java.time.LocalDateTime
import java.time.OffsetDateTime


class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

class JVMUUID : AppUUID {
    override val name: String = UUID.randomUUID().toString()
}
class JVMLocalDateTime: AppLocalDateTime {
    override val name: String = LocalDateTime.now().toString()
}

actual fun getPlatform(): Platform = JVMPlatform()
actual fun getAppUUID(): AppUUID = JVMUUID()
actual fun getAppLocalDateTime(): AppLocalDateTime = JVMLocalDateTime()

class JVMCurrentTimeMillis : AppCurrentTimeMillis {
    override val value: Long = System.currentTimeMillis()
}

actual fun getAppCurrentTimeMillis(): AppCurrentTimeMillis = JVMCurrentTimeMillis()
