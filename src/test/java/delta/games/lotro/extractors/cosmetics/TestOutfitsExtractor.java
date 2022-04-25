package delta.games.lotro.extractors.cosmetics;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.character.CharacterFile;
import delta.games.lotro.character.CharactersManager;
import delta.games.lotro.character.cosmetics.OutfitsManager;
import delta.games.lotro.character.cosmetics.io.xml.OutfitsIO;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.dat.wlib.AttributeDefinition;
import delta.games.lotro.dat.wlib.ClassDefinition;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.dat.wlib.ValueType;
import junit.framework.TestCase;

/**
 * Outfits data extractor.
 * @author DAM
 */
public class TestOutfitsExtractor extends TestCase
{
  private ClassDefinition _outfitRegistry;
  private ClassDefinition _outfit;
  private ClassDefinition _outfitData;

  /**
   * Constructor.
   */
  public TestOutfitsExtractor()
  {
    init();
  }

  private void init()
  {
    _outfitRegistry=buildOutfitRegistryClass();
    _outfit=buildOutfitClass();
    _outfitData=buildOutfitDataClass();
  }

  private PropertiesSet buildPlayerProperties()
  {
    PropertiesSet props=new PropertiesSet();
    setValue(props,"Outfit_ActiveOutfitType",PropertyType.INT,Integer.valueOf(3));
    setValue(props,"Outfit_InventorySlotVisibility",PropertyType.INT,Integer.valueOf(2097020));
    setValue(props,"Outfit_Outfit1SlotVisibility",PropertyType.INT,Integer.valueOf(2097150));
    setValue(props,"Outfit_Outfit2SlotVisibility",PropertyType.INT,Integer.valueOf(4194110));
    return props;
  }

  private void setValue(PropertiesSet props, String propertyName, PropertyType type, Object value)
  {
    PropertyDefinition def=new PropertyDefinition(100,propertyName,type);
    PropertyValue propValue=new PropertyValue(def,value,null);
    props.setProperty(propValue);
  }

  private ClassInstance buildOutfitRegistry()
  {
     Map<Integer,ClassInstance> map=new HashMap<Integer,ClassInstance>();
     map.put(Integer.valueOf(1),buildOutfitElements159149());
     map.put(Integer.valueOf(2),buildOutfitElements159156());
     map.put(Integer.valueOf(3),buildOutfitElements159159());

     ClassInstance ret=new ClassInstance(_outfitRegistry);
     AttributeDefinition mapAttr=_outfitRegistry.getAttributeByName("64160996");
     ret.setAttributeValue(mapAttr,map);
     return ret;
  }

  private ClassInstance buildOutfitElements159159()
  {
    Map<Integer,ClassInstance> map=new HashMap<Integer,ClassInstance>();
    map.put(Integer.valueOf(32),buildOutfitDataInstance(1879377579,0.0f));
    map.put(Integer.valueOf(4),buildOutfitDataInstance(1879379233,0.0f));
    map.put(Integer.valueOf(2),buildOutfitDataInstance(1879377577,0.0f));

    ClassInstance ret=new ClassInstance(_outfit);
    AttributeDefinition mapAttr=_outfit.getAttributeByName("249837057");
    ret.setAttributeValue(mapAttr,map);
    return ret;
  }

  private ClassInstance buildOutfitElements159149()
  {
    Map<Integer,ClassInstance> map=new HashMap<Integer,ClassInstance>();
    map.put(Integer.valueOf(2),buildOutfitDataInstance(1879229515,0.55f));
    map.put(Integer.valueOf(4),buildOutfitDataInstance(1879215196,0.0f));
    map.put(Integer.valueOf(65536),buildOutfitDataInstance(1879094727,0.0f));
    map.put(Integer.valueOf(128),buildOutfitDataInstance(1879215195,0.0f));
    map.put(Integer.valueOf(131072),buildOutfitDataInstance(1879169513,0.0f));
    ClassInstance ret=new ClassInstance(_outfit);
    AttributeDefinition mapAttr=_outfit.getAttributeByName("249837057");
    ret.setAttributeValue(mapAttr,map);
    return ret;
  }

  private ClassInstance buildOutfitElements159156()
  {
    Map<Integer,ClassInstance> map=new HashMap<Integer,ClassInstance>();
    map.put(Integer.valueOf(2),buildOutfitDataInstance(1879052266,0.2f));
    ClassInstance ret=new ClassInstance(_outfit);
    AttributeDefinition mapAttr=_outfit.getAttributeByName("249837057");
    ret.setAttributeValue(mapAttr,map);
    return ret;
  }

  private ClassInstance buildOutfitDataInstance(int did, float color)
  {
    ClassInstance ret=new ClassInstance(_outfitData);
    AttributeDefinition didAttr=_outfitData.getAttributeByName("m_didItem");
    ret.setAttributeValue(didAttr,Integer.valueOf(did));
    AttributeDefinition colorAttr=_outfitData.getAttributeByName("m_fColor");
    ret.setAttributeValue(colorAttr,Float.valueOf(color));
    return ret;
  }

  private ClassDefinition buildOutfitDataClass()
  {
    ClassDefinition ret=new ClassDefinition(100,"OutfitData");
    // m_didItem
    AttributeDefinition didItem=new AttributeDefinition(ret,"m_didItem",0,ValueType.INTEGER);
    ret.addAttribute(didItem);
    // m_fColor
    AttributeDefinition color=new AttributeDefinition(ret,"m_fColor",1,ValueType.FLOAT);
    ret.addAttribute(color);
    return ret;
  }

  private ClassDefinition buildOutfitClass()
  {
    ClassDefinition ret=new ClassDefinition(101,"Outfit");
    // 249837057
    AttributeDefinition map=new AttributeDefinition(ret,"249837057",0,ValueType.REFERENCE);
    ret.addAttribute(map);
    return ret;
  }

  private ClassDefinition buildOutfitRegistryClass()
  {
    ClassDefinition ret=new ClassDefinition(102,"OutfitRegistry");
    // 64160996
    AttributeDefinition map=new AttributeDefinition(ret,"64160996",0,ValueType.REFERENCE);
    ret.addAttribute(map);
    return ret;
  }

  /**
   * Test outfit import and XML I/O.
   */
  public void testOutfitImport()
  {
    OutfitsExtractor extractor=new OutfitsExtractor();
    PropertiesSet playerProps=buildPlayerProperties();
    ClassInstance outfitsRegistry=buildOutfitRegistry();
    OutfitsManager outfitsMgr=extractor.extract(playerProps,outfitsRegistry);
    System.out.println(outfitsMgr);
    CharacterFile file=CharactersManager.getInstance().getToonById("Landroval","Kargarth");
    OutfitsIO.saveOutfits(file,outfitsMgr);
    OutfitsManager outfitsMgrReloaded=OutfitsIO.loadOutfits(file);
    System.out.println(outfitsMgrReloaded);
    //OutfitsIO.saveOutfits(file,outfitsMgrReloaded);
  }
}
