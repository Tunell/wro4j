/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.processor.algorithm.DataUriGenerator;


/**
 * Rewrites background images by replacing the url with data uri of the image. If the replacement is not successful, it
 * is left unchanged.
 * <p/>
 * For more details, @see http://en.wikipedia.org/wiki/Data_URI_scheme
 *
 * @author Alex Objelean
 * @created May 9, 2010
 */
public class CssDataUriPreProcessor
  extends AbstractCssUrlRewritingProcessor {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CssDataUriPreProcessor.class);
  /**
   * Generates dataUri based on inputStream of the url's found inside the css resource.
   */
  private DataUriGenerator dataUriGenerator;
  /**
   * Contains a {@link UriLocatorFactory} reference injected externally.
   */
  @Inject
  private UriLocatorFactory uriLocatorFactory;

  /**
   * Default constructor.
   */
  public CssDataUriPreProcessor() {
    dataUriGenerator = new DataUriGenerator();
  }

  /**
   * Replace provided url with the new url if needed.
   *
   * @param imageUrl to replace.
   * @param cssUri Uri of the parsed css.
   * @return replaced url.
   */
  @Override
  protected final String replaceImageUrl(final String cssUri, final String imageUrl) {
    LOG.debug("replace url for image: " + imageUrl + ", from css: " + cssUri);
    final String cleanImageUrl = cleanImageUrl(imageUrl);
    final String fileName = FilenameUtils.getName(imageUrl);
    final String fullPath = FilenameUtils.getFullPath(cssUri) + cleanImageUrl;
    String result = imageUrl;
    try {
      final UriLocator uriLocator = uriLocatorFactory.getInstance(fullPath);
      final String dataUri = dataUriGenerator.generateDataURI(uriLocator.locate(fullPath), fileName);
      if (replaceWithDataUri(dataUri)) {
        result = dataUri;
      }
    } catch (final IOException e) {
      LOG.warn("Couldn't extract dataUri from:" + fullPath + ", because: " + e.getMessage());
    }
    return result;
  }


  /**
   * Decides if the computed dataUri should replace the image url. It is useful when you want to limit the dataUri size.
   * By default the size of dataUri is limited to 32KB (because IE8 has a 32KB limitation).
   *
   * @param dataUri base64 encoded stream.
   * @return true if dataUri should replace original image url.
   */
  protected boolean replaceWithDataUri(final String dataUri) throws UnsupportedEncodingException {
    final byte[] bytes = dataUri.getBytes("UTF8");
    final int limit = 32 * 1024;
    return bytes.length < limit;
  }
}
