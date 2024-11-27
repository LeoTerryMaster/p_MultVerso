package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractFileParser;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.RecipeHolder;
import l2.gameserver.model.Recipe;
import l2.gameserver.templates.item.ItemTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class RecipeParser extends AbstractFileParser<RecipeHolder>
{
	private static final Logger LOG = LoggerFactory.getLogger(RecipeParser.class);
	private static final RecipeParser INSTANCE = new RecipeParser(RecipeHolder.getInstance());
	
	protected RecipeParser(RecipeHolder holder)
	{
		super(holder);
	}
	
	public static RecipeParser getInstance()
	{
		return INSTANCE;
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/recipe.xml");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "recipes.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator recipeListElementIt = rootElement.elementIterator();
		while(recipeListElementIt.hasNext())
		{
			Element recipeListElement = (Element) recipeListElementIt.next();
			if(!"recipe".equalsIgnoreCase(recipeListElement.getName()))
				continue;
			int recipeId = Integer.parseInt(recipeListElement.attributeValue("id"));
			int minCraftSkillLvl = Integer.parseInt(recipeListElement.attributeValue("level"));
			int craftMpConsume = Integer.parseInt(recipeListElement.attributeValue("mp_consume"));
			int successRate = Integer.parseInt(recipeListElement.attributeValue("success_rate"));
			int recipeItemId = Integer.parseInt(recipeListElement.attributeValue("item_id"));
			ItemTemplate recipeItem = ItemHolder.getInstance().getTemplate(recipeItemId);
			Recipe.ERecipeType recipeType = Boolean.parseBoolean(recipeListElement.attributeValue("is_common")) ? Recipe.ERecipeType.ERT_COMMON : Recipe.ERecipeType.ERT_DWARF;
			ArrayList<Pair<ItemTemplate, Long>> materials = new ArrayList<>();
			ArrayList<Pair<ItemTemplate, Long>> products = new ArrayList<>();
			ArrayList<Pair<ItemTemplate, Long>> npcFees = new ArrayList<>();
			Iterator recipeElementIt = recipeListElement.elementIterator();
			while(recipeElementIt.hasNext())
			{
				Element recipeElement = (Element) recipeElementIt.next();
				if("materials".equalsIgnoreCase(recipeElement.getName()))
				{
					Iterator recipeMaterialsElementIt = recipeElement.elementIterator();
					while(recipeMaterialsElementIt.hasNext())
					{
						Element recipeMaterialElement = (Element) recipeMaterialsElementIt.next();
						Pair<ItemTemplate, Long> material = parseItem(recipeMaterialElement);
						if(material == null)
							continue;
						materials.add(material);
					}
					continue;
				}
				if("products".equalsIgnoreCase(recipeElement.getName()))
				{
					Iterator recipeProductElementIt = recipeElement.elementIterator();
					while(recipeProductElementIt.hasNext())
					{
						Element recipeProductElement = (Element) recipeProductElementIt.next();
						Pair<ItemTemplate, Long> newProduct = parseItem(recipeProductElement);
						if(newProduct == null)
							continue;
						products.add(newProduct);
					}
					continue;
				}
				if(!"npc_fee".equalsIgnoreCase(recipeElement.getName()))
					continue;
				Iterator npcFeeElementIt = recipeElement.elementIterator();
				while(npcFeeElementIt.hasNext())
				{
					Element npcFeeElement = (Element) npcFeeElementIt.next();
					Pair<ItemTemplate, Long> npcFee = parseItem(npcFeeElement);
					if(npcFee == null)
						continue;
					npcFees.add(npcFee);
				}
			}
			if(recipeItem == null)
			{
				LOG.warn("Skip recipe " + recipeId);
				continue;
			}
			if(products.isEmpty())
			{
				LOG.warn("Recipe " + recipeId + " have empty product list. Skip");
				continue;
			}
			if(products.size() > 1)
			{
				LOG.warn("Recipe " + recipeId + " have more than one product. Skip");
			}
			if(materials.isEmpty())
			{
				LOG.warn("Recipe " + recipeId + " have empty material list. Skip");
				continue;
			}
			Recipe recipe = new Recipe(recipeId, recipeItem, recipeType, minCraftSkillLvl, craftMpConsume, successRate, Collections.unmodifiableList(materials), Collections.unmodifiableList(products), Collections.unmodifiableList(npcFees));
			getHolder().addRecipe(recipe);
		}
	}
	
	private Pair<ItemTemplate, Long> parseItem(Element itemElement)
	{
		if(!"item".equalsIgnoreCase(itemElement.getName()))
		{
			return null;
		}
		int itemId = Integer.parseInt(itemElement.attributeValue("id"));
		ItemTemplate itemTemplate = ItemHolder.getInstance().getTemplate(itemId);
		if(itemElement == null)
		{
			return null;
		}
		long itemCount = Long.parseLong(itemElement.attributeValue("count"));
		return Pair.of(itemTemplate, itemCount);
	}
}