package com.evam.marketing.communication.template.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class PerformanceCounter {

    private final AtomicLong batchCountTotal = new AtomicLong(0);

    private final AtomicLong batchCountSuccess = new AtomicLong(0);
    private final AtomicLong batchCountError = new AtomicLong(0);
    private final AtomicLong batchCountDuplicate = new AtomicLong(0);


    private final AtomicLong eventCountTotal = new AtomicLong(0);

    private final AtomicLong eventCountSilent = new AtomicLong(0);
    private final AtomicLong eventCountTimeConstraint = new AtomicLong(0);

    private final AtomicLong eventCountSuccess = new AtomicLong(0);
    private final AtomicLong eventCountFail = new AtomicLong(0);


    private final AtomicLong callbackCountTotal = new AtomicLong(0);

    private final AtomicLong callbackCountNonDeliveryReport = new AtomicLong(0);
    private final AtomicLong callbackCountDeliverySuccess = new AtomicLong(0);
    private final AtomicLong callbackCountDeliveryFail = new AtomicLong(0);
    private final AtomicLong callbackCountIncoming = new AtomicLong(0);

    private final AtomicLong ts = new AtomicLong(System.currentTimeMillis());

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public PerformanceCounter() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
            new CustomizableThreadFactory("StatWorker-" + getClass().getName()));
        scheduledExecutorService.scheduleAtFixedRate(this::report, 30, 30,
            TimeUnit.SECONDS);
    }

    public void incrementBatchCountSuccess() {
        batchCountSuccess.incrementAndGet();
        batchCountTotal.incrementAndGet();
    }

    public void incrementBatchCountError() {
        batchCountError.incrementAndGet();
        batchCountTotal.incrementAndGet();
    }

    public void incrementBatchCountDuplicate() {
        batchCountDuplicate.incrementAndGet();
        batchCountTotal.incrementAndGet();
    }


    public void incrementEventCountSilent() {
        eventCountSilent.incrementAndGet();
        eventCountTotal.incrementAndGet();
    }

    public void incrementEventCountTimeConstraint() {
        eventCountTimeConstraint.incrementAndGet();
        eventCountTotal.incrementAndGet();
    }


    public void incrementEventCountSuccess() {
        eventCountSuccess.incrementAndGet();
        eventCountTotal.incrementAndGet();
    }

    public void incrementEventCountFail() {
        eventCountFail.incrementAndGet();
        eventCountTotal.incrementAndGet();
    }


    public void incrementCallbackCountNonDeliveryReport() {
        callbackCountNonDeliveryReport.incrementAndGet();
        callbackCountTotal.incrementAndGet();
    }

    public void incrementCallbackCountDeliverySuccess() {
        callbackCountDeliverySuccess.incrementAndGet();
        callbackCountTotal.incrementAndGet();
    }

    public void incrementCallbackCountDeliveryFail() {
        callbackCountDeliveryFail.incrementAndGet();
        callbackCountTotal.incrementAndGet();
    }

    public void incrementCallbackCountIncoming() {
        callbackCountIncoming.incrementAndGet();
        callbackCountTotal.incrementAndGet();
    }


    public synchronized void report() {
        long bCountSuccess = batchCountSuccess.getAndSet(0);
        long bCountError = batchCountError.getAndSet(0);
        long bCountDuplicate = batchCountDuplicate.getAndSet(0);
        long bCountTotal = batchCountTotal.getAndSet(0);

        long eCountSilent = eventCountSilent.getAndSet(0);
        long eCountTimeConstraint = eventCountTimeConstraint.getAndSet(0);
        long eCountSuccess = eventCountSuccess.getAndSet(0);
        long eCountFail = eventCountFail.getAndSet(0);
        long eCountTotal = eventCountTotal.getAndSet(0);

        long cCountNonDeliveryReport = callbackCountNonDeliveryReport.getAndSet(0);
        long cCountDeliverySuccess = callbackCountDeliverySuccess.getAndSet(0);
        long cCountDeliveryFail = callbackCountDeliveryFail.getAndSet(0);
        long cCountIncoming = callbackCountIncoming.getAndSet(0);
        long cCountTotal = callbackCountTotal.getAndSet(0);

        long now = System.currentTimeMillis();
        long diff = now - ts.getAndSet(now);
        double multiplier = 1000d / diff;
        log.info(
            "batch(success={},error={},duplicate={},total={},tps={}), event(silent={},timeConstraint={},success={},fail={},total={},tps={}), callback(nonDeliveryReport={},deliverySuccess={},deliveryFail={},incomingSMS={},total={},tps={})",
            new Object[]{
                bCountSuccess,
                bCountError,
                bCountDuplicate,
                bCountTotal,
                decimalFormat.format(bCountTotal * multiplier),
                eCountSilent,
                eCountTimeConstraint,
                eCountSuccess,
                eCountFail,
                eCountTotal,
                decimalFormat.format(eCountTotal * multiplier),
                cCountNonDeliveryReport,
                cCountDeliverySuccess,
                cCountDeliveryFail,
                cCountIncoming,
                cCountTotal,
                decimalFormat.format(cCountTotal * multiplier)
            }
        );
    }
}
