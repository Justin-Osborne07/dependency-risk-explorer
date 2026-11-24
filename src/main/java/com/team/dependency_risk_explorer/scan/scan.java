package com.team.dependency_risk_explorer.scan;

import java.time.Instant;

public class scan {

    private Long id;
    private Long snapshotId;
    private ScanStatus status;

    private int totalPackages;
    private int checkedPackages;
    private int failedPackages;

    private Instant startedAt;
    private Instant completedAt;

    public Scan() {
    }

    public Scan(Long snapshotId) {
        this.snapshotId = snapshotId;
        this.status = ScanStatus.RUNNING;
        this.startedAt = Instant.now();
    }
}