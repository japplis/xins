/*
 * $Id: StatisticsInterceptor.java,v 1.3 2013/01/14 11:14:30 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xins.common.collections.InvalidPropertyValueException;
import org.xins.common.collections.MissingRequiredPropertyException;
import org.xins.common.manageable.BootstrapException;
import org.xins.common.spec.FunctionSpec;
import org.xins.common.spec.InvalidSpecificationException;
import org.xins.common.text.DateConverter;

/**
 * Maintain the call statistics.
 *
 * @since xins 3.0
 *
 * @version $Revision: 1.3 $ $Date: 2013/01/14 11:14:30 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class StatisticsInterceptor extends Interceptor {

   protected Map<String, FunctionStatistics> statistics = new LinkedHashMap<String, FunctionStatistics>();

   /**
    * Last time the statistics were reset. Initially the startup timestamp.
    */
   protected long _lastStatisticsReset;

   @Override
   protected void bootstrapImpl(Map<String, String> properties)
   throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          BootstrapException {
      _lastStatisticsReset = getApi().getStartupTimestamp();
      try {
         Map<String, FunctionSpec> functions = getApi().getAPISpecification().getFunctions();
         for (String functionName: functions.keySet()) {
            statistics.put(functionName, new FunctionStatistics());
         }
      } catch (InvalidSpecificationException ex) {
         // TODO log
      }
   }

   @Override
   public FunctionResult afterFunctionCall(FunctionRequest functionRequest, FunctionResult xinsResult, HttpServletResponse httpResponse) {
      String functionName = functionRequest.getFunctionName();
      FunctionStatistics statistic = statistics.get(functionName);
      if (statistic != null) {
         long start = (Long) functionRequest.getBackpack().get(BackpackConstants.START);
         statistic.recordCall(start, xinsResult);
      }
      return xinsResult;
   }

   /**
    * Returns the call statistics for all functions in this API.
    *
    * @param detailed
    *    If <code>true</code>, the unsuccessful result will be returned sorted
    *    per error code. Otherwise the unsuccessful result won't be displayed
    *    by error code.
    *
    * @param functionName
    *    the name of the specific function to return the statistics for,
    *    if <code>null</code>, then the stats for all functions are returned.
    *
    * @return
    *    the call result, never <code>null</code>.
    */
   protected FunctionResult getStatistics(boolean detailed, String functionName) {

      // Initialize a builder
      FunctionResult builder = new FunctionResult();
      TimeZone timeZone = getApi().getTimeZone();

      builder.param("startup",   DateConverter.toDateString(timeZone, getApi().getStartupTimestamp()));
      builder.param("lastReset", DateConverter.toDateString(timeZone, _lastStatisticsReset));
      builder.param("now",       DateConverter.toDateString(timeZone, System.currentTimeMillis()));

      // Currently available processors
      Runtime rt = Runtime.getRuntime();
      builder.param("availableProcessors", String.valueOf(rt.availableProcessors()));

      // Heap memory statistics
      Element heap = builder.getDataElementBuilder().createElement("heap");
      long free  = rt.freeMemory();
      long total = rt.totalMemory();
      heap.setAttribute("used",  String.valueOf(total - free));
      heap.setAttribute("free",  String.valueOf(free));
      heap.setAttribute("total", String.valueOf(total));
      long max = rt.maxMemory();
      heap.setAttribute("max", String.valueOf(max));
      double percentageUsed = (total - free) / (double) max;
      heap.setAttribute("percentageUsed", String.valueOf((int) (percentageUsed * 100)));
      builder.getDataElement().appendChild(heap);

      // Function-specific statistics
      for (Map.Entry<String, FunctionStatistics> stat : statistics.entrySet()) {
         String statFunctionName = stat.getKey();

         // Possibly only results for a specific function are to be returned
         if (functionName != null && !functionName.equals(statFunctionName)) {
            continue;
         }

         FunctionStatistics stats = stat.getValue();

         Element functionElem = builder.getDataElementBuilder().createElement("function");
         functionElem.setAttribute("name", statFunctionName);

         // Successful
         Document functionDoc = functionElem.getOwnerDocument();
         Element successful = stats.getSuccessfulElement();
         functionElem.appendChild(functionDoc.importNode(successful, true));

         // Not Modified
         if (stats.hasNotModified()) {
            Element notModified = stats.getNotModifiedElement();
            functionElem.appendChild(functionDoc.importNode(notModified, true));
         }

         // Unsuccessful
         Element[] unsuccessful = stats.getUnsuccessfulElement(detailed);
         for(int j = 0; j < unsuccessful.length; j++) {
            functionElem.appendChild(functionDoc.importNode(unsuccessful[j], true));
         }

         builder.getDataElement().appendChild(functionElem);
      }

      return builder;
   }

   /**
    * Resets the statistics.
    *
    * @return
    *    the call result, never <code>null</code>.
    */
   protected FunctionResult resetStatistics() {

      // Remember when we last reset the statistics
      _lastStatisticsReset = System.currentTimeMillis();

      // Function-specific statistics
      for (FunctionStatistics stat : statistics.values()) {
         stat.resetStatistics();
      }
      return API.SUCCESSFUL_RESULT;
   }

   public Map<String, FunctionStatistics> getStatistics() {
      return statistics;
   }
}
