package com.timeback.ui.integration;

import com.timeback.device.contract.UsageAccessGateway;
import com.timeback.device.contract.UsageAccessStatus;
import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.domain.model.*;
import com.timeback.ui.fake.PermissionViewData;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Production CT-04 entry point. Until OS-04 identity verification is completed it
 * exposes real Usage Access state and blocks every identity-dependent feature.
 */
public final class ProductionFeatureGateway implements FeatureGateway {
    private final UsageAccessGateway usageAccess;
    private final boolean identityReady;

    public ProductionFeatureGateway(UsageAccessGateway usageAccess, boolean identityReady) {
        this.usageAccess = Objects.requireNonNull(usageAccess, "usageAccess");
        this.identityReady = identityReady;
    }

    @Override
    public CompletableFuture<ScreenState> readAccessState() {
        try {
            boolean granted = usageAccess.readCurrentStatus() == UsageAccessStatus.GRANTED;
            if (!granted) {
                return screen(new ScreenState.Blocked(BlockReason.PERMISSION_REQUIRED));
            }
            if (!identityReady) {
                return screen(new ScreenState.Blocked(BlockReason.IDENTITY_UNAVAILABLE));
            }
            return screen(new ScreenState.Content(new PermissionViewData(true, true, true)));
        } catch (RuntimeException error) {
            return screen(new ScreenState.Error(ErrorReason.PERMISSION_CHECK_FAILED));
        }
    }

    @Override
    public CompletableFuture<ActionResult> openUsageAccessSettings() {
        return action(usageAccess.openUsageAccessSettings()
                ? new ActionResult.Success()
                : new ActionResult.Failure(ErrorReason.PERMISSION_CHECK_FAILED));
    }

    @Override
    public CompletableFuture<ScreenState> refreshAccessState() {
        return readAccessState();
    }

    @Override public CompletableFuture<ScreenState> readHome() { return blockedScreen(); }
    @Override public CompletableFuture<ScreenState> refreshHome() { return blockedScreen(); }
    @Override public CompletableFuture<ScreenState> readTimeline(long date) { return blockedScreen(); }
    @Override public CompletableFuture<ScreenState> refreshTimeline(long date) { return blockedScreen(); }
    @Override public CompletableFuture<ActionResult> createActivity(ActivityType type, String name, TimeRange timeRange) { return blockedAction(); }
    @Override public CompletableFuture<ActionResult> updateActivity(Identifier activityId, String name, TimeRange timeRange) { return blockedAction(); }
    @Override public CompletableFuture<ActionResult> confirmContext(Identifier contextId) { return blockedAction(); }
    @Override public CompletableFuture<ActionResult> updateContext(Identifier contextId, ContextClassification classification) { return blockedAction(); }
    @Override public CompletableFuture<ScreenState> readApps() { return blockedScreen(); }
    @Override public CompletableFuture<ActionResult> changeDefaultClassification(String packageName, AppClassification classification) { return blockedAction(); }
    @Override public CompletableFuture<ScreenState> readRecoveryEntry() { return blockedScreen(); }
    @Override public CompletableFuture<ActionResult> startGoalTimer(Identifier goalId) { return blockedAction(); }
    @Override public CompletableFuture<ActionResult> completeGoalTimer() { return blockedAction(); }
    @Override public CompletableFuture<ActionResult> createManualRecoveredTime(Identifier goalId, TimeRange timeRange) { return blockedAction(); }
    @Override public CompletableFuture<ActionResult> selectRepresentativeGoal(Identifier overlapGroupId, Identifier goalId) { return blockedAction(); }
    @Override public CompletableFuture<ScreenState> readGoals() { return blockedScreen(); }
    @Override public CompletableFuture<ActionResult> createGoal(String name, Duration targetDuration) { return blockedAction(); }
    @Override public CompletableFuture<ScreenState> readReport(ReportPeriod period) { return blockedScreen(); }
    @Override public CompletableFuture<ScreenState> readDataManagementState() { return blockedScreen(); }
    @Override public CompletableFuture<ActionResult> changeRetentionSelection(RetentionSelection selection) { return blockedAction(); }
    @Override public CompletableFuture<ActionResult> requestFullDeletion() { return blockedAction(); }
    @Override public CompletableFuture<ActionResult> confirmFullDeletion() { return blockedAction(); }
    @Override public CompletableFuture<ScreenState> refreshDeletionStatus(Identifier jobId) { return blockedScreen(); }

    private CompletableFuture<ScreenState> blockedScreen() {
        return screen(new ScreenState.Blocked(BlockReason.IDENTITY_UNAVAILABLE));
    }

    private CompletableFuture<ActionResult> blockedAction() {
        return action(new ActionResult.Blocked(BlockReason.IDENTITY_UNAVAILABLE));
    }

    private static CompletableFuture<ScreenState> screen(ScreenState state) {
        return CompletableFuture.completedFuture(state);
    }

    private static CompletableFuture<ActionResult> action(ActionResult result) {
        return CompletableFuture.completedFuture(result);
    }
}
