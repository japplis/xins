/*
 * $Id: PatternUtils.java,v 1.6 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.text;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.ProgrammingException;
import org.xins.common.Utils;

/**
 * Regular expressions related utility functions.
 *
 * @version $Revision: 1.6 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 2.0
 */
public final class PatternUtils {

   /**
    * Constructs a new <code>PatternUtils</code> object.
    */
   private PatternUtils() {
      // empty
   }

   /**
    * Compiles the given regular expression to a Perl5 pattern object.
    *
    * @param regexp
    *     the String value of the Perl5 regular expresssion, cannot be <code>null</code>.
    *
    * @return
    *    the Perl5 pattern, never <code>null</code>
    *
    * @throws IllegalArgumentException
    *    if <code>regexp == null</code>.
    *
    * @throws ProgrammingException
    *    if the pattern cannot be complied.
    *
    * @since XINS 2.0
    */
   public static Pattern createPattern(String regexp) throws IllegalArgumentException, ProgrammingException {
      MandatoryArgumentChecker.check("regexp", regexp);
      try {
         Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
         return pattern;
      } catch (PatternSyntaxException exception) {
         throw Utils.logProgrammingError(exception);
      }
   }
}
