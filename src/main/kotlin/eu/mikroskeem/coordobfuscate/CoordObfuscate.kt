/*
 * This file is part of project CoordObfuscate, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 Mark Vainomaa <mikroskeem@mikroskeem.eu>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package eu.mikroskeem.coordobfuscate

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority.LOWEST
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.reflect.StructureModifier
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGH
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.WeakHashMap
import java.util.concurrent.ThreadLocalRandom

/**
 * A coordinate obfuscation plugin
 *
 * @author Mark Vainomaa
 */
class Coordobfuscate: JavaPlugin(), Listener {
    // Player-offset map
    internal val players: MutableMap<Player, Int> = WeakHashMap()
    private val randomOffset: Int get() = ThreadLocalRandom.current().nextInt(-1000, 1000)

    private lateinit var adapter: CoordinateObfuscatingAdapter

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)

        val params = PacketAdapter.AdapterParameteters()
                .plugin(this)
                .listenerPriority(LOWEST)

        adapter = CoordinateObfuscatingAdapter(params)
        ProtocolLibrary.getProtocolManager().addPacketListener(adapter)
    }

    override fun onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListener(adapter)
    }

    @EventHandler(priority = HIGH, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if(!event.player.hasPermission("coordobfuscate.ignore"))
            players.put(event.player, randomOffset)
    }
}

private val coordobfuscate by lazy { JavaPlugin.getPlugin(Coordobfuscate::class.java) }

class CoordinateObfuscatingAdapter(params: AdapterParameteters): PacketAdapter(params) {
    override fun onPacketSending(event: PacketEvent) {
        // Don't intercept if player is in ignored players set
        if(coordobfuscate.players.contains(event.player))
            return

        // Get player specific offset
        val offset = coordobfuscate.players[event.player]!!.toDouble()

        // Clone packet
        val packet = event.packet.deepClone()

        when(event.packetType) {
            PacketType.Play.Server.SPAWN_ENTITY,
            PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB,
            PacketType.Play.Server.SPAWN_ENTITY_WEATHER,
            PacketType.Play.Server.SPAWN_ENTITY_LIVING,
            PacketType.Play.Server.NAMED_ENTITY_SPAWN -> {
                packet.doubles.run {
                    add(0, offset) // X
                    add(2, offset) // Z
                }
            }
            PacketType.Play.Server.SPAWN_ENTITY_PAINTING -> {
                /*packet.blockPositionModifier.run {
                    getField(0)
                }*/
                // TODO - 0x04
            }
            PacketType.Play.Server.BLOCK_BREAK_ANIMATION -> {
                // TODO - 0x08
            }
            PacketType.Play.Server.TILE_ENTITY_DATA -> {
                // TODO - 0x09
            }
            PacketType.Play.Server.MULTI_BLOCK_CHANGE -> {
                // TODO - 0x10
            }
            PacketType.Play.Server.BLOCK_ACTION -> {
                // TODO - 0x0A
            }
            PacketType.Play.Server.BLOCK_CHANGE -> {
                // TODO - 0x0B
            }
            PacketType.Play.Server.CUSTOM_SOUND_EFFECT -> {
                // TODO - 0x19
            }
            PacketType.Play.Server.EXPLOSION -> {
                // TODO - 0x1C
                packet.float.run {
                    add(0, offset.toFloat()) // X
                    add(2, offset.toFloat()) // Z
                }
            }
            PacketType.Play.Server.UNLOAD_CHUNK -> {
                // TODO - 0x1D
            }
            PacketType.Play.Server.MAP_CHUNK -> {
                // TODO - 0x20
            }
            PacketType.Play.Server.WORLD_EVENT -> {
                // TODO - 0x21
            }
            PacketType.Play.Server.WORLD_PARTICLES -> {
                // TODO - 0x22
                packet.float.run {
                    add(0, offset.toFloat()) // X
                    add(2, offset.toFloat()) // Z
                }
            }
            PacketType.Play.Server.MAP -> {
                // TODO - 0x24
            }
            PacketType.Play.Server.REL_ENTITY_MOVE,
            PacketType.Play.Server.REL_ENTITY_MOVE_LOOK -> {
                // TODO - 0x26 & 0x27
                packet.shorts.run {
                    val x = read(0) / 128
                    val z = read(2) / 128
                }
            }
            PacketType.Play.Server.VEHICLE_MOVE -> {
                // TODO - 0x29
            }
            PacketType.Play.Server.OPEN_SIGN_EDITOR -> {
                // TODO - 0x2A
            }
            PacketType.Play.Server.POSITION -> {
                // TODO - 0x2F
            }
            PacketType.Play.Server.BED -> {
                // TODO - 0x30
            }
            PacketType.Play.Server.WORLD_BORDER -> {
                // TODO - 0x38
            }
            PacketType.Play.Server.SPAWN_POSITION -> {
                // TODO - 0x46
            }
            PacketType.Play.Server.NAMED_SOUND_EFFECT -> {
                // TODO - 0x49
            }
            PacketType.Play.Server.ENTITY_TELEPORT -> {
                // TODO - 0x4C
                packet.doubles.run {
                    add(0, offset) // X
                    add(2, offset) // Z
                }
            }
        }

        // Replace packet
        event.packet = packet
    }

    override fun onPacketReceiving(event: PacketEvent) {
        // Don't intercept if player is in ignored players set
        if(coordobfuscate.players.contains(event.player))
            return

        // Get player specific offset
        val offset = coordobfuscate.players[event.player]!!.toDouble()

        // Clone packet
        val packet = event.packet.deepClone()

        when(event.packetType) {
            PacketType.Play.Client.USE_ENTITY -> {
                // TODO - 0x0A
            }
            PacketType.Play.Client.POSITION,
            PacketType.Play.Client.POSITION_LOOK -> {
                // TODO - 0x0D & 0x0E
                packet.doubles.run {
                    add(0, offset) // X
                    add(2, offset) // Z
                }
            }
            PacketType.Play.Client.VEHICLE_MOVE -> {
                // TODO - 0x10
                packet.doubles.run {
                    add(0, offset) // X
                    add(2, offset) // Z
                }
            }
            PacketType.Play.Client.BLOCK_DIG -> {
                // TODO - 0x14
            }
            PacketType.Play.Client.UPDATE_SIGN -> {
                // TODO - 0x1C
            }
            PacketType.Play.Client.BLOCK_PLACE -> {
                // TODO - 0x1F
            }

        }
    }
}

private fun StructureModifier<Double>.add(fieldIndex: Int, value: Double): Unit {
    write(fieldIndex, read(fieldIndex) + value)
}

private fun StructureModifier<Float>.add(fieldIndex: Int, value: Float): Unit {
    write(fieldIndex, read(fieldIndex) + value)
}