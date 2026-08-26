package com.timeback.device.contract;

/**
 * APP-05–APP-09가 APP-11에 맡기는 논리 레코드의 저장 표현이다.
 * payload는 트랙 통합 어댑터가 직렬화하며 APP-11은 도메인 의미를 재계산하지 않는다.
 */
public record DeviceRecord(
        String recordId,
        DataOwnerScope owner,
        DeviceEntityType entityType,
        TimeRange range,
        String payload
) {
    public DeviceRecord {
        if (recordId == null || recordId.isBlank()) {
            throw new IllegalArgumentException("record id must not be blank");
        }
    }

    public DeviceRecord withPayload(String newPayload) {
        return new DeviceRecord(recordId, owner, entityType, range, newPayload);
    }
}
