/*
 * $Id: CallResultOutputter.java,v 1.59 2012/03/03 10:41:19 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;

import org.xins.common.MandatoryArgumentChecker;
import org.w3c.dom.Element;
import org.xins.common.xml.ElementFormatter;

import org.znerd.xmlenc.XMLEncoder;

/**
 * Converter that can be used by calling conventions to generate responses
 * which are compatible with the XINS standard calling convention.
 *
 * <p>The result output is always in the UTF-8 encoding.
 *
 * @version $Revision: 1.59 $ $Date: 2012/03/03 10:41:19 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.5.0
 */
public final class CallResultOutputter {

   /**
    * The first output for each output conversion. Never <code>null</code>.
    */
   private static final char[] DOCUMENT_PREFACE =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result".toCharArray();

   /**
    * The output for the new-style calling convention in case success is
    * false, just before the name of the error code.
    * Never <code>null</code>.
    */
   private static final char[] ERRORCODE_IS =
      " errorcode=\"".toCharArray();

   /**
    * The output just before a parameter name. Never <code>null</code>.
    */
   private static final char[] PARAM_PREFACE = "<param name=\"".toCharArray();

   /**
    * The output right after a parameter value. Never <code>null</code>.
    */
   private static final char[] PARAM_SUFFIX = "</param>".toCharArray();

   /**
    * The final output for each output conversion. Never <code>null</code>.
    */
   private static final char[] DOCUMENT_SUFFIX = "</result>".toCharArray();

   /**
    * An <code>XMLEncoder</code> for the UTF-8 encoding. Initialized by the
    * class initialized and then never <code>null</code>.
    */
   private static final XMLEncoder XML_ENCODER;

   static {
      try {
         XML_ENCODER = XMLEncoder.getEncoder("UTF-8");
      } catch (UnsupportedEncodingException exception) {
         Error error = new Error(exception);
         throw error;
      }
   }

   /**
    * Constructs a new <code>CallResultOutputter</code> object.
    */
   private CallResultOutputter() {
      // empty
   }

   /**
    * Generates XML for the specified call result. The XML is sent to the
    * specified output stream.
    *
    * @param out
    *    the output stream to send the XML to, cannot be <code>null</code>.
    *
    * @param result
    *    the call result to convert to XML, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>out      == null
    *          || result   == null</code>.
    *
    * @throws IOException
    *    if there was an I/O error while writing to the output stream.
    */
   public static void output(Writer out, FunctionResult result)
   throws IllegalArgumentException, IOException {

      // Check preconditions
      MandatoryArgumentChecker.check("out", out, "result", result);

      // Output the declaration
      out.write(DOCUMENT_PREFACE);

      // Output the start of the <result> element
      String code = result.getErrorCode();
      if (code == null) {
         out.write('>');
      } else {
         out.write(ERRORCODE_IS);
         out.write(code);
         out.write('"');
         out.write('>');
      }

      // Write the output parameters, if any
      Map<String, String> params = result.getParameters();
      for (String paramName : params.keySet()) {
         if (paramName != null && paramName.length() > 0) {
            String v = params.get(paramName);
            if (v != null && v.length() > 0) {
               out.write(PARAM_PREFACE);
               XML_ENCODER.text(out, paramName, true);
               out.write('"');
               out.write('>');
               XML_ENCODER.text(out, v, true);
               out.write(PARAM_SUFFIX);
            }
         }
      }

      // Write the data element, if any
      Element dataElement = result.getDataElement();
      if (dataElement != null) {
         String dataXML = ElementFormatter.format(dataElement);
         out.write(dataXML);
      }

      // End the root element <result>
      out.write(DOCUMENT_SUFFIX);
   }
}
