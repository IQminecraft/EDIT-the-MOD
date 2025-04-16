/*
 * Decompiled with CFR 0.152.
 */
package com.netease.mc.mod.filter;

import com.netease.mc.mod.filter.FilterHelper;

static class FilterHelper.1
implements Runnable {
    FilterHelper.1() {
    }

    @Override
    public void run() {
        ThreadRunning = true;
        try {
            while (!launcherList.isEmpty()) {
                while (!launcherList.isEmpty()) {
                    needLauncherList.add(launcherList.poll());
                }
                Thread.sleep(10L);
                while (!needLauncherList.isEmpty()) {
                    FilterHelper.doLauncherSdkFilterInner(needLauncherList.poll());
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        ThreadRunning = false;
    }
}
