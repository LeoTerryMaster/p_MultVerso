package services.community;

import l2.gameserver.Config;
import l2.gameserver.data.StringHolder;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.RecipeHolder;
import l2.gameserver.handler.bbs.CommunityBoardManager;
import l2.gameserver.handler.bbs.ICommunityBoardHandler;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.Recipe;
import l2.gameserver.model.World;
import l2.gameserver.model.items.ManufactureItem;
import l2.gameserver.model.items.TradeItem;
import l2.gameserver.network.l2.s2c.RadarControl;
import l2.gameserver.network.l2.s2c.ShowBoard;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.item.ItemTemplate.Grade;
import l2.gameserver.utils.MapUtils;
import l2.gameserver.utils.Util;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class RegionCommunity implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(RegionCommunity.class);
	private static final TownEntry[] _towns = {new TownEntry("Gatekeeper.TheTownofGludio", 19, 21), new TownEntry("Gatekeeper.TheTownofDion", 20, 22), new TownEntry("Gatekeeper.TheTownofGiran", 22, 22), new TownEntry("Gatekeeper.TheTownofOren", 22, 19), new TownEntry("Gatekeeper.TheTownofAden", 24, 18), new TownEntry("Gatekeeper.Heine", 23, 24), new TownEntry("Gatekeeper.TheTownofGoddard", 24, 16), new TownEntry("Gatekeeper.RuneTownship", 21, 16), new TownEntry("Gatekeeper.TheTownofSchuttgart", 22, 13)};
	private static final String[] _regionTypes = {"&$596;", "&$597;", "&$665;"};
	private static final String[] _elements = {"&$1622;", "&$1623;", "&$1624;", "&$1625;", "&$1626;", "&$1627;"};
	private static final String[] _grade = {"&$1291;", "&$1292;", "&$1293;", "&$1294;", "&$1295;", "S80 Grade", "S84 Grade"};
	private static final int SELLER_PER_PAGE = 12;
	
	private static List<Player> getSellersList(int townId, int type, String search, boolean byItem)
	{
		List<Player> list = new ArrayList();
		return list;
	}
	
	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			_log.info("CommunityBoard: Region service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}
	
	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			CommunityBoardManager.getInstance().removeHandler(this);
		}
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[] {"_bbsloc", "_bbsregion_", "_bbsreglist_", "_bbsregsearch", "_bbsregview_", "_bbsregtarget_"};
	}
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		player.setSessionVar("add_fav", null);
		int page;
		String tpl;
		TownEntry town;
		String search;
		int start;
		int tx;
		int ep;
		if("bbsloc".equals(cmd))
		{
			tpl = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_regiontpl.htm", player);
			StringBuilder rl = new StringBuilder("");
			
			for(page = 0;page < _towns.length;++page)
			{
				town = _towns[page];
				search = tpl.replace("%region_bypass%", "_bbsregion_" + String.valueOf(page));
				search = search.replace("%region_name%", StringHolder.getInstance().getNotNull(player, town.getTownNameAddr()));
				search = search.replace("%region_desc%", "&$498;: &$1157;, &$1434;, &$645;.");
				search = search.replace("%region_type%", "l2ui.bbs_folder");
				int sellers = town.getX();
				start = town.getY();
				int offset = 0;
				Iterator var54 = GameObjectsStorage.getAllPlayersForIterate().iterator();
				
				while(var54.hasNext())
				{
					Player seller = (Player) var54.next();
					ep = MapUtils.regionX(seller);
					tx = MapUtils.regionY(seller);
					if(ep >= sellers - offset && ep <= sellers + offset && tx >= start - offset && tx <= start + offset && seller.getPrivateStoreType() > 0 && seller.getPrivateStoreType() != 7)
					{
						++sellers;
					}
				}
				
				search = search.replace("%sellers_count%", String.valueOf(sellers));
				rl.append(search);
			}
			
			HashMap<Integer, String> tpls = Util.parseTemplate(HtmCache.getInstance().getNotNull("scripts/services/community/bbs_region_list.htm", player));
			String html = tpls.get(0);
			html = html.replace("%REGION_LIST%", rl.toString());
			html = html.replace("<?tree_menu?>", tpls.get(1));
			ShowBoard.separateAndSend(html, player);
		}
		else
		{
			int byItem;
			int type;
			int end;
			int i;
			if("bbsregion".equals(cmd))
			{
				tpl = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_regiontpl.htm", player);
				type = Integer.parseInt(st.nextToken());
				StringBuilder rl = new StringBuilder("");
				town = _towns[type];
				player.setSessionVar("add_fav", bypass + "&Region " + type);
				
				label390:
				for(byItem = 0;byItem < _regionTypes.length;++byItem)
				{
					search = tpl.replace("%region_bypass%", "_bbsreglist_" + type + "_" + byItem + "_1_0_");
					search = search.replace("%region_name%", _regionTypes[byItem]);
					search = search.replace("%region_desc%", _regionTypes[byItem] + ".");
					search = search.replace("%region_type%", "l2ui.bbs_board");
					start = town.getX();
					end = town.getY();
					int offset = 0;
					Iterator var39 = GameObjectsStorage.getAllPlayersForIterate().iterator();
					
					int sellers = 0;
					while(true)
					{
						while(true)
						{
							Player seller;
							do
							{
								do
								{
									do
									{
										do
										{
											if(!var39.hasNext())
											{
												search = search.replace("%sellers_count%", String.valueOf(sellers));
												rl.append(search);
												continue label390;
											}
											
											seller = (Player) var39.next();
											tx = MapUtils.regionX(seller);
											i = MapUtils.regionY(seller);
										}
										while(tx < start - offset);
									}
									while(tx > start + offset);
								}
								while(i < end - offset);
							}
							while(i > end + offset);
							
							if(byItem == 0 && (seller.getPrivateStoreType() == 1 || seller.getPrivateStoreType() == 8))
							{
								++sellers;
							}
							else if(byItem == 1 && seller.getPrivateStoreType() == 3)
							{
								++sellers;
							}
							else if(byItem == 2 && seller.getPrivateStoreType() == 5)
							{
								++sellers;
							}
						}
					}
				}
				
				HashMap<Integer, String> tpls = Util.parseTemplate(HtmCache.getInstance().getNotNull("scripts/services/community/bbs_region_list.htm", player));
				search = tpls.get(0);
				search = search.replace("%REGION_LIST%", rl.toString());
				search = search.replace("<?tree_menu?>", tpls.get(2).replace("%TREE%", "&nbsp;>&nbsp;" + StringHolder.getInstance().getNotNull(player, town.getTownNameAddr())));
				ShowBoard.separateAndSend(search, player);
			}
			else
			{
				int townId;
				StringBuilder sb;
				String desc;
				if("bbsreglist".equals(cmd))
				{
					townId = Integer.parseInt(st.nextToken());
					type = Integer.parseInt(st.nextToken());
					page = Integer.parseInt(st.nextToken());
					byItem = Integer.parseInt(st.nextToken());
					search = st.hasMoreTokens() ? st.nextToken().toLowerCase() : "";
					town = _towns[townId];
					player.setSessionVar("add_fav", bypass + "&Region " + townId + " " + _regionTypes[type]);
					List<Player> sellers = getSellersList(townId, type, search, byItem == 1);
					start = (page - 1) * 12;
					end = Math.min(page * 12, sellers.size());
					tpl = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_region_sellers.htm", player);
					if(page == 1)
					{
						tpl = tpl.replace("%ACTION_GO_LEFT%", "");
						tpl = tpl.replace("%GO_LIST%", "");
						tpl = tpl.replace("%NPAGE%", "1");
					}
					else
					{
						tpl = tpl.replace("%ACTION_GO_LEFT%", "bypass _bbsreglist_" + townId + "_" + type + "_" + (page - 1) + "_" + byItem + "_" + search);
						tpl = tpl.replace("%NPAGE%", String.valueOf(page));
						sb = new StringBuilder("");
						
						for(ep = page > 10 ? page - 10 : 1;ep < page;++ep)
						{
							sb.append("<td><a action=\"bypass _bbsreglist_").append(townId).append("_").append(type).append("_").append(ep).append("_").append(byItem).append("_").append(search).append("\"> ").append(ep).append(" </a> </td>\n\n");
						}
						
						tpl = tpl.replace("%GO_LIST%", sb.toString());
					}
					
					int pages = Math.max(sellers.size() / 12, 1);
					if(sellers.size() > pages * 12)
					{
						++pages;
					}
					
					if(pages <= page)
					{
						tpl = tpl.replace("%ACTION_GO_RIGHT%", "");
						tpl = tpl.replace("%GO_LIST2%", "");
					}
					else
					{
						tpl = tpl.replace("%ACTION_GO_RIGHT%", "bypass _bbsreglist_" + townId + "_" + type + "_" + (page + 1) + "_" + byItem + "_" + search);
						ep = Math.min(page + 10, pages);
						StringBuilder goList = new StringBuilder("");
						
						for(i = page + 1;i <= ep;++i)
						{
							goList.append("<td><a action=\"bypass _bbsreglist_").append(townId).append("_").append(type).append("_").append(i).append("_").append(byItem).append("_").append(search).append("\"> ").append(i).append(" </a> </td>\n\n");
						}
						
						tpl = tpl.replace("%GO_LIST2%", goList.toString());
					}
					
					StringBuilder seller_list = new StringBuilder("");
					tpl = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_region_stpl.htm", player);
					
					for(i = start;i < end;++i)
					{
						Player seller = sellers.get(i);
						List<TradeItem> tl = seller.getTradeList();
						List<ManufactureItem> cl = seller.getCreateList();
						if(tl != null || cl != null)
						{
							desc = tpl.replace("%view_bypass%", "bypass _bbsregview_" + townId + "_" + type + "_" + page + "_" + seller.getObjectId() + "_" + byItem + "_" + search);
							desc = desc.replace("%seller_name%", seller.getName());
							String title = "-";
							if(type == 0)
							{
								title = tl != null && seller.getSellStoreName() != null && !seller.getSellStoreName().isEmpty() ? seller.getSellStoreName() : "-";
							}
							else if(type == 1)
							{
								title = tl != null && seller.getBuyStoreName() != null && !seller.getBuyStoreName().isEmpty() ? seller.getBuyStoreName() : "-";
							}
							else if(type == 2 && seller.getPrivateStoreType() == 5)
							{
								title = cl != null && seller.getManufactureName() != null && !seller.getManufactureName().isEmpty() ? seller.getManufactureName() : "-";
							}
							
							title = title.replace("<", "");
							title = title.replace(">", "");
							title = title.replace("&", "");
							title = title.replace("$", "");
							if(title.isEmpty())
							{
								title = "-";
							}
							
							desc = desc.replace("%seller_title%", title);
							seller_list.append(desc);
						}
					}
					
					tpl = tpl.replace("%SELLER_LIST%", seller_list.toString());
					tpl = tpl.replace("%search_bypass%", "_bbsregsearch_" + townId + "_" + type);
					tpl = tpl.replace("%TREE%", "&nbsp;>&nbsp;<a action=\"bypass _bbsregion_" + townId + "\">" + StringHolder.getInstance().getNotNull(player, town.getTownNameAddr()) + "</a>&nbsp;>&nbsp;" + _regionTypes[type]);
					ShowBoard.separateAndSend(tpl, player);
				}
				else if("bbsregview".equals(cmd))
				{
					townId = Integer.parseInt(st.nextToken());
					type = Integer.parseInt(st.nextToken());
					page = Integer.parseInt(st.nextToken());
					int sellerObjectId = Integer.parseInt(st.nextToken());
					byItem = Integer.parseInt(st.nextToken());
					search = st.hasMoreTokens() ? st.nextToken().toLowerCase() : "";
					town = _towns[townId];
					Player seller = World.getPlayer(sellerObjectId);
					if(seller == null || seller.getPrivateStoreType() == 0)
					{
						onBypassCommand(player, "_bbsreglist_" + townId + "_" + type + "_" + page + "_" + byItem + "_" + search);
						return;
					}
					
					tpl = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_region_storetpl.htm", player);
					sb = new StringBuilder("");
					List sl;
					ItemTemplate item;
					String stpl;
					String title = "-";
					if(type < 2)
					{
						sl = type == 0 ? seller.getSellList() : seller.getBuyList();
						List<TradeItem> tl = seller.getTradeList();
						if(sl == null || sl.isEmpty() || tl == null)
						{
							onBypassCommand(player, "_bbsreglist_" + townId + "_" + type + "_" + page + "_" + byItem + "_" + search);
							return;
						}
						
						if(type == 0 && seller.getSellStoreName() != null && !seller.getSellStoreName().isEmpty())
						{
							title = seller.getSellStoreName();
						}
						else if(type == 1 && seller.getBuyStoreName() != null && !seller.getBuyStoreName().isEmpty())
						{
							title = seller.getBuyStoreName();
						}
						
						Iterator var18 = sl.iterator();
						
						block:
						while(true)
						{
							TradeItem ti;
							do
							{
								if(!var18.hasNext())
								{
									break block;
								}
								
								ti = (TradeItem) var18.next();
								item = ItemHolder.getInstance().getTemplate(ti.getItemId());
							}
							while(item == null);
							
							stpl = tpl.replace("%item_name%", item.getName() + (item.isEquipment() && ti.getEnchantLevel() > 0 ? " +" + ti.getEnchantLevel() : ""));
							stpl = stpl.replace("%item_img%", item.getIcon());
							stpl = stpl.replace("%item_count%", String.valueOf(ti.getCount()));
							stpl = stpl.replace("%item_price%", String.format("%,3d", ti.getOwnersPrice()).replace(" ", ","));
							desc = "";
							if(item.getCrystalType() != Grade.NONE)
							{
								desc = _grade[item.getCrystalType().ordinal() - 1];
								desc = desc + (item.getCrystalCount() > 0 ? (player.isLangRus() ? " Кристаллов: " : " Crystals: ") + item.getCrystalCount() + ";&nbsp;" : ";&nbsp;");
							}
							
							if(item.isEquipment())
							{
								if(ti.getAttackElement() >= 0 && ti.getAttackElementValue() > 0)
								{
									desc = desc + "&$1620;: " + _elements[ti.getAttackElement()] + " +" + ti.getAttackElementValue();
								}
								else if(ti.getDefenceFire() > 0 || ti.getDefenceWater() > 0 || ti.getDefenceWind() > 0 || ti.getDefenceEarth() > 0 || ti.getDefenceHoly() > 0 || ti.getDefenceUnholy() > 0)
								{
									desc = desc + "&$1651;:";
									if(ti.getDefenceFire() > 0)
									{
										desc = desc + " &$1622; +" + ti.getDefenceFire() + ";&nbsp;";
									}
									
									if(ti.getDefenceWater() > 0)
									{
										desc = desc + " &$1623; +" + ti.getDefenceWater() + ";&nbsp;";
									}
									
									if(ti.getDefenceWind() > 0)
									{
										desc = desc + " &$1624; +" + ti.getDefenceWind() + ";&nbsp;";
									}
									
									if(ti.getDefenceEarth() > 0)
									{
										desc = desc + " &$1625; +" + ti.getDefenceEarth() + ";&nbsp;";
									}
									
									if(ti.getDefenceHoly() > 0)
									{
										desc = desc + " &$1626; +" + ti.getDefenceHoly() + ";&nbsp;";
									}
									
									if(ti.getDefenceUnholy() > 0)
									{
										desc = desc + " &$1627; +" + ti.getDefenceUnholy() + ";&nbsp;";
									}
								}
							}
							
							if(item.isStackable())
							{
								desc = desc + (player.isLangRus() ? "Стыкуемый;&nbsp;" : "Stackable;&nbsp;");
							}
							
							if(item.isSealedItem())
							{
								desc = desc + (player.isLangRus() ? "Запечатанный;&nbsp;" : "Sealed;&nbsp;");
							}
							
							if(item.isShadowItem())
							{
								desc = desc + (player.isLangRus() ? "Теневой предмет;&nbsp;" : "Shadow item;&nbsp;");
							}
							
							if(item.isTemporal())
							{
								desc = desc + (player.isLangRus() ? "Временный;&nbsp;" : "Temporal;&nbsp;");
							}
							
							stpl = stpl.replace("%item_desc%", desc);
							sb.append(stpl);
						}
					}
					else
					{
						sl = seller.getCreateList();
						if(sl == null)
						{
							onBypassCommand(player, "_bbsreglist_" + townId + "_" + type + "_" + page + "_" + byItem + "_" + search);
							return;
						}
						
						if((title = seller.getManufactureName()) == null)
						{
							title = "-";
						}
						
						Iterator var43 = sl.iterator();
						
						while(var43.hasNext())
						{
							ManufactureItem mi = (ManufactureItem) var43.next();
							Recipe rec = RecipeHolder.getInstance().getRecipeById(mi.getRecipeId() - 1);
							if(rec != null && !rec.getProducts().isEmpty())
							{
								item = (ItemTemplate) ((Pair) rec.getProducts().get(0)).getKey();
								if(item != null)
								{
									stpl = tpl.replace("%item_name%", item.getName());
									stpl = stpl.replace("%item_img%", item.getIcon());
									stpl = stpl.replace("%item_count%", "N/A");
									stpl = stpl.replace("%item_price%", String.format("%,3d", mi.getCost()).replace(" ", ","));
									desc = "";
									if(item.getCrystalType() != Grade.NONE)
									{
										desc = _grade[item.getCrystalType().ordinal() - 1] + (item.getCrystalCount() > 0 ? (player.isLangRus() ? " Кристаллов: " : " Crystals: ") + item.getCrystalCount() + ";&nbsp;" : ";&nbsp;");
									}
									
									if(item.isStackable())
									{
										desc = player.isLangRus() ? "Стыкуемый;&nbsp;" : "Stackable;&nbsp;";
									}
									
									if(item.isSealedItem())
									{
										desc = desc + (player.isLangRus() ? "Запечатанный;&nbsp;" : "Sealed;&nbsp;");
									}
									
									stpl = stpl.replace("%item_desc%", desc);
									sb.append(stpl);
								}
							}
						}
					}
					
					String html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_region_view.htm", player);
					html = html.replace("%sell_type%", _regionTypes[type]);
					title = title.replace("<", "");
					title = title.replace(">", "");
					title = title.replace("&", "");
					title = title.replace("$", "");
					if(title.isEmpty())
					{
						title = "-";
					}
					
					html = html.replace("%title%", title);
					html = html.replace("%char_name%", seller.getName());
					html = html.replace("%object_id%", String.valueOf(seller.getObjectId()));
					html = html.replace("%STORE_LIST%", sb.toString());
					html = html.replace("%list_bypass%", "_bbsreglist_" + townId + "_" + type + "_" + page + "_" + byItem + "_" + search);
					html = html.replace("%TREE%", "&nbsp;>&nbsp;<a action=\"bypass _bbsregion_" + townId + "\">" + StringHolder.getInstance().getNotNull(player, town.getTownNameAddr()) + "</a>&nbsp;>&nbsp;<a action=\"bypass _bbsreglist_" + townId + "_" + type + "_" + page + "_" + byItem + "_\">" + _regionTypes[type] + "</a>&nbsp;>&nbsp;" + seller.getName());
					ShowBoard.separateAndSend(html, player);
				}
				else if("bbsregtarget".equals(cmd))
				{
					townId = Integer.parseInt(st.nextToken());
					Player seller = World.getPlayer(townId);
					if(seller != null)
					{
						player.sendPacket(new RadarControl(0, 2, seller.getLoc()));
						if(player.knowsObject(seller))
						{
							player.setObjectTarget(seller);
							seller.broadcastRelationChanged();
						}
					}
					else
					{
						player.sendActionFailed();
					}
				}
			}
		}
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		if("bbsregsearch".equals(cmd))
		{
			int townId = Integer.parseInt(st.nextToken());
			int type = Integer.parseInt(st.nextToken());
			String byItem = "Item".equals(arg4) ? "1" : "0";
			if(arg3 == null)
			{
				arg3 = "";
			}
			
			arg3 = arg3.replace("<", "");
			arg3 = arg3.replace(">", "");
			arg3 = arg3.replace("&", "");
			arg3 = arg3.replace("$", "");
			if(arg3.length() > 30)
			{
				arg3 = arg3.substring(0, 30);
			}
			
			onBypassCommand(player, "_bbsreglist_" + townId + "_" + type + "_1_" + byItem + "_" + arg3);
		}
	}
	
	private static class PlayersComparator<T> implements Comparator<T>
	{
		@Override
		public int compare(Object o1, Object o2)
		{
			if(o1 instanceof Player && o2 instanceof Player)
			{
				Player p1 = (Player) o1;
				Player p2 = (Player) o2;
				return p1.getName().compareTo(p2.getName());
			}
			else
			{
				return 0;
			}
		}
	}
	
	private static class TownEntry
	{
		private final String _townNameAddr;
		private final int _gx;
		private final int _gy;
		
		private TownEntry(String townNameAddr, int gx, int gy)
		{
			_townNameAddr = townNameAddr;
			_gx = gx;
			_gy = gy;
		}
		
		public String getTownNameAddr()
		{
			return _townNameAddr;
		}
		
		public int getX()
		{
			return _gx;
		}
		
		public int getY()
		{
			return _gy;
		}
	}
}