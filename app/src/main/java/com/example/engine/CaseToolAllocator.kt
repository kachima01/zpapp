package com.example.engine

data class ToolAllocation(
    val toolName: String,
    val category: String,
    val isPrimaryAllocation: Boolean,
    val allocationReason: String,
    val keyCapabilities: String
)

object CaseToolAllocator {

    fun getAllocatedToolsForCase(
        deviceType: String,
        crimeType: String,
        isLocked: Boolean,
        cloudAuthorized: Boolean
    ): List<ToolAllocation> {
        val allocations = mutableListOf<ToolAllocation>()

        val isMobile = deviceType.contains("Mobile", ignoreCase = true) ||
                deviceType.contains("Smartphone", ignoreCase = true) ||
                crimeType.contains("Mobile Money", ignoreCase = true)

        val isComputerOrServer = deviceType.contains("Computer", ignoreCase = true) ||
                deviceType.contains("Server", ignoreCase = true) ||
                deviceType.contains("HDD", ignoreCase = true) ||
                crimeType.contains("Hacking", ignoreCase = true) ||
                crimeType.contains("Intrusion", ignoreCase = true)

        val isVolatileOrMalware = deviceType.contains("Volatile", ignoreCase = true) ||
                deviceType.contains("RAM", ignoreCase = true) ||
                crimeType.contains("Malware", ignoreCase = true) ||
                crimeType.contains("Active Infection", ignoreCase = true)

        val isCrossPlatformOrFinancial = deviceType.contains("Cross-Platform", ignoreCase = true) ||
                deviceType.contains("Correlation", ignoreCase = true) ||
                crimeType.contains("Financial", ignoreCase = true) ||
                crimeType.contains("Embezzlement", ignoreCase = true)

        val isCloudTarget = deviceType.contains("Cloud", ignoreCase = true) || cloudAuthorized ||
                crimeType.contains("Defamation", ignoreCase = true)

        // 1. MOBILE AND CLOUD FORENSICS
        if (isMobile || isCloudTarget) {
            allocations.add(
                ToolAllocation(
                    toolName = "Cellebrite UFED & Physical Analyzer",
                    category = "Mobile and Cloud Forensics",
                    isPrimaryAllocation = true,
                    allocationReason = if (isLocked) "Required to bypass device locks and perform bit-stream physical extraction." else "Decodes encrypted messaging databases, call logs, and application storage.",
                    keyCapabilities = "Physical Extraction • Lock Bypass • Encrypted Messenger Decoding"
                )
            )
            allocations.add(
                ToolAllocation(
                    toolName = "MSAB XRY",
                    category = "Mobile and Cloud Forensics",
                    isPrimaryAllocation = true,
                    allocationReason = "Extracts and parses secure mobile system files and SIM card transaction records.",
                    keyCapabilities = "Field-Ready Mobile Extraction • System File Parsing • SIM Forensics"
                )
            )
            allocations.add(
                ToolAllocation(
                    toolName = "Oxygen Forensics",
                    category = "Mobile and Cloud Forensics",
                    isPrimaryAllocation = cloudAuthorized || isCloudTarget,
                    allocationReason = if (cloudAuthorized) "Pulls authenticated cloud accounts (iCloud, Google, WhatsApp backups)." else "Bypasses smartphone lock partitions and recovers cached social chats.",
                    keyCapabilities = "Live Cloud Account Pulls • Lock Bypass • Smartphone Decoding"
                )
            )
        }

        // 2. ALL-IN-ONE AND ARTIFACT CORRELATION
        if (isCrossPlatformOrFinancial || (isMobile && isComputerOrServer)) {
            allocations.add(
                ToolAllocation(
                    toolName = "Magnet AXIOM",
                    category = "All-in-One and Artifact Correlation",
                    isPrimaryAllocation = true,
                    allocationReason = "Stitches disparate evidence (browser history, phone GPS location, cloud records) into a unified timeline.",
                    keyCapabilities = "Disparate Artifact Correlation • Unified Timeline • Multi-Device Cross-Match"
                )
            )
            allocations.add(
                ToolAllocation(
                    toolName = "Belkasoft X",
                    category = "All-in-One and Artifact Correlation",
                    isPrimaryAllocation = true,
                    allocationReason = "Integrated DFIR suite parsing hard drives, mobile data, cloud services, and memory in one sweep.",
                    keyCapabilities = "Integrated DFIR • Multi-Artifact Sweep • Volatile Memory + Storage"
                )
            )
        }

        // 3. COMPUTER AND DEEP FILE SYSTEM FORENSICS
        if (isComputerOrServer) {
            allocations.add(
                ToolAllocation(
                    toolName = "EnCase Forensic",
                    category = "Computer and Deep File System Forensics",
                    isPrimaryAllocation = true,
                    allocationReason = "Generates bit-stream disk images and structured, court-admissible evidence packages.",
                    keyCapabilities = "Bit-Stream Disk Imaging • Deep File Systems • Court-Admissible Packages"
                )
            )
            allocations.add(
                ToolAllocation(
                    toolName = "FTK (Forensic Toolkit)",
                    category = "Computer and Deep File System Forensics",
                    isPrimaryAllocation = true,
                    allocationReason = "Heavyweight desktop indexing for raw speed across massive enterprise datasets.",
                    keyCapabilities = "High-Speed Indexing • Enterprise Datasets • Raw File Carving"
                )
            )
        }

        // 4. VOLATILE MEMORY / LIVE RAM FORENSICS
        if (isVolatileOrMalware) {
            allocations.add(
                ToolAllocation(
                    toolName = "Volatility Framework",
                    category = "Free & Open-Source Forensic Frameworks",
                    isPrimaryAllocation = true,
                    allocationReason = "Analyzes volatile RAM dumps to uncover active malware, hidden processes, and open network sockets.",
                    keyCapabilities = "RAM Memory Dumps • Active Malware Analysis • Network Sockets"
                )
            )
            if (!allocations.any { it.toolName == "Belkasoft X" }) {
                allocations.add(
                    ToolAllocation(
                        toolName = "Belkasoft X",
                        category = "All-in-One and Artifact Correlation",
                        isPrimaryAllocation = true,
                        allocationReason = "Parses volatile RAM memory dumps and correlates running processes with disk artifacts.",
                        keyCapabilities = "Volatile Memory Extraction • Crash Dump Analysis"
                    )
                )
            }
        }

        // 5. FREE & OPEN-SOURCE FRAMEWORKS (Always available as non-commercial or supporting option)
        if (!allocations.any { it.toolName == "Autopsy" }) {
            allocations.add(
                ToolAllocation(
                    toolName = "Autopsy",
                    category = "Free & Open-Source Forensic Frameworks",
                    isPrimaryAllocation = !isVolatileOrMalware && allocations.size <= 2,
                    allocationReason = "Open-source graphical pipeline to parse disk images, smartphone databases, and recover deleted media without licensing cost.",
                    keyCapabilities = "Disk Image Analysis • SQLite Database Parsing • Media Carving"
                )
            )
        }

        if (!allocations.any { it.toolName == "Volatility Framework" } && (isComputerOrServer || isVolatileOrMalware)) {
            allocations.add(
                ToolAllocation(
                    toolName = "Volatility Framework",
                    category = "Free & Open-Source Forensic Frameworks",
                    isPrimaryAllocation = false,
                    allocationReason = "Analyzes live RAM captures for unencrypted keys and active connections.",
                    keyCapabilities = "Volatile Memory Inspection • Open Sockets"
                )
            )
        }

        // Fallback default if nothing was triggered
        if (allocations.isEmpty()) {
            allocations.add(
                ToolAllocation(
                    toolName = "Cellebrite UFED & Physical Analyzer",
                    category = "Mobile and Cloud Forensics",
                    isPrimaryAllocation = true,
                    allocationReason = "Standard mobile forensic extraction and lock bypass.",
                    keyCapabilities = "Physical Extraction • Lock Bypass"
                )
            )
            allocations.add(
                ToolAllocation(
                    toolName = "Autopsy",
                    category = "Free & Open-Source Forensic Frameworks",
                    isPrimaryAllocation = true,
                    allocationReason = "Open-source disk image and database analysis.",
                    keyCapabilities = "Disk Imaging • Media Recovery"
                )
            )
        }

        return allocations
    }
}
