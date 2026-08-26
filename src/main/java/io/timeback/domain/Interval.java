package io.timeback.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

/** Immutable half-open interval [start, end). */
public record Interval(Instant start, Instant end) {
    public Interval {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end must be after start");
        }
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    public boolean overlaps(Interval other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    public boolean contains(Interval other) {
        return !other.start.isBefore(start) && !other.end.isAfter(end);
    }

    public Optional<Interval> intersection(Interval other) {
        Instant intersectionStart = start.isAfter(other.start) ? start : other.start;
        Instant intersectionEnd = end.isBefore(other.end) ? end : other.end;
        return intersectionEnd.isAfter(intersectionStart)
                ? Optional.of(new Interval(intersectionStart, intersectionEnd))
                : Optional.empty();
    }

    public List<Interval> splitAt(Collection<Instant> boundaries) {
        TreeSet<Instant> points = new TreeSet<>();
        points.add(start);
        points.add(end);
        for (Instant boundary : boundaries) {
            if (boundary.isAfter(start) && boundary.isBefore(end)) {
                points.add(boundary);
            }
        }
        List<Instant> ordered = new ArrayList<>(points);
        List<Interval> result = new ArrayList<>();
        for (int index = 0; index < ordered.size() - 1; index++) {
            result.add(new Interval(ordered.get(index), ordered.get(index + 1)));
        }
        return List.copyOf(result);
    }

    public List<Interval> sliceByLocalDate(ZoneId zoneId) {
        Objects.requireNonNull(zoneId, "zoneId");
        List<Instant> boundaries = new ArrayList<>();
        ZonedDateTime cursor = start.atZone(zoneId).toLocalDate().plusDays(1).atStartOfDay(zoneId);
        while (cursor.toInstant().isBefore(end)) {
            boundaries.add(cursor.toInstant());
            cursor = cursor.toLocalDate().plusDays(1).atStartOfDay(zoneId);
        }
        return splitAt(boundaries);
    }

    public static Duration unionDuration(Collection<Interval> intervals) {
        if (intervals.isEmpty()) {
            return Duration.ZERO;
        }
        List<Interval> ordered = intervals.stream()
                .sorted(Comparator.comparing(Interval::start).thenComparing(Interval::end))
                .toList();
        Instant mergedStart = ordered.get(0).start;
        Instant mergedEnd = ordered.get(0).end;
        Duration total = Duration.ZERO;
        for (int index = 1; index < ordered.size(); index++) {
            Interval next = ordered.get(index);
            if (!next.start.isAfter(mergedEnd)) {
                if (next.end.isAfter(mergedEnd)) {
                    mergedEnd = next.end;
                }
            } else {
                total = total.plus(Duration.between(mergedStart, mergedEnd));
                mergedStart = next.start;
                mergedEnd = next.end;
            }
        }
        return total.plus(Duration.between(mergedStart, mergedEnd));
    }
}
