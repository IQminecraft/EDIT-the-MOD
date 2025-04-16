/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.netease.mc.mod.filter.AESHelper
 *  com.netease.mc.mod.filter.FilterWhiteListHelper
 *  com.netease.mc.mod.filter.FilterWrapper
 *  com.netease.mc.mod.filter.Helper
 *  com.netease.mc.mod.filter.LoadFilterReRunnable
 *  com.netease.mc.mod.network.common.GameState
 *  com.netease.mc.mod.network.common.Library
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentContents
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.contents.PlainTextContents
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.server.network.Filterable
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.WrittenBookItem
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.item.component.WrittenBookContent
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.filter;

import com.google.common.base.Joiner;
import com.netease.mc.mod.filter.AESHelper;
import com.netease.mc.mod.filter.FilterWhiteListHelper;
import com.netease.mc.mod.filter.FilterWrapper;
import com.netease.mc.mod.filter.Helper;
import com.netease.mc.mod.filter.LoadFilterReRunnable;
import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.common.Library;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilterHelper {
    public static final Logger logger = LogManager.getLogger(FilterHelper.class);
    private static List<String> namingFilterRegularExpList = Collections.synchronizedList(new ArrayList());
    private static List<String> chatFilterRegularExpList = Collections.synchronizedList(new ArrayList());
    public static long lastLoadFilterReTime = 0L;
    static volatile boolean ThreadRunning = false;
    public static int globalWordId = 0;
    public static ConcurrentLinkedQueue<String> needLauncherList = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue<String> launcherList = new ConcurrentLinkedQueue();
    public static HashMap<Integer, Object> lockObjectMap = new HashMap();
    public static HashMap<Integer, String> filterWordMap = new HashMap();
    private static final int TIMEOUT = 1000;
    private static String[] whiteCmds = new String[] { "ability ", "clear ", "clone ", "connect ", "deop ",
            "difficulty ", "effect ", "enchant ", "experience ", "fill ", "gamemode ", "gamerule ", "give ", "help ",
            "kill ", "list ", "locate ", "mobevent ", "op ", "replaceitem ", "setblock", "setmaxplayers ",
            "setworldspawn ", "spawnpoint ", "spreadplayers ", "stopsound ", "teleport ", "testforblock ",
            "testforblocks ", "tickingarea ", "time ", "tp ", "weather ", "xp " };
    private static ArrayList<String> whiteMsgs = new ArrayList<String>() {
        {
            this.add("{\"text\":\"npcPhase\"}");
            this.add("npcPhase");
            this.add("2be4bffc-dbcb-45f8-8abe-7bbc4ca0baff");
            this.add("e62e0bbc-6014-4567-bf3b-440dc8461b10");
            this.add("e5c2e7bf-c365-4e28-bbc0-8b8808a9af22");
            this.add("89be8c5a-f261-45e7-8166-3487bbc6e91c");
            this.add("bbc4773a-36ce-4be7-aff0-91fa99b1bf6f");
            this.add("402bbc85-58c1-4baa-ba93-ba24479d5956");
            this.add("f4a12a2c-126b-40b1-8bbc-9930cec62142");
            this.add("144bd2b2-753e-4399-bbc6-917d0bc7f20f");
            this.add("3a4be989-1af8-4bbc-b2b1-aa9d9f9f1960");
        }
    };

    public static String doSdkFilter(String message, boolean nameLib) {
        Object[] lines = message.split("\n", -1);
        for (int i = 0; i < lines.length; ++i) {
            lines[i] = FilterHelper.doSdkFilterInner((String) lines[i], nameLib);
        }
        String result = Joiner.on((String) "\n").join(lines);
        return result;
    }

    private static String doSdkFilterInner(String message, boolean nameLib) {
        int code = nameLib ? Library.reviewName((String) message) : Library.reviewWord((String) message);
        Helper.debugLog((String) ("doSdkFilterInner:code:" + code + ":" + message));
        switch (code) {
            case 0: {
                return message;
            }
            case 1: {
                String replaceStr = new String(new char[message.length()]);
                replaceStr = replaceStr.replace('\u0000', '*');
                launcherList.add(message);
                if (!ThreadRunning) {
                    FilterHelper.doLauncherSdkFilterHandler();
                }
                return replaceStr;
            }
        }
        return FilterHelper.doLauncherSdkFilterInner(message);
    }

    public static void doLauncherSdkFilterHandler() {
        FilterWrapper.getSchduler().schedule(new Runnable() {
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
                } catch (Exception exception) {
                    // empty catch block
                }
                ThreadRunning = false;
            }
        }, 1L, TimeUnit.MILLISECONDS);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String doLauncherSdkFilterInner(String message) {
        Helper.debugLog((String) ("doLauncherSdkFilterInner message:" + message));
        if (message == null || message.isEmpty()) {
            return message;
        }
        String result = message;
        MessageRequest mrq = new MessageRequest();
        int wordId = globalWordId++;
        Object object = new Object();
        lockObjectMap.put(wordId, object);
        mrq.send(4608, new Object[] { wordId, message });
        Object object2 = lockObjectMap.get(wordId);
        synchronized (object2) {
            try {
                lockObjectMap.get(wordId).wait(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lockObjectMap.remove(wordId);
        if (filterWordMap.containsKey(wordId)) {
            result = filterWordMap.get(wordId);
            filterWordMap.remove(wordId);
        }
        return result;
    }

    public static void loadFilterReInAnotherThread() {
        LoadFilterReRunnable loadThread = new LoadFilterReRunnable();
        FilterWrapper.getSchduler().scheduleAtFixedRate((Runnable) loadThread, 0L, 20L, TimeUnit.MINUTES);
    }

    public static void loadFilterRe() {
        lastLoadFilterReTime = System.currentTimeMillis();
        String decryptionKey = GameState.filterkey;
        String filterPathPrefex = GameState.filterpath;
        logger.info("filterkey: " + decryptionKey);
        logger.info("filterpath: " + filterPathPrefex);
        ArrayList<String> md5List = FilterHelper.readFilterReMd5(filterPathPrefex + "/gamelib.txt", decryptionKey);
        ArrayList<String> reList = FilterHelper.readFilterReFromFile(filterPathPrefex + "/GAME_LIB.txt", decryptionKey,
                true, md5List.size() == 1 ? md5List.get(0) : null);
        logger.info("load filter re success!!! ");
        ArrayList<String> combined = new ArrayList<String>(reList);
        FilterHelper.setNamingFilterRE(combined);
        FilterHelper.setChatFilterRE(combined);
    }

    private static ArrayList<String> readFilterReMd5(String path, String decryptionKey) {
        ArrayList<String> md5List = new ArrayList<String>();
        if (null != decryptionKey && !decryptionKey.isEmpty()) {
            Path fileLocation = Paths.get(path, new String[0]);
            try {
                byte[] data = Files.readAllBytes(fileLocation);
                byte[] reBytes = AESHelper.Decrypt((byte[]) data, (String) decryptionKey);
                String reStr = new String(reBytes, StandardCharsets.UTF_8);
                String[] reStrArray = reStr.split("\n");
                md5List = new ArrayList<String>(Arrays.asList(reStrArray));
            } catch (Exception e) {
                logger.error("readFilterReMd5", (Throwable) e);
            }
        }
        return md5List;
    }

    private static ArrayList<String> readFilterReFromFile(String path, String decryptionKey, boolean checkMd5,
            String md5) {
        ArrayList<String> reList = new ArrayList<String>();
        if (!(null == decryptionKey || "" == decryptionKey || checkMd5 && null == md5)) {
            Path fileLocation = Paths.get(path, new String[0]);
            try {
                byte[] data = Files.readAllBytes(fileLocation);
                byte[] reBytes = AESHelper.Decrypt((byte[]) data, (String) decryptionKey);
                String reStr = new String(reBytes, StandardCharsets.UTF_8);
                String strMd5 = FilterHelper.getStringMd5(data);
                if (reStr.equals("") || !checkMd5 || !md5.equals(strMd5)) {
                    // empty if block
                }
                String[] reStrArray = reStr.split("\n");
                reList = new ArrayList<String>(Arrays.asList(reStrArray));
            } catch (Exception e) {
                logger.error("readFilterReFromFile", (Throwable) e);
            }
        }
        return reList;
    }

    private static String getStringMd5(byte[] original) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(original);
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void setNamingFilterRE(ArrayList<String> reList) {
        namingFilterRegularExpList = Collections.synchronizedList(reList);
    }

    public static void setChatFilterRE(ArrayList<String> reList) {
        chatFilterRegularExpList = Collections.synchronizedList(reList);
    }

    public static String filterMessage(String message, List<String> patternList) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        Object[] lines = message.split("\n", -1);
        for (int i = 0; i < lines.length; ++i) {
            lines[i] = FilterHelper.filterMessageInner((String) lines[i], patternList);
        }
        String result = Joiner.on((String) "\n").join(lines);
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String filterMessageInner(String message, List<String> patternList) {
        if (whiteMsgs.contains(message)) {
            return message;
        }
        String msgForFilter = message;
        int specialReplace = 32;
        char[] preFilterChars = message.toCharArray();
        StringBuilder specialFilterChars = new StringBuilder();
        for (int i = 0; i < preFilterChars.length; ++i) {
            if (preFilterChars[i] == '*') {
                specialFilterChars.append(' ');
                continue;
            }
            if (preFilterChars[i] == '\u00a7') {
                ++i;
                continue;
            }
            specialFilterChars.append(preFilterChars[i]);
        }
        String afterFilter = specialFilterChars.toString();
        List<String> list = patternList;
        synchronized (list) {
            block6: for (String re : patternList) {
                try {
                    Pattern pat = null;
                    pat = Pattern.compile(re);
                    Matcher matcher = pat.matcher(afterFilter);
                    while (matcher.find()) {
                        String temp = afterFilter.substring(matcher.start(), matcher.end());
                        String replaceStr = new String(new char[temp.length()]);
                        if (temp.length() == 0 || temp.equals(replaceStr))
                            continue block6;
                        afterFilter = String.join((CharSequence) "", afterFilter.substring(0, matcher.start()),
                                replaceStr, afterFilter.substring(matcher.end(), afterFilter.length()));
                        matcher = pat.matcher(afterFilter);
                    }
                } catch (Throwable e) {
                }
            }
        }
        afterFilter = afterFilter.replaceAll("\u0000", "*");
        char[] afterFilterChars = afterFilter.toCharArray();
        StringBuilder resultChars = new StringBuilder();
        int j = 0;
        for (int i = 0; i < preFilterChars.length && j < afterFilterChars.length; ++i) {
            if (preFilterChars[i] == '*') {
                resultChars.append('*');
                ++j;
                continue;
            }
            if (preFilterChars[i] == '\u00a7') {
                resultChars.append(preFilterChars[i]);
                if (i + 1 >= preFilterChars.length)
                    continue;
                resultChars.append(preFilterChars[i + 1]);
                ++i;
                continue;
            }
            resultChars.append(afterFilterChars[j]);
            ++j;
        }
        String result = resultChars.toString();
        if (!result.equals(message)) {
            FilterHelper.doLauncherSdkFilterInner(message);
        }
        return result;
    }

    public static String doNamingFilter(String message) {
        return FilterHelper.filterMessage(message, namingFilterRegularExpList);
    }

    public static Component filterChatComponentText(Component component, boolean useSdk, int logType) {
        return FilterHelper.filterChatComponentText(component, useSdk, logType, false);
    }

    public static Component filterChatComponentText(Component component, boolean useSdk, int logType, boolean nameLib) {
        String filterStr;
        if (component == null) {
            return component;
        }
        String str = component.getString();
        if (str.equals(filterStr = FilterWhiteListHelper.filter((String) str, (boolean) useSdk, (int) logType,
                (boolean) nameLib))) {
            return component;
        }
        if (component.getContents() instanceof TranslatableContents) {
            TranslatableContents chat = (TranslatableContents) component.getContents();
            Object[] args = chat.getArgs();
            for (int i = 0; i < args.length; ++i) {
                if (args[i] instanceof Component) {
                    args[i] = FilterHelper.filterChatComponentText((Component) args[i], useSdk, logType, nameLib);
                    continue;
                }
                if (!(args[i] instanceof String))
                    continue;
                args[i] = FilterWhiteListHelper.filter((String) ((String) args[i]), (boolean) useSdk, (int) logType,
                        (boolean) nameLib);
            }
            TranslatableContents translatableContents = new TranslatableContents(chat.getKey(), chat.getFallback(),
                    args);
            MutableComponent translatableComponent = MutableComponent.create((ComponentContents) translatableContents);
            translatableComponent.setStyle(component.getStyle());
            ArrayList sliblings = new ArrayList();
            for (Component subChat : component.getSiblings()) {
                translatableComponent.append(FilterHelper.filterChatComponentText(subChat, useSdk, logType, nameLib));
            }
            return translatableComponent;
        }
        if (component.getContents() instanceof PlainTextContents) {
            String text = ((PlainTextContents) component.getContents()).text();
            MutableComponent textComponent = str.equals(text) ? Component.literal((String) filterStr)
                    : Component.literal((String) FilterWhiteListHelper.filter((String) text, (boolean) useSdk,
                            (int) logType, (boolean) nameLib));
            textComponent.setStyle(component.getStyle());
            for (Component subChat : component.getSiblings()) {
                textComponent.append(FilterHelper.filterChatComponentText(subChat, useSdk, logType, nameLib));
            }
            return textComponent;
        }
        return component;
    }

    public static boolean isWhiteListCmd(String text) {
        String cmd = text;
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1).trim();
            for (String white : whiteCmds) {
                if (!cmd.startsWith(white))
                    continue;
                return true;
            }
        }
        return false;
    }

    public static void filterItemStack(ItemStack itemstack, HolderLookup.Provider access) {
        FilterHelper.filterItemStack(itemstack, access, true);
    }

    public static void filterItemStack(ItemStack itemstack, HolderLookup.Provider access, boolean enableFilterLog) {
        Item item;
        if (itemstack == null) {
            return;
        }
        if (itemstack.has(DataComponents.CUSTOM_NAME)) {
            int logType = enableFilterLog ? 4 : 0;
            Component name = FilterHelper.filterChatComponentText(itemstack.getHoverName(), false, logType);
            itemstack.set(DataComponents.CUSTOM_NAME, (Object) name);
        }
        if ((item = itemstack.getItem()) instanceof WrittenBookItem) {
            WrittenBookContent content = (WrittenBookContent) itemstack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (content == null) {
                return;
            }
            int logType = enableFilterLog ? 3 : 0;
            String title = (String) content.title().raw();
            title = FilterWhiteListHelper.filter((String) title, (boolean) false, (int) logType);
            String author = content.author();
            author = FilterWhiteListHelper.filter((String) author, (boolean) false, (int) logType);
            ArrayList<Filterable> pages = new ArrayList<Filterable>();
            for (int i = 0; i < content.pages().size(); ++i) {
                Component c = (Component) ((Filterable) content.pages().get(i)).raw();
                c = FilterHelper.filterChatComponentText(c, false, logType);
                pages.add(new Filterable((Object) c, ((Filterable) content.pages().get(i)).filtered()));
            }
            WrittenBookContent newContent = new WrittenBookContent(
                    new Filterable((Object) title, content.title().filtered()), author, content.generation(), pages,
                    content.resolved());
            itemstack.set(DataComponents.WRITTEN_BOOK_CONTENT, (Object) newContent);
        }
        if (item instanceof BlockItem) {
            if (!(((BlockItem) item).getBlock() instanceof ShulkerBoxBlock)) {
                return;
            }
            ItemContainerContents itemCompound = (ItemContainerContents) itemstack.get(DataComponents.CONTAINER);
            if (itemCompound != null) {
                itemCompound.nonEmptyItems().forEach(x -> FilterHelper.filterItemStack(x, access));
                itemstack.set(DataComponents.CONTAINER, (Object) itemCompound);
            }
        }
    }
}