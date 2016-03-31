package com.ginger_walnut.sqsmoothcraft.ship;

import java.util.List;
import java.util.ArrayList;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ginger_walnut.sqsmoothcraft.SQSmoothCraft;

public class ShipDetection {

	static List<Block> attemptedBlocks = new ArrayList<Block>();
	static List<Block> attemptableBlocksFrom = new ArrayList<Block>();
	static List<Block> detectedBlocks = new ArrayList<Block>();
	
	public static boolean detectShip(Block startingBlock, Player player) {
		
		player.sendMessage(ChatColor.GREEN + "Registering Your Ship...");
		
		attemptedBlocks.clear();
		attemptableBlocksFrom.clear();
		detectedBlocks.clear();
		
		attemptedBlocks.add(startingBlock);
		detectSideBlocks(startingBlock);
		
		boolean detectable = true;
		boolean failed = false;
		
		float fuel = 0.0f;
		
		String failureReason = "";
		
		if (!SQSmoothCraft.shipBlockTypes.contains(startingBlock.getType())) {
			
			failed = true;
			
			failureReason = "you must stand on a detectable block";
			
		}
		
		if (attemptableBlocksFrom.size() > 0) {
			
			do {

				detectSideBlocks(attemptableBlocksFrom.get(0));
				attemptableBlocksFrom.remove(0);
				
				if (attemptableBlocksFrom.size() <= 0) {
					
					detectable = false;
					
				}
				
				if (detectedBlocks.size() > 100) {
					
					detectable = false;
					failed = true;
					
					failureReason = "more blocks than the limit of 100";
					
				}
				
			} while (detectable);
			
		}
		
		if (detectedBlocks.size() < 9) {
			
			failed = true;
			
			failureReason = "less blocks than the minimum of 10";
			
		}
		
		if (!failed) {
			
			List<ShipBlock> blockList = new ArrayList<ShipBlock>();
			
			Location location = player.getLocation();
			location.setY(location.getY() - 1);
			
			ItemStack mainItemStack = new ItemStack(startingBlock.getType());
			mainItemStack.setDurability(startingBlock.getData());
			
			blockList.add(new ShipBlock(location, new ShipLocation(0, 0, 0, null), mainItemStack));
			
			startingBlock.setType(Material.AIR);

			for (Block block : detectedBlocks) {
				
				ItemStack itemStack = new ItemStack(block.getType());
				itemStack.setDurability(block.getData());
				
				if (block.getType().equals(Material.DROPPER)) {
					
					Dropper dropper = (Dropper) block.getState();
					
					Inventory inventory = dropper.getInventory();
					
					for (int i = 0; i < inventory.getContents().length; i ++) {
						
						if (inventory.getContents()[i] != null) {
							
							if (inventory.getContents()[i].getType().equals(Material.COAL)) {
								
								fuel = fuel + (SQSmoothCraft.config.getInt("utilities.reactor.fuel per coal") * inventory.getContents()[i].getAmount());

							} else {
								
								block.getLocation().getWorld().dropItem(block.getLocation(), inventory.getContents()[i]);
								
							}

						}
						
					}
					
					inventory.setContents(new ItemStack[9]);
					
				}
				
				float yaw = player.getLocation().getYaw();
				
				if (yaw < 0) {
					
					yaw = yaw * -1;
						
					yaw = 360 - yaw;
						
				}
				
				if (yaw >= 315 || yaw < 45) {
					
					blockList.add(new ShipBlock(new ShipLocation(block.getX() - startingBlock.getX(), block.getY() - startingBlock.getY(), block.getZ() - startingBlock.getZ(), blockList.get(0)), itemStack, blockList.get(0)));
					
				} else if (yaw >= 225 && yaw < 315) {
					
					blockList.add(new ShipBlock(new ShipLocation(startingBlock.getZ() - block.getZ(), block.getY() - startingBlock.getY(), block.getX() - startingBlock.getX(), blockList.get(0)), itemStack, blockList.get(0)));
					
				} else if (yaw >= 135 && yaw < 225) {

					blockList.add(new ShipBlock(new ShipLocation(startingBlock.getX() - block.getX(), block.getY() - startingBlock.getY(), startingBlock.getZ() - block.getZ(), blockList.get(0)), itemStack, blockList.get(0)));
					
				} else if (yaw >= 45 && yaw < 135) {

					blockList.add(new ShipBlock(new ShipLocation(block.getZ() - startingBlock.getZ(), block.getY() - startingBlock.getY(), startingBlock.getX() - block.getX(), blockList.get(0)), itemStack, blockList.get(0)));
					
				}
				
				block.setType(Material.AIR);
				
			}
			
			float maxSpeed = 0f;
			
			double averageWeight = 0;
			
			for (ShipBlock block : blockList) {
				
				averageWeight = averageWeight + block.weight;
				
			}
			
			averageWeight = averageWeight / blockList.size();
			
			maxSpeed = (float) (1 / averageWeight);
			
			if (maxSpeed > 1) {
				
				maxSpeed = 1;
				
			}
			
			new Ship(blockList, blockList.get(0), player, maxSpeed, maxSpeed * 5, maxSpeed / 20, fuel);
			
		}
		else {
			
			player.sendMessage(ChatColor.RED + "Registration Failed - " + failureReason);
			
		}
		
		return failed;
		
	}
	
	private static void detectSideBlocks(Block block) {
		
		World world = block.getWorld();
		
		Location location = block.getLocation();
		
		location.add(1, 0, 0);
		
		Block sideBlock = world.getBlockAt(location);
		
		if (!attemptedBlocks.contains(sideBlock)) {
			
			if (SQSmoothCraft.shipBlockTypes.contains(sideBlock.getType())) {
				
				detectedBlocks.add(sideBlock);		
				attemptableBlocksFrom.add(sideBlock);
				attemptedBlocks.add(sideBlock);
				
			}
			
		}
		
		location = block.getLocation();
		location.add(-1, 0, 0);
		
		sideBlock = world.getBlockAt(location);
		
		if (!attemptedBlocks.contains(sideBlock)) {
			
			if (SQSmoothCraft.shipBlockTypes.contains(sideBlock.getType())) {
				
				detectedBlocks.add(sideBlock);		
				attemptableBlocksFrom.add(sideBlock);
				attemptedBlocks.add(sideBlock);
				
			}
			
		}
		
		location = block.getLocation();
		location.add(0, 1, 0);
		
		sideBlock = world.getBlockAt(location);
		
		if (!attemptedBlocks.contains(sideBlock)) {
			
			if (SQSmoothCraft.shipBlockTypes.contains(sideBlock.getType())) {
				
				detectedBlocks.add(sideBlock);		
				attemptableBlocksFrom.add(sideBlock);
				attemptedBlocks.add(sideBlock);
				
			}
			
		}
		
		location = block.getLocation();
		location.add(0, -1, 0);
		
		sideBlock = world.getBlockAt(location);
		
		if (!attemptedBlocks.contains(sideBlock)) {
			
			if (SQSmoothCraft.shipBlockTypes.contains(sideBlock.getType())) {
				
				detectedBlocks.add(sideBlock);		
				attemptableBlocksFrom.add(sideBlock);
				attemptedBlocks.add(sideBlock);
				
			}
			
		}
		
		location = block.getLocation();
		location.add(0, 0, 1);

		sideBlock = world.getBlockAt(location);
		
		if (!attemptedBlocks.contains(sideBlock)) {
			
			if (SQSmoothCraft.shipBlockTypes.contains(sideBlock.getType())) {
				
				detectedBlocks.add(sideBlock);		
				attemptableBlocksFrom.add(sideBlock);
				attemptedBlocks.add(sideBlock);
				
			}
			
		}
		
		location = block.getLocation();
		location.add(0, 0, -1);
		
		sideBlock = world.getBlockAt(location);
		
		if (!attemptedBlocks.contains(sideBlock)) {
			
			if (SQSmoothCraft.shipBlockTypes.contains(sideBlock.getType())) {
				
				detectedBlocks.add(sideBlock);		
				attemptableBlocksFrom.add(sideBlock);
				attemptedBlocks.add(sideBlock);
				
			}
			
		}
		
	}
	
}