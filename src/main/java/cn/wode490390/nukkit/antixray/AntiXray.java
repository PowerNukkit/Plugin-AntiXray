/**
 * wodeTeam is pleased to support the open source community by making AntiXray available.
 * 
 * Copyright (C) 2019  Woder
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0>.
 */

package cn.wode490390.nukkit.antixray;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockUpdateEvent;
import cn.nukkit.event.level.ChunkUnloadEvent;
import cn.nukkit.event.level.LevelUnloadEvent;
import cn.nukkit.event.player.PlayerChunkRequestEvent;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.stream.FastByteArrayOutputStream;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.openhft.hashing.LongHashFunction;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class AntiXray extends PluginBase implements Listener {

    public final static String PERMISSION_WHITELIST = "antixray.whitelist";

    LongHashFunction XX64;

    File CACHE_DIR;

    int height = 4;
    boolean mode = true;
    boolean memoryCache = true;
    boolean localCache = true;
    int fake_o = Block.STONE;
    int fake_n = Block.NETHERRACK;
    private List<String> worlds;

    boolean[] ore = new boolean[256];
    boolean[] filter = new boolean[256];

    private final Map<Level, WorldHandler> handlers = Maps.newHashMap();

    @Override
    public void onEnable() {
        try {
            new MetricsLite(this, 5123);
        } catch (Throwable ignore) {

        }

        this.saveDefaultConfig();
        Config config = this.getConfig();

        String node = "scan-chunk-height-limit";
        if (config.exists(node)) {
            try {
                this.height = config.getInt(node, this.height);
            } catch (Exception e) {
                this.logLoadException(node, e);
            }
        } else { //compatible
            node = "scan-height-limit";
            try {
                this.height = config.getInt(node, 64) >> 4;
            } catch (Exception e) {
                this.logLoadException("scan-chunk-height-limit", e);
            }
        }
        this.height = Math.max(Math.min(this.height, 15), 1);

        node = "memory-cache";
        if (config.exists(node)) {
            try {
                this.memoryCache = config.getBoolean(node, this.memoryCache);
            } catch (Exception e) {
                this.logLoadException(node, e);
            }
        } else { //compatible
            node = "cache-chunks";
            try {
                this.memoryCache = config.getBoolean(node, this.memoryCache);
            } catch (Exception e) {
                this.logLoadException("memory-cache", e);
            }
        }
        node = "local-cache";
        try {
            this.localCache = config.getBoolean(node, this.localCache);
        } catch (Exception e) {
            this.logLoadException(node, e);
        }
        node = "obfuscator-mode";
        try {
            this.mode = config.getBoolean(node, this.mode);
        } catch (Exception e) {
            this.logLoadException(node, e);
        }
        node = "overworld-fake-block";
        try {
            this.fake_o = config.getInt(node, this.fake_o) & 0xff;
            GlobalBlockPalette.getOrCreateRuntimeId(this.fake_o, 0);
        } catch (Exception e) {
            this.fake_n = Block.STONE;
            this.logLoadException(node, e);
        }
        node = "nether-fake-block";
        try {
            this.fake_n = config.getInt(node, this.fake_n) & 0xff;
            GlobalBlockPalette.getOrCreateRuntimeId(this.fake_n, 0);
        } catch (Exception e) {
            this.fake_n = Block.NETHERRACK;
            this.logLoadException(node, e);
        }
        node = "protect-worlds";
        try {
            this.worlds = config.getStringList(node);
        } catch (Exception e) {
            this.logLoadException(node, e);
        }
        node = "ores";
        List<Integer> ores = null;
        try {
            ores = config.getIntegerList(node);
        } catch (Exception e) {
            this.logLoadException(node, e);
        }

        if (this.worlds != null && ores != null && !this.worlds.isEmpty() && !ores.isEmpty()) {
            node = "filters";
            List<Integer> filters;
            try {
                filters = config.getIntegerList(node);
            } catch (Exception e) {
                filters = Collections.emptyList();
                this.logLoadException(node, e);
            }

            for (int id : ores) {
                this.ore[id] = true;
            }
            for (int id : filters) {
                this.filter[id] = true;
            }

            if (this.localCache) {
                XX64 = LongHashFunction.xx();
                CACHE_DIR = new File(new File(this.getDataFolder(), "cache"), Long.toHexString(XX64.hashBytes(GlobalBlockPalette.BLOCK_PALETTE)));
                if (!CACHE_DIR.exists()) {
                    CACHE_DIR.mkdirs();
                } else if (!CACHE_DIR.isDirectory()) {
                    CACHE_DIR.delete();
                    CACHE_DIR.mkdirs();
                }
                if (!CACHE_DIR.exists() || !CACHE_DIR.isDirectory()) {
                    this.localCache = false;
                    this.getLogger().warning("Failed to initialize cache! Disabled cache.");
                }
            }
            this.getServer().getPluginManager().registerEvents(this, this);
            if (this.memoryCache) {
                this.getServer().getPluginManager().registerEvents(new CleanerListener(), this);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChunkRequest(PlayerChunkRequestEvent event) {
        Player player = event.getPlayer();
        Level level = player.getLevel();
        if (!player.hasPermission(PERMISSION_WHITELIST) && this.worlds.contains(level.getName()) && player.getLoaderId() > 0) {
            event.setCancelled();
            WorldHandler handler = this.handlers.get(level);
            if (handler == null) {
                handler = new WorldHandler(this, level);
                handler.start();
                this.handlers.put(level, handler);
            }
            handler.requestChunk(event.getChunkX(), event.getChunkZ(), player);
        }
    }

    @EventHandler
    //TODO: Use BlockBreakEvent instead of BlockUpdateEvent
    public void onBlockUpdate(BlockUpdateEvent event) {
        Position position = event.getBlock();
        Level level = position.getLevel();
        if (this.worlds.contains(level.getName())) {
            List<UpdateBlockPacket> packets = Lists.newArrayList();
            for (Vector3 vector : new Vector3[]{
                    position.add(1),
                    position.add(-1),
                    position.add(0, 1),
                    position.add(0, -1),
                    position.add(0, 0, 1),
                    position.add(0, 0, -1)
            }) {
                int y = vector.getFloorY();
                if (y > 255 || y < 0) {
                    continue;
                }
                int x = vector.getFloorX();
                int z = vector.getFloorZ();
                UpdateBlockPacket packet = new UpdateBlockPacket();
                try {
                    packet.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(level.getFullBlock(x, y, z));
                } catch (Exception tryAgain) {
                    try {
                        packet.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(level.getBlockIdAt(x, y, z), 0);
                    } catch (Exception ex) {
                        continue;
                    }
                }
                packet.x = x;
                packet.y = y;
                packet.z = z;
                packet.flags = UpdateBlockPacket.FLAG_ALL_PRIORITY;
                packets.add(packet);
            }
            if (packets.size() > 0) {
                /*Set<Player> players = Sets.newHashSet();
                level.getChunkPlayers(position.getChunkX(), position.getChunkZ()).values().stream()
                        .filter(player -> !player.hasPermission(PERMISSION_WHITELIST))
                        .forEach(player -> players.add(player));
                this.getServer().batchPackets(players.toArray(new Player[0]), packets.toArray(new UpdateBlockPacket[0]));*/
                this.getServer().batchPackets(level.getChunkPlayers(position.getChunkX(), position.getChunkZ()).values().toArray(new Player[0]), packets.toArray(new UpdateBlockPacket[0]));
            }
        }
    }

    @EventHandler
    public void onLevelUnload(LevelUnloadEvent event) {
        Level level = event.getLevel();
        WorldHandler handler = this.handlers.get(level);
        if (handler != null) {
            handler.interrupt();
            this.handlers.remove(level);
        }
    }

    long getCacheHash(byte[] buffer) {
        return XX64.hashBytes(buffer);
    }

    void createCache(long hash, byte[] buffer) {
        this.getServer().getScheduler().scheduleAsyncTask(this, new CacheWriteTask(hash, buffer));
    }

    byte[] readCache(long hash) {
        File file = new File(CACHE_DIR, String.valueOf(hash));
        if (file.exists() && file.isFile() && file.length() > 0) {
            try {
                try (InputStream inputStream = new InflaterInputStream(new BufferedInputStream(new FileInputStream(file)), new Inflater(true)); FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream(1024)) {
                    byte[] temp = new byte[1024];
                    int length;
                    while ((length = inputStream.read(temp)) != -1) {
                        outputStream.write(temp, 0, length);
                    }
                    return outputStream.toByteArray();
                }
            } catch (IOException e) {
                this.getLogger().debug("Unable to read cache file", e);
            }
        }
        return null;
    }

    private void logLoadException(String node, Exception ex) {
        this.getLogger().alert("Failed to get the configuration '" + node + "'. Use the default value.", ex);
    }

    private static boolean deleteFolder(File file) {
        if (file.isDirectory()) {
            for (String children : file.list()) {
                boolean success = deleteFolder(new File(file, children));
                if (!success) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    public class CleanerListener implements Listener {

        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent event) {
            WorldHandler handler = AntiXray.this.handlers.get(event.getLevel());
            if (handler != null) {
                FullChunk chunk = event.getChunk();
                handler.clearCache(chunk.getX(), chunk.getZ());
            }
        }
    }

    private class CacheWriteTask extends AsyncTask {

        private final long hash;
        private final byte[] buffer;

        private CacheWriteTask(long hash, byte[] buffer) {
            this.hash = hash;
            this.buffer = buffer;
        }

        @Override
        public void onRun() {
            try {
                File file = new File(CACHE_DIR, String.valueOf(hash));
                if (!file.exists()) {
                    file.createNewFile();
                } else if (file.isDirectory()) {
                    deleteFolder(file);
                    file.createNewFile();
                }

                try (DeflaterOutputStream outputStream = new DeflaterOutputStream(new BufferedOutputStream(new FileOutputStream(file)), new Deflater(Deflater.BEST_COMPRESSION, true)); InputStream inputStream = new ByteArrayInputStream(this.buffer)) {
                    byte[] temp = new byte[1024];
                    int length;
                    while ((length = inputStream.read(temp)) != -1) {
                        outputStream.write(temp, 0, length);
                    }
                    outputStream.finish();
                }
            } catch (IOException e) {
                AntiXray.this.getLogger().debug("Unable to save cache file", e);
            }
        }
    }
}
