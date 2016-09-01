package com.Ben12345rocks.VotingPlugin.Commands.Executers;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.Ben12345rocks.AdvancedCore.Utils;
import com.Ben12345rocks.AdvancedCore.Objects.CommandHandler;
import com.Ben12345rocks.AdvancedCore.Util.Inventory.BInventory;
import com.Ben12345rocks.AdvancedCore.Util.Inventory.BInventoryButton;
import com.Ben12345rocks.AdvancedCore.Util.Request.InputListener;
import com.Ben12345rocks.AdvancedCore.Util.Request.RequestManager;
import com.Ben12345rocks.AdvancedCore.Util.Updater.Updater;
import com.Ben12345rocks.VotingPlugin.Main;
import com.Ben12345rocks.VotingPlugin.Bungee.BungeeVote;
import com.Ben12345rocks.VotingPlugin.Commands.Commands;
import com.Ben12345rocks.VotingPlugin.Config.Config;
import com.Ben12345rocks.VotingPlugin.Config.ConfigFormat;
import com.Ben12345rocks.VotingPlugin.Config.ConfigOtherRewards;
import com.Ben12345rocks.VotingPlugin.Config.ConfigRewards;
import com.Ben12345rocks.VotingPlugin.Config.ConfigVoteSites;
import com.Ben12345rocks.VotingPlugin.Data.Data;
import com.Ben12345rocks.VotingPlugin.Data.ServerData;
import com.Ben12345rocks.VotingPlugin.Events.VotiferEvent;
import com.Ben12345rocks.VotingPlugin.Objects.Reward;
import com.Ben12345rocks.VotingPlugin.Objects.User;
import com.Ben12345rocks.VotingPlugin.Objects.VoteSite;
import com.Ben12345rocks.VotingPlugin.TopVoter.TopVoter;
import com.vexsoftware.votifier.model.Vote;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandAdminVote.
 */
public class CommandAdminVote implements CommandExecutor {

	/** The instance. */
	private static CommandAdminVote instance = new CommandAdminVote();

	/**
	 * Gets the single instance of CommandAdminVote.
	 *
	 * @return single instance of CommandAdminVote
	 */
	public static CommandAdminVote getInstance() {
		return instance;
	}

	/** The bonus reward. */
	ConfigOtherRewards bonusReward = ConfigOtherRewards.getInstance();

	/** The config. */
	Config config = Config.getInstance();

	/** The format. */
	ConfigFormat format = ConfigFormat.getInstance();

	/** The plugin. */
	private Main plugin = Main.plugin;

	/** The vote sites. */
	ConfigVoteSites voteSites = ConfigVoteSites.getInstance();

	/**
	 * Instantiates a new command admin vote.
	 */
	private CommandAdminVote() {
	}

	/**
	 * Instantiates a new command admin vote.
	 *
	 * @param plugin
	 *            the plugin
	 */
	public CommandAdminVote(Main plugin) {
		this.plugin = plugin;
	}

	/**
	 * Check update.
	 *
	 * @param sender
	 *            the sender
	 */
	public void checkUpdate(CommandSender sender) {
		plugin.updater = new Updater(plugin, 15358, false);
		final Updater.UpdateResult result = plugin.updater.getResult();
		switch (result) {
		case FAIL_SPIGOT: {
			sender.sendMessage(Utils.getInstance().colorize(
					"&cFailed to check for update for &c&l" + plugin.getName()
							+ "&c!"));
			break;
		}
		case NO_UPDATE: {
			sender.sendMessage(Utils.getInstance().colorize(
					"&c&l" + plugin.getName()
							+ " &cis up to date! Version: &c&l"
							+ plugin.updater.getVersion()));
			break;
		}
		case UPDATE_AVAILABLE: {
			sender.sendMessage(Utils.getInstance().colorize(
					"&c&l" + plugin.getName()
							+ " &chas an update available! Your Version: &c&l"
							+ plugin.getDescription().getVersion()
							+ " &cNew Version: &c&l"
							+ plugin.updater.getVersion()));
			break;
		}
		default: {
			break;
		}
		}
	}

	/**
	 * Open admin GUI.
	 *
	 * @param player
	 *            the player
	 */
	public void openAdminGUI(Player player) {
		BInventory inv = new BInventory("AdminGUI");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("&cOnly enabled sites are listed in this section");
		lore.add("&cMiddle Click to create");
		inv.addButton(0, new BInventoryButton("&cVoteSites", Utils
				.getInstance().convertArray(lore),
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					if (event.getClick().equals(ClickType.MIDDLE)) {
						player.closeInventory();
						new RequestManager(
								player,
								new User(player).getInputMethod(),
								new InputListener() {

									@Override
									public void onInput(
											Player player,
											String input) {

										ConfigVoteSites.getInstance()
												.generateVoteSite(input);
										ConfigVoteSites.getInstance()
												.setEnabled(input, true);
										player
												.sendMessage("Generated site");
										plugin.reload();

									}
								}

								,
								"Type value in chat to send, cancel by typing cancel",
								"");
					} else {
						openAdminGUIVoteSites(player);
					}
				}
			}
		});
		lore = new ArrayList<String>();
		inv.addButton(1, new BInventoryButton("&cRewards", Utils.getInstance()
				.convertArray(lore), new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					openAdminGUIRewards(player);
				}
			}
		});
		lore = new ArrayList<String>();
		inv.addButton(2, new BInventoryButton("&cConfig", Utils.getInstance()
				.convertArray(lore), new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					openAdminGUIConfig(player);
				}
			}
		});

		lore = new ArrayList<String>();
		lore.add("Middle click to enter offline/specific player");
		inv.addButton(3, new BInventoryButton("&cPlayers", Utils.getInstance()
				.convertArray(lore), new ItemStack(Material.SKULL_ITEM, 1,
				(short) 3)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					if (event.getClick().equals(ClickType.MIDDLE)) {
						User user = new User(player);
						new RequestManager(
								player,
								user.getInputMethod(),
								new InputListener() {

									@Override
									public void onInput(
											Player player,
											String input) {

										openAdminGUIPlayers(player, input);

									}
								}

								,
								"Type value in chat to send, cancel by typing cancel",
								"");

					} else {
						openAdminGUIPlayers(player, "");
					}
				}
			}

		});
		inv.openInventory(player);
	}

	/**
	 * Open admin GUI players.
	 *
	 * @param player
	 *            the player
	 * @param string
	 *            the string
	 */
	public void openAdminGUIPlayers(Player player, String string) {

		if (string.equals("")) {
			BInventory inv = new BInventory("Players");
			int count = 0;
			for (Player players : Bukkit.getOnlinePlayers()) {
				inv.addButton(
						count,
						new BInventoryButton(players.getName(), new String[0],
								Utils.getInstance().setSkullOwner(
										new ItemStack(Material.SKULL_ITEM, 1,
												(short) 3), players.getName())) {

							@Override
							public void onClick(InventoryClickEvent event) {
								if (event.getWhoClicked() instanceof Player) {
									Player player = (Player) event
											.getWhoClicked();
									player.closeInventory();
									if (event.getCurrentItem() != null) {
										String playerName = event
												.getCurrentItem().getItemMeta()
												.getDisplayName();
										openAdminGUIPlayers(player, playerName);
									}
								}

							}
						});
				count++;
			}
			inv.openInventory(player);
		} else {
			BInventory inv = new BInventory("Player: " + string);
			inv.addButton(0, new BInventoryButton("SetPoints", new String[0],
					new ItemStack(Material.STONE)) {

				@Override
				public void onClick(InventoryClickEvent event) {
					if (event.getWhoClicked() instanceof Player) {
						Player player = (Player) event.getWhoClicked();
						String playerName = event.getInventory().getTitle()
								.split(" ")[1];
						player.closeInventory();
						User user = new User(player);
						new RequestManager(
								player,
								user.getInputMethod(),
								new InputListener() {

									@Override
									public void onInput(
											Player player,
											String input) {
										if (Utils.getInstance().isInt(input)) {
											User user = new User(playerName);
											user.setPoints(Integer
													.parseInt(input));
											player
													.sendMessage("Set points");
											plugin.reload();
										} else {
											player
													.sendMessage("Must be an integer");
										}
									}
								}

								,
								"Type value in chat to send, cancel by typing cancel",
								"" + new User(playerName).getPoints());
					}

				}
			});

			inv.addButton(1, new BInventoryButton("SetTotal", new String[0],
					new ItemStack(Material.STONE)) {

				@Override
				public void onClick(InventoryClickEvent event) {
					if (event.getWhoClicked() instanceof Player) {
						Player player = (Player) event.getWhoClicked();
						String playerName = event.getInventory().getTitle()
								.split(" ")[1];
						player.closeInventory();

						BInventory inv = new BInventory("SetTotal: "
								+ playerName);

						int count = 0;
						for (VoteSite site : plugin.voteSites) {
							inv.addButton(count,
									new BInventoryButton(site.getSiteName(),
											new String[0], new ItemStack(
													Material.STONE)) {

										@Override
										public void onClick(
												InventoryClickEvent event) {
											if (event.getWhoClicked() instanceof Player) {
												Player player = (Player) event
														.getWhoClicked();
												String playerName = event
														.getInventory()
														.getTitle().split(" ")[1];
												player.closeInventory();
												User user = new User(player);
												new RequestManager(
														player,
														user.getInputMethod(),
														new InputListener() {

															@Override
															public void onInput(
																	Player player,
																	String input) {
																if (Utils
																		.getInstance()
																		.isInt(input)) {
																	User user = new User(
																			playerName);
																	user.setTotal(
																			plugin.getVoteSite(event
																					.getCurrentItem()
																					.getItemMeta()
																					.getDisplayName()),
																			Integer.parseInt(input));
																	player
																			.sendMessage("Total set");
																	plugin.reload();
																} else {
																	player
																			.sendMessage("Must be an integer");
																}
															}
														}

														,
														"Type value in chat to send, cancel by typing cancel",
														""
																+ new User(
																		playerName)
																		.getTotal(site));

											}

										}
									});
							count++;
						}

						inv.openInventory(player);

					}

				}
			});

			inv.openInventory(player);
		}

	}

	/**
	 * Open admin GUI config.
	 *
	 * @param player
	 *            the player
	 */
	public void openAdminGUIConfig(Player player) {
		BInventory inv = new BInventory("Config");
		inv.addButton(0, new BInventoryButton("SetDebug", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					User user = new User(player);
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									Config.getInstance().setDebugEnabled(
											Boolean.valueOf(input));
									player.sendMessage("Set Debug");
									plugin.reload();

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + Config.getInstance().getDebugEnabled());

				}

			}
		});

		inv.openInventory(player);

	}

	/**
	 * Open admin GUI rewards.
	 *
	 * @param player
	 *            the player
	 */
	public void openAdminGUIRewards(Player player) {
		BInventory inv = new BInventory("Rewards");
		int count = 0;
		for (Reward reward : plugin.rewards) {
			ArrayList<String> lore = new ArrayList<String>();
			if (reward.isDelayEnabled()) {
				lore.add("DelayEnabled: true");
				lore.add("Delay: " + reward.getDelayHours() + ":"
						+ reward.getDelayMinutes());
			}
			if (reward.isTimedEnabled()) {
				lore.add("TimedEnabled: true");
				lore.add("Timed: " + reward.getTimedHour() + ":"
						+ reward.getTimedMinute());
			}
			if (reward.isRequirePermission()) {
				lore.add("RequirePermission: true");
			}
			if (reward.getWorlds().size() > 0) {
				lore.add("Worlds: "
						+ Utils.getInstance()
								.makeStringList(reward.getWorlds()));
				lore.add("GiveInEachWorld: " + reward.isGiveInEachWorld());
			}
			if (!reward.getRewardType().equals("BOTH")) {
				lore.add("RewardType: " + reward.getRewardType());
			}
			if (reward.getItems().size() > 0) {
				lore.add("Items:");
				for (String item : reward.getItems()) {
					lore.add(reward.getItemMaterial().get(item) + ":"
							+ reward.getItemData().get(item) + " "
							+ reward.getItemAmount().get(item));
				}
			}

			if (reward.getMoney() != 0) {
				lore.add("Money: " + reward.getMoney());
			}

			if (reward.getMaxMoney() != 0) {
				lore.add("MaxMoney: " + reward.getMaxMoney());
			}

			if (reward.getMinMoney() != 0) {
				lore.add("MinMoney: " + reward.getMinMoney());
			}

			if (reward.getExp() != 0) {
				lore.add("EXP: " + reward.getExp());
			}

			if (reward.getMaxExp() != 0) {
				lore.add("MaxEXP: " + reward.getMaxExp());
			}

			if (reward.getMinExp() != 0) {
				lore.add("MinEXP: " + reward.getMinExp());
			}

			if (reward.getConsoleCommands().size() > 0) {
				lore.add("ConsoleCommands:");
				lore.addAll(reward.getConsoleCommands());
			}
			if (reward.getPlayerCommands().size() > 0) {
				lore.add("PlayerCommands:");
				lore.addAll(reward.getPlayerCommands());
			}
			if (reward.getPotions().size() > 0) {
				lore.add("Potions:");
				for (String potion : reward.getPotions()) {
					lore.add(potion + " "
							+ reward.getPotionsDuration().get(potion) + " "
							+ reward.getPotionsAmplifier().get(potion));
				}
			}

			if (ConfigRewards.getInstance().getTitleEnabled(
					reward.getRewardName())) {
				lore.add("TitleEnabled: true");
				lore.add("TitleTitle: "
						+ ConfigRewards.getInstance().getTitleTitle(
								reward.getRewardName()));
				lore.add("TitleSubTitle: "
						+ ConfigRewards.getInstance().getTitleSubTitle(
								reward.getRewardName()));
				lore.add("Timings: "
						+ ConfigRewards.getInstance().getTitleFadeIn(
								reward.getRewardName())
						+ " "
						+ ConfigRewards.getInstance().getTitleShowTime(
								reward.getRewardName())
						+ " "
						+ ConfigRewards.getInstance().getTitleFadeOut(
								reward.getRewardName()));
			}

			if (reward.isBossBarEnabled()) {
				lore.add("BossBarEnabled: true");
				lore.add("BossBarMessage: " + reward.getBossBarMessage());
				lore.add("Color/Style/Progress/Delay: "
						+ reward.getBossBarColor() + "/"
						+ reward.getBossBarStyle() + "/"
						+ reward.getBossBarProgress() + "/"
						+ reward.getBossBarDelay());
			}
			if (ConfigRewards.getInstance().getSoundEnabled(
					reward.getRewardName())) {
				lore.add("SoundEnabled: true");
				lore.add("Sound/Volume/Pitch: "
						+ ConfigRewards.getInstance().getSoundSound(
								reward.getRewardName())
						+ "/"
						+ ConfigRewards.getInstance().getSoundVolume(
								reward.getRewardName())
						+ "/"
						+ ConfigRewards.getInstance().getSoundPitch(
								reward.getRewardName()));
			}

			if (ConfigRewards.getInstance().getEffectEnabled(
					reward.getRewardName())) {
				lore.add("EffectEnabled: true");
				lore.add("Effect/Data/Particles/Radius: "
						+ ConfigRewards.getInstance().getEffectEffect(
								reward.getRewardName())
						+ "/"
						+ ConfigRewards.getInstance().getEffectData(
								reward.getRewardName())
						+ "/"
						+ ConfigRewards.getInstance().getEffectParticles(
								reward.getRewardName())
						+ "/"
						+ ConfigRewards.getInstance().getEffectRadius(
								reward.getRewardName()));
			}

			lore.add("ActioBarMessage/Delay: " + reward.getActionBarMsg() + "/"
					+ reward.getActionBarDelay());

			lore.add("MessagesReward: " + reward.getRewardMsg());

			inv.addButton(count, new BInventoryButton(reward.getRewardName(),
					Utils.getInstance().convertArray(lore), new ItemStack(
							Material.STONE)) {

				@Override
				public void onClick(InventoryClickEvent event) {
					if (event.getWhoClicked() instanceof Player) {
						Player player = (Player) event.getWhoClicked();
						openAdminGUIReward(player, reward);
					}
				}
			});
			count++;
		}

		inv.openInventory(player);
	}

	/**
	 * Open admin GUI reward.
	 *
	 * @param player
	 *            the player
	 * @param reward
	 *            the reward
	 */
	public void openAdminGUIReward(Player player, Reward reward) {
		BInventory inv = new BInventory("Reward: " + reward.getRewardName());
		User user = new User(player);

		inv.addButton(0, new BInventoryButton("SetChance", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String reward = event.getInventory()
											.getTitle().split(" ")[1];
									if (Utils.getInstance().isInt(input)) {
										ConfigRewards
												.getInstance()
												.setChance(reward,
														Integer.parseInt(input));
										player
												.sendMessage("Set Chance");
										plugin.reload();
									} else {
										player
												.sendMessage("Must be an interger");
									}

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + reward.getChance());

				}
			}
		});

		inv.addButton(1, new BInventoryButton("SetMoney", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String reward = event.getInventory()
											.getTitle().split(" ")[1];
									if (Utils.getInstance().isInt(input)) {
										ConfigRewards
												.getInstance()
												.setMoney(reward,
														Integer.parseInt(input));
										player.sendMessage("Set money");
										plugin.reload();
									} else {
										player
												.sendMessage("Must be an interger");
									}

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + reward.getMoney());

				}
			}
		});

		inv.addButton(2, new BInventoryButton("SetMinMoney", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String reward = event.getInventory()
											.getTitle().split(" ")[1];
									if (Utils.getInstance().isInt(input)) {
										ConfigRewards
												.getInstance()
												.setMinMoney(reward,
														Integer.parseInt(input));
										player
												.sendMessage("Set minmoney");
										plugin.reload();
									} else {
										player
												.sendMessage("Must be an interger");
									}

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + reward.getMinMoney());

				}
			}
		});

		inv.addButton(3, new BInventoryButton("SetMaxMoney", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String reward = event.getInventory()
											.getTitle().split(" ")[1];
									if (Utils.getInstance().isInt(input)) {
										ConfigRewards
												.getInstance()
												.setMaxMoney(reward,
														Integer.parseInt(input));
										player
												.sendMessage("Set maxmoney");
										plugin.reload();
									} else {
										player
												.sendMessage("Must be an interger");
									}

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + reward.getMaxMoney());

				}
			}
		});

		inv.addButton(4, new BInventoryButton("SetExp", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String reward = event.getInventory()
											.getTitle().split(" ")[1];
									if (Utils.getInstance().isInt(input)) {
										ConfigRewards
												.getInstance()
												.setEXP(reward,
														Integer.parseInt(input));
										player.sendMessage("Set Exp");
										plugin.reload();
									} else {
										player
												.sendMessage("Must be an interger");
									}

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + reward.getExp());

				}
			}
		});

		inv.addButton(5, new BInventoryButton("SetMinExp", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String reward = event.getInventory()
											.getTitle().split(" ")[1];
									if (Utils.getInstance().isInt(input)) {
										ConfigRewards
												.getInstance()
												.setMinExp(reward,
														Integer.parseInt(input));
										player
												.sendMessage("Set minExp");
										plugin.reload();
									} else {
										player
												.sendMessage("Must be an interger");
									}

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + reward.getMaxExp());

				}
			}
		});

		inv.addButton(6, new BInventoryButton("SetMaxExp", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String reward = event.getInventory()
											.getTitle().split(" ")[1];
									if (Utils.getInstance().isInt(input)) {
										ConfigRewards
												.getInstance()
												.setMaxExp(reward,
														Integer.parseInt(input));
										player
												.sendMessage("Set maxExp");
										plugin.reload();
									} else {
										player
												.sendMessage("Must be an interger");
									}

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + reward.getMinExp());

				}
			}
		});

		ArrayList<String> lore = new ArrayList<String>();
		lore.add("&cAdd current item inhand");
		inv.addButton(7, new BInventoryButton("Add item", Utils.getInstance()
				.convertArray(lore), new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					String reward = event.getInventory().getTitle().split(" ")[1];
					@SuppressWarnings("deprecation")
					ItemStack item = player.getItemInHand();
					if (item != null && !item.getType().equals(Material.AIR)) {
						String material = item.getType().toString();
						@SuppressWarnings("deprecation")
						int data = item.getData().getData();
						int amount = item.getAmount();
						int durability = item.getDurability();
						String name = item.getItemMeta().getDisplayName();
						ArrayList<String> lore = (ArrayList<String>) item
								.getItemMeta().getLore();
						Map<Enchantment, Integer> enchants = item
								.getEnchantments();
						String itemStack = material;
						ConfigRewards.getInstance().setItemAmount(reward,
								itemStack, amount);
						ConfigRewards.getInstance().setItemData(reward,
								itemStack, data);
						ConfigRewards.getInstance().setItemMaterial(reward,
								itemStack, material);
						ConfigRewards.getInstance().setItemName(reward,
								itemStack, name);
						ConfigRewards.getInstance().setItemLore(reward,
								itemStack, lore);
						ConfigRewards.getInstance().setItemDurability(reward,
								itemStack, durability);
						for (Entry<Enchantment, Integer> entry : enchants
								.entrySet()) {
							ConfigRewards.getInstance().setItemEnchant(reward,
									itemStack, entry.getKey().getName(),
									entry.getValue().intValue());
						}
						plugin.reload();
					}
				}
			}
		});

		lore = new ArrayList<String>();
		inv.addButton(8, new BInventoryButton("Remove item", Utils
				.getInstance().convertArray(lore),
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					String rewardName = event.getInventory().getTitle()
							.split(" ")[1];
					BInventory inv = new BInventory("RewardRemoveItem: "
							+ rewardName);
					Reward reward = ConfigRewards.getInstance().getReward(
							rewardName);

					int slot = 0;
					for (String item : reward.getItems()) {
						ItemStack itemStack = new ItemStack(Material
								.valueOf(reward.getItemMaterial().get(item)),
								reward.getItemAmount(item), Short
										.valueOf(Integer.toString(reward
												.getItemData().get(item))));
						String name = reward.getItemName().get(item);
						if (name != null) {
							itemStack = Utils.getInstance().nameItem(
									itemStack,
									name.replace("%Player%",
											user.getPlayerName()));
						}
						itemStack = Utils.getInstance().addLore(
								itemStack,
								Utils.getInstance().replace(
										reward.getItemLore().get(item),
										"%Player%", user.getPlayerName()));
						itemStack = Utils.getInstance().addEnchants(itemStack,
								reward.getItemEnchants().get(item));
						itemStack = Utils.getInstance().setDurabilty(itemStack,
								reward.getItemDurabilty().get(item));
						String skull = reward.getItemSkull().get(item);
						if (skull != null) {
							itemStack = Utils.getInstance().setSkullOwner(
									itemStack,
									skull.replace("%Player%",
											user.getPlayerName()));
						}
						inv.addButton(slot, new BInventoryButton(item,
								new String[0], itemStack) {

							@Override
							public void onClick(InventoryClickEvent event) {
								if (event.getWhoClicked() instanceof Player) {
									Player player = (Player) event
											.getWhoClicked();
									String item = event.getCurrentItem()
											.getItemMeta().getDisplayName();
									String reward = event.getInventory()
											.getTitle().split(" ")[1];
									ConfigRewards.getInstance().set(reward,
											"Items." + item, null);
									player.closeInventory();
									player.sendMessage("Removed item");
									plugin.reload();

								}

							}
						});
						slot++;
					}

					inv.openInventory(player);

				}
			}
		});

		inv.addButton(9, new BInventoryButton("SetMessage", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String reward = event.getInventory()
											.getTitle().split(" ")[1];

									ConfigRewards.getInstance()
											.setMessagesReward(reward, input);
									player.sendMessage("Set message");
									plugin.reload();

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + reward.getRewardMsg());

				}
			}
		});

		inv.addButton(10, new BInventoryButton("AddConsoleCommand",
				new String[0], new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String reward = event.getInventory()
											.getTitle().split(" ")[1];

									ArrayList<String> commands = ConfigRewards
											.getInstance().getCommandsConsole(
													reward);
									commands.add(input);

									ConfigRewards.getInstance()
											.setCommandsConsole(reward,
													commands);
									player
											.sendMessage("Added console command");
									plugin.reload();

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"");

				}
			}
		});

		inv.addButton(11, new BInventoryButton("AddPlayerCommand",
				new String[0], new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String reward = event.getInventory()
											.getTitle().split(" ")[1];

									ArrayList<String> commands = ConfigRewards
											.getInstance().getCommandsPlayer(
													reward);
									commands.add(input);

									ConfigRewards
											.getInstance()
											.setCommandsPlayer(reward, commands);
									player
											.sendMessage("Added player command");
									plugin.reload();

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"");

				}
			}
		});

		inv.addButton(12, new BInventoryButton("RemoveConsoleCommand",
				new String[0], new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					String reward = event.getInventory().getTitle().split(" ")[1];
					BInventory inv = new BInventory("RemoveConsoleCommand: "
							+ reward);
					int count = 0;
					for (String cmd : ConfigRewards.getInstance()
							.getCommandsConsole(reward)) {
						inv.addButton(count, new BInventoryButton(cmd,
								new String[0], new ItemStack(Material.STONE)) {

							@Override
							public void onClick(InventoryClickEvent event) {
								if (event.getWhoClicked() instanceof Player) {
									Player player = (Player) event
											.getWhoClicked();
									String reward = event.getInventory()
											.getTitle().split(" ")[1];
									ArrayList<String> commands = ConfigRewards
											.getInstance().getCommandsConsole(
													reward);
									if (event.getCurrentItem() != null
											&& !event.getCurrentItem()
													.getType()
													.equals(Material.AIR)) {
										commands.remove(event.getCurrentItem()
												.getItemMeta().getDisplayName());
										ConfigRewards.getInstance()
												.setCommandsConsole(reward,
														commands);

									}
									player.closeInventory();
									player.sendMessage("Removed command");
									plugin.reload();
								}
							}
						});
						count++;
					}

					inv.openInventory(player);

				}
			}
		});

		inv.addButton(13, new BInventoryButton("RemovePlayerCommand",
				new String[0], new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					String reward = event.getInventory().getTitle().split(" ")[1];
					BInventory inv = new BInventory("RemovePlayerCommand: "
							+ reward);
					int count = 0;
					for (String cmd : ConfigRewards.getInstance()
							.getCommandsPlayer(reward)) {
						inv.addButton(count, new BInventoryButton(cmd,
								new String[0], new ItemStack(Material.STONE)) {

							@Override
							public void onClick(InventoryClickEvent event) {
								if (event.getWhoClicked() instanceof Player) {
									Player player = (Player) event
											.getWhoClicked();
									String reward = event.getInventory()
											.getTitle().split(" ")[1];
									ArrayList<String> commands = ConfigRewards
											.getInstance().getCommandsPlayer(
													reward);
									if (event.getCurrentItem() != null
											&& !event.getCurrentItem()
													.getType()
													.equals(Material.AIR)) {
										commands.remove(event.getCurrentItem()
												.getItemMeta().getDisplayName());
										ConfigRewards.getInstance()
												.setCommandsPlayer(reward,
														commands);

									}
									player.closeInventory();
									player.sendMessage("Removed command");
									plugin.reload();
								}
							}
						});
						count++;
					}

					inv.openInventory(player);

				}
			}
		});

		inv.addButton(14, new BInventoryButton("SetRequirePermission",
				new String[0], new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String reward = event.getInventory()
											.getTitle().split(" ")[1];

									ConfigRewards.getInstance()
											.setRequirePermission(reward,
													Boolean.valueOf(input));
									player
											.sendMessage("Set require permission");
									plugin.reload();

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + reward.isRequirePermission());

				}
			}
		});

		inv.addButton(15, new BInventoryButton("Execute", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String reward = event.getInventory()
											.getTitle().split(" ")[1];

									ConfigRewards
											.getInstance()
											.getReward(reward)
											.runCommands(
													new User(
															(Player) player));
									player
											.sendMessage("Ran Reward file");
									plugin.reload();

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + reward.isRequirePermission());

				}
			}
		});

		inv.openInventory(player);
	}

	/**
	 * Open admin GUI vote sites.
	 *
	 * @param player
	 *            the player
	 */
	public void openAdminGUIVoteSites(Player player) {
		BInventory inv = new BInventory("VoteSites");
		int count = 0;
		for (VoteSite voteSite : plugin.voteSites) {
			ArrayList<String> lore = new ArrayList<String>();
			lore.add("Priority: " + voteSite.getPriority());
			lore.add("ServiceSite: " + voteSite.getServiceSite());
			lore.add("VoteURL: " + voteSite.getVoteURL());
			lore.add("VoteDelay: " + voteSite.getVoteDelay());
			lore.add("Rewards: "
					+ Utils.getInstance().makeStringList(voteSite.getRewards()));
			lore.add("CumulativeVotes: " + voteSite.getCumulativeVotes());
			lore.add("CumulativeRewards: "
					+ Utils.getInstance().makeStringList(
							voteSite.getCumulativeRewards()));

			inv.addButton(count, new BInventoryButton(voteSite.getSiteName(),
					Utils.getInstance().convertArray(lore), new ItemStack(
							Material.STONE)) {

				@Override
				public void onClick(InventoryClickEvent event) {
					if (event.getWhoClicked() instanceof Player) {
						Player player = (Player) event.getWhoClicked();
						openAdminGUIVoteSiteSite(player, voteSite);
					}
				}
			});
			count++;
		}
		inv.openInventory(player);
	}

	/**
	 * Open admin GUI vote site site.
	 *
	 * @param player
	 *            the player
	 * @param voteSite
	 *            the vote site
	 */
	public void openAdminGUIVoteSiteSite(Player player, VoteSite voteSite) {
		BInventory inv = new BInventory("VoteSite: " + voteSite.getSiteName());
		User user = new User(player);
		inv.addButton(0, new BInventoryButton("SetPriority", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									if (Utils.getInstance().isInt(input)) {
										ConfigVoteSites
												.getInstance()
												.setPriority(
														event.getInventory()
																.getTitle()
																.split(" ")[1],
														Integer.parseInt(input));
										player
												.sendMessage("Set Priority");
										plugin.reload();
									} else {
										player
												.sendMessage("Must be an interger");
									}

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + voteSite.getPriority());

				}

			}
		});

		inv.addButton(1, new BInventoryButton("SetServiceSite", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String siteName = event.getInventory()
											.getTitle().split(" ")[1];
									ConfigVoteSites.getInstance()
											.setServiceSite(siteName, input);
									player
											.sendMessage("Set ServiceSite");
									plugin.reload();

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + voteSite.getServiceSite());

				}

			}
		});

		inv.addButton(2, new BInventoryButton("SetVoteURL", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String siteName = event.getInventory()
											.getTitle().split(" ")[1];
									ConfigVoteSites.getInstance().setVoteURL(
											siteName, input);
									player.sendMessage("Set VoteURL");
									plugin.reload();

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + voteSite.getVoteURL());

				}

			}
		});

		inv.addButton(3, new BInventoryButton("SetVoteDelay", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									if (Utils.getInstance().isInt(input)) {
										ConfigVoteSites
												.getInstance()
												.setVoteDelay(
														event.getInventory()
																.getTitle()
																.split(" ")[1],
														Integer.parseInt(input));
										player
												.sendMessage("Set VoteDelay");
										plugin.reload();
									} else {
										player
												.sendMessage("Must be an interger");
									}

								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							"" + voteSite.getVoteDelay());

				}

			}
		});
		inv.addButton(4, new BInventoryButton("SetEnabled", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					new RequestManager(
							player,
							user.getInputMethod(),
							new InputListener() {

								@Override
								public void onInput(Player player,
										String input) {
									String siteName = event.getInventory()
											.getTitle().split(" ")[1];
									ConfigVoteSites.getInstance().setEnabled(
											siteName, Boolean.valueOf(input));
									player.sendMessage("Set Enabled");
									plugin.reload();
								}
							}

							,
							"Type value in chat to send, cancel by typing cancel",
							""
									+ ConfigVoteSites.getInstance()
											.getVoteSiteEnabled(
													voteSite.getSiteName()));

				}

			}
		});

		inv.addButton(5, new BInventoryButton("Add Reward", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					String siteName = event.getInventory().getTitle()
							.split(" ")[1];
					BInventory inv = new BInventory("AddReward: " + siteName);
					int count = 0;
					for (Reward reward : plugin.rewards) {
						inv.addButton(count,
								new BInventoryButton(reward.getRewardName(),
										new String[0], new ItemStack(
												Material.STONE)) {

									@Override
									public void onClick(
											InventoryClickEvent event) {
										if (event.getWhoClicked() instanceof Player) {
											Player player = (Player) event
													.getWhoClicked();
											player.closeInventory();
											String siteName = event
													.getInventory().getTitle()
													.split(" ")[1];
											ArrayList<String> rewards = ConfigVoteSites
													.getInstance().getRewards(
															siteName);
											rewards.add(reward.getRewardName());
											ConfigVoteSites.getInstance()
													.setRewards(siteName,
															rewards);
											player.sendMessage("Reward added");
											plugin.reload();
										}
									}
								});
						count++;
					}

					inv.openInventory(player);

				}

			}
		});

		inv.addButton(6, new BInventoryButton("Remove Reward", new String[0],
				new ItemStack(Material.STONE)) {

			@Override
			public void onClick(InventoryClickEvent event) {
				if (event.getWhoClicked() instanceof Player) {
					Player player = (Player) event.getWhoClicked();
					String siteName = event.getInventory().getTitle()
							.split(" ")[1];
					BInventory inv = new BInventory("RemoveReward: " + siteName);
					int count = 0;
					for (String rewardName : voteSite.getRewards()) {
						Reward reward = ConfigRewards.getInstance().getReward(
								rewardName);
						inv.addButton(count,
								new BInventoryButton(reward.getRewardName(),
										new String[0], new ItemStack(
												Material.STONE)) {

									@Override
									public void onClick(
											InventoryClickEvent event) {
										if (event.getWhoClicked() instanceof Player) {
											Player player = (Player) event
													.getWhoClicked();
											player.closeInventory();
											String siteName = event
													.getInventory().getTitle()
													.split(" ")[1];
											ArrayList<String> rewards = ConfigVoteSites
													.getInstance().getRewards(
															siteName);
											rewards.remove(reward
													.getRewardName());
											ConfigVoteSites.getInstance()
													.setRewards(siteName,
															rewards);
											player.sendMessage("Reward removed");
											plugin.reload();
										}
									}
								});
						count++;
					}

					inv.openInventory(player);

				}

			}
		});

		inv.openInventory(player);
	}

	/**
	 * Check vote site.
	 *
	 * @param sender
	 *            the sender
	 * @param siteName
	 *            the site name
	 */
	public void checkVoteSite(CommandSender sender, String siteName) {
		if (!ConfigVoteSites.getInstance().isServiceSiteGood(siteName)) {
			sender.sendMessage(Utils.getInstance().colorize(
					"&cServiceSite is invalid, votes may not work properly"));
		} else {
			sender.sendMessage(Utils.getInstance().colorize(
					"&aServiceSite is properly setup"));
		}
		if (!ConfigVoteSites.getInstance().isVoteURLGood(siteName)) {
			sender.sendMessage(Utils.getInstance().colorize(
					"&cVoteURL is invalid"));
		} else {
			sender.sendMessage(Utils.getInstance().colorize(
					"&aVoteURL is properly setup"));
		}
	}

	/**
	 * Creates the vote site.
	 *
	 * @param sender
	 *            the sender
	 * @param voteSite
	 *            the vote site
	 */
	public void createVoteSite(CommandSender sender, String voteSite) {

		sender.sendMessage(Utils.getInstance().colorize(
				"&cCreating VoteSite..."));
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				ConfigVoteSites.getInstance().generateVoteSite(voteSite);
				sender.sendMessage(Utils.getInstance().colorize(
						"&cCreated VoteSite: &c&l" + voteSite));
			}
		});

	}

	/**
	 * Help.
	 *
	 * @param sender
	 *            the sender
	 * @param page
	 *            the page
	 */
	public void help(CommandSender sender, int page) {
		if (sender instanceof Player) {
			User user = new User((Player) sender);
			user.sendJson(Commands.getInstance().adminHelp(sender, page - 1));
		} else {
			sender.sendMessage(Utils.getInstance()
					.convertArray(
							Utils.getInstance().comptoString(
									Commands.getInstance().adminHelp(sender,
											page - 1))));
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender
	 * , org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {

		for (CommandHandler commandHandler : plugin.adminVoteCommand) {
			if (commandHandler.runCommand(sender, args)) {
				return true;
			}
		}

		// invalid command
		sender.sendMessage(ChatColor.RED
				+ "No valid arguments, see /adminvote help!");

		return true;
	}

	/**
	 * Perm list.
	 *
	 * @param sender
	 *            the sender
	 */
	public void permList(CommandSender sender) {

		sender.sendMessage(Commands.getInstance().listPerms());

	}

	/**
	 * Reload.
	 *
	 * @param sender
	 *            the sender
	 */
	public void reload(CommandSender sender) {

		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				sender.sendMessage(ChatColor.RED + "Reloading "
						+ plugin.getName() + "...");
				plugin.reload();
				sender.sendMessage(ChatColor.RED + plugin.getName() + " v"
						+ plugin.getDescription().getVersion() + " reloaded!");
			}
		});

	}

	/**
	 * Reset player totals.
	 *
	 * @param sender
	 *            the sender
	 * @param playerName
	 *            the player name
	 */
	public void resetPlayerTotals(CommandSender sender, String playerName) {
		sender.sendMessage(Utils.getInstance().colorize(
				"&cResseting totals for player &c&l" + playerName));
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				TopVoter.getInstance().resetTotalsPlayer(new User(playerName));
				sender.sendMessage(Utils.getInstance().colorize(
						"&cDone resseting totals for &c&l" + playerName));
				plugin.update();
			}
		});
	}

	/**
	 * Reset totals.
	 *
	 * @param sender
	 *            the sender
	 */
	public void resetTotals(CommandSender sender) {

		sender.sendMessage(Utils.getInstance().colorize(
				"&cResseting totals for all players..."));
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				TopVoter.getInstance().resetTotalsMonthly();
				sender.sendMessage(Utils.getInstance().colorize(
						"&cDone resseting totals"));
				plugin.update();
			}
		});

	}

	/**
	 * Reward.
	 *
	 * @param sender
	 *            the sender
	 * @param reward
	 *            the reward
	 */
	public void reward(CommandSender sender, String reward) {

		sender.sendMessage(Commands.getInstance().voteCommandRewardInfo(reward));

	}

	/**
	 * Rewards.
	 *
	 * @param sender
	 *            the sender
	 */
	public void rewards(CommandSender sender) {

		sender.sendMessage(Commands.getInstance().voteCommandRewards());

	}

	/**
	 * Sets the config allow unjoined.
	 *
	 * @param sender
	 *            the sender
	 * @param value
	 *            the value
	 */
	public void setConfigAllowUnjoined(CommandSender sender, boolean value) {

		Config.getInstance().setAllowUnJoined(value);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet AllowUnjoined to &c&l" + value));

	}

	/**
	 * Sets the config broadcast vote.
	 *
	 * @param sender
	 *            the sender
	 * @param value
	 *            the value
	 */
	public void setConfigBroadcastVote(CommandSender sender, boolean value) {

		Config.getInstance().setDebugEnabled(value);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet BroadcastVote to &c&l" + value));

	}

	/**
	 * Sets the config debug.
	 *
	 * @param sender
	 *            the sender
	 * @param value
	 *            the value
	 */
	public void setConfigDebug(CommandSender sender, boolean value) {

		Config.getInstance().setDebugEnabled(value);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet Debug to &c&l" + value));

	}

	/**
	 * Sets the config enable top voter awards.
	 *
	 * @param sender
	 *            the sender
	 * @param value
	 *            the value
	 */
	public void setConfigEnableTopVoterAwards(CommandSender sender,
			boolean value) {

		Config.getInstance().setTopVoterAwardsEnabled(value);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet DisableTopVoterAwards to &c&l" + value));

	}

	/**
	 * Sets the reward max money.
	 *
	 * @param sender
	 *            the sender
	 * @param reward
	 *            the reward
	 * @param money
	 *            the money
	 */
	public void setRewardMaxMoney(CommandSender sender, String reward, int money) {
		ConfigRewards.getInstance().setMaxMoney(reward, money);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet maxmoney to &c&l" + money + "&c on &c&l" + reward));
	}

	/**
	 * Sets the reward message.
	 *
	 * @param sender
	 *            the sender
	 * @param reward
	 *            the reward
	 * @param msg
	 *            the msg
	 */
	public void setRewardMessage(CommandSender sender, String reward, String msg) {
		ConfigRewards.getInstance().setMessagesReward(reward, msg);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet reward message to &c&l" + msg + "&c on &c&l" + reward));
	}

	/**
	 * Sets the reward min money.
	 *
	 * @param sender
	 *            the sender
	 * @param reward
	 *            the reward
	 * @param money
	 *            the money
	 */
	public void setRewardMinMoney(CommandSender sender, String reward, int money) {
		ConfigRewards.getInstance().setMinMoney(reward, money);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet minmoney to &c&l" + money + "&c on &c&l" + reward));
	}

	/**
	 * Sets the reward money.
	 *
	 * @param sender
	 *            the sender
	 * @param reward
	 *            the reward
	 * @param money
	 *            the money
	 */
	public void setRewardMoney(CommandSender sender, String reward, int money) {
		ConfigRewards.getInstance().setMoney(reward, money);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet money to &c&l" + money + "&c on &c&l" + reward));
	}

	/**
	 * Sets the reward require permission.
	 *
	 * @param sender
	 *            the sender
	 * @param reward
	 *            the reward
	 * @param value
	 *            the value
	 */
	public void setRewardRequirePermission(CommandSender sender, String reward,
			boolean value) {
		ConfigRewards.getInstance().setRequirePermission(reward, value);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet require permission to &c&l" + value + "&c on &c&l"
						+ reward));
	}

	/**
	 * Sets the server data prev month.
	 *
	 * @param sender
	 *            the sender
	 * @param month
	 *            the month
	 */
	public void setServerDataPrevMonth(CommandSender sender, int month) {

		ServerData.getInstance().setPrevMonth(month);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet PreMonth to &c&l" + month));

	}

	/**
	 * Sets the total.
	 *
	 * @param sender
	 *            the sender
	 * @param playerName
	 *            the player name
	 * @param voteSite
	 *            the vote site
	 * @param amount
	 *            the amount
	 */
	public void setTotal(CommandSender sender, String playerName,
			String voteSite, int amount) {

		Data.getInstance().setTotal(new User(playerName), voteSite, amount);
		sender.sendMessage(ChatColor.GREEN + playerName + " total votes for "
				+ voteSite + " has been set to " + amount);
		plugin.update();

	}

	/**
	 * Sets the vote site enabled.
	 *
	 * @param sender
	 *            the sender
	 * @param voteSite
	 *            the vote site
	 * @param value
	 *            the value
	 */
	public void setVoteSiteEnabled(CommandSender sender, String voteSite,
			boolean value) {
		ConfigVoteSites.getInstance().setEnabled(voteSite, value);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet votesite " + voteSite + " enabled to " + value));
	}

	/**
	 * Sets the vote site priority.
	 *
	 * @param sender
	 *            the sender
	 * @param voteSite
	 *            the vote site
	 * @param value
	 *            the value
	 */
	public void setVoteSitePriority(CommandSender sender, String voteSite,
			int value) {

		ConfigVoteSites.getInstance().setPriority(voteSite, value);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet priortiy to &c&l" + value + "&c on &c&l" + voteSite));

	}

	/**
	 * Sets the vote site service site.
	 *
	 * @param sender
	 *            the sender
	 * @param voteSite
	 *            the vote site
	 * @param serviceSite
	 *            the service site
	 */
	public void setVoteSiteServiceSite(CommandSender sender, String voteSite,
			String serviceSite) {

		ConfigVoteSites.getInstance().setServiceSite(voteSite, serviceSite);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet ServiceSite to &c&l" + serviceSite + "&c on &c&l"
						+ voteSite));

	}

	/**
	 * Sets the vote site vote delay.
	 *
	 * @param sender
	 *            the sender
	 * @param voteSite
	 *            the vote site
	 * @param delay
	 *            the delay
	 */
	public void setVoteSiteVoteDelay(CommandSender sender, String voteSite,
			int delay) {

		ConfigVoteSites.getInstance().setVoteDelay(voteSite, delay);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet VoteDelay to &c&l" + delay + "&c on &c&l" + voteSite));

	}

	/**
	 * Sets the vote site vote URL.
	 *
	 * @param sender
	 *            the sender
	 * @param voteSite
	 *            the vote site
	 * @param url
	 *            the url
	 */
	public void setVoteSiteVoteURL(CommandSender sender, String voteSite,
			String url) {

		ConfigVoteSites.getInstance().setVoteURL(voteSite, url);
		sender.sendMessage(Utils.getInstance().colorize(
				"&cSet VoteURL to &c&l" + url + "&c on &c&l" + voteSite));

	}

	/**
	 * Site.
	 *
	 * @param sender
	 *            the sender
	 * @param site
	 *            the site
	 */
	public void site(CommandSender sender, String site) {

		sender.sendMessage(Commands.getInstance().voteCommandSiteInfo(site));

	}

	/**
	 * Sites.
	 *
	 * @param sender
	 *            the sender
	 */
	public void sites(CommandSender sender) {

		sender.sendMessage(Commands.getInstance().voteCommandSites());

	}

	/**
	 * Uuid.
	 *
	 * @param sender
	 *            the sender
	 * @param playerName
	 *            the player name
	 */
	public void uuid(CommandSender sender, String playerName) {

		sender.sendMessage(ChatColor.GREEN + "UUID of player "
				+ ChatColor.DARK_GREEN + playerName + ChatColor.GREEN + " is: "
				+ Utils.getInstance().getUUID(playerName));

	}

	/**
	 * Version.
	 *
	 * @param sender
	 *            the sender
	 */
	public void version(CommandSender sender) {
		if (sender instanceof Player) {

			Player player = (Player) sender;
			player.performCommand("bukkit:version " + plugin.getName());

		} else {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
					"bukkit:version " + plugin.getName());
		}
	}

	/**
	 * Vote.
	 *
	 * @param sender
	 *            the sender
	 * @param playerName
	 *            the player name
	 * @param voteSite
	 *            the vote site
	 */
	public void Vote(CommandSender sender, String playerName, String voteSite) {

		VotiferEvent.playerVote(playerName, voteSite);

		Vote vote = new com.vexsoftware.votifier.model.Vote();
		vote.setServiceName(new VoteSite(voteSite).getServiceSite());
		vote.setUsername(playerName);

		BungeeVote.getInstance().sendVote(vote);

	}

}