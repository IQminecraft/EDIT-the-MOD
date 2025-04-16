/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.filter.FilterWrapper
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package com.netease.mc.mod.filter;

import com.netease.mc.mod.filter.FilterWrapper;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.entity.BlockEntity;

/*
 * Exception performing whole class analysis ignored.
 */
class FilterWrapper.1
implements Runnable {
    FilterWrapper.1() {
    }

    @Override
    public void run() {
        FilterWrapper.ThreadRunning = true;
        try {
            while (!FilterWrapper.needHandleBlockEntities.isEmpty()) {
                while (!FilterWrapper.needHandleBlockEntities.isEmpty()) {
                    FilterWrapper.handlingBlockEntities.add((BlockEntity)FilterWrapper.needHandleBlockEntities.poll());
                }
                Thread.sleep(10L);
                while (!FilterWrapper.handlingBlockEntities.isEmpty()) {
                    BlockEntity blockEntity = (BlockEntity)FilterWrapper.handlingBlockEntities.poll();
                    FilterWrapper.filterTileEntity((BlockEntity)blockEntity, (HolderLookup.Provider)blockEntity.getLevel().registryAccess(), (boolean)false);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        FilterWrapper.ThreadRunning = false;
    }
}
