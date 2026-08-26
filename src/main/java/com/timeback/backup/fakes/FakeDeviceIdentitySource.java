package com.timeback.backup.fakes;

/**
 * CT-06 테스트 대역 — OS-04 + APP-02 익명 식별자 변환 시뮬레이션.
 * 원본 하드웨어 값은 노출하지 않는다.
 */
public class FakeDeviceIdentitySource {

    private boolean shouldSucceed = true;
    private String fakeAnonymousId = "fake-anonymous-user-001";
    private int transformCallCount = 0;

    /**
     * APP-02 변환 경계 시뮬레이션.
     * @return 성공 시 익명 식별자, 실패 시 null
     */
    public String transformToAnonymousId() {
        transformCallCount++;
        return shouldSucceed ? fakeAnonymousId : null;
    }

    public void setSuccess(String anonymousId) {
        this.shouldSucceed = true;
        this.fakeAnonymousId = anonymousId;
    }

    public void setFailure() {
        this.shouldSucceed = false;
    }

    public int getTransformCallCount() { return transformCallCount; }

    public void reset() {
        transformCallCount = 0;
        shouldSucceed = true;
        fakeAnonymousId = "fake-anonymous-user-001";
    }
}
