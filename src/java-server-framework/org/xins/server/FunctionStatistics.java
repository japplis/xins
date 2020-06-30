/*
 * $Id: FunctionStatistics.java,v 1.31 2013/01/14 11:14:30 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.log4j.NDC;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xins.common.text.DateConverter;
import org.xins.common.xml.ElementFormatter;

/**
 * Statistics of a function.
 *
 * <p>The implementation of this class is thread-safe.
 *
 * @version $Revision: 1.31 $ $Date: 2013/01/14 11:14:30 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.0.0
 */
class FunctionStatistics {

   /**
    * String to insert instead of a figure when the figure is unavailable.
    */
   private static final String NOT_AVAILABLE = "N/A";

   /**
    * The time zone used when generating dates for output.
    */
   private static final TimeZone TIME_ZONE = TimeZone.getDefault();

   /**
    * Constructs a new <code>FunctionStatistics</code> instance.
    */
   FunctionStatistics() {
      _successful          = new Statistic();
      _unsuccessful        = new Statistic();
      _notModified         = new Statistic();
      _errorCodeStatistics = new TreeMap();
   }

   /**
    * Statistics for the successful calls. Never <code>null</code>.
    */
   private final Statistic _successful;

   /**
    * Statistic over the unsuccessful calls. Never <code>null</code>.
    */
   private final Statistic _unsuccessful;

   /**
    * Statistic over the not modified results. Never <code>null</code>.
    */
   private final Statistic _notModified;

   /**
    * Statistics over the unsuccessful calls sorted by error code.
    * The key of the map is the error code and the Statistic object
    * corresponding to the error code. Never <code>null</code>.
    */
   private final Map<String, Statistic>  _errorCodeStatistics;

   /**
    * Callback method that may be called after a call to this function. This
    * method will store statistics-related information.
    *
    * <p />This method does not <em>have</em> to be called. If statistics
    * gathering is disabled, then this method should not be called.
    *
    * @param start
    *    the start time, in milliseconds since the UNIX Epoch.
    *
    * @param xinsResult
    *    the result of the function call, cannot be <code>null</code>.
    */
   final synchronized void recordCall(long start, FunctionResult xinsResult) {

      long duration = System.currentTimeMillis() - start;

      // Call resulted in not modified
      if (xinsResult instanceof NotModifiedResult) {

         _notModified.recordCall(start, duration);

      // Call succeeded
      } else if (xinsResult.getErrorCode() == null) {

         _successful.recordCall(start, duration);

      // Call failed
      } else {
         _unsuccessful.recordCall(start, duration);

         String errorCode = xinsResult.getErrorCode();
         Statistic errorCodeStat = _errorCodeStatistics.get(errorCode);
         if (errorCodeStat == null) {
            errorCodeStat = new Statistic();
         }
         errorCodeStat.recordCall(start, duration);
         _errorCodeStatistics.put(errorCode, errorCodeStat);
      }
   }

   /**
    * Resets the statistics for this function.
    */
   final synchronized void resetStatistics() {
      _successful.reset();
      _unsuccessful.reset();
      _notModified.reset();
      _errorCodeStatistics.clear();
   }

   /**
    * Get the successful statistic as an {@link org.xins.common.xml.Element}.
    *
    * @return
    *    the successful element, cannot be <code>null</code>
    */
   public synchronized Element getSuccessfulElement() {
      return _successful.getElement("successful", null);
   }

   /**
    * Indicates whether not modified has been returned by the function.
    *
    * @return
    *    <code>true</code> is not modified has been returned at least once, <code>false</code> otherwise
    */
   public synchronized boolean hasNotModified() {
      return _notModified._calls > 0;
   }

   /**
    * Get the not modified statistic as an {@link org.xins.common.xml.Element}.
    *
    * @return
    *    the not modified element, cannot be <code>null</code>
    */
   public synchronized Element getNotModifiedElement() {
      return _notModified.getElement("not-modified", null);
   }

   /**
    * Get the unsuccessful statistics as an array of {@link org.xins.common.xml.Element}.
    *
    * @param detailed
    *    If <code>true</code>, the unsuccessful results will be returned
    *    per error code. Otherwise only one unsuccessful containing all
    *    unsuccessful result will be returned.
    *
    * @return
    *    the successful element, cannot be empty.
    */
   public synchronized Element[] getUnsuccessfulElement(boolean detailed) {
      if (!detailed || _errorCodeStatistics.isEmpty()) {
         Element[] result = new Element[1];
         result[0] = _unsuccessful.getElement("unsuccessful", null);
         return result;
      } else {
         Element[] result = new Element[_errorCodeStatistics.size()];
         int i = 0;
         for (String nextErrorCode : _errorCodeStatistics.keySet()) {
            Statistic nextStat = _errorCodeStatistics.get(nextErrorCode);
            result[i] = nextStat.getElement("unsuccessful", nextErrorCode);
            i++;
         }
         return result;
      }
   }

   /**
    * Group of statistics data.
    *
    * <p>The implementation of this class is thread-safe.
    *
    * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
    *
    * @since XINS 1.1.0
    */
   private static final class Statistic {

      /**
       * The number of calls executed up until now. Initially <code>0L</code>.
       */
      private int _calls;

      /**
       * The start time of the most recent call. Initially <code>0L</code>.
       */
      private long _lastStart;

      /**
       * The duration of the most recent call. Initially <code>0L</code>.
       */
      private long _lastDuration;

      /**
       * The context identifier of the most recent call. Initially empty.
       */
      private String _lastContextId;

      /**
       * The total duration of all calls up until now. Initially
       * <code>0L</code>.
       */
      private long _duration;

      /**
       * The minimum time a call took. Initially set to
       * {@link Long#MAX_VALUE}.
       */
      private long _min = Long.MAX_VALUE;

      /**
       * The start time of the call that took the shortest. Initially
       * <code>0L</code>.
       */
      private long _minStart;

      /**
       * The context identifier of the call that took the shortest. Initially empty.
       */
      private String _minContextId;

      /**
       * The duration of the call that took the longest. Initially
       * <code>0L</code>.
       */
      private long _max;

      /**
       * The start time of the call that took the longest. Initially
       * <code>0L</code>.
       */
      private long _maxStart;

      /**
       * The context identifier of the call that took the longest. Initially empty.
       */
      private String _maxContextId;

      /**
       * Constructs a new <code>Statistic</code> object.
       */
      private Statistic() {
         _min = Long.MAX_VALUE;
      }

      /**
       * Records a call.
       *
       * @param start
       *    the start time, in milliseconds since the UNIX Epoch, not
       *    <code>null</code>.
       *
       * @param duration
       *    duration of the call, in milliseconds since the
       *    <a href="http://en.wikipedia.org/wiki/Unix_Epoch">UNIX Epoch</a>.
       */
      public synchronized void recordCall(long start, long duration) {
         _lastStart    = start;
         _lastDuration = duration;
         _calls++;
         _duration += duration;
         _min      = _min > duration ? duration : _min;
         _max      = _max < duration ? duration : _max;
         _minStart = (_min == duration) ? start : _minStart;
         _maxStart = (_max == duration) ? start : _maxStart;
         _lastContextId = NDC.peek();
         if (_min == duration) {
            _minContextId = _lastContextId;
         }
         if (_max == duration) {
            _maxContextId = _lastContextId;
         }
      }

      /**
       * Get this statistic as an {@link Element}.
       *
       * @param successful
       *    true if the result is successful, false otherwise.
       * @param errorCode
       *    the errorCode of the unsuccessful result, if you want it also
       *    specified in the returned element.
       *
       * @return
       *    the statistic, cannot be <code>null</code>
       */
      public synchronized Element getElement(String name, String errorCode) {

         String average;
         String min;
         String minStart;
         String max;
         String maxStart;
         String lastStart;
         String lastDuration;
         if (_calls == 0) {
            average      = NOT_AVAILABLE;
            min          = NOT_AVAILABLE;
            minStart     = NOT_AVAILABLE;
            max          = NOT_AVAILABLE;
            maxStart     = NOT_AVAILABLE;
            lastStart    = NOT_AVAILABLE;
            lastDuration = NOT_AVAILABLE;
         } else if (_duration == 0) {
            average      = "0";
            min          = String.valueOf(_min);
            minStart     = DateConverter.toDateString(TIME_ZONE, _minStart);
            max          = String.valueOf(_max);
            maxStart     = DateConverter.toDateString(TIME_ZONE, _maxStart);
            lastStart    = DateConverter.toDateString(TIME_ZONE, _lastStart);
            lastDuration = String.valueOf(_lastDuration);
         } else {
            average      = String.valueOf(_duration / _calls);
            min          = String.valueOf(_min);
            minStart     = DateConverter.toDateString(TIME_ZONE, _minStart);
            max          = String.valueOf(_max);
            maxStart     = DateConverter.toDateString(TIME_ZONE, _maxStart);
            lastStart    = DateConverter.toDateString(TIME_ZONE, _lastStart);
            lastDuration = String.valueOf(_lastDuration);
         }
         Element element = ElementFormatter.createMainElement(name);
         Document doc = element.getOwnerDocument();
         element.setAttribute("count",    String.valueOf(_calls));
         element.setAttribute("average",  average);
         if (errorCode != null) {
            element.setAttribute("errorcode", errorCode);
         }
         Element minElem = doc.createElement("min");
         minElem.setAttribute("start",    minStart);
         minElem.setAttribute("duration", min);
         minElem.setAttribute("contextId", _minContextId);
         element.appendChild(minElem);
         Element maxElem = doc.createElement("max");
         maxElem.setAttribute("start",    maxStart);
         maxElem.setAttribute("duration", max);
         maxElem.setAttribute("contextId", _maxContextId);
         element.appendChild(maxElem);
         Element lastElem = doc.createElement("last");
         lastElem.setAttribute("start",    lastStart);
         lastElem.setAttribute("duration", lastDuration);
         lastElem.setAttribute("contextId", _lastContextId);
         element.appendChild(lastElem);
         return element;
      }

      /**
       * Resets this statistic.
       */
      public synchronized void reset() {
         _calls        = 0;
         _lastStart    = 0L;
         _lastDuration = 0L;
         _duration     = 0L;
         _min          = Long.MAX_VALUE;
         _minStart     = 0L;
         _max          = 0L;
         _maxStart     = 0L;
      }
   }
}
