/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.sass.SassCss;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.ObjectFactory;


/**
 * A processor using sass engine:
 * 
 * @author Alex Objelean
 * @created 27 Oct 2010
 */
@SupportedResourceType(ResourceType.CSS)
public class SassCssProcessor
  implements ResourceProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(SassCssProcessor.class);
  public static final String ALIAS = "sassCss";
  
  private ObjectPoolHelper<SassCss> enginePool;

  /**
   * default constructor that sets the engine used to RHINO for backwards compatibility.
   */
  public SassCssProcessor() {
    enginePool = new ObjectPoolHelper<SassCss>(new ObjectFactory<SassCss>() {
      @Override
      public SassCss create() {
        return newEngine();
      }
    });
  }
  
  
  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final String content = IOUtils.toString(reader);
    final SassCss engine = enginePool.getObject();
    try {
      writer.write(newEngine().process(content));
    } catch (final WroRuntimeException e) {
      onException(e);
      writer.write(content);
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + SassCss.class.getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
      try {
        enginePool.returnObject(engine);
      } catch (final Exception e) {
        //should never happen
        LOG.error("Cannot return lessCss engine to the pool", e);
      }
    }
  }
  
  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final WroRuntimeException e) {
  }
  
  /**
   * Method for processing with Rhino based engine
   * 
   * @param content
   * @return
   */
  protected SassCss newEngine() {
    return new SassCss();
  }
}
