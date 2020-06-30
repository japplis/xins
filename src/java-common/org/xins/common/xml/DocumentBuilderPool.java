package org.xins.common.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class used to provide DocumentBuilder objects.
 *
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 3.0
 */
public class DocumentBuilderPool {

   private final static int MAX_POOL_SIZE = 100;

   private static DocumentBuilderPool INSTANCE = null;

   private Stack<DocumentBuilder> builderPool = new Stack<DocumentBuilder>();

   private final Object builderLock = new Object();

   private DocumentBuilderPool() {
   }

   public static synchronized DocumentBuilderPool getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new DocumentBuilderPool();
      }
      return INSTANCE;
   }

   public DocumentBuilder getBuilder() {
      DocumentBuilder builder = null;
      synchronized (builderLock) {
         if (!builderPool.empty()) {
            //SoftReference<DocumentBuilder> refBuilder = builderPool.pop();
            builder = builderPool.pop();
         }
      }
      if (builder == null) {
         builder = createDocumentBuilder();
      } else {
         builder.reset();
         initDocumentBuilder(builder);
      }
      return builder;
   }

   public void releaseBuilder(DocumentBuilder builder) {
      // no need to synchronized as Stack is synchronized
      if (!builderPool.contains(builder) && builderPool.size() < MAX_POOL_SIZE) {
         builderPool.push(builder);
      }
   }

   /**
    * Creates a DocumentBuilder.
    *
    * @return
    *    a newly created DocumentBuilder.
    */
   private DocumentBuilder createDocumentBuilder() {
      try {
         DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         builderFactory.setIgnoringElementContentWhitespace(true);
         builderFactory.setNamespaceAware(true);
         builderFactory.setValidating(false);
         DocumentBuilder builder = builderFactory.newDocumentBuilder();
         initDocumentBuilder(builder);
         return builder;
      } catch (ParserConfigurationException ex) {
         // Without Dom the program should fail
         // TODO logging
         throw new IllegalStateException(("Failed to create a DOM document builder: " + ex.getMessage()));
      }
   }

   private DocumentBuilder initDocumentBuilder(DocumentBuilder builder) {
      builder.setEntityResolver(new EntityResolver() {
         public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
             return new InputSource(new ByteArrayInputStream(new byte[0]));
         }
      });
      return builder;
   }
}
