package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp






import platform.Foundation.NSUUID
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.timeIntervalSince1970

import platform.UIKit.UIDevice




class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

class IOSUUID : AppUUID {
    override val name: String
        get() = NSUUID().UUIDString()
}

class IOSLocalDateTime: AppLocalDateTime {
    override val name: String
        get() {
            val formatter = NSDateFormatter()
            formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
            formatter.locale = NSLocale.currentLocale
            return formatter.stringFromDate(NSDate())
        }
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun getAppUUID(): AppUUID = IOSUUID()
actual fun getAppLocalDateTime(): AppLocalDateTime = IOSLocalDateTime()

class IOSCurrentTimeMillis : AppCurrentTimeMillis {
    override val value: Long
        get() = (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun getAppCurrentTimeMillis(): AppCurrentTimeMillis = IOSCurrentTimeMillis()
