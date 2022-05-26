/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.script.javascript.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.StringFunctions;
import net.rptools.maptool.client.functions.TokenLightFunctions;
import net.rptools.maptool.client.functions.TokenPropertyFunctions;
import net.rptools.maptool.client.script.javascript.*;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenProperty;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.parser.ParserException;
import org.graalvm.polyglot.HostAccess;

public class JSAPIToken implements MapToolJSAPIInterface {
  @Override
  public String serializeToString() {
    return token.getId().toString();
  }

  private final Token token;
  private Set<String> names;
  private Iterator<String> names_iter;

  public JSAPIToken(Token token) {
    this.token = token;
  }

  public JSAPIToken(String tid) {
    this(MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(new GUID(tid)));
  }

  @HostAccess.Export
  public String getName() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getName();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getName"));
  }

  @HostAccess.Export
  public void setName(String name) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      if (name.isEmpty()) {
        throw new ParserException(
            I18N.getText("macro.function.tokenName.emptyTokenNameForbidden", "setName"));
      }
      token.validateName(name);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setName, name);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setName"));
    }
  }

  @HostAccess.Export
  public boolean hasSight() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getHasSight();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "hasSight"));
  }

  @HostAccess.Export
  public void setSight(boolean sight) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setHasSight, sight);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setSight"));
    }
  }

  @HostAccess.Export
  public String toString() {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return "Token(id=" + token.getId() + ")";
    }
    // Maybe this should throw a ParserException instead?
    return "Token(id=accessdenied)";
  }

  @HostAccess.Export
  public String getId() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return "" + token.getId();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setSight"));
  }

  @HostAccess.Export
  public Object getProperty(String name) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      // TODO: Rework this function to not rely on MTScript (store a Function?)
      return token.getEvaluatedProperty(name);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getProperty"));
    }
  }

  @HostAccess.Export
  public Object getRawProperty(String name) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return "" + token.getProperty(name);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getRawProperty"));
    }
  }

  @HostAccess.Export
  public void setProperty(String name, Object value) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      // TODO: Figure out a way to store Objects in properties (only limitation is this
      //  serverCommand business, wasn't there some innovation in serialization?)
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setProperty, name, value.toString());
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setProperty"));
    }
  }

  @HostAccess.Export
  public String getPropertyType() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getPropertyType();
    }
    throw new ParserException(
        I18N.getText("macro.function.initiative.gmOrOwner", "getPropertyType"));
  }

  @HostAccess.Export
  public void setPropertyType(String type) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setPropertyType, type);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "setPropertyType"));
    }
  }

  @HostAccess.Export
  public Set<String> getPropertyNames(boolean raw) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return raw ? token.getPropertyNamesRaw() : token.getPropertyNames();
    }
    throw new ParserException(
        I18N.getText("macro.function.initiative.gmOrOwner", "getPropertyNames"));
  }

  @HostAccess.Export
  public Set<String> getMatchingProperties(String pattern) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      Set<String> namesList = new HashSet<String>();
      Pattern pat = Pattern.compile(pattern);
      Set<String> propSet = token.getPropertyNames();
      String[] propArray = new String[propSet.size()];
      propSet.toArray(propArray);
      Arrays.sort(propArray);

      for (String name : propArray) {
        Matcher m = pat.matcher(name);
        if (m.matches()) {
          namesList.add(name);
        }
      }

      return namesList;
    }
    throw new ParserException(
        I18N.getText("macro.function.initiative.gmOrOwner", "getMatchingProperties"));
  }

  @HostAccess.Export
  public Set<String> getAllPropertyNames(String type) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      if (type == null || type.length() == 0 || type.equals("*")) {
        Map<String, List<TokenProperty>> pmap =
            MapTool.getCampaign().getCampaignProperties().getTokenTypeMap();
        Set<String> namesList = new HashSet<String>();

        for (Entry<String, List<TokenProperty>> entry : pmap.entrySet()) {
          for (TokenProperty tp : entry.getValue()) {
            namesList.add(tp.getName());
          }
        }

        return namesList;
      } else {
        List<TokenProperty> props =
            MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(type);
        if (props == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.tokenProperty.unknownPropType", "getAllPropertyNames", type));
        }
        Set<String> namesList = new HashSet<String>();
        for (TokenProperty tp : props) {
          namesList.add(tp.getName());
        }

        return namesList;
      }
    }
    throw new ParserException(
        I18N.getText("macro.function.initiative.gmOrOwner", "getAllPropertyNames"));
  }

  @HostAccess.Export
  public boolean hasProperty(String name) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      Object val = token.getProperty(name);
      if (val == null) {
        return false;
      }
      return !val.toString().isEmpty();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "hasProperty"));
  }

  @HostAccess.Export
  public boolean isNPC() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getType() == Token.Type.NPC;
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "isNPC"));
  }

  @HostAccess.Export
  public boolean isPC() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getType() == Token.Type.PC;
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "isPC"));
  }

  @HostAccess.Export
  public void setPC() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setPC);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setPC"));
    }
  }

  @HostAccess.Export
  public void setNPC() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setNPC);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setNPC"));
    }
  }

  @HostAccess.Export
  public String getLayer() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getLayer().name();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getLayer"));
  }

  @HostAccess.Export
  public String setLayer(String layerName, boolean forceShape) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      // TODO: Refactor remaining TokenPropertyFunctions calls
      Zone.Layer layer = TokenPropertyFunctions.getLayer(layerName);
      Token.TokenShape tokenShape = TokenPropertyFunctions.getTokenShape(token, layer, forceShape);

      if (tokenShape != null) {
        MapTool.serverCommand()
            .updateTokenProperty(
                token, Token.Update.setLayerShape, layer.name(), tokenShape.name());
      } else {
        MapTool.serverCommand().updateTokenProperty(token, Token.Update.setLayer, layer.name());
      }
      return layer.name();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setLayer"));
  }

  @HostAccess.Export
  public String getSize() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      // TODO: Refactor remaining TokenPropertyFunctions calls
      return TokenPropertyFunctions.getSize(token);
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getSize"));
  }

  @HostAccess.Export
  public void setSize(String size) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      TokenPropertyFunctions.setSize(token, size);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setSize"));
    }
  }

  @HostAccess.Export
  public void resetSize() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      TokenPropertyFunctions.resetSize(token);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "genericFunction"));
    }
  }

  @HostAccess.Export
  public Set<String> getOwners() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getOwners();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getOwners"));
  }

  @HostAccess.Export
  public boolean isOwnedByAll() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.isOwnedByAll();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "isOwnedByAll"));
  }

  @HostAccess.Export
  public boolean isOwner() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.isOwner(playerId);
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "isOwner"));
  }

  @HostAccess.Export
  public boolean isOwner(String playerName) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.isOwner(playerName);
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "isOwner"));
  }

  @HostAccess.Export
  public void resetProperty(String property) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.resetProperty, property);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "resetProperty"));
    }
  }

  @HostAccess.Export
  public boolean isPropertyEmpty(String name) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getProperty(name) == null;
    }
    throw new ParserException(
        I18N.getText("macro.function.initiative.gmOrOwner", "isPropertyEmpty"));
  }

  @HostAccess.Export
  public String getGMNotes() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    // String playerId = MapTool.getPlayer().getName();
    if (trusted /* || token.isOwner(playerId)*/) {
      String notes = token.getGMNotes();
      return notes != null ? notes : "";
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOnly", "getGMNotes"));
  }

  @HostAccess.Export
  public void setGMNotes(String gmNotes) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    // String playerId = MapTool.getPlayer().getName();
    if (trusted /* || token.isOwner(playerId)*/) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setGMNotes, gmNotes);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOnly", "setGMNotes"));
    }
  }

  @HostAccess.Export
  public String getNotes() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      String notes = token.getNotes();
      return notes != null ? notes : "";
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getNotes"));
  }

  @HostAccess.Export
  public void setNotes(String notes) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setNotes, notes);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setNotes"));
    }
  }

  @HostAccess.Export
  public void bringToFront() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      Zone zone = token.getZoneRenderer().getZone();
      int zOrder = zone.getLargestZOrder() + 1;
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setZOrder, zOrder);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "bringToFront"));
    }
  }

  @HostAccess.Export
  public void sendToBack() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      Zone zone = token.getZoneRenderer().getZone();
      int zOrder = zone.getSmallestZOrder() - 1;
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setZOrder, zOrder);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "sendToBack"));
    }
  }

  @HostAccess.Export
  public int getTokenFacing() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      if (token.getFacing() == null) {
        return -1; // Has to be an int, so might as well be what was desired
      }
      return token.getFacing();
    }
    throw new ParserException(
        I18N.getText("macro.function.initiative.gmOrOwner", "getTokenFacing"));
  }

  @HostAccess.Export
  public int getTokenRotation() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getFacingInDegrees();
    }
    throw new ParserException(
        I18N.getText("macro.function.initiative.gmOrOwner", "getTokenRotation"));
  }

  @HostAccess.Export
  public void setTokenFacing(int facing) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    // String playerId = MapTool.getPlayer().getName();
    if (trusted /* || token.isOwner(playerId)*/) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setFacing, facing);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "setTokenFacing"));
    }
  }

  @HostAccess.Export
  public void removeTokenFacing() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    // String playerId = MapTool.getPlayer().getName();
    if (trusted /* || token.isOwner(playerId)*/) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setFacing, (Integer) null);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "removeTokenFacing"));
    }
  }

  @HostAccess.Export
  public boolean isSnapToGrid() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.isSnapToGrid();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "isSnapToGrid"));
  }

  @HostAccess.Export
  public boolean isFlippedX() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.isFlippedX();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "isFlippedX"));
  }

  @HostAccess.Export
  public boolean isFlippedY() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.isFlippedY();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "isFlippedY"));
  }

  @HostAccess.Export
  public boolean isFlippedIso() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.isFlippedIso();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "isFlippedIso"));
  }

  @HostAccess.Export
  public void setOwner(String ownerName) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      // Remove current owners, but if this macro is untrusted and the current player is an owner,
      // keep the
      // ownership there.
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.clearAllOwners);
      if (!ownerName.isEmpty()) {
        MapTool.serverCommand().updateTokenProperty(token, Token.Update.addOwner, ownerName);
      }
      // Do nothing when trusted, since all ownership should be turned off for an empty string
      // used in such a macro.

      // If not trusted we must have been in the owner list -- keep us there.
      if (!trusted) {
        MapTool.serverCommand().updateTokenProperty(token, Token.Update.addOwner, playerId);
      }
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setOwner"));
    }
  }

  @HostAccess.Export
  public void setOwner(List<String> ownerNames) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      // Remove current owners, but if this macro is untrusted and the current player is an owner,
      // keep the
      // ownership there.
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.clearAllOwners);

      for (String ownerName : ownerNames) {
        MapTool.serverCommand().updateTokenProperty(token, Token.Update.addOwner, ownerName);
      }

      // If not trusted we must have been in the owner list -- keep us there.
      if (!trusted) {
        MapTool.serverCommand().updateTokenProperty(token, Token.Update.addOwner, playerId);
      }
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setOwner"));
    }
  }

  @HostAccess.Export
  public void setOwnedByAll(boolean ownedByAll) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    // String playerId = MapTool.getPlayer().getName();
    if (trusted /* || token.isOwner(playerId)*/) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setOwnedByAll, ownedByAll);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOnly", "setOwnedByAll"));
    }
  }

  @HostAccess.Export
  public String getTokenShape() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getShape().name();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getTokenShape"));
  }

  @HostAccess.Export
  public void setTokenShape(String shape) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      Token.TokenShape newShape =
          Token.TokenShape.valueOf(shape.toUpperCase().trim().replace(" ", "_"));
      // do some error checking, maybe?
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setShape, newShape.name());
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "setTokenShape"));
    }
  }

  @HostAccess.Export
  public int getTokenNativeWidth() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getWidth();
    }
    throw new ParserException(
        I18N.getText("macro.function.initiative.gmOrOwner", "getTokenNativeWidth"));
  }

  @HostAccess.Export
  public int getTokenNativeHeight() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getHeight();
    }
    throw new ParserException(
        I18N.getText("macro.function.initiative.gmOrOwner", "getTokenNativeHeight"));
  }

  @HostAccess.Export
  public int getTokenWidth() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      ZoneRenderer zoneR = token.getZoneRenderer();
      Zone zone = zoneR.getZone();

      // Get the pixel width or height of a given token
      Rectangle tokenBounds = token.getBounds(zone);

      return tokenBounds.width;
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getTokenWidth"));
  }

  @HostAccess.Export
  public int getTokenHeight() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      ZoneRenderer zoneR = token.getZoneRenderer();
      Zone zone = zoneR.getZone();

      // Get the pixel width or height of a given token
      Rectangle tokenBounds = token.getBounds(zone);

      return tokenBounds.height;
    }
    throw new ParserException(
        I18N.getText("macro.function.initiative.gmOrOwner", "getTokenHeight"));
  }

  @HostAccess.Export
  public void setTokenWidth(double width) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      ZoneRenderer zoneR = token.getZoneRenderer();
      Zone zone = zoneR.getZone();

      Rectangle tokenBounds = token.getBounds(zone);

      double newScaleX;
      double newScaleY;
      newScaleX = width / token.getWidth();
      newScaleY = (double) tokenBounds.height / token.getHeight();

      MapTool.serverCommand()
          .updateTokenProperty(token, Token.Update.setScaleXY, newScaleX, newScaleY);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "setTokenWidth"));
    }
  }

  @HostAccess.Export
  public void setTokenHeight(double height) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      ZoneRenderer zoneR = token.getZoneRenderer();
      Zone zone = zoneR.getZone();

      Rectangle tokenBounds = token.getBounds(zone);

      double newScaleX;
      double newScaleY;
      newScaleX = (double) tokenBounds.width / token.getWidth();
      newScaleY = height / token.getHeight();

      MapTool.serverCommand()
          .updateTokenProperty(token, Token.Update.setScaleXY, newScaleX, newScaleY);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "setTokenHeight"));
    }
  }

  @HostAccess.Export
  public void setTokenSnapToGrid(boolean snapToGrid) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setSnapToGrid, snapToGrid);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "setTokenSnapToGrid"));
    }
  }

  @HostAccess.Export
  public void flipTokenX() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.flipX);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "flipTokenX"));
    }
  }

  @HostAccess.Export
  public void flipTokenY() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.flipY);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "flipTokenY"));
    }
  }

  @HostAccess.Export
  public void flipTokenIso() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.flipIso);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "flipTokenIso"));
    }
  }

  @HostAccess.Export
  public HashMap<String, Double> getTokenLayoutProps() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      HashMap<String, Double> tokenLayoutProps = new HashMap<>();
      tokenLayoutProps.put("scale", token.getSizeScale());
      tokenLayoutProps.put("xOffset", (double) token.getAnchorX());
      tokenLayoutProps.put("yOffset", (double) token.getAnchorY());
      return tokenLayoutProps;
    }
    throw new ParserException(
        I18N.getText("macro.function.initiative.gmOrOwner", "getTokenLayoutProps"));
  }

  @HostAccess.Export
  public void setTokenLayoutProps(Double scale, Integer xOffset, Integer yOffset)
      throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      if (scale == null) {
        scale = token.getSizeScale();
      }
      if (xOffset == null) {
        xOffset = token.getAnchorX();
      }
      if (yOffset == null) {
        yOffset = token.getAnchorY();
      }

      MapTool.serverCommand()
          .updateTokenProperty(token, Token.Update.setLayout, scale, xOffset, yOffset);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "setTokenLayoutProps"));
    }
  }

  @HostAccess.Export
  public boolean getAllowsURIAccess() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getAllowURIAccess();
    }
    throw new ParserException(
        I18N.getText("macro.function.initiative.gmOrOwner", "getAllowsURIAccess"));
  }

  @HostAccess.Export
  public void setAllowsURIAccess(boolean allowsURIAccess) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    // String playerId = MapTool.getPlayer().getName();
    if (trusted /* || token.isOwner(playerId)*/) {
      if (!Token.isValidLibTokenName(token.getName())) {
        throw new ParserException(
            I18N.getText("macro.setAllowsURIAccess.notLibToken", token.getName()));
      }
      var libraryManager = new LibraryManager();
      String name = token.getName().substring(4);
      if (libraryManager.usesReservedPrefix(name)) {
        throw new ParserException(
            I18N.getText(
                "macro.setAllowsURIAccess.reservedPrefix", libraryManager.getReservedPrefix(name)));
      } else if (libraryManager.usesReservedName(name)) {
        throw new ParserException(
            I18N.getText("macro.setAllowsURIAccess.reserved", token.getName()));
      }
      // TODO: This should be a server command
      token.setAllowURIAccess(allowsURIAccess);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "setAllowsURIAccess"));
    }
  }

  @HostAccess.Export
  public int getX() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return this.token.getX();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getX"));
  }

  @HostAccess.Export
  public int getY() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return this.token.getY();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getY"));
  }

  @HostAccess.Export
  public void setX(int x) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      this.token.setX(x);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setXY, x, token.getY());
    } else {

      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setX"));
    }
  }

  @HostAccess.Export
  public void setY(int y) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      this.token.setY(y);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setXY, token.getX(), y);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setY"));
    }
  }

  @HostAccess.Export
  public void setAllStates(boolean val) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setAllStates, val);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setAllStates"));
    }
  }

  @HostAccess.Export
  public boolean getState(String state) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      if (!MapTool.getCampaign().getTokenStatesMap().containsKey(state)) {
        throw new ParserException(
            I18N.getText("macro.function.tokenStateFunctions.unknownState", state));
      }

      // TODO: investigate the behind-the-scenes type situation.
      //  With the standardization of JS, I think it would be worth it to store
      //  specific types instead of Objects. Maybe give Token Properties a type selector in MapTool?
      // Object val = token.getState(state);
      // return (getBooleanFromValue(val));
      return (Boolean) token.getState(state);
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getState"));
  }

  @HostAccess.Export
  public void setState(String state, boolean val) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      if (!MapTool.getCampaign().getTokenStatesMap().containsKey(state)) {
        throw new ParserException(
            I18N.getText("macro.function.tokenStateFunctions.unknownState", state));
      }
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setState, state, val);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setState"));
    }
  }

  @HostAccess.Export
  public boolean hasLightSource() throws ParserException {
    return hasLightSource("*", "*");
  }

  @HostAccess.Export
  public boolean hasLightSource(String type) throws ParserException {
    return hasLightSource(type, "*");
  }

  @HostAccess.Export
  public boolean hasLightSource(String type, String name) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return TokenLightFunctions.hasLightSource(token, type, name);
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "hasLightSource"));
  }

  @HostAccess.Export
  public void clearLights() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.clearLightSources);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.initiative.gmOrOwner", "clearLights"));
    }
  }

  @HostAccess.Export
  public void setLight(String category, String name, boolean value) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      TokenLightFunctions.setLight(token, category, name, value ? BigDecimal.ONE : BigDecimal.ZERO);
    } else {
      throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "setLight"));
    }
  }

  @HostAccess.Export
  public List<String> getLights() throws ParserException {
    return getLights("*");
  }

  @HostAccess.Export
  public List<String> getLights(String category) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      ArrayList<String> lightList = new ArrayList<>();
      Map<String, Map<GUID, LightSource>> lightSourcesMap =
          MapTool.getCampaign().getLightSourcesMap();

      if (category == null || category.equals("*")) {
        for (Map<GUID, LightSource> lsMap : lightSourcesMap.values()) {
          for (LightSource ls : lsMap.values()) {
            if (token.hasLightSource(ls)) {
              lightList.add(ls.getName());
            }
          }
        }
      } else {
        if (lightSourcesMap.containsKey(category)) {
          for (LightSource ls : lightSourcesMap.get(category).values()) {
            if (token.hasLightSource(ls)) {
              lightList.add(ls.getName());
            }
          }
        } else {
          throw new ParserException(
              I18N.getText("macro.function.tokenLight.unknownLightType", "getLights", category));
        }
      }
      return lightList;
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getLights"));
  }


}
