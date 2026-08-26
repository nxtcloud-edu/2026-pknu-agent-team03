package com.timeback.device.contract;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class StableIds {
    private StableIds() {}

    public static String usageEvent(DataOwnerScope owner, ObservedUsageEvent observed) {
        return sha256(String.join(
                "|",
                "usage-event",
                owner.value(),
                observed.packageName(),
                observed.kind().name(),
                Long.toString(observed.occurredAtMillis()),
                Integer.toString(observed.occurrenceInTimestampGroup())
        ));
    }

    public static String logicalSession(
            DataOwnerScope owner,
            String packageName,
            long startedAtMillis,
            long endedAtMillis
    ) {
        return sha256("logical-session|" + owner.value() + "|" + packageName + "|"
                + startedAtMillis + "|" + endedAtMillis);
    }

    public static String sessionPart(String logicalSessionId, TimeRange range) {
        return sha256("session-part|" + logicalSessionId + "|"
                + range.startAtMillis() + "|" + range.endAtMillis());
    }

    public static String change(
            DataOwnerScope owner,
            long sequence,
            DeviceEntityType entityType,
            String entityId,
            ChangeOperation operation
    ) {
        return sha256("change|" + owner.value() + "|" + sequence + "|"
                + entityType + "|" + entityId + "|" + operation);
    }

    public static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }
}
