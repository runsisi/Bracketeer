package com.chookapp.org.bracketeer.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.common.IHintConfiguration;
import com.chookapp.org.bracketeer.preferences.PreferencesConstants;

public class ProcessorConfiguration implements IPropertyChangeListener
{
    

    public class PairConfiguration
    {
        private RGB[] _fgColors;
        private RGB[] _bgColors;
        
        private int _surroundingPairsCount;
        private boolean _surroundingPairsEnable;
        private String _surroundingPairsToInclude;
        
        private boolean _hoveredPairsEnable;
        
        public PairConfiguration()
        {
            _fgColors = new RGB[PreferencesConstants.MAX_PAIRS];
            _bgColors = new RGB[PreferencesConstants.MAX_PAIRS];
        }

        /* setters */
        
        public void setColor(boolean foregound, int colorIndex, RGB color)
        {
            if(foregound)
                _fgColors[colorIndex] = color;
            else
                _bgColors[colorIndex] = color;
        }
        
        public void setEnableSurrounding(boolean enable)
        {
            _surroundingPairsEnable = enable;
        }

        public void setEnableHovering(boolean enable)
        {
            _hoveredPairsEnable = enable;
        }
        
        public void setSurroundingPairsCount(int count)
        {
            _surroundingPairsCount = count;
        }
        
        public void setSurroundingPairsToInclude(String pairs)
        {
            _surroundingPairsToInclude = pairs;
        }
        
        /* getters */
                
        public RGB getColor(boolean foregound, int colorIndex)
        {
            if(foregound)
                return _fgColors[colorIndex];
            else
                return _bgColors[colorIndex];
        }

        public boolean isSurroundingPairsEnabled()
        {
            return _surroundingPairsEnable;
        }
        
        public int getSurroundingPairsCount()
        {
            return _surroundingPairsCount;
        }
        
        public String getSurroundingPairsToInclude()
        {
            return _surroundingPairsToInclude;
        }

        public boolean isHoveredPairsEnabled()
        {
            return _hoveredPairsEnable;
        }

   
        
    }
    
    public class SingleBracketConfiguration
    {
        private RGB _fgColor;
        private RGB _bgColor;
        
        public SingleBracketConfiguration()
        {
        }
        
        /* setters */
        
        public void setColor(boolean foregound, RGB color)
        {
            if(foregound)
                _fgColor = color;
            else
                _bgColor = color;
        }
        
        /* getters */
        
        public RGB getColor(boolean foregound)
        {
            if(foregound)
                return _fgColor;
            else
                return _bgColor;
        }
    }
    
    public class HintConfiguration implements IHintConfiguration
    {
        private HashMap<String, HashMap<String, Object>> _attrMaps; // the "Object" is either a String or a RGB
        
        public HintConfiguration()
        {
            _attrMaps = new HashMap<String, HashMap<String, Object>>();
        }

        public boolean getBoolAttr(String type, String attr)
        {
            return Boolean.parseBoolean(getStringAttr(type,attr));
        }
        
        public int getIntAttr(String type, String attr)
        {
            return Integer.parseInt(getStringAttr(type,attr));
        }
        
        public String getStringAttr(String type, String attr)
        {
            return (String) getAttr(type,attr);
        }
        
        public Object getAttr(String type, String attr)
        {
            HashMap<String, Object> attrMap = _attrMaps.get(type);
            if( attrMap == null )
                return null;
            return attrMap.get(attr);
        }
        
        public void setAttr(String type, String attr, Object val)
        {
            HashMap<String,Object> attrMap = _attrMaps.get(type);
            if( attrMap == null )
            {
                attrMap = new HashMap<String, Object>();
                _attrMaps.put(type, attrMap);
            }
            
            attrMap.put(attr, val);
        }

        public boolean isShowAlways(String type)
        {
            return (getAttr(type, PreferencesConstants.Hints.WhenToShow.Criteria.ATTR) ==
                    PreferencesConstants.Hints.WhenToShow.Criteria.VAL_ALWAYS);
        }
        
        public boolean isShowOnlyOnHover(String type)
        {
            return (getAttr(type, PreferencesConstants.Hints.WhenToShow.Criteria.ATTR) ==
                    PreferencesConstants.Hints.WhenToShow.Criteria.VAL_HOVER);
        }
        
        public RGB getColor(String type, boolean foreground)
        {
            return (RGB) getAttr(type, foreground ? PreferencesConstants.Hints.Font.FG_COLOR :
                                                    PreferencesConstants.Hints.Font.BG_COLOR );
        }
        
        public void setColor(String type, boolean foreground, RGB color)
        {
            setAttr(type, foreground ? PreferencesConstants.Hints.Font.FG_COLOR :
                                       PreferencesConstants.Hints.Font.BG_COLOR, 
                    color);
        }

        public String formatText(String type, String txt)
        {
            if( getBoolAttr(type, PreferencesConstants.Hints.Display.STRIP_WHITESPACE) )
                txt = txt.replaceAll("[\\t ]+", "");
            txt = performEllipsis(type, txt);
            txt = " /* " + txt + " */";
            return txt;
        }
        
        public boolean isItalic(String type)
        {
            return getBoolAttr(type, PreferencesConstants.Hints.Font.ITALIC);
        }
        

        public int getMinLineDistance(String type)
        {
            return getIntAttr(type, PreferencesConstants.Hints.WhenToShow.MIN_LINES_DISTANCE);
        }

        
        private String performEllipsis(String type, String txt)
        {
            String elip = getStringAttr(type, PreferencesConstants.Hints.Display.Ellipsis.ATTR);
            int maxLen = getIntAttr(type, PreferencesConstants.Hints.Display.MAX_LENGTH);

            if( txt.length() <= maxLen )
                return txt;
            
            if( elip.equals(PreferencesConstants.Hints.Display.Ellipsis.VAL_END))
            {
                txt = txt.substring(0, maxLen-3);
                txt = txt + "...";
            }
            else if( elip.equals(PreferencesConstants.Hints.Display.Ellipsis.VAL_MID))
            {
                int partLen = (maxLen-3)/2;
                txt = txt.substring(0, partLen) + "..." + txt.substring(txt.length()-(partLen+1));
            }
            else
            {
                throw new IllegalArgumentException("Unknown ellipsis function: " + elip);
            }
            
            return txt;
        }

      

    }
    
    private PairConfiguration _pairConf;
    private SingleBracketConfiguration _singleConf;
    private HintConfiguration _hintConf;
    private String _name;
    
    private IPreferenceStore _prefStore;
    
    private List<IProcessorConfigurationListener> _listeners;
    private List<String> _hintTypes;
    
    public ProcessorConfiguration(IConfigurationElement confElement)
    {
        _pairConf = new PairConfiguration();
        _singleConf = new SingleBracketConfiguration();
        _hintConf = new HintConfiguration();
        
        _hintTypes = new ArrayList<String>();
        
        _name = confElement.getAttribute("name");
        IConfigurationElement[] hints = confElement.getChildren("Hint");
        for (IConfigurationElement hint : hints)
        {
            _hintTypes.add(hint.getAttribute("type"));            
        }
        
        List<IPreferenceStore> stores= new ArrayList<IPreferenceStore>();        
        stores.add(Activator.getDefault().getPreferenceStore());
        
        _prefStore = new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
        _prefStore.addPropertyChangeListener(this);

        _listeners = new LinkedList<IProcessorConfigurationListener>();
        
        updateConfiguartion();
    }
    
    public String getName()
    {
        return _name;
    }
    
    public PairConfiguration getPairConfiguration()
    {
        return _pairConf;
    }    
    
    public SingleBracketConfiguration getSingleBracketConfiguration()
    {
        return _singleConf;
    }
    
    public HintConfiguration getHintConfiguration()
    {
        return _hintConf; 
    }
    
    private void updateConfiguartion()
    {
        updateHighlightConf();
        updateHintConf();
        
        /* notify listeners */
        
        for( IProcessorConfigurationListener listener : _listeners )
        {
            listener.configurationUpdated();
        }
    }

    private void updateHintConf()
    {
       
        String defaultBase = PreferencesConstants.preferencePath(_name) +
                PreferencesConstants.Hints.preferencePath(PreferencesConstants.Hints.DEFAULT_TYPE);
        
        for (String hintType : _hintTypes)
        {
            String prefBase = PreferencesConstants.preferencePath(_name) +
                    PreferencesConstants.Hints.preferencePath(hintType);
            
            /* When to show */
            
            String[] listOfAttrs = {PreferencesConstants.Hints.WhenToShow.MIN_LINES_DISTANCE,
                                    PreferencesConstants.Hints.WhenToShow.Criteria.ATTR};
            String baseToUse = prefBase;
            if( _prefStore.getBoolean( prefBase + PreferencesConstants.Hints.WhenToShow.USE_DEFAULT ) )
                baseToUse = defaultBase;
            
            for (String attr : listOfAttrs)
            {
                _hintConf.setAttr(hintType, attr, _prefStore.getString(baseToUse + attr));
            }
            
            /* Font */ 
            
            listOfAttrs = new String[] {PreferencesConstants.Hints.Font.ITALIC};
            baseToUse = prefBase;
            String typeToUse = hintType;
            if( _prefStore.getBoolean( prefBase + PreferencesConstants.Hints.Font.USE_DEFAULT ) )
            {
                typeToUse = PreferencesConstants.Hints.DEFAULT_TYPE;
                baseToUse = defaultBase;
            }
          
            _hintConf.setColor(hintType, true, getHintColor(typeToUse, true));
            _hintConf.setColor(hintType, false, getHintColor(typeToUse, false));
            for (String attr : listOfAttrs)
            {
                _hintConf.setAttr(hintType, attr, _prefStore.getString(baseToUse + attr));
            }
            
            /* Display */
            
            listOfAttrs = new String[] {PreferencesConstants.Hints.Display.MAX_LENGTH,
                                        PreferencesConstants.Hints.Display.STRIP_WHITESPACE,
                                        PreferencesConstants.Hints.Display.Ellipsis.ATTR};
            baseToUse = prefBase;
            if( _prefStore.getBoolean( prefBase + PreferencesConstants.Hints.Display.USE_DEFAULT ) )
            {
                baseToUse = defaultBase;
            }
            for (String attr : listOfAttrs)
            {
                _hintConf.setAttr(hintType, attr, _prefStore.getString(baseToUse + attr));
            }
        }
    }

    private RGB getHintColor(String type, boolean foreground)
    {
        String prefBase = PreferencesConstants.Hints.preferencePath(_name, type);
        String suffix = foreground ? PreferencesConstants.Hints.Font.FG_DEFAULT : PreferencesConstants.Hints.Font.BG_DEFAULT;
        if( _prefStore.getBoolean(prefBase + suffix) )
            return null;
        
        suffix = foreground ? PreferencesConstants.Hints.Font.FG_COLOR : PreferencesConstants.Hints.Font.BG_COLOR;
        return PreferenceConverter.getColor(_prefStore, prefBase + suffix);
    }

    private void updateHighlightConf()
    {
        RGB[] defColor = new RGB[2]; // 0 - BG, 1 - FG 

        defColor[0] = null;
        defColor[1] = null;
        
        String prefBase = PreferencesConstants.preferencePath(_name);

        for( int fgIndex = 0; fgIndex < 2; fgIndex++ )
        {
            boolean foregound = (fgIndex == 1);  

            /* default */ 
            
            if( !_prefStore.getBoolean( prefBase +
                                        PreferencesConstants.Highlights.getAttrPath(0, foregound) +
                                        PreferencesConstants.Highlights.UseDefault ) )
            {
                defColor[fgIndex] = PreferenceConverter.getColor(_prefStore, prefBase +
                                                                             PreferencesConstants.Highlights.getAttrPath(0, foregound) +
                                                                             PreferencesConstants.Highlights.Color );
            }
            
            /* pair */
            
            for (int pairIdx = 0; pairIdx < PreferencesConstants.MAX_PAIRS; pairIdx++)
            {
                if( _prefStore.getBoolean( prefBase +
                                           PreferencesConstants.Highlights.getAttrPath(pairIdx+1, foregound) +
                                           PreferencesConstants.Highlights.UseDefault ) )
                {
                    _pairConf.setColor(foregound, pairIdx, defColor[fgIndex]);
                }
                else
                {
                    _pairConf.setColor(foregound, pairIdx, 
                                       PreferenceConverter.getColor( _prefStore, prefBase +
                                                                                 PreferencesConstants.Highlights.getAttrPath(pairIdx+1, foregound) +
                                                                                 PreferencesConstants.Highlights.Color ) );
                }
            }
            
            _pairConf.setEnableSurrounding(_prefStore.getBoolean(prefBase +
                                                                 PreferencesConstants.Surrounding.Enable));
            _pairConf.setEnableHovering(_prefStore.getBoolean(prefBase +
                                                              PreferencesConstants.Hovering.Enable));
            _pairConf.setSurroundingPairsCount(_prefStore.getInt(prefBase +
                                                                 PreferencesConstants.Surrounding.NumBracketsToShow));
            _pairConf.setSurroundingPairsToInclude(_prefStore.getString(prefBase +
                                                                        PreferencesConstants.Surrounding.ShowBrackets));
            
            /* single */ 
            
            int pairIdx = PreferencesConstants.MAX_PAIRS + 1;
            
            if( _prefStore.getBoolean( prefBase +
                                       PreferencesConstants.Highlights.getAttrPath(pairIdx, foregound) +
                                       PreferencesConstants.Highlights.UseDefault ) )
            {
                _singleConf.setColor(foregound, defColor[fgIndex]);
            }
            else
            {
                _singleConf.setColor(foregound, 
                                     PreferenceConverter.getColor( _prefStore, prefBase +
                                                                               PreferencesConstants.Highlights.getAttrPath(pairIdx, foregound) +
                                                                               PreferencesConstants.Highlights.Color ) );
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        updateConfiguartion();
    }

    public void addListener(IProcessorConfigurationListener listener)
    {
        _listeners.add(listener);
    }
    
    public void removeListener(IProcessorConfigurationListener listener)
    {
        if( !_listeners.remove(listener) )
            Activator.log("listener was not found");
    }


  
}
