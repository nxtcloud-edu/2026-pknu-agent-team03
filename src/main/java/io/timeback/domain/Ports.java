package io.timeback.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** SDK-independent boundaries; production adapters belong to their owning tracks. */
interface SessionSource {
    List<AppSession> listSessions(Interval period);
}

interface DomainDataAuthority {
    Map<MeasurementKey, MeasurementDay> measurementDays();
    void replaceMeasurementSnapshot(Map<MeasurementKey, MeasurementDay> snapshot);
}

interface TimeBoundary {
    Instant now();
}

interface ChangeNotifier {
    void changed(String entityType, String entityId, String operation);
}

/** Replaces the full Context revision snapshot or preserves the prior snapshot on failure. */
interface ContextSnapshotStore {
    void replaceAtomically(List<ContextRevision> expectedSnapshot, List<ContextRevision> replacementSnapshot);
}
