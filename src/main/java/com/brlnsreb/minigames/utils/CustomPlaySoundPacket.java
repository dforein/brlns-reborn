package com.brlnsreb.minigames.utils;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PacketHandler;
import cn.nukkit.Player;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.connection.util.HandleByteBuf;

public class CustomPlaySoundPacket extends DataPacket {
    
   public String name;
   public double x;
   public double y;
   public double z;
   public float volume = 1.0f;
   public float pitch = 1.0f;

   @Override
   public int pid() {
      return 86;
   }

   @Override
   public void decode(HandleByteBuf byteBuf) {}

   @Override
   public void encode(HandleByteBuf byteBuf) {
      byteBuf.writeString(this.name);
      
      byteBuf.writeBlockVector3(
         (int) Math.floor(this.x * 8), 
         (int) Math.floor(this.y * 8), 
         (int) Math.floor(this.z * 8)
      );
      
      byteBuf.writeFloatLE(this.volume);
      byteBuf.writeFloatLE(this.pitch);
   }

   @Override
   public void handle(PacketHandler handler) {}

   public void sendDirectionalSoundTo(Player player, String soundName) {
      this.name = soundName;
      Vector3 direction = player.getDirectionVector();

      this.x = player.x + direction.x;
      this.y = player.y + player.getEyeHeight() + direction.y;
      this.z = player.z + direction.z;

      player.dataPacket(this);
   }

   public void sendTo(Player player, String soundName, float pitch, float volume) {
      this.pitch = pitch;
      this.volume = volume;

      sendTo(player, soundName);
   }

   public void sendTo(Player player, String soundName, float pitch) {
      this.pitch = pitch;
      
      sendTo(player, soundName);
   }

   public void sendTo(Player player, String soundName) {
      this.name = soundName;

      this.x = player.x;
      this.y = player.y;
      this.z = player.z;

      player.dataPacket(this);
   }

}