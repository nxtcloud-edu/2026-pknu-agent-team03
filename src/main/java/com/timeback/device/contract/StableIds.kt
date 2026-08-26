package com.timeback.device.contract

import java.security.MessageDigest

object StableIds {
    fun usageEvent(owner: DataOwnerScope, observed: ObservedUsageEvent): String = sha256(
        listOf(
            "usage-event",
            owner.value,
            observed.packageName,
            observed.kind.name,
            observed.occurredAtMillis.toString(),
            observed.occurrenceInTimestampGroup.toString(),
        ).joinToString("|"),
    )

    fun logicalSession(
        owner: DataOwnerScope,
        packageName: String,
        startedAtMillis: Long,
        endedAtMillis: Long,
    ): String = sha256("logical-session|${owner.value}|$packageName|$startedAtMillis|$endedAtMillis")

    fun sessionPart(logicalSessionId: String, range: TimeRange): String =
        sha256("session-part|$logicalSessionId|${range.startAtMillis}|${range.endAtMillis}")

    fun change(
        owner: DataOwnerScope,
        sequence: Long,
        entityType: DeviceEntityType,
        entityId: String,
        operation: ChangeOperation,
    ): String = sha256("change|${owner.value}|$sequence|$entityType|$entityId|$operation")

    fun sha256(value: String): String = MessageDigest
        .getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(byte) }
}
