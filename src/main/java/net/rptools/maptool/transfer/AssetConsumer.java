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
package net.rptools.maptool.transfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.server.proto.AssetChunkDto;

/**
 * Receiving end of AssetProducer
 *
 * @author trevor
 */
public class AssetConsumer {
  private File destinationDir;
  private AssetHeader header;
  private long currentPosition;

  /**
   * Create a new asset consumer, it will prepare a place to receive the incoming data chunks. When
   * complete the resulting file can be found at getFilename()
   *
   * @param destinationDir - location to store the incoming file
   * @param header - from the corresponding AssetProducer
   */
  public AssetConsumer(File destinationDir, AssetHeader header) {
    if (header == null) {
      throw new IllegalArgumentException("Header cannot be null");
    }
    if (destinationDir == null) {
      destinationDir = new File(".");
    }
    this.destinationDir = destinationDir;
    this.header = header;
    // Setup
    if (!destinationDir.exists()) {
      destinationDir.mkdirs();
    }
    // Cleanup
    if (getFilename().exists()) {
      getFilename().delete();
    }
  }

  /** @return the ID of the incoming asset */
  public MD5Key getId() {
    return header.getId();
  }

  public String getName() {
    return header.getName();
  }

  /**
   * Add the next chunk of data to this consumer
   *
   * @param chunk produced from the corresponding AssetProducer
   * @throws IOException if the file exists but is a directory rather than a regular file, does not
   *     exist but cannot be created, or cannot be opened for any other reason
   */
  public void update(AssetChunkDto chunk) throws IOException {
    File file = getFilename();
    byte[] data = chunk.getData().toByteArray();
    try (FileOutputStream out = new FileOutputStream(file, true)) {
      out.write(data);
    }
    currentPosition += data.length;
  }

  /**
   * Whether all the data has been transferred
   *
   * @return true if all data been transferred
   */
  public boolean isComplete() {
    return currentPosition >= header.getSize();
  }

  public double getPercentComplete() {
    return currentPosition / (double) header.getSize();
  }

  public long getSize() {
    return header.getSize();
  }

  /**
   * When complete this will point to the file containing the data
   *
   * @return the file with the data
   */
  public File getFilename() {
    return new File(destinationDir.getAbsolutePath() + "/" + header.getId() + ".part");
  }
}
